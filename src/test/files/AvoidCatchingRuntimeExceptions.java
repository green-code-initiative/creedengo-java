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

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.runtime.RuntimeException;
import java.lang.ArithmeticException;
import java.lang.IndexOutOfBoundsException;
import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;
import java.lang.Integer;

public class AvoidCatchingRuntimeExceptions {
    public void nominalRuntimeException() {
        try {
            // some code that may throw an exception
        } catch (RuntimeException e) { // Noncompliant {{Avoid catching Runtime exceptions : RuntimeException}}
        }
    }

    public void nominalRuntimeExceptionDependant() {
        int[] array = new int[10];
        try {
            array[10] = 0;
        } catch (IndexOutOfBoundsException e) { // Noncompliant {{Avoid catching Runtime exceptions : IndexOutOfBoundsException}}
        }
    }

    public void nominalRuntimeExceptionDependant_2(){
        Object obj = null;
        try {
            obj.toString();
        } catch (NullPointerException e) { // Noncompliant {{Avoid catching Runtime exceptions : NullPointerException}}
        }
    }

    public void nominalRuntimeExceptionDependant_3(){
        try {
            int result = 1 / 0;
        } catch (ArithmeticException e) { // Noncompliant {{Avoid catching Runtime exceptions : ArithmeticException}}
        }
    }

    //these exceptions are ok because they aren't runtime exceptions

    public void compliantNonRuntimeException() {
        try {
            File file = new File("nonexistent.txt");
            FileReader fr = new FileReader(file);
            fr.read();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    // these exceptions are ok because they are IllegalArgumentExceptions, and can hardly be avoided

    public void compliantIllegalArgumentException() {
        try {
            // some code that may throw an exception
        } catch (IllegalArgumentException e) {
        }
    }

    public void compliantIllegalArgumentExceptionDependant() {
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException e) {
        }
    }
    
}
