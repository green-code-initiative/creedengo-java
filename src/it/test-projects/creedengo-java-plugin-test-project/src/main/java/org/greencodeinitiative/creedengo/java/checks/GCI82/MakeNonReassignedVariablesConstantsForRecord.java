package org.greencodeinitiative.creedengo.java.checks;

public class MakeNonReassignedVariablesConstantsForRecord {

    private record myRecord(
            String myImplicitlyFinalStringField, // Compliant
            Integer myImplicitlyFinalIntField) // Compliant
    {
        private static String myRecordStaticField = "static"; // Noncompliant {{The variable is never reassigned and can be 'final'}}

        private static final String MY_RECORD_CONSTANT = "constant"; // Compliant
    }
}
