package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Rule(key = "GCI91")
public class UseFilterBeforeSort extends IssuableSubscriptionVisitor {
    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        if (tree instanceof MethodInvocationTree) {
            MethodInvocationTree mit = (MethodInvocationTree) tree;

            if (!isTerminalOperation(mit)) {
                return; // On ignore les appels intermédiaires (filter, sorted, etc.)
            }

            List<String> methodChain = extractChainedMethodNames(mit);

            int sortedIndex = methodChain.indexOf("sorted");
            int filterIndex = methodChain.indexOf("filter");

            if (sortedIndex != -1 && filterIndex != -1 && sortedIndex < filterIndex) {
                reportIssue(tree, "Use 'filter' before 'sorted' for better efficiency.");
            }
        }
    }

    private List<String> extractChainedMethodNames(MethodInvocationTree terminalInvocation) {
        List<String> methodNames = new ArrayList<>();
        ExpressionTree current = terminalInvocation;

        while (current instanceof MethodInvocationTree) {
            MethodInvocationTree methodCall = (MethodInvocationTree) current;
            ExpressionTree methodSelect = methodCall.methodSelect();

            if (methodSelect instanceof MemberSelectExpressionTree) {
                MemberSelectExpressionTree memberSelect = (MemberSelectExpressionTree) methodSelect;
                methodNames.add(memberSelect.identifier().name());
                current = memberSelect.expression(); // remonter à l'appel précédent
            } else {
                break;
            }
        }

        Collections.reverse(methodNames); // pour avoir l’ordre stream → ... → collect
        return methodNames;
    }

    private boolean isTerminalOperation(MethodInvocationTree tree) {
        if (!(tree.methodSelect() instanceof MemberSelectExpressionTree)) {
            return false;
        }
        String methodName = ((MemberSelectExpressionTree) tree.methodSelect()).identifier().name();
        // Ajouter ici tous les terminaux connus
        return "collect".equals(methodName)
                || "forEach".equals(methodName)
                || "reduce".equals(methodName)
                || "count".equals(methodName)
                || "toArray".equals(methodName)
                || "anyMatch".equals(methodName)
                || "allMatch".equals(methodName)
                || "noneMatch".equals(methodName)
                || "findFirst".equals(methodName)
                || "findAny".equals(methodName);
    }

}
