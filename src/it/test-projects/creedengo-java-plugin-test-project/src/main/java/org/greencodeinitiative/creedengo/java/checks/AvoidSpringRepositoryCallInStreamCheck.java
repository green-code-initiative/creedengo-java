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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AvoidSpringRepositoryCallInStreamCheck {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> smellGetAllEmployeesByIdsForEach() {
        List<Employee> employees = new ArrayList<>();
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        stream.forEach(id -> {
            Optional<Employee> employee = employeeRepository.findById(id); // Noncompliant {{Avoid Spring repository call in loop or stream}}
            employee.ifPresent(employees::add);
        });
        return employees;
    }

    public List<Employee> smellGetAllEmployeesByIdsForEachOrdered() {
        List<Employee> employees = new ArrayList<>();
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        stream.forEachOrdered(id -> {
            Optional<Employee> employee = employeeRepository.findById(id); // Noncompliant {{Avoid Spring repository call in loop or stream}}
            employee.ifPresent(employees::add);
        });
        return employees;
    }

    public List<List<Employee>> smellGetAllEmployeesByIdsMap() {
        List<Employee> employees = new ArrayList<>();
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        return stream.map(id -> {
            Optional<Employee> employee = employeeRepository.findById(id); // Noncompliant {{Avoid Spring repository call in loop or stream}}
                    employee.ifPresent(employees::add);
            return employees;
        })
        .collect(Collectors.toList());
    }

    public List<Integer> smellGetAllEmployeesByIdsPeek() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        return stream.peek(id -> {
            Optional<Employee> employee = employeeRepository.findById(id); // Noncompliant {{Avoid Spring repository call in loop or stream}}
        })
        .collect(Collectors.toList());
    }

    public List<Employee> smellGetAllEmployeesByIdsWithOptional(List<Integer> ids) {
        return ids
                .stream()
                .map(element -> {
                    Employee employ = new Employee(1, "name");
                    return employeeRepository.findById(element).orElse(employ);// Noncompliant {{Avoid Spring repository call in loop or stream}}
                })
                .collect(Collectors.toList());
    }

    public List<Optional<Employee>> smellGetAllEmployeesByIds(List<Integer> ids) {
        Stream<Integer> stream = ids.stream();
        return stream.map(element -> {
                    return employeeRepository.findById(element);// Noncompliant {{Avoid Spring repository call in loop or stream}}
                })
                .collect(Collectors.toList());
    }

    public List<Employee> smellGetAllEmployeesByIdsWithoutStream(List<Integer> ids) {
        return employeeRepository.findAllById(ids); // Compliant
    }

    public List<Optional<Employee>> smellDeleteEmployeeById(List<Integer> ids) {
        Stream<Integer> stream = ids.stream();
        return stream.map(id -> {
                    return employeeRepository.findById(id);// Noncompliant {{Avoid Spring repository call in loop or stream}}
                })
                .collect(Collectors.toList());
    }

    public List<Employee> smellGetAllEmployeesByIdsWithSeveralMethods(List<Integer> ids) {
        Stream<Integer> stream = ids.stream();
        return stream.map(element -> {
                    Employee empl = new Employee(1, "name");
                    return employeeRepository.findById(element).orElse(empl);// Noncompliant {{Avoid Spring repository call in loop or stream}}
                })
                .collect(Collectors.toList());
    }

    public static class Employee {
      private final Integer id;
        private final String name;

        public Employee(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
    }

    public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    }
}
