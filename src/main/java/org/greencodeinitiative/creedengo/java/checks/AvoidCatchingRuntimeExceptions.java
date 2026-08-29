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

import java.util.Collections;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

@Rule(key = "GCI96")
public class AvoidCatchingRuntimeExceptions extends IssuableSubscriptionVisitor {

    protected static final String MESSAGERULE = "Avoid catching Runtime exceptions";
    protected static final String RUNTIME_EXCEPTION = "java.lang.RuntimeException";
    protected static final String ILLEGAL_ARGUMENT_EXCEPTION = "java.lang.IllegalArgumentException";

    @Override
    public List<Kind> nodesToVisit() {
        return Collections.singletonList(Kind.CATCH);
    }

    @Override
    public void visitNode(Tree tree) {
        CatchTree catchTree = (CatchTree) tree;
        if(catchTree.parameter().type().symbolType().isSubtypeOf(RUNTIME_EXCEPTION) &&
                !catchTree.parameter().type().symbolType().isSubtypeOf(ILLEGAL_ARGUMENT_EXCEPTION)) {
            String caughtExceptionType = catchTree.parameter().type().symbolType().name();
            String message = MESSAGERULE + " : " + caughtExceptionType;
            reportIssue(tree, message);
        }
    }
}
