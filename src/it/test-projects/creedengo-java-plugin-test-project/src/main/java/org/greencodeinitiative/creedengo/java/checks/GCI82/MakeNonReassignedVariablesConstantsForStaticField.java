package org.greencodeinitiative.creedengo.java.checks;

import java.util.logging.Logger;

/**
 * A 'static final' field is trusted as a constant by the JIT once its class is initialized, whatever its initializer :
 * a static field never reassigned should be 'final'.
 */
public class MakeNonReassignedVariablesConstantsForStaticField {

    private static String staticString = "static"; // Noncompliant {{The variable is never reassigned and can be 'final'}}

    private static Object staticObject = new Object(); // Noncompliant {{The variable is never reassigned and can be 'final'}}

    private static int staticFromMethod = compute(); // Noncompliant {{The variable is never reassigned and can be 'final'}}

    private static int[] staticArray = {1, 2}; // Noncompliant {{The variable is never reassigned and can be 'final'}}

    public static Logger staticLogger = Logger.getLogger("GCI82"); // Noncompliant {{The variable is never reassigned and can be 'final'}}

    private static final String STATIC_CONSTANT = "constant"; // Compliant

    private static int reassignedInMethod = 0; // Compliant

    private static int incremented = 0; // Compliant

    private static String reassignedWithClassName = "initial"; // Compliant

    private static String assignedInStaticBlock; // Compliant : no initializer

    static {
        assignedInStaticBlock = "static block";
    }

    public void useStaticFields() {
        reassignedInMethod = 1;
        incremented++;
        MakeNonReassignedVariablesConstantsForStaticField.reassignedWithClassName = "reassigned";
        staticLogger.info(staticString + staticObject + staticFromMethod + staticArray.length + STATIC_CONSTANT
                + reassignedInMethod + incremented + reassignedWithClassName + assignedInStaticBlock);
    }

    private static int compute() {
        return 1;
    }

}

interface MakeNonReassignedVariablesConstantsForStaticFieldInInterface {

    String INTERFACE_CONSTANT = "interface"; // Compliant : an interface field is implicitly 'static final'

}
