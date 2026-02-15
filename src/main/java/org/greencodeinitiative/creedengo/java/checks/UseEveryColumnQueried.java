/*
 * ecoCode - Java language - Provides rules to reduce the environmental footprint of your Java programs
 * Copyright © 2023 Green Code Initiative (https://www.ecocode.io)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Symbol.VariableSymbol;
import org.sonar.plugins.java.api.tree.Arguments;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

@Rule(key = "GCI95")
public class UseEveryColumnQueried extends IssuableSubscriptionVisitor {

    protected static final String MESSAGERULE = "Avoid querying SQL columns that are not used";
    private static final String JAVA_SQL_STATEMENT = "java.sql.Statement";
    private static final String JAVA_SQL_RESULTSET = "java.sql.ResultSet";
    private static final MethodMatchers SQL_STATEMENT_DECLARE_SQL = MethodMatchers.create()
            .ofSubTypes(JAVA_SQL_STATEMENT)
            .names("executeQuery", "execute")
            .addParametersMatcher("java.lang.String")
            .build();
    private static final MethodMatchers SQL_STATEMENT_RETRIEVE_RESULTSET = MethodMatchers.create()
            .ofSubTypes(JAVA_SQL_STATEMENT)
            .names("executeQuery", "getResultSet")
            .withAnyParameters()
            .build();
    private static final MethodMatchers SQL_RESULTSET_GET_COLNAME = MethodMatchers.create()
            .ofSubTypes(JAVA_SQL_RESULTSET)
            .name(n -> n.startsWith("get"))
            .addParametersMatcher("java.lang.String")
            .build();
    private static final MethodMatchers SQL_RESULTSET_GET_COLID = MethodMatchers.create()
            .ofSubTypes(JAVA_SQL_RESULTSET)
            .name(n -> n.startsWith("get"))
            .addParametersMatcher("int")
            .build();
    private static final Pattern SELECTED_COLUMNS_PATTERN = Pattern.compile("SELECT\\s+(.*)\\s+FROM\\s+.*");

    @Override
    public List<Kind> nodesToVisit() {
        return Arrays.asList(Kind.METHOD_INVOCATION);
    }

    /**
     * How this rule works : 
     * We start from the method invocation that declares the SQL query ( stmt.executeQuery("SELECT ... FROM ...") )
     * the selected columns are directly extracted from the parameters of this method invocation
     * We explore the stmt object to find where the method invocation that returns the ResultSet object
     * finally we explore all invocations of this ResultSet object to list all the column used.
     * the selected and used columns are compared, and an issue is reported if columns are selected but not used.
     * 
     * 
     *               stmt.execute("SELECT ... FROM ...") or stmt.executeQuery(...)
     *                |                    |
     *                |                    ----> Selected Columns
     *          [Statement Object]
     *                |
     *                |
     *                v
     *         res = stmt.getResultSet() or stmt.executeQuery(...)
     *          |
     *          |
     *  [ResultSet Object]
     *          |
     *          |
     *          v
     *         res.getInt(...) or any column extraction method
     *                     |
     *                     ----> Used Column
     * 
     */
    @Override
    public void visitNode(Tree tree) {
        MethodInvocationTree methodInvocationTree = (MethodInvocationTree) tree;
        if (!SQL_STATEMENT_DECLARE_SQL.matches(methodInvocationTree)) {
            return;
        }

        // extraction of the selected columns
        List<String> selectedColumns = getSelectedColumns(methodInvocationTree);
        if (selectedColumns.isEmpty()) {
            return;
        }

        //if selected columns includes "*", stop the search
        if (selectedColumns.contains("*")) {
            return;
        }

        // get the ResultSet object and check it's validity
        Symbol resultSet = getResultSetNode(methodInvocationTree);
        if(isResultSetInvalid(resultSet)){
            return;
        }

        // extraction of the used columns
        List<String> usedColumns = getUsedColumns(resultSet, selectedColumns);

        // if there are selected columns that are not used in the code, report the issue
        List<String> differences = selectedColumns.stream()
                .filter(element -> !usedColumns.contains(element))
                .collect(Collectors.toList());
        if (!differences.isEmpty()) {
            reportIssue(methodInvocationTree, MESSAGERULE);
        }
    }

    private static List<String> getSelectedColumns(MethodInvocationTree methodInvocationTree) {
        // get the first argument of the query definition method
        Arguments arguments = methodInvocationTree.arguments();
        if (arguments.isEmpty()) {
            return new ArrayList<>();
        }
        ExpressionTree argument = arguments.get(0);
        // get the contents of the string in this first parameters
        LiteralTree literal = extractLiteralFromVariable(argument);
        if (literal == null) {
            return new ArrayList<>();
        }
        String query = literal.value();
        //get the list of selected columns from this string
        return extractSelectedSQLColumns(query);
    }

    /**
     * returns a list of used columns from a resultset object and a list of the selected columns
     */
    private static List<String> getUsedColumns(Symbol resultSet, List<String> selectedColumns) {
        // iterate across all usages of the ResultSet
        List<String> usedColumns = new ArrayList<>();
        List<IdentifierTree> resultSetUsages = resultSet.usages();
        for (IdentifierTree usage : resultSetUsages) {
            // check this usage is an assignement, and the method parameters
            if (usage.parent().is(Tree.Kind.ASSIGNMENT)) {
                break;
            }
            MethodInvocationTree methodInvocation = getMethodInvocationFromTree(usage);
            if (methodInvocation == null || methodInvocation.arguments().isEmpty()) {
                continue;
            }
            // get the value of the first parameter
            ExpressionTree parameter = methodInvocation.arguments().get(0);
            LiteralTree columnGot = extractLiteralFromVariable(parameter);
            if (columnGot == null) {
                continue;
            }
            String column;
            String value = columnGot.value();
            // if this first parameter is a string, clean up and use as is for used column name
            if (SQL_RESULTSET_GET_COLNAME.matches(methodInvocation) && columnGot.is(Tree.Kind.STRING_LITERAL)) {
                column = value.toUpperCase()
                        .replaceAll("^['\"]", "")
                        .replaceAll("['\"]$", "");
            // if this first parameter is an int, use as and id for used column name
            } else if (SQL_RESULTSET_GET_COLID.matches(methodInvocation) && columnGot.is(Tree.Kind.INT_LITERAL)) {
                int columnId = Integer.parseInt(value);
                if (columnId > selectedColumns.size()) {
                    break;
                }
                column = selectedColumns.get(columnId - 1);
            } else {
                continue;
            }
            usedColumns.add(column);
        }
        return usedColumns;
    }

    /**
     * returns the litteral assigned to a variable var
     * var has to be either a litteral itself or a final variable
     * returns null if the ExpressionTree is not a variable, 
     * if the variable is not final, or if the variable has not been initialized
     */
    @Nullable
    private static LiteralTree extractLiteralFromVariable(ExpressionTree tree) {
        if (tree instanceof LiteralTree) {
            return (LiteralTree) tree;
        }
        if (!tree.is(Tree.Kind.IDENTIFIER)) {
            return null;
        }
        IdentifierTree identifierTree = (IdentifierTree) tree;
        Symbol symbol = identifierTree.symbol();
        if (symbol == null || !symbol.isFinal() || !symbol.isVariableSymbol()) {
            return null;
        }
        VariableSymbol variableSymbol = (VariableSymbol) symbol;
        Tree assignment = variableSymbol.declaration();
        if (!assignment.is(Tree.Kind.VARIABLE)) {
            return null;
        }
        VariableTree variableTree = (VariableTree) assignment;
        ExpressionTree initializer = variableTree.initializer();
        if (initializer instanceof LiteralTree) {
            return (LiteralTree) initializer;
        }
        return null;
    }

    /**
     * get the ResultSet Object assigned from the result of the retrieve resultset method
     * from the sql declaration method (via the shared stmt object)
     * stmt.execute(...) -> rs = stmt.getResultSet()
     */
    @Nullable
    private static Symbol getResultSetNode(MethodInvocationTree methodInvocationTree) {
        // get the Statement object on witch the method is called
        ExpressionTree et = methodInvocationTree.methodSelect();
        if (!et.is(Tree.Kind.MEMBER_SELECT)) {
            return null;
        }
        MemberSelectExpressionTree mset = (MemberSelectExpressionTree) et;
        ExpressionTree expression = mset.expression();
        if (!expression.is(Tree.Kind.IDENTIFIER)) {
            return null;
        }
        IdentifierTree id = (IdentifierTree) expression;
        Symbol statement = id.symbol();
        if (statement == null) {
            return null;
        }
        // iterate over all usages of this Statement object
        List<IdentifierTree> usages = statement.usages();
        Symbol resultSet = null;
        for (IdentifierTree usage : usages) {
            // does this usage of the Statement object match SQL_STATEMENT_RETRIEVE_RESULTSET ?
            MethodInvocationTree methodInvocation = getMethodInvocationFromTree(usage);
            if (methodInvocation == null || !SQL_STATEMENT_RETRIEVE_RESULTSET.matches(methodInvocation)) {
                continue;
            }
            // if so end the search, we have found our resultSet object
            Tree parent = methodInvocation.parent();
            if (parent.is(Tree.Kind.VARIABLE)) {
                resultSet = ((VariableTree) parent).symbol();
                break;
            }
        }
        return resultSet;
    }

    /**
     * unpacks a chain call to get the method invocation node
     * example : this.object.chain.method() -> method()
     */
    @Nullable
    private static MethodInvocationTree getMethodInvocationFromTree(IdentifierTree tree) {
        Tree parent = tree;
        while (parent != null && !parent.is(Tree.Kind.METHOD_INVOCATION)) {
            parent = parent.parent();
        }
        return (MethodInvocationTree) parent;
    }

    /**
     * checks the two conditions that make a ResultSet object invalid,
     * and would stop the search for used columns because of side effects
     * - the ResultSet object being passed in a method 
     * - the ResultSet object being reassigned
     */
    private static boolean isResultSetInvalid(Symbol resultSet) {
        return resultSet==null || isObjectUsedInMethodParameters(resultSet)
            || isObjectReassigned(resultSet);
    }

    /**
     * checks if an object is used as a parameter a method
     */
    private static boolean isObjectUsedInMethodParameters(Symbol obj) {
        List<IdentifierTree> usages = obj.usages();
        for (IdentifierTree usage : usages) {
            Tree parent = usage.parent();
            if (parent.is(Tree.Kind.ARGUMENTS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * checks if an object is reassigned
     */
    private static boolean isObjectReassigned(Symbol obj) {
        List<IdentifierTree> usages = obj.usages();
        for (IdentifierTree usage : usages) {
            Tree parent = usage.parent();
            if (parent.is(Tree.Kind.ASSIGNMENT)) {
                AssignmentExpressionTree assignment = (AssignmentExpressionTree) parent;
                ExpressionTree expressionTree = assignment.variable();
                if (expressionTree.is(Tree.Kind.IDENTIFIER)
                        && obj.equals(((IdentifierTree) expressionTree).symbol())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * extract from a SQL query in the form of "SELECT X, Y AS Z FROM TABLE ..." 
     * a list of all the column names and aliases (X and Z) without whitespace and in uppercase
     */
    static List<String> extractSelectedSQLColumns(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        query = query.toUpperCase()
                .replaceAll("^['\"]", "")
                .replaceAll("['\"]$", "");
        List<String> columns = new ArrayList<>();
        Matcher matcher = SELECTED_COLUMNS_PATTERN.matcher(query);
        if (matcher.matches()) {
            String columnString = matcher.group(1);
            columns = Arrays.asList(columnString.split(","));
            columns.replaceAll(column -> column.replaceAll("\\s+", " "));
            columns.replaceAll(column -> column.contains(" AS ") ? column.split(" AS ")[1].trim() : column.trim());
        }
        return columns;
    }
}
