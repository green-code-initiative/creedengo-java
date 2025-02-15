package org.greencodeinitiative.creedengo.java.checks;

import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

class OptimizeSQLQueriesWithLimit {

    public void literalSQLrequest() {
        dummyCall("SELECT user FROM myTable"); // Noncompliant {{Optimize Database SQL Queries (Clause LIMIT)}}
        dummyCall("SELECT user FROM myTable LIMIT 50"); // Compliant
    }

    @Query("select t from Todo t where t.status != 'COMPLETED'") // Noncompliant {{Optimize Database SQL Queries (Clause LIMIT)}}
    public List<Object> findAllUsers() {
        return new ArrayList<>();
    }

    @Query("select t from Todo t where t.status != 'COMPLETED' LIMIT 25") // Compliant
    public List<Object> findFirstUsers() {
        return new ArrayList<>();
    }

    private void callQuery() {
        String sql1 = "SELECT user FROM myTable"; // Noncompliant {{Optimize Database SQL Queries (Clause LIMIT)}}
        String sql2 = "SELECT user FROM myTable LIMIT 50"; // Compliant
    }

    private void dummyCall(String request) {
    }
}
