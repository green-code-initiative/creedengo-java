package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

@Rule(key = "GCI82")
public class MakeNonReassignedVariablesConstants extends IssuableSubscriptionVisitor {

    protected static final String MESSAGE_RULE = "The variable is never reassigned and can be 'final'";

    private static final Logger LOGGER = Loggers.get(MakeNonReassignedVariablesConstants.class);

    private static final String LOMBOK_PACKAGE = "lombok";
    private static final String SETTER = "Setter";
    private static final String DATA = "Data";
    private static final String ACCESS_LEVEL_NONE = "AccessLevel.NONE";
    private static final String NONE = "NONE";

    @Override
    public List<Kind> nodesToVisit() {
        return List.of(Kind.VARIABLE);
    }

    @Override
    public void visitNode(@Nonnull Tree tree) {
        VariableTree variableTree = (VariableTree) tree;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Variable > {}", getVariableNameForLogger(variableTree));
            LOGGER.debug("   => isNotFinalAndNotStatic(variableTree) = {}", isNotFinalAndNotStatic(variableTree));
            LOGGER.debug("   => usages = {}", variableTree.symbol().usages().size());
            LOGGER.debug("   => isNotReassigned = {}", isNotReassigned(variableTree));
            LOGGER.debug("   => isPassedAsNonFinalParameter = {}", isPassedAsNonFinalParameter(variableTree));
        }
        // the Lombok check is the most expensive predicate : it is evaluated last, on actual candidates only
        if (isNotFromRecord(variableTree) &&
                isNotFinalAndNotStatic(variableTree) &&
                isNotReassigned(variableTree) &&
                !isLombokManaged(variableTree)) {
            reportIssue(tree, MESSAGE_RULE);
        } else {
            super.visitNode(tree);
        }
    }

    private static boolean isNotFromRecord(VariableTree variableTree) {
        Tree parent = variableTree.parent();
        if (parent == null) return false;

        return !parent.is(Kind.RECORD);
    }

    private static boolean isNotReassigned(VariableTree variableTree) {
        return variableTree.symbol()
                .usages()
                .stream()
                .noneMatch(MakeNonReassignedVariablesConstants::parentIsAssignment) 
            && !isPassedAsNonFinalParameter(variableTree); // if a variable is passed into a method as a non-final parameter, it may have been reassigned
    }

    private static boolean isPassedAsNonFinalParameter(VariableTree variableTree) {
        return variableTree.symbol()
                .usages()
                .stream()
                .anyMatch(MakeNonReassignedVariablesConstants::parentIsNonFinalParameter);
    }

    private static boolean parentIsNonFinalParameter(Tree tree) {
        // Skip the parent if it is a member select (e.g. "this.myVar")
        while (tree.parent().is(Kind.MEMBER_SELECT)) {
            tree = tree.parent();
        }
        if(!parentIsKind(tree, Kind.ARGUMENTS))
            return false;
        if(tree.parent() == null)
            return false;
        Arguments arguments = (Arguments) tree.parent();
        if (parentIsKind(arguments, Kind.METHOD_INVOCATION, Kind.NEW_CLASS)) {
            MethodTree methodTree = arguments.parent().is(Kind.METHOD_INVOCATION)
                ? ((MethodInvocationTree) arguments.parent()).methodSymbol().declaration()
                : ((NewClassTree) arguments.parent()).methodSymbol().declaration();
            int argument_idx = arguments.indexOf(tree);
            return methodTree != null && !hasModifier(methodTree.parameters().get(argument_idx).modifiers(), Modifier.FINAL);
        }
        return false;
        
    }

    private static boolean parentIsAssignment(Tree tree) {
        // Skip the parent if it is a member select (e.g. "this.myVar")
        while (tree.parent().is(Kind.MEMBER_SELECT)) {
            tree = tree.parent();   
        }
        return parentIsKind(tree,
                Kind.ASSIGNMENT,
                Kind.MULTIPLY_ASSIGNMENT,
                Kind.DIVIDE_ASSIGNMENT,
                Kind.REMAINDER_ASSIGNMENT,
                Kind.PLUS_ASSIGNMENT,
                Kind.MINUS_ASSIGNMENT,
                Kind.LEFT_SHIFT_ASSIGNMENT,
                Kind.RIGHT_SHIFT_ASSIGNMENT,
                Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
                Kind.AND_ASSIGNMENT,
                Kind.XOR_ASSIGNMENT,
                Kind.OR_ASSIGNMENT,
                Kind.POSTFIX_INCREMENT,
                Kind.POSTFIX_DECREMENT,
                Kind.PREFIX_INCREMENT,
                Kind.PREFIX_DECREMENT
        );
    }

    private static boolean parentIsKind(Tree tree, Kind... orKind) {
        Tree parent = tree.parent();
        if (parent == null) return false;

        for (Kind k : orKind) {
            if (parent.is(k)) return true;
        }

        return false;
    }

    private static boolean isNotFinalAndNotStatic(VariableTree variableTree) {
        return hasNoneOf(variableTree.modifiers(), Modifier.FINAL, Modifier.STATIC);
    }

    private static boolean hasNoneOf(ModifiersTree modifiersTree, Modifier... unexpectedModifiers) {
        return !hasAnyOf(modifiersTree, unexpectedModifiers);
    }

    private static boolean hasAnyOf(ModifiersTree modifiersTree, Modifier... expectedModifiers) {
        for(Modifier expectedModifier : expectedModifiers) {
            if (hasModifier(modifiersTree, expectedModifier)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasModifier(ModifiersTree modifiersTree, Modifier expectedModifier) {
        for(ModifierKeywordTree modifierKeywordTree : modifiersTree.modifiers()) {
            if (modifierKeywordTree.modifier() == expectedModifier) {
                return true;
            }
        }

        return false;
    }

    private String getVariableNameForLogger(VariableTree variableTree) {
        String name = variableTree.simpleName().name();

        if (variableTree.parent() != null) return name;

        if (variableTree.parent().is(Kind.CLASS)) {
            ClassTree cTree = (ClassTree) variableTree.parent();
            name += "  ---  from CLASS '" + cTree.simpleName() + "'";
        }
        if (variableTree.parent().is(Kind.BLOCK)) {
            BlockTree bTree = (BlockTree) variableTree.parent();
            if (bTree.parent() != null && bTree.parent().is(Kind.METHOD)) {
                MethodTree mTree = (MethodTree) bTree.parent();
                name += "  ---  from METHOD '" + mTree.simpleName() + "'";
            }
        }

        return name;

    }

    /**
     * A variable is "Lombok managed" when Lombok generates a setter for it : making it {@code final}
     * would not compile, so the rule must stay silent.
     * <p>
     * This happens when the field itself is annotated with {@code @Setter}, or when its owner class is
     * annotated with {@code @Setter} or {@code @Data}. A field level {@code @Setter(AccessLevel.NONE)}
     * explicitly disables the generation and therefore wins over the class level annotation.
     */
    private static boolean isLombokManaged(VariableTree variableTree) {
        AnnotationTree fieldSetter = findLombokAnnotation(variableTree.modifiers(), SETTER);
        if (fieldSetter != null) {
            return !isSetterDisabled(fieldSetter);
        }

        // covers CLASS, but also ENUM and INTERFACE owners, which Kind.CLASS alone would miss
        if (variableTree.parent() instanceof ClassTree classTree) {
            ModifiersTree classModifiers = classTree.modifiers();
            return findLombokAnnotation(classModifiers, SETTER) != null
                    || findLombokAnnotation(classModifiers, DATA) != null;
        }

        return false;
    }

    @CheckForNull
    private static AnnotationTree findLombokAnnotation(ModifiersTree modifiers, String simpleName) {
        for (AnnotationTree annotation : modifiers.annotations()) {
            if (isLombokAnnotation(annotation, simpleName)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Relies on the semantic model when it is available : the resolved type handles the regular import,
     * the wildcard import ({@code import lombok.*}) and the fully qualified usage ({@code @lombok.Setter})
     * indifferently, and rules out a same named annotation coming from another library.
     * <p>
     * When Lombok is missing from the analysis classpath the type cannot be resolved, so we fall back on the
     * written form and accept both {@code @Setter} and {@code @lombok.Setter}.
     */
    private static boolean isLombokAnnotation(AnnotationTree annotation, String simpleName) {
        String fullyQualifiedName = LOMBOK_PACKAGE + "." + simpleName;

        Type annotationType = annotation.symbolType();
        if (!annotationType.isUnknown()) {
            return annotationType.is(fullyQualifiedName);
        }

        String writtenName = writtenNameOf(annotation.annotationType());
        return simpleName.equals(writtenName) || fullyQualifiedName.equals(writtenName);
    }

    /**
     * Detects {@code AccessLevel.NONE}, whatever the way it is written : positional or named argument
     * ({@code value = ...}), simple, fully qualified or statically imported constant.
     */
    private static boolean isSetterDisabled(AnnotationTree annotation) {
        return annotation.arguments()
                .stream()
                .map(MakeNonReassignedVariablesConstants::annotationArgumentValue)
                .map(MakeNonReassignedVariablesConstants::writtenNameOf)
                .filter(Objects::nonNull)
                .anyMatch(value -> value.endsWith(ACCESS_LEVEL_NONE) || NONE.equals(value));
    }

    private static ExpressionTree annotationArgumentValue(ExpressionTree argument) {
        return argument.is(Kind.ASSIGNMENT)
                ? ((AssignmentExpressionTree) argument).expression()
                : argument;
    }

    /**
     * Rebuilds the name as written in the source ({@code Setter}, {@code lombok.Setter},
     * {@code lombok.AccessLevel.NONE}) by walking the tree : {@code toString()} only returns the source
     * text for identifiers, not for member selects.
     *
     * @return {@code null} when the tree is neither an identifier nor a member select
     */
    @CheckForNull
    private static String writtenNameOf(Tree tree) {
        if (tree.is(Kind.IDENTIFIER)) {
            return ((IdentifierTree) tree).name();
        }
        if (tree.is(Kind.MEMBER_SELECT)) {
            MemberSelectExpressionTree memberSelect = (MemberSelectExpressionTree) tree;
            String qualifier = writtenNameOf(memberSelect.expression());
            return qualifier == null ? null : qualifier + "." + memberSelect.identifier().name();
        }
        return null;
    }

}
