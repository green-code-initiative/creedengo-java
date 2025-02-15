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
// TODO DDC : class to delete because is only the final class with all tests to be OK
class LimitDbQueryResult {

    public void callLiteralSQLString() {
        dummyCall("SELECT user FROM myTable"); // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
        dummyCall("SELECT user " + " FROM myTable"); // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
        dummyCall("SELECT user FROM myTable LIMIT 50"); // Compliant
        dummyCall("SELECT user FROM myTable WHERE user.name = 'titi'"); // Compliant
    }

    private void buildAndCallQuery() {
        String sql1 = "SELECT user FROM myTable"; // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
        String sql2 = "SELECT user FROM myTable LIMIT 50"; // Compliant
        String sql3 = "SELECT user FROM myTable WHERE user.name = 'titi'"; // Compliant
        String sql4 = "SELECT user"; // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
        sql4 += "FROM myTable"; // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}

        dummyCall(sql1);
        dummyCall(sql2);
        dummyCall(sql3);
        dummyCall(sql4);
    }

    private void callQueryWithReturn() {
        String sql1 = "SELECT user FROM myTable"; // Compliant (more exactly, not controlled and not issued because string returned)
        dumyCall("");
        return sql1;
    }

    private void callQueryWithReturn() {
        String sql1 = "SELECT user FROM myTable"; // Compliant (more exactly, not controlled and not issued because string returned)
        return sql1;
    }

    private void callQueryWithReturn2() {
        return "SELECT user FROM myTable"; // Compliant (more exactly, not controlled and not issued because string returned)
    }

    private void callQueryWithReturn3() {
        String sql2 = "SELECT user"; // Compliant (more exactly, not controlled and not issued because string returned)
        sql2 += "FROM myTable"; // Compliant (more exactly, not controlled and not issued because string returned)
        return sql2;
    }

    private void callQueryWithInput(String sqlInit) {
        String sql1 = "SELECT user FROM myTable"; // Compliant (more exactly, not controlled and not issued because input string used)
        sql1 += sqlInit;
        dummyCall(sql1);
    }

    private void buildAndCallQueryCompliant()
    {
        String sql4 = "SELECT user"; // Compliant because of coming next
        sql4 += " FROM myTable"; // Compliant because of coming next
        sql4 += " LIMIT 10"; // Compliant)
        dummyCall(sql4);
    }

    private void buildAndCallQueryNonCompliant()
    {
        String sql4 = "SELECT user"; // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
        sql4 += " FROM myTable"; // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
        dummyCall(sql4);
    }

    private void dummyCall(String request) {
    }
}