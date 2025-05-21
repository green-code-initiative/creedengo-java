package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Arrays;
import java.util.List;

@Rule(key = "GCI91")
public class UseFilterBeforeSort extends IssuableSubscriptionVisitor {
    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Arrays.asList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;

        // On vérifie si c’est un appel à "sorted"
        if (methodInvocation.methodSelect() instanceof MemberSelectExpressionTree) {
            MemberSelectExpressionTree sortedSelect = (MemberSelectExpressionTree) methodInvocation.methodSelect();
            String sortedMethod = sortedSelect.identifier().name();

            if ("sorted".equals(sortedMethod)) {
                Tree methodCall = methodInvocation.parent();
                if (methodCall instanceof MemberSelectExpressionTree) {
                    MemberSelectExpressionTree filterCall = (MemberSelectExpressionTree) methodCall;
                    String filterMethod = filterCall.identifier().name();

                    if ("filter".equals(filterMethod)) {
                        // Mauvais ordre détecté : sorted().filter() au lieu de filter().sorted()
                        reportIssue(methodInvocation, "Use 'filter' before 'sorted' for better efficiency.");
                    }
                }
            }
        }
    }

}
