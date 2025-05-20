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
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

// TODO DDC : rule already existing natively in SonarQube 9.9 (see java:S3012) for a part of checks
// ==> analyse / add our tag to it (?)

/**
 * Array Copy Check
 *
 * @author Aubay
 * @formatter:off
 */
@Rule(key = "GCI604")
public class SpringMaxRetryableCheck extends IssuableSubscriptionVisitor {

    public static final String MESSAGE_RULE = "Avoid using Pattern.compile() in a non-static context.";


    @Override
    public List<Kind> nodesToVisit() {
        return Collections.singletonList(Kind.METHOD);
    }

    /**
     * Check a node. Report issue when found.
     */
    @Override
    public void visitNode(@Nonnull Tree tree) {
        if (tree instanceof MethodTree) {
            final MethodTree methodTree = (MethodTree) tree;

            if (!methodTree.is(Kind.CONSTRUCTOR)) {
//                methodTree.accept(visitor);
            }
        }
    }
}
