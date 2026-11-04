package org.greencodeinitiative.creedengo.java.checks;

import java.util.logging.Logger;

public class MakeNonReassignedVariablesConstantsForField {

    private static final Logger LOGGER = Logger.getLogger("GCI82");

    private static final String CONSTANT = "constant";

    private static int counter = 0;

    private final int size;

    private String attribute = "attribute";

    private String copyOfAttribute = attribute; // Compliant : a value read from a field depends on the state of the object

    public MakeNonReassignedVariablesConstantsForField(final int size) {
        this.size = size;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
        counter++;
    }

    public void initializedFromField() {
        String fromField = attribute; // Compliant
        LOGGER.info(fromField);
    }

    public void initializedFromFieldWithThis() {
        String fromThisField = this.attribute; // Compliant
        LOGGER.info(fromThisField);
    }

    public void initializedFromFinalInstanceField() {
        int fromFinalField = this.size; // Compliant : a final instance field still has a value per object
        LOGGER.info(String.valueOf(fromFinalField));
    }

    public void initializedFromStaticNonFinalField() {
        int fromStaticField = counter; // Compliant
        LOGGER.info(String.valueOf(fromStaticField));
    }

    public void initializedFromFieldOfAnotherObject(final MakeNonReassignedVariablesConstantsForField other) {
        String fromOtherField = other.attribute; // Compliant
        LOGGER.info(fromOtherField + copyOfAttribute);
    }

    static class ParentClass {

        protected String parentAttribute = "parent";

        void setParentAttribute(final String parentAttribute) {
            this.parentAttribute = parentAttribute;
        }
    }

    static class ChildClass extends ParentClass {

        void initializedFromInheritedField() {
            String fromInherited = parentAttribute; // Compliant
            LOGGER.info(fromInherited);
        }

        void initializedFromInheritedFieldWithThis() {
            String fromInheritedWithThis = this.parentAttribute; // Compliant
            LOGGER.info(fromInheritedWithThis);
        }

        void initializedFromInheritedFieldWithSuper() {
            String fromInheritedWithSuper = super.parentAttribute; // Compliant
            LOGGER.info(fromInheritedWithSuper);
        }
    }

    // a variable initialized from a compile-time constant (constant field, final local constant, literal) is still checked

    public void initializedFromConstant() {
        String fromConstant = CONSTANT; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        LOGGER.info(fromConstant);
    }

    public void initializedFromQualifiedConstant() {
        String fromQualifiedConstant = MakeNonReassignedVariablesConstantsForField.CONSTANT; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        LOGGER.info(fromQualifiedConstant);
    }

    public void initializedFromLocalVariable() {
        final String local = "local";
        String fromLocal = local; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        LOGGER.info(fromLocal);
    }

    public void initializedFromParameter(final String parameter) {
        String fromParameter = parameter; // Compliant : a parameter is not a compile-time constant
        LOGGER.info(fromParameter);
    }

    public void initializedFromNonConstantStaticFinalField() {
        Logger fromLogger = LOGGER; // Compliant : a static final object is not a compile-time constant
        fromLogger.info("logger");
    }

    public void initializedFromLiteral() {
        String fromLiteral = "literal"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        LOGGER.info(fromLiteral);
    }

}
