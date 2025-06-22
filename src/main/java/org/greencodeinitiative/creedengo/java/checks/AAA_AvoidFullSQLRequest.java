package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.check.Rule;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

@Rule(key = "GCI74")
public class AAA_AvoidFullSQLRequest extends IssuableSubscriptionVisitor {

    protected static final String MESSAGERULE = "BZH25 : no Full SQL request, guy !!!";
    private static final Predicate<String> SELECT_FROM_REGEXP =
            Pattern.compile("select\\s*\\*\\s*from", Pattern.CASE_INSENSITIVE).asPredicate();

    @Override
    public List<Kind> nodesToVisit() {
        return Collections.singletonList(Kind.STRING_LITERAL);
    }

    @Override
    public void visitNode(Tree tree) {
        String value = ((LiteralTree) tree).value();
        if (SELECT_FROM_REGEXP.test(value)) {
            reportIssue(tree, MESSAGERULE);
        }
    }

}