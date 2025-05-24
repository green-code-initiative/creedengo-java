package org.greencodeinitiative.creedengo.java.checks;

import java.util.List;
import java.util.stream.Collectors;

class UseFilterBeforeSort {
    UseFilterBeforeSort() {
    }

    public void manipulateStream(final List<String> list) {
        list.stream() // Noncompliant {{Use 'filter' before 'sorted' for better efficiency.}}
                .sorted()
                .filter(s -> s.startsWith("A"))
                .collect(Collectors.toList());

        list.stream() // Compliant {{Use 'filter' before 'sorted' for better efficiency.}}
                .filter(s -> s.startsWith("A"))
                .sorted()
                .collect(Collectors.toList());
    }
}