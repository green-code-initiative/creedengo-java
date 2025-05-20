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
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.UnaryExpressionTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@Rule(key = "GCI67")
@DeprecatedRuleKey(repositoryKey = "ecocode-java", ruleKey = "EC67")
@DeprecatedRuleKey(repositoryKey = "greencodeinitiative-java", ruleKey = "S67")
public class IncrementCheck extends IssuableSubscriptionVisitor {

    protected static final String MESSAGERULE = "Use ++i instead of i++";

    @Override
    public List<Kind> nodesToVisit() {
        return Collections.singletonList(Kind.POSTFIX_INCREMENT);
    }

    @Override
    public void visitNode(Tree tree) {
        UnaryExpressionTree unaryExprTree = (UnaryExpressionTree) tree;

        if (unaryExprTree.parent() instanceof BinaryExpressionTree
            || unaryExprTree.parent() instanceof Arguments
            || unaryExprTree.expression().is(Tree.Kind.MEMBER_SELECT)) {
            return ;
        }
        reportIssue(tree, MESSAGERULE);
    }
}
