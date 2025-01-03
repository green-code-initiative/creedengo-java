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
package org.greencodeinitiative.creedengo.java.utils;

public class GoodWayConcatenateStringsLoop {

    public String concatenateStrings(String[] strings) {
        StringBuilder result = new StringBuilder();

        for (String string : strings) {
            result.append(string);
        }
        return result.toString();
    }

    public void testConcateOutOfLoop() {
        String result = "";
        result += "another";
    }

    public void testConcateOutOfLoop2() {
        String result = "";
        result = result + "another";
    }

    public String changeValueStringInLoop() {
        String result3 = "";

        for (int i = 0; i < 1; ++i) {
            result3 = "another";
        }
        return result3;
    }

}
