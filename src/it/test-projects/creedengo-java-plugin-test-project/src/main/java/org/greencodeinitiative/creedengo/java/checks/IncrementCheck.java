package org.greencodeinitiative.creedengo.java.checks;

class IncrementCheck {
    IncrementCheck(IncrementCheck mc) {
    }

    int foo1() {
        int counter = 0;
        return counter++; // Noncompliant {{Use ++i instead of i++}}
    }

    private int j = 0;
    int foo10() {
        return this.j++; // Compliant because maybe the use case needs to return j AND increment it
    }

    int foo11() {
        int counter = 0;
        return ++counter;
    }

    int foo2() {
        int counter = 0;
        counter++; // Noncompliant {{Use ++i instead of i++}}
        return counter;
    }

    int foo22() {
        int counter = 0;
        ++counter;
        return counter;
    }

    int foo3() {
        int counter = 0;
        counter = counter + 197845 ;
        return counter;
    }

    int foo4() {
        int counter = 0;
        counter = counter + 35 + 78 ;
        return counter;
    }

    void foo50() {
        for (int i=0; i < 10; i++) { // Noncompliant {{Use ++i instead of i++}}
            System.out.println(i); //NOSONAR
        }
    }

    void foo51() {
        for (int i=0; i < 10; ++i) {
            System.out.println(i); //NOSONAR
        }
    }

    void bar61(int value) {
        // For test purpose
    }

    int foo61() {
        int i = 0;
        bar61(i++); // Compliant because maybe bar61 needs the unincremented value
        return i;
    }

    int foo62() {
        int i = 0;
        bar61(2 + i++); // Compliant because maybe bar61 needs the unincremented value
        return i;
    }

    void foo71() {
        int counter = 0;
        int a = 2 + counter++;  // Compliant because we probably want to increment counter
                                // then to add it to 2 to initialize a
    }
}
