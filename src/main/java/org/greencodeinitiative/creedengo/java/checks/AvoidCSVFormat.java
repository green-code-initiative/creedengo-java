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
import java.util.Set;

import javax.annotation.Nonnull;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.Tree;

@Rule(key = "GCI99")
public class AvoidCSVFormat extends IssuableSubscriptionVisitor {

    protected static final String MESSAGE_RULE = "Avoid CSV format, prefer Parquet format for better performance and smaller footprint.";

    // Known Java CSV library package prefixes.
    private static final Set<String> CSV_PACKAGES = Set.of(
            "com.opencsv.",
            "org.apache.commons.csv.",
            "com.univocity.parsers.csv.",
            "com.fasterxml.jackson.dataformat.csv.",
            "org.supercsv.",
            "net.sf.flatpack."
    );

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.IMPORT);
    }

    @Override
    public void visitNode(@Nonnull Tree tree) {
        ImportTree importTree = (ImportTree) tree;
        String importName = buildImportString(importTree.qualifiedIdentifier());
        for (String csvPackage : CSV_PACKAGES) {
            if (importName.startsWith(csvPackage)) {
                reportIssue(importTree, MESSAGE_RULE);
                return;
            }
        }
    }

    private static String buildImportString(Tree tree) {
        if (tree instanceof IdentifierTree) {
            return ((IdentifierTree) tree).name();
        }
        if (tree instanceof MemberSelectExpressionTree) {
            MemberSelectExpressionTree mset = (MemberSelectExpressionTree) tree;
            return buildImportString(mset.expression()) + "." + mset.identifier().name();
        }
        return "";
    }
}
