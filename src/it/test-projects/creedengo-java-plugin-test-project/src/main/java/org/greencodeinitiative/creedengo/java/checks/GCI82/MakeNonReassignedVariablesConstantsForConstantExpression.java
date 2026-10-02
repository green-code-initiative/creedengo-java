package org.greencodeinitiative.creedengo.java.checks;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

/**
 * A variable of primitive or String type initialized with a compile-time constant expression (JLS 15.29)
 * becomes a constant variable once 'final' : javac inlines its value and computes the expressions using it.
 */
public class MakeNonReassignedVariablesConstantsForConstantExpression {

    private static final Logger LOGGER = Logger.getLogger("GCI82");

    private static final int MAX = 10;

    private int instanceConstant = 42; // Noncompliant {{The variable is never reassigned and can be 'final'}}

    private int instanceFromMethod = compute(); // Compliant : not a compile-time constant

    public void constantExpressions() {
        int sum = 1 + 2; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        long big = 10L * 1024; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        String concat = "a" + "b"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        boolean flag = true; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        char letter = 'c'; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        double ratio = 1.5 / 3; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        int fromConstant = MAX * 2; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        int ternary = MAX > 5 ? 1 : 2; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        var inferred = 42; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        LOGGER.info(sum + big + concat + flag + letter + ratio + fromConstant + ternary + inferred + instanceConstant);
    }

    public void notConstantExpressions() {
        Integer boxed = 42; // Compliant : a boxed type can't be a constant variable
        Object object = "object"; // Compliant : neither a primitive nor a String
        String nullString = null; // Compliant : null is not a constant expression
        int fromMethod = compute(); // Compliant
        String fromStringMethod = String.valueOf(1); // Compliant
        int length = "abc".length(); // Compliant
        LOGGER.info(boxed + " " + object + nullString + fromMethod + fromStringMethod + length + instanceFromMethod);
    }

    public void initializedFromNonFinalVariable() {
        int source = 1; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        int copy = source; // Compliant : 'source' is not final, so it is not a constant expression
        LOGGER.info(String.valueOf(source + copy));
    }

    public void variablesWithoutInitializer() throws IOException {
        for (String item : new String[]{"a", "b"}) { // Compliant : for-each variable
            LOGGER.info(item);
        }
        UnaryOperator<String> identity = value -> value; // Compliant : lambda and lambda parameter
        try (StringReader reader = new StringReader("reader")) { // Compliant : try-with-resources variable
            LOGGER.info(identity.apply(String.valueOf(reader.read())));
        }
    }

    private static int compute() {
        return MAX;
    }

}
