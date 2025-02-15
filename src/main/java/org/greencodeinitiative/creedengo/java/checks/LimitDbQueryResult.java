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
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Rule(key = "GCI24")
public class LimitDbQueryResult extends IssuableSubscriptionVisitor {

    protected static final String MESSAGE_RULE = "Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)";

    private static final Pattern PATTERN = Pattern.compile("(LIMIT|TOP|ROW_NUMBER|FETCH FIRST|WHERE)", Pattern.CASE_INSENSITIVE);

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.STRING_LITERAL);
    }

    @Override
    public void visitNode(Tree tree) {
        String value = ((LiteralTree) tree).value().toUpperCase();
        if (value.contains("SELECT") && value.contains("FROM") && !PATTERN.matcher(value).find()) {
            reportIssue(tree, MESSAGE_RULE);
        }
    }

}
