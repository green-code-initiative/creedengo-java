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

    @Override
    public List<Kind> nodesToVisit() {
        return List.of(Kind.VARIABLE);
    }

    @Override
    public void visitNode(@Nonnull Tree tree) {
        VariableTree variableTree = (VariableTree) tree;
        LOGGER.debug("Variable > " + getVariableNameForLogger(variableTree));
        LOGGER.debug("   => isNotFinalAndNotStatic(variableTree) = " + isNotFinalAndNotStatic(variableTree));
        LOGGER.debug("   => usages = " + variableTree.symbol().usages().size());
        LOGGER.debug("   => isNotReassigned = " + isNotReassigned(variableTree));

        if (isNotFromRecord(variableTree) && isNotFinalAndNotStatic(variableTree) && isNotReassigned(variableTree)) {
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

    private static boolean isNotFromRecord(VariableTree variableTree) {
        Tree parent = variableTree.parent();
        if (parent == null) return false;

        return !parent.is(Kind.RECORD);
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

}
