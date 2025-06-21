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

import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

// Annotation de déclaration du numéro de la règle
@Rule(key = "GCI74")
// Héritage de la classe IssuableSubscriptionVisitor de l'API SonarQube
// permettant de déclarer la classe actuelle comme pouvant analyser des noeuds de l'AST
public class AvoidFullSQLRequest extends IssuableSubscriptionVisitor {

    // Expression régulière pour détecter la requête SELECT * FROM
    private static final Predicate<String> SELECT_FROM_REGEXP =
            compile("select\\s*\\*\\s*from", CASE_INSENSITIVE).asPredicate();

    // Détermination des types de noeud dans l'AST à visiter
    @Override
    public List<Kind> nodesToVisit() {
        return singletonList(Tree.Kind.STRING_LITERAL);
    }

    // Traitement effectué sur chaque noeud de l'AST visité
    @Override
    public void visitNode(Tree tree) {
        String value = ((LiteralTree) tree).value();
        if (SELECT_FROM_REGEXP.test(value)) {
            reportIssue(tree, "Don't use the query SELECT * FROM");
        }
    }
}
