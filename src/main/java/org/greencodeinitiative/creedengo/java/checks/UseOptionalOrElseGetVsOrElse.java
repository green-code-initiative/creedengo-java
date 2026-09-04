/*
 * creedengo - Java language - Provides rules to reduce the environmental footprint of your Java programs
 * Copyright © 2024 Green Code Initiative (https://green-code-initiative.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.NewArrayTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@Rule(key = "GCI94")
public class UseOptionalOrElseGetVsOrElse extends IssuableSubscriptionVisitor {

    private static final String MESSAGE_RULE = "Use optional orElseGet instead of orElse.";
    private final UseOptionalOrElseGetVsOrElseVisitor visitorInFile = new UseOptionalOrElseGetVsOrElseVisitor();

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.METHOD_INVOCATION);
    }

    @Override
    public void visitNode(@Nonnull Tree tree) {
        tree.accept(visitorInFile);
    }

    private class UseOptionalOrElseGetVsOrElseVisitor extends BaseTreeVisitor {
        @Override
        public void visitMethodInvocation(MethodInvocationTree tree) {
            if (!tree.methodSelect().is(Tree.Kind.MEMBER_SELECT)) {
                return;
            }
            MemberSelectExpressionTree memberSelect = (MemberSelectExpressionTree) tree.methodSelect();
            if (memberSelect.identifier().name().equals("orElse") &&
                    memberSelect.expression().symbolType().is("java.util.Optional") &&
                    !tree.arguments().isEmpty() &&
                    containsComputation(tree.arguments().get(0))) {
                reportIssue(memberSelect, MESSAGE_RULE);
            }
        }
    }

    private static boolean containsComputation(Tree argument) {
        ComputationDetector detector = new ComputationDetector();
        argument.accept(detector);
        return detector.found;
    }

    private static class ComputationDetector extends BaseTreeVisitor {
        boolean found = false;

        @Override
        public void visitMethodInvocation(@Nonnull MethodInvocationTree tree) {
            found = true;
        }

        @Override
        public void visitNewClass(@Nonnull NewClassTree tree) {
            found = true;
        }

        @Override
        public void visitNewArray(@Nonnull NewArrayTree tree) {
            found = true;
        }
    }
}