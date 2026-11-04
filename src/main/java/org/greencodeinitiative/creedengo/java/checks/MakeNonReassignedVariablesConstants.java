package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
            LOGGER.debug("Variable > {}", variableTree.simpleName().name());
            LOGGER.debug("   => isOptimizableOnceFinal = {}", isOptimizableOnceFinal(variableTree));
            LOGGER.debug("   => usages = {}", variableTree.symbol().usages().size());
            LOGGER.debug("   => isNotReassigned = {}", isNotReassigned(variableTree));
        }

        // the Lombok check is the most expensive predicate : it is evaluated last, on actual candidates only
        if (isOptimizableOnceFinal(variableTree) &&
                isNotReassigned(variableTree) &&
                !isLombokManaged(variableTree)) {
            reportIssue(tree, MESSAGE_RULE);
        } else {
            super.visitNode(tree);
        }
    }

    /**
     * The rule only targets the variables for which the 'final' keyword brings an actual optimization :
     * <ul>
     *     <li>a constant variable (JLS 4.12.4) : javac inlines its value and computes the expressions using it,</li>
     *     <li>a static final field : the JIT trusts it as a constant once its class is initialized.</li>
     * </ul>
     * Anywhere else (parameters, objects, arrays, values computed at runtime...), 'final' doesn't change the bytecode.
     * A variable without initializer (parameter, catch, for-each, pattern, record component...) is never a candidate.
     */
    private static boolean isOptimizableOnceFinal(VariableTree variableTree) {
        ExpressionTree initializer = variableTree.initializer();
        Symbol symbol = variableTree.symbol();
        if (initializer == null || symbol.isFinal()) {
            return false;
        }
        return isConstantVariableCandidate(symbol, initializer) || isStaticField(symbol);
    }

    private static boolean isConstantVariableCandidate(Symbol symbol, ExpressionTree initializer) {
        return isPrimitiveOrString(symbol.type()) && isConstantExpression(initializer, new HashSet<>());
    }

    private static boolean isPrimitiveOrString(Type type) {
        return type.isPrimitive() || type.is("java.lang.String");
    }

    /**
     * Follows the definition of a constant expression (JLS 15.29) : {@code ExpressionTree#asConstant()} can't be used,
     * it misses some of them (char literals, floating point divisions, conditional expressions, final local constants).
     *
     * @param visited the variables already followed, to stop on circular definitions (e.g. "A = C.B" and "B = C.A")
     */
    private static boolean isConstantExpression(ExpressionTree expression, Set<Symbol> visited) {
        if (expression.is(Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.FLOAT_LITERAL, Kind.DOUBLE_LITERAL,
                Kind.BOOLEAN_LITERAL, Kind.CHAR_LITERAL, Kind.STRING_LITERAL, Kind.TEXT_BLOCK)) {
            return true;
        }
        if (expression instanceof ParenthesizedTree parenthesized) {
            return isConstantExpression(parenthesized.expression(), visited);
        }
        if (expression instanceof TypeCastTree typeCast) {
            return isPrimitiveOrString(typeCast.type().symbolType()) && isConstantExpression(typeCast.expression(), visited);
        }
        if (expression.is(Kind.UNARY_PLUS, Kind.UNARY_MINUS, Kind.BITWISE_COMPLEMENT, Kind.LOGICAL_COMPLEMENT)) {
            return isConstantExpression(((UnaryExpressionTree) expression).expression(), visited);
        }
        if (expression instanceof BinaryExpressionTree binary) {
            return isConstantExpression(binary.leftOperand(), visited) && isConstantExpression(binary.rightOperand(), visited);
        }
        if (expression instanceof ConditionalExpressionTree conditional) {
            return isConstantExpression(conditional.condition(), visited)
                    && isConstantExpression(conditional.trueExpression(), visited)
                    && isConstantExpression(conditional.falseExpression(), visited);
        }
        if (expression instanceof IdentifierTree identifier) {
            return isConstantVariable(identifier.symbol(), visited);
        }
        // only the "TypeName.Identifier" form is a constant expression, not "this.CONSTANT" or "object.CONSTANT"
        if (expression instanceof MemberSelectExpressionTree memberSelect) {
            return isTypeName(memberSelect.expression()) && isConstantVariable(memberSelect.identifier().symbol(), visited);
        }
        return false;
    }

    /**
     * A constant variable (JLS 4.12.4) is a final variable of primitive or String type, initialized by a constant expression.
     * The semantic model only gives the constant value of fields : a local variable is checked from its declaration.
     */
    private static boolean isConstantVariable(Symbol symbol, Set<Symbol> visited) {
        if (!(symbol instanceof Symbol.VariableSymbol variableSymbol)) {
            return false;
        }
        if (variableSymbol.constantValue().isPresent()) {
            return true;
        }
        if (!variableSymbol.isFinal() || !isPrimitiveOrString(variableSymbol.type()) || !visited.add(variableSymbol)) {
            return false;
        }
        VariableTree declaration = variableSymbol.declaration();
        return declaration != null && declaration.initializer() != null
                && isConstantExpression(declaration.initializer(), visited);
    }

    private static boolean isTypeName(ExpressionTree expression) {
        if (expression instanceof IdentifierTree identifier) {
            return identifier.symbol().isTypeSymbol();
        }
        return expression instanceof MemberSelectExpressionTree memberSelect && memberSelect.identifier().symbol().isTypeSymbol();
    }

    private static boolean isStaticField(Symbol symbol) {
        Symbol owner = symbol.owner();
        return symbol.isStatic() && owner != null && owner.isTypeSymbol();
    }

    private static boolean isNotReassigned(VariableTree variableTree) {
        return variableTree.symbol()
                .usages()
                .stream()
                .noneMatch(MakeNonReassignedVariablesConstants::parentIsAssignment);
    }

    private static boolean parentIsAssignment(Tree tree) {
        // Skip the parent if it is a member select (e.g. "this.myVar")
        while (tree.parent().is(Kind.MEMBER_SELECT)) {
            tree = tree.parent();
        }
        Tree parent = tree.parent();
        return parent != null && parent.is(
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
