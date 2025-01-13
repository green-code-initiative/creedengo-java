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

import static java.util.Arrays.asList;
import static org.greencodeinitiative.creedengo.java.checks.enums.ConstOrLiteralDeclare.isLiteral;
import static org.sonar.plugins.java.api.semantic.Type.Primitives.INT;
import static org.sonar.plugins.java.api.tree.Tree.Kind.MEMBER_SELECT;
import static org.sonar.plugins.java.api.tree.Tree.Kind.METHOD_INVOCATION;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Stream;
import org.greencodeinitiative.creedengo.java.checks.enums.ConstOrLiteralDeclare;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@Rule(key = "GCI78")
@DeprecatedRuleKey(repositoryKey = "ecocode-java", ruleKey = "EC78")
@DeprecatedRuleKey(repositoryKey = "greencodeinitiative-java", ruleKey = "S78")
public class AvoidSetConstantInBatchUpdate extends IssuableSubscriptionVisitor {

  protected static final String MESSAGERULE = "Avoid setting constants in batch update";
  private final AvoidSetConstantInBatchUpdateVisitor visitorInFile =
      new AvoidSetConstantInBatchUpdateVisitor();

  @Override
  public List<Kind> nodesToVisit() {
    return asList(
        Tree.Kind.FOR_EACH_STATEMENT,
        Tree.Kind.FOR_STATEMENT,
        Tree.Kind.WHILE_STATEMENT,
        Tree.Kind.DO_STATEMENT);
  }

  @Override
  public void visitNode(Tree tree) {
    tree.accept(visitorInFile);
  }

  private class AvoidSetConstantInBatchUpdateVisitor extends BaseTreeVisitor {

    private final MethodMatchers setters =
        MethodMatchers.create()
            .ofSubTypes(PreparedStatement.class.getName())
            .names(
                "setBoolean",
                "setByte",
                "setShort",
                "setInt",
                "setLong",
                "setFloat",
                "setDouble",
                "setBigDecimal",
                "setString")
            .addParametersMatcher(args -> args.size() == 2 && args.get(0).isPrimitive(INT))
            .build();

    @Override
    public void visitMethodInvocation(MethodInvocationTree tree) {
      if (setters.matches(tree) && isConstant(tree.arguments().get(1))) {
        reportIssue(tree, MESSAGERULE);
      } else {
        super.visitMethodInvocation(tree);
      }
    }
  }

  private static final boolean isConstant(Tree arg) {

    if (arg.is(METHOD_INVOCATION)) {
      MethodInvocationTree m = (MethodInvocationTree) arg;
      return Stream.of(ConstOrLiteralDeclare.values()).anyMatch(o -> o.isLiteralDeclare(m));
    } else if (arg.is(MEMBER_SELECT)) {
      MemberSelectExpressionTree m = (MemberSelectExpressionTree) arg;
      return Stream.of(ConstOrLiteralDeclare.values()).anyMatch(o -> o.isPublicMember(m));
    }
    return isLiteral(arg);
  }
}
