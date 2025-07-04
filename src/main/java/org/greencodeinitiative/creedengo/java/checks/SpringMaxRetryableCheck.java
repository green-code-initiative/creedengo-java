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

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// TODO DDC : rule already existing natively in SonarQube 9.9 (see java:S3012) for a part of checks
// ==> analyse / add our tag to it (?)

@Rule(key = "GCI604")
public class SpringMaxRetryableCheck extends IssuableSubscriptionVisitor {

    public static final String MESSAGE_RULE = "Please use optimized @Retryable parameters.";
    private static final long MAX_TIMEOUT = 5000;
    private static final int MAX_RETRY = 3;

    public static long calculateRetryTimeout(Integer maxAttempts, Long delay, Double multiplier) {
        int attempts = (maxAttempts != null) ? maxAttempts : 3;
        long initialDelay = (delay != null) ? delay : 1000L;
        double factor = (multiplier != null) ? multiplier : 1.0;
        long total = 0;
        long currentDelay = initialDelay;

        for (int i = 1; i < attempts; i++) {
            total += currentDelay;
            currentDelay = (long) (currentDelay * factor);
        }
        return total;
    }

    @Override
    public List<Kind> nodesToVisit() {
        return Collections.singletonList(Kind.ANNOTATION);
    }

    private Optional<String> extractConstantAsString(ExpressionTree tree) {
        Optional<Integer> asInt = tree.asConstant(Integer.class);
        if (asInt.isPresent()) {
            return Optional.of(String.valueOf(asInt.get()));
        }

        Optional<Double> asDouble = tree.asConstant(Double.class);
        return asDouble.map(String::valueOf).or(() -> tree.asConstant(String.class));
    }

    @Override
    public void visitNode(Tree tree) {
        AnnotationTree annotationTree = (AnnotationTree) tree;
        if (!"Retryable".equals(annotationTree.symbolType().fullyQualifiedName())) {
            return;
        }

        List<ArgumentDetails> params = new ArrayList<>();

        for (ExpressionTree argument : annotationTree.arguments()) {
            if (!argument.is(Tree.Kind.ASSIGNMENT)) {
                continue;
            }
            AssignmentExpressionTree assignmentTree = (AssignmentExpressionTree) argument;
            String paramName = ((IdentifierTree) assignmentTree.variable()).name();
            ExpressionTree valueTree = assignmentTree.expression();

            var extractedParams = extractParametersAndValues(argument, valueTree, paramName);
            if (!extractedParams.isEmpty()) {
                params.addAll(extractedParams);
            }
        }

        if (!params.isEmpty()) {
            checkValues(params);
        }
    }

    private List<ArgumentDetails> extractParametersAndValues(ExpressionTree argument, ExpressionTree valueTree, String paramName) {
        List<ArgumentDetails> params = new ArrayList<>();

        if (valueTree.asConstant(Integer.class).isPresent()) {
            int value = valueTree.asConstant(Integer.class).get();
            params.add(new ArgumentDetails(argument, paramName, String.valueOf(value)));
        } else if (valueTree.is(Kind.ANNOTATION)) {
            AnnotationTree nestedAnnotation = (AnnotationTree) valueTree;
            for (ExpressionTree nestedArg : nestedAnnotation.arguments()) {
                if (nestedArg.is(Kind.ASSIGNMENT)) {
                    AssignmentExpressionTree nestedAssignment = (AssignmentExpressionTree) nestedArg;
                    String nestedParam = ((IdentifierTree) nestedAssignment.variable()).name();
                    ExpressionTree nestedValueTree = nestedAssignment.expression();

                    Optional<String> constValue = extractConstantAsString(nestedValueTree);
                    constValue.ifPresent(val ->
                            params.add(new ArgumentDetails(nestedArg, paramName + "." + nestedParam, val))
                    );
                }
            }
        }

        return params;
    }

    void checkValues(List<ArgumentDetails> params) {
        Integer maxAttempts = params.stream()
                .filter(argumentDetails -> argumentDetails.paramName.equals("maxAttempts"))
                .map(ArgumentDetails::getParamValue)
                .map(Integer::valueOf)
                .findFirst().orElse(null);

        Long delay = params.stream()
                .filter(argumentDetails -> argumentDetails.paramName.equals("backoff.delay"))
                .map(ArgumentDetails::getParamValue)
                .map(Long::valueOf)
                .findFirst().orElse(null);

        Double multiplier = params.stream()
                .filter(argumentDetails -> argumentDetails.paramName.equals("backoff.multiplier"))
                .map(ArgumentDetails::getParamValue)
                .map(Double::parseDouble)
                .findFirst().orElse(null);

        if (isGreaterThanMax(maxAttempts, delay, multiplier)) {
            reportIssue(params.get(0).getArgument(), MESSAGE_RULE);
        }
    }

    public boolean isGreaterThanMax(Integer maxAttempts, Long delay, Double multiplier) {
        return (calculateRetryTimeout(maxAttempts, delay, multiplier) > MAX_TIMEOUT) || maxAttempts > MAX_RETRY;
    }

    class ArgumentDetails {
        private final ExpressionTree argument;
        private final String paramName;
        private final String paramValue;

        ArgumentDetails(ExpressionTree argument, String paramName, String paramValue) {
            this.argument = argument;
            this.paramName = paramName;
            this.paramValue = paramValue;
        }

        public ExpressionTree getArgument() {
            return argument;
        }

        public String getParamName() {
            return paramName;
        }

        public String getParamValue() {
            return paramValue;
        }
    }
}
