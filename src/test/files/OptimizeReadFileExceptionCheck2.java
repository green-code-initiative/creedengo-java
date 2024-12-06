/*
 * ecoCode - Java language - Provides rules to reduce the environmental footprint of your Java programs
 * Copyright © 2024 Green Code Initiative (https://www.ecocode.io)
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
package org.greencodeinitiative.java.checks;

import java.util.Arrays;
import java.util.List;

class ReadFile {
    ReadFile(ReadFile readFile) {
    }

    public void readPreferences(String filename) {
        //...
        try (InputStream in = new FileInputStream(filename)) { // Noncompliant {{Optimize Read File Exceptions}}
            logger.log("my log");
        } catch (FileNotFoundException e) {
            logger.log(e);
        }
        //...
    }
}
