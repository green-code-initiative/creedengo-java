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

class AvoidMultipleIfElseStatementCompareMethod {

    public int compare(FieldVo o1, FieldVo o2) {

        if (o1.getIdBlock().equals(o2.getIdBlock())) {
            if (o1.getIdField().equals(o2.getIdField())) {
                return 0;
            }
            // First original
            if (o1.isOriginal() && !o2.isOriginal()) {
                return -1;
            } else if (!o1.isOriginal() && o2.isOriginal()) {
                return 1;
            }
            // First min posgafld
            Long result = o1.getColumnPos() - o2.getColumnPos();
            if (result != 0) {
                return result.intValue();
            }

            // First min ordgaflc
            result = o1.getIndex() - o2.getIndex();
            return result.intValue();
        }
        // First BQRY block
        if (o1.getIdBlock().startsWith("BQRY") && !o2.getIdBlock().startsWith("BQRY")) {
            return -1;
        } else if (!o1.getIdBlock().startsWith("BQRY") && o2.getIdBlock().startsWith("BQRY")) {
            return 1;
        }
        // If both block don't start with BQRY, sort alpha with String.compareTo method
        return o1.getIdBlock().compareTo(o2.getIdBlock());
    }

}
