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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TryStatementTree;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

/**
 * This rule checks that objects implementing AutoCloseable interface are properly managed
 * using try-with-resources statement instead of try-finally blocks.
 * <p>
 * Try-with-resources ensures proper resource management and reduces the risk of resource leaks.
 * It also reduces boilerplate code and improves code readability.
 * <p>
 * From an environmental perspective, proper resource management prevents resource leaks
 * which can lead to increased memory consumption and unnecessary CPU cycles.
 *
 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
 */
@Rule(key = "GCI79")
@DeprecatedRuleKey(repositoryKey = "ecocode-java", ruleKey = "EC79")
@DeprecatedRuleKey(repositoryKey = "greencodeinitiative-java", ruleKey = "S79")
public class FreeResourcesOfAutoCloseableInterface extends IssuableSubscriptionVisitor {

    /**
     * Stack to track nested try statements while traversing the AST
     */
    private final Deque<TryStatementContext> tryStack = new ArrayDeque<>();

    private static final String JAVA_LANG_AUTOCLOSEABLE = "java.lang.AutoCloseable";
    protected static final String MESSAGE_RULE = "try-with-resources Statement needs to be implemented for any object that implements the AutoCloseable interface.";

    @Override
    @ParametersAreNonnullByDefault
    public void leaveFile(JavaFileScannerContext context) {
        tryStack.clear();
    }

    @Override
    @Nonnull
    public List<Tree.Kind> nodesToVisit() {
        return List.of(Tree.Kind.TRY_STATEMENT, Tree.Kind.NEW_CLASS);
    }

    @Override
    public void visitNode(@Nonnull Tree tree) {
        if (tree.is(Tree.Kind.TRY_STATEMENT)) {
            handleTryStatement((TryStatementTree) tree);
        } else if (tree.is(Tree.Kind.NEW_CLASS)) {
            handleNewClass((NewClassTree) tree);
        }
    }

    @Override
    public void leaveNode(@Nonnull Tree tree) {
        if (tree.is(Tree.Kind.TRY_STATEMENT)) {
            leaveTryStatement();
        }
    }

    /**
     * Handle entering a try statement by pushing it onto the stack
     */
    private void handleTryStatement(@Nonnull TryStatementTree tryStatement) {
        tryStack.push(new TryStatementContext(tryStatement));
    }

    /**
     * Handle leaving a try statement by popping it from the stack and reporting issues if needed
     */
    private void leaveTryStatement() {
        if (!tryStack.isEmpty()) {
            TryStatementContext context = tryStack.pop();
            if (!context.autoCloseableInstances.isEmpty()) {
                reportIssue(context.tryStatement, MESSAGE_RULE);
            }
        }
    }

    /**
     * Handle new class instantiation to detect AutoCloseable objects
     * that are created inside a try-finally block (without try-with-resources)
     */
    private void handleNewClass(@Nonnull NewClassTree newClass) {
        // Check if the new instance is an AutoCloseable
        if (!newClass.symbolType().isSubtypeOf(JAVA_LANG_AUTOCLOSEABLE)) {
            return;
        }

        // Check if we are inside a non-compliant try statement
        if (isInNonCompliantTry()) {
            TryStatementContext context = tryStack.peek();
            if (context != null) {
                context.autoCloseableInstances.add(newClass);
            }
        }
    }

    /**
     * Check if we are currently inside a try statement that:
     * - Does NOT use try-with-resources (no resource list)
     * - Has a finally block (indicating manual resource management)
     *
     * @return true if inside a non-compliant try statement
     */
    private boolean isInNonCompliantTry() {
        if (tryStack.isEmpty()) {
            return false;
        }

        TryStatementTree currentTry = tryStack.peek().tryStatement;

        // If try-with-resources is already used, it's compliant
        if (!currentTry.resourceList().isEmpty()) {
            return false;
        }

        // If there's a finally block, it suggests manual resource management
        return currentTry.finallyBlock() != null;
    }

    /**
     * Context class to track information about a try statement during AST traversal
     */
    private static class TryStatementContext {
        final TryStatementTree tryStatement;
        final List<Tree> autoCloseableInstances;

        TryStatementContext(@Nonnull TryStatementTree tryStatement) {
            this.tryStatement = tryStatement;
            this.autoCloseableInstances = new ArrayList<>();
        }
    }
}
