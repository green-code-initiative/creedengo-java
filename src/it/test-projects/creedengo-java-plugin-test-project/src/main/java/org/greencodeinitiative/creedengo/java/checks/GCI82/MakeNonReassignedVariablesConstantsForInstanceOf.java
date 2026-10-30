package org.greencodeinitiative.creedengo.java.checks;

public class MakeNonReassignedVariablesConstantsForInstanceOf {

    public String nonReasignedVariableWithPatternInstanceOfShouldBeNonCompliant() {
        final Object o = "NON-COMPLIANT";
        if (o instanceof String var) { // Compliant : a pattern variable is not a compile-time constant
            return var;
        }
        return "";
    }

    public String nonReasignedVariableWithPatternInstanceOfWithFinalShouldBeCompliant() {
        final Object o = "COMPLIANT";
        if (o instanceof final String var) {   // Compliant : here final keyword should be recognized and not trigger the rule
            return var;
        }
        return "";
    }

    public String reasignedVariableWithPatternInstanceOfShouldBeCompliant() {
        final Object o = "COMPLIANT";
        if (o instanceof String var) { // Compliant : Variable is reassigned
            var = "REASSIGN";
            return var;
        }
        return "";
    }

}
