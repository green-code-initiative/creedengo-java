package org.greencodeinitiative.creedengo.java.checks;

import java.util.logging.Logger;

public class MakeNonReassignedVariablesConstantsForEnum {

    private static final Logger LOGGER = Logger.getLogger("GCI82");

    enum SimpleEnum { // Compliant : enum constants are implicitly 'static final'
        ONE, TWO, THREE
    }

    enum EnumWithArguments {
        LOW("low"), // Compliant
        HIGH("high"); // Compliant

        private final String label; // Compliant

        EnumWithArguments(final String label) {
            this.label = label;
        }

        String getLabel() {
            return label;
        }
    }

    enum EnumWithBody {
        PLUS { // Compliant
            @Override
            int apply(final int a, final int b) {
                return a + b;
            }
        },
        MINUS { // Compliant
            @Override
            int apply(final int a, final int b) {
                return a - b;
            }
        };

        abstract int apply(int a, int b);
    }

    // non-regression : the rule still applies to the fields and variables declared inside an enum

    enum EnumWithNonFinalField {
        FIRST, SECOND;

        private String description = "description"; // Noncompliant {{The variable is never reassigned and can be 'final'}}

        String describe() {
            String prefix = "enum : "; // Noncompliant {{The variable is never reassigned and can be 'final'}}
            return prefix + description;
        }
    }

    public void localVariableFromEnumConstant() {
        SimpleEnum value = SimpleEnum.ONE; // Compliant : an enum constant is not a compile-time constant
        LOGGER.info(value.name() + EnumWithArguments.LOW.getLabel() + EnumWithBody.PLUS.apply(1, 2)
                + EnumWithNonFinalField.FIRST.describe());
    }

}
