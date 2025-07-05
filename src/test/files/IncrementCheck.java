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

private class Foo {
    public int i; //NOSONAR
}

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

    int foo12() {
        Foo f;
        return f.i++; // Compliant because maybe the use case needs to return j AND increment it
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
