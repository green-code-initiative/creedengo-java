package org.greencodeinitiative.creedengo.java.checks;

public class MakeNonReassignedVariablesConstantsForRecord {

    private record myRecord(
            String myImplicitlyFinalStringField, // Compliant
            Integer myImplicitlyFinalIntField) // Compliant
    { }
}
