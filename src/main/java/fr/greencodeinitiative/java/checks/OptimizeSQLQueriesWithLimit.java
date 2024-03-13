package fr.greencodeinitiative.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

@Rule(key = "EC80")
public class OptimizeSQLQueriesWithLimit extends IssuableSubscriptionVisitor {

    public static final String MESSAGE_RULE = "Optimize Database SQL Queries (Clause LIMIT)";
    private static final Predicate<String> LIMIT_REGEXP =
            compile("limit", CASE_INSENSITIVE).asPredicate();
    private static final Predicate<String> SELECT_REGEXP =
            compile("select", CASE_INSENSITIVE).asPredicate();
    private static final Predicate<String> FROM_REGEXP =
            compile("from", CASE_INSENSITIVE).asPredicate();

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return singletonList(Tree.Kind.STRING_LITERAL);
    }

    @Override
    public void visitNode(Tree tree) {
        String value = ((LiteralTree) tree).value();
        if (SELECT_REGEXP.test(value) && FROM_REGEXP.test(value) && !LIMIT_REGEXP.test(value)) {
            reportIssue(tree, MESSAGE_RULE);
        }
    }

}
