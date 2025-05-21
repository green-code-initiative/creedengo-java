package org.greencodeinitiative.creedengo.java.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Symbol.VariableSymbol;
import org.sonar.plugins.java.api.tree.*;

import java.util.*;

@Rule(key = "GCI604")
public class AvoidNPlusOneProblemInJPAEntitiesCheck extends IssuableSubscriptionVisitor {

    protected static final String RULE_MESSAGE = " Evitez le N+1 : utilisez un fetch join ou une récupération eager. ";

    private static final String SPRING_REPOSITORY = "org.springframework.data.repository.Repository";

    private static final MethodMatchers SPRING_REPOSITORY_METHOD_FIND_ALL =
            MethodMatchers.create()
                    .ofSubTypes(SPRING_REPOSITORY)
                    .names("findAll")
                    .withAnyParameters()
                    .build();

    private final Map<Symbol, Tree> repositoryFindAllVars = new HashMap<>();

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Arrays.asList(Tree.Kind.VARIABLE, Tree.Kind.METHOD_INVOCATION, Tree.Kind.FOR_EACH_STATEMENT);
    }

    @Override
    public void visitNode(Tree tree) {
        if (tree.is(Tree.Kind.VARIABLE)) {
            VariableTree variableTree = (VariableTree) tree;
            ExpressionTree initializer = variableTree.initializer();
            if (initializer != null && initializer.is(Tree.Kind.METHOD_INVOCATION)) {
                MethodInvocationTree methodInvocation = (MethodInvocationTree) initializer;
                if (SPRING_REPOSITORY_METHOD_FIND_ALL.matches(methodInvocation)) {
                    VariableSymbol symbol = (VariableSymbol) variableTree.symbol();
                    repositoryFindAllVars.put(symbol, tree);
                }
            }
        }

        // Cas d'un foreach sur une variable issue de findAll()
        if (tree.is(Tree.Kind.FOR_EACH_STATEMENT)) {
            ForEachStatement forEach = (ForEachStatement) tree;
            ExpressionTree iterable = forEach.expression();
            if (iterable.is(Tree.Kind.IDENTIFIER)) {
                Symbol symbol = ((IdentifierTree) iterable).symbol();
                if (repositoryFindAllVars.containsKey(symbol)) {
                    // On marque la variable d'itération comme issue d'un findAll()
                    VariableSymbol loopVar = (VariableSymbol) forEach.variable().symbol();
                    repositoryFindAllVars.put(loopVar, tree);
                }
            }
        }

        // Détection d'un appel de getter sur une variable issue de findAll()
        if (tree.is(Tree.Kind.METHOD_INVOCATION)) {
            MethodInvocationTree methodInvocation = (MethodInvocationTree) tree;

            // Check if the call is something like post.getAuthor() or post.getAuthor().getName()
            ExpressionTree select = methodInvocation.methodSelect();
            if (select.is(Tree.Kind.MEMBER_SELECT)) {
                MemberSelectExpressionTree memberSelect = (MemberSelectExpressionTree) select;
                ExpressionTree root = memberSelect.expression();

                if (root.is(Tree.Kind.IDENTIFIER)) {
                    Symbol symbol = ((IdentifierTree) root).symbol();
                    if (repositoryFindAllVars.containsKey(symbol) && isGetter(memberSelect.identifier().name())) {
                        reportIssue(methodInvocation, RULE_MESSAGE);
                    }
                }

                // Handle nested getter chains (e.g., post.getAuthor().getName())
                if (root.is(Tree.Kind.METHOD_INVOCATION)) {
                    MethodInvocationTree rootInvocation = (MethodInvocationTree) root;
                    ExpressionTree deeperSelect = rootInvocation.methodSelect();
                    if (deeperSelect.is(Tree.Kind.MEMBER_SELECT)) {
                        MemberSelectExpressionTree deeperMemberSelect = (MemberSelectExpressionTree) deeperSelect;
                        ExpressionTree deeperRoot = deeperMemberSelect.expression();

                        if (deeperRoot.is(Tree.Kind.IDENTIFIER)) {
                            Symbol rootSymbol = ((IdentifierTree) deeperRoot).symbol();
                            if (repositoryFindAllVars.containsKey(rootSymbol) && isGetter(deeperMemberSelect.identifier().name())) {
                                reportIssue(methodInvocation, RULE_MESSAGE);
                            }
                        }
                    }
                }
            }
        }
    }

    // Méthode utilitaire pour détecter un getter
    private boolean isGetter(String methodName) {
        return methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3));
    }
}
