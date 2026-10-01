package org.greencodeinitiative.creedengo.java.checks;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MakeNonReassignedVariablesConstantsForCatch {

    private static final Logger LOGGER = Logger.getLogger("GCI82");

    public void simpleCatch() {
        try {
            mayThrow();
        } catch (IOException e) { // Compliant : a catch parameter is out of the scope of the rule
            LOGGER.log(Level.SEVERE, "error", e);
        }
    }

    public void multiCatch() {
        try {
            mayThrow();
            mayThrowReflective();
        } catch (IOException | ReflectiveOperationException e) { // Compliant : a multi-catch parameter is implicitly final
            LOGGER.log(Level.SEVERE, "error", e);
        }
    }

    public void finalCatch() {
        try {
            mayThrow();
        } catch (final IOException e) { // Compliant
            LOGGER.log(Level.SEVERE, "error", e);
        }
    }

    public void catchParameterReassigned() {
        try {
            mayThrow();
        } catch (IOException e) { // Compliant
            e = new IOException("wrapped", e);
            LOGGER.log(Level.SEVERE, "error", e);
        }
    }

    public void unusedCatchParameter() {
        try {
            mayThrow();
        } catch (IOException ignored) { // Compliant
            LOGGER.info("ignored");
        }
    }

    public void nestedCatch() {
        try {
            mayThrow();
        } catch (IOException outer) { // Compliant
            try {
                mayThrowReflective();
            } catch (ReflectiveOperationException inner) { // Compliant
                LOGGER.log(Level.SEVERE, "error", inner);
            }
            LOGGER.log(Level.SEVERE, "error", outer);
        }
    }

    public void catchRethrow() {
        try {
            mayThrow();
        } catch (IOException e) { // Compliant
            throw new IllegalStateException(e);
        }
    }

    public Runnable catchInLambda() {
        return () -> {
            try {
                mayThrow();
            } catch (IOException e) { // Compliant
                LOGGER.log(Level.SEVERE, "error", e);
            }
        };
    }

    // non-regression : the fix must only target the catch parameter, not the variables declared around it

    public void localVariablesAroundCatch() {
        try {
            String inTry = "try"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
            LOGGER.info(inTry);
            mayThrow();
        } catch (IOException e) { // Compliant
            String inCatch = "catch"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
            LOGGER.log(Level.SEVERE, inCatch, e);
        } finally {
            String inFinally = "finally"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
            LOGGER.info(inFinally);
        }
    }

    private void mayThrow() throws IOException {
        LOGGER.info("may throw IOException");
    }

    private void mayThrowReflective() throws ReflectiveOperationException {
        LOGGER.info("may throw ReflectiveOperationException");
    }

}
