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
interface LimitDbQueryResultInterface {

    @Query("select t from Todo t") // Noncompliant {{Try and limit the number of data returned for a single query (by using the LIMIT keyword for example)}}
    List<Todo> getTodo();

    @Query("select t from Todo t where t.status != 'COMPLETED'") // Compliant
    List<Todo> getTodoNotCompleted();

    @Query("select t from Todo t where t.status != 'COMPLETED' LIMIT 25") // Compliant
    List<Todo> getTodoNotCompletedLimit25();

}