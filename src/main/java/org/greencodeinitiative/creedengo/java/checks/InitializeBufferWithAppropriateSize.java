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
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@Rule(key = "GCI32")
@DeprecatedRuleKey(repositoryKey = "ecocode-java", ruleKey = "EC32")
@DeprecatedRuleKey(repositoryKey = "greencodeinitiative-java", ruleKey = "GRSP0032")
public class InitializeBufferWithAppropriateSize extends IssuableSubscriptionVisitor {

  protected static final String RULE_MESSAGE =
      "Initialize StringBuilder or StringBuffer with appropriate size";

  @Override
  public List<Kind> nodesToVisit() {
    return Collections.singletonList(Kind.NEW_CLASS);
  }

  @Override
  public void visitNode(Tree tree) {
    NewClassTree newClassTree = (NewClassTree) tree;
    if ((newClassTree.symbolType().is("java.lang.StringBuffer")
            || newClassTree.symbolType().is("java.lang.StringBuilder"))
        && newClassTree.arguments().isEmpty()) {
      reportIssue(tree, RULE_MESSAGE);
    }
  }
}
