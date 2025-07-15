package org.greencodeinitiative.creedengo.java.integration.tests;/*
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Collections;


class NoFunctionCallWhenDeclaringForLoop {

    public int getMyValue() {
        return 6;
    }

    public int incrementeMyValue(final int i) {
        return i + 100;
    }

    public void test1() {
        for (int i = 0; i < 20; ++i) {
            System.out.println(i);
            final boolean b = getMyValue() > 6;
            System.out.println(b);
        }
    }

    public void test2() {
        final String[] cars = {"Volvo", "BMW", "Ford", "Mazda"};
        for (final String i : cars) {
            System.out.println(i);
        }

    }

    // compliant, the function is called only once in the initialization so it's not a performance issue
    public void test3() {
        for (int i = getMyValue(); i < 20; ++i) {
            System.out.println(i);
            final boolean b = getMyValue() > 6;
            System.out.println(b);
        }
    }

    public void test4() {
        for (int i = 0; i < getMyValue(); ++i) {  // Noncompliant {{Do not call a function when declaring a for-type loop}}
            System.out.println(i);
            final boolean b = getMyValue() > 6;
            System.out.println(b);
        }
    }

    public void test5() {
        for (final int i = 0; i < getMyValue(); incrementeMyValue(i)) {  // Noncompliant {{Do not call a function when declaring a for-type loop}}
            System.out.println(i);
            final boolean b = getMyValue() > 6;
            System.out.println(b);
        }
    }

    public void test6() {
        for (int i = getMyValue(); i < getMyValue(); ++i) { // Noncompliant {{Do not call a function when declaring a for-type loop}}
            System.out.println(i);
            final boolean b = getMyValue() > 6;
            System.out.println(b);
        }
    }

    // compliant, iterators are allowed to be called in a for loop
    public void test7() {
        final List<String> joursSemaine = Arrays.asList("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

        String jour = null;
        // iterator is allowed
        for (final Iterator<String> iterator = joursSemaine.iterator(); iterator.hasNext(); jour = iterator.next()) {
            System.out.println(jour);
        }

        // subclass of iterator is allowed
        for (final ListIterator<String> iterator = joursSemaine.listIterator(); iterator.hasNext(); jour = iterator.next()) {
            System.out.println(jour);
        }

        // iterator called in an indirect way is allowed
        for (final OtherClassWrapper otherClass = new OtherClassWrapper(joursSemaine.iterator()); otherClass.iterator.hasNext(); jour = otherClass.iterator.next()) {
            System.out.println(jour);
        }

        // but using a method that returns an iterator causes an issue
        for (final OtherClassWrapper otherClass = new OtherClassWrapper(joursSemaine.iterator()); otherClass.getIterator().hasNext(); jour = otherClass.getIterator().next()) {  // Noncompliant {{Do not call a function when declaring a for-type loop}}
            System.out.println(jour);
        }

    }

    // compliant, enumeration is allowed
    public void test8() {
        final List<String> joursSemaine = Arrays.asList("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

        String jour = null;
        for (final Enumeration<String> enumeration = Collections.enumeration(joursSemaine); enumeration.hasMoreElements(); jour = enumeration.nextElement()) {
            System.out.println(jour);
        }

        // enumeration called in an indirect way is allowed
        for(final OtherClassWrapper otherClass = new OtherClassWrapper(Collections.enumeration(joursSemaine)); otherClass.enumeration.hasMoreElements(); jour = otherClass.enumeration.nextElement()) {
            System.out.println(jour);
        }

        // but using a method that returns an enumeration causes an issue
        for(final OtherClassWrapper otherClass = new OtherClassWrapper(Collections.enumeration(joursSemaine)); otherClass.getEnumeration().hasMoreElements(); jour = otherClass.getEnumeration().nextElement()) {  // Noncompliant {{Do not call a function when declaring a for-type loop}}
            System.out.println(jour);
        }

    }

}

class OtherClassWrapper {
    public Iterator<String> iterator = null;
    public Enumeration<String> enumeration = null;

    public OtherClassWrapper(Iterator<String> iterator){
        this.iterator = iterator;
    }
    public OtherClassWrapper(Enumeration<String> enumeration){
        this.enumeration = enumeration;
    }

    public Iterator<String> getIterator(){
        return iterator;
    }

    public Enumeration<String> getEnumeration(){
        return enumeration;
    }
}
