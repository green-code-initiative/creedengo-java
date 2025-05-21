package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import javax.annotation.Nonnull;
import java.util.List;

@Rule(key = "GCI82")
public class MakeNonReassignedVariablesConstants extends IssuableSubscriptionVisitor {

    protected static final String MESSAGE_RULE = "The variable is never reassigned and can be 'final'";

    private static final Logger LOGGER = Loggers.get(MakeNonReassignedVariablesConstants.class);

    private final String LOMBOK_SETTER = "Setter";
    private final String LOMBOK_DATA = "Data";

    private boolean hasParsedImports = false;
    private boolean hasLombokSetterImport = false;
    private boolean hasLombokDataImport = false;


    @Override
    public List<Kind> nodesToVisit() {
        return List.of(Kind.VARIABLE);
    }

    @Override
    public void visitNode(@Nonnull Tree tree) {

        VariableTree variableTree = (VariableTree) tree;
        LOGGER.info("Variable > " + getVariableNameForLogger(variableTree));
        LOGGER.info("   => isNotFinalAndNotStatic(variableTree) = " + isNotFinalAndNotStatic(variableTree));
        LOGGER.info("   => usages = " + variableTree.symbol().usages().size());
        LOGGER.info("   => isNotReassigned = " + isNotReassigned(variableTree));
        if (hasNoLombokSetter(variableTree) && isNotFinalAndNotStatic(variableTree) && isNotReassigned(variableTree)) {
            reportIssue(tree, MESSAGE_RULE);
        } else {
            super.visitNode(tree);
        }
    }

    private static boolean isNotReassigned(VariableTree variableTree) {
        return variableTree.symbol()
                .usages()
                .stream()
                .noneMatch(MakeNonReassignedVariablesConstants::parentIsAssignment);
    }

    private static boolean parentIsAssignment(Tree tree) {
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

    private boolean hasNoLombokSetter(VariableTree variableTree) {
        // Check if the variable is annotated with @Setter

        for (AnnotationTree annotation : variableTree.modifiers().annotations()) {
            if (annotation.annotationType().toString().equals(LOMBOK_SETTER)) {
                if (hasLombokImport(variableTree, LOMBOK_SETTER)) {

                    // Ignore if the annotation has AccessLevel.NONE
                    if (!annotation.arguments().isEmpty()) {
                        for (ExpressionTree argument : annotation.arguments()) {
                            if (argument.is(Kind.MEMBER_SELECT)) {
                                MemberSelectExpressionTree memberSelectExpressionTree = (MemberSelectExpressionTree) argument;
                                if (memberSelectExpressionTree.expression().toString().equals("AccessLevel")
                                        && memberSelectExpressionTree.identifier().name().equals("NONE")) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            }
        }
        // Check if the variable is in a class with @Setter or with @Data
        if( variableTree.parent() != null && !variableTree.parent().is(Kind.CLASS)){
            return true;
        }
        if (variableTree.parent() != null && variableTree.parent().is(Kind.CLASS)) {
            ClassTree classTree = (ClassTree) variableTree.parent();
            for (AnnotationTree annotation : classTree.modifiers().annotations()) {
                if (annotation.annotationType().toString().equals(LOMBOK_SETTER) && hasLombokImport(variableTree, LOMBOK_SETTER)) {
                    return false;
                }
                if (annotation.annotationType().toString().equals(LOMBOK_DATA) && hasLombokImport(variableTree, LOMBOK_DATA)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasLombokImport(VariableTree variableTree, String lombokImport) {
        if (!hasParsedImports) {
            Tree currentTree = variableTree;
            while (currentTree.parent() != null && !currentTree.parent().is(Kind.COMPILATION_UNIT)) {
                currentTree = currentTree.parent();
            }
            if (currentTree != null) {
                CompilationUnitTree rootNode = (CompilationUnitTree) currentTree.parent();
                for (var importClauseTree : rootNode.imports()) {
                    ImportTree importTree = (ImportTree) importClauseTree;
                    MemberSelectExpressionTree identifier = (MemberSelectExpressionTree) importTree.qualifiedIdentifier();

                    if ("lombok".equals(identifier.expression().toString())) {
                        if ("*".equals(identifier.identifier().name())) {
                            hasLombokSetterImport = true;
                            hasLombokDataImport = true;
                        } else if (LOMBOK_SETTER.equals(identifier.identifier().name())) {
                            hasLombokSetterImport = true;
                        } else if (LOMBOK_DATA.equals(identifier.identifier().name())) {
                            hasLombokDataImport = true;
                        }
                    }
                }
            }
            hasParsedImports = true;
        }
        return LOMBOK_SETTER.equals(lombokImport) ? hasLombokSetterImport : hasLombokDataImport;
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

}
