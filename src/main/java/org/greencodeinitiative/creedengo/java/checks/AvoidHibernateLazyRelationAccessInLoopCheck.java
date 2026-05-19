package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Arrays;
import java.util.List;

@Rule(key = "CRJVM206")
public class AvoidHibernateLazyRelationAccessInLoopCheck extends IssuableSubscriptionVisitor {

    private static final String MESSAGE =
            "Potential Hibernate N+1 query detected: avoid accessing a lazy relationship inside a loop. "
                    + "Use JOIN FETCH, EntityGraph, or batch fetching.";

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Arrays.asList(
                Tree.Kind.FOR_EACH_STATEMENT,
                Tree.Kind.FOR_STATEMENT,
                Tree.Kind.WHILE_STATEMENT,
                Tree.Kind.DO_STATEMENT,
                Tree.Kind.METHOD_INVOCATION
        );
    }

    @Override
    public void visitNode(Tree tree) {
        if (tree.is(Tree.Kind.METHOD_INVOCATION)) {
            MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;

            if (isStreamOperation(methodInvocationTree)) {
                checkInsideTree(tree);
            }
        } else {
            checkInsideTree(tree);
        }
    }

    private void checkInsideTree(Tree tree) {
        tree.accept(new BaseTreeVisitor() {
            @Override
            public void visitMethodInvocation(MethodInvocationTree methodInvocationTree) {
                if (isPotentialLazyRelationGetter(methodInvocationTree)) {
                    reportIssue(methodInvocationTree, MESSAGE);
                }
                super.visitMethodInvocation(methodInvocationTree);
            }
        });
    }

    private boolean isStreamOperation(MethodInvocationTree methodInvocationTree) {
        String methodName = methodInvocationTree.symbol().name();
        return "forEach".equals(methodName)
                || "forEachOrdered".equals(methodName)
                || "map".equals(methodName)
                || "peek".equals(methodName);
    }

    private boolean isPotentialLazyRelationGetter(MethodInvocationTree methodInvocationTree) {
        String methodName = methodInvocationTree.symbol().name();
        return methodName.startsWith("get")
                && methodName.length() > 4
                && methodName.endsWith("s");
    }
}