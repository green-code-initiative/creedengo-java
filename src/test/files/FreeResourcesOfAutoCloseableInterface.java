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

import java.io.*;

class FreeResourcesOfAutoCloseableInterface {
    FreeResourcesOfAutoCloseableInterface(FreeResourcesOfAutoCloseableInterface mc) {

    }

    public void foo1() {
        String fileName = "./FreeResourcesOfAutoCloseableInterface.java";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader br = new BufferedReader(fr)) { // Compliant
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void foo2() {
        String fileName = "./FreeResourcesOfAutoCloseableInterface.java";
        try { // Noncompliant {{try-with-resources Statement needs to be implemented for any object that implements the AutoCloseable interface.}}
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            System.out.printl(br.readLine());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (fr) {
                org.close();
            }
            if (br) {
                br.close();
            }
        }
    }

    /**
     * The first method adds a "try" in the stack used to follow if the code is in a try
     */
    public void callingMethodWithTheTry() throws IOException {
        try { // Compliant
            calledMethodWithoutTry();
        } finally {
            // Empty block of code
        }
    }

    /**
     * The "try" should have been popped from the stack before entering here
     */
    private void calledMethodWithoutTry() throws IOException {
        FileWriter myWriter = new FileWriter("somefilepath");
        myWriter.write("something");
        myWriter.flush();
        myWriter.close();
    }
}
