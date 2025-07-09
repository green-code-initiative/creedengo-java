import java.util.logging.Logger;

public class MakeNonReassignedVariablesConstants {

    private final Logger logger = Logger.getLogger(""); // Compliant

    private Object myNonFinalAndNotReassignedObject = new Object(); // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private Object myNonFinalAndReassignedObject = new Object(); // Compliant
    private final Object myFinalAndNotReassignedObject = new Object(); // Compliant

    private static final String CONSTANT = "toto";  // Compliant
    private String varDefinedInClassNotReassigned = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassNotUsed = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassReassigned = "0"; // Compliant
    private String varDefinedInConstructorReassigned = "1"; // Compliant

    // using "this" 
    private String varDefinedInClassNotReassignedByThis = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassReassignedByThis = "0"; // Compliant
    private String varDefinedInConstructorReassignedByThis = "1"; // Compliant

    // passing through a method
    private String varDefinedInClassReassignedInMethod = "0"; // Compliant
    private String varDefinedInClassInFinalMethod = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassNotReassignedInMethod = "0"; // Compliant (the String was passed as a non-final parameter to the method)
    private String varDefinedInClassReassignedInConstructor = "0"; // Compliant
    private String varDefinedInClassInFinalConstructor = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassNotReassignedInConstructor = "0"; // Compliant (the String was passed as a non-final parameter to the constructor)

    public MakeNonReassignedVariablesConstants() {
        varDefinedInConstructorReassigned = "3";
        this.varDefinedInConstructorReassignedByThis = "3";
        logger.info(varDefinedInConstructorReassigned);
        logger.info(this.varDefinedInConstructorReassignedByThis);
    }

    public void parameterReassigned(String reassigned) {
        reassigned = "10";
        logger.info(reassigned);
    }

    public void parameterNotReassigned(final String notReassigned) {
        logger.info(notReassigned);
    }

    public void parameterNotReassignedNotFinal(String notReassigned) { // Noncompliant {{The variable is never reassigned and can be 'final'}}
        logger.info(notReassigned);
    }

    void localVariableReassigned() {
        String y1 = "10"; // Compliant
        final String PI = "3.14159"; // Compliant

        y1 = "titi";

        logger.info(y1);
        logger.info(PI);
    }

    void localVariableIncrement() {
        String y2 = "10"; // Compliant
        y2 += "titi";
        logger.info(y2);
    }

    void localIntVariableIncrement() {
        int y3 = 10; // Compliant
        ++y3;
        logger.info(y3+"");
    }

    void localVariableNotReassigned() {
        String y4 = "10"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        final String PI2 = "3.14159"; // Compliant

        logger.info(y4);
        logger.info(PI2);
    }

    void classVariableReassigned() {
        varDefinedInClassReassigned = "1";

        logger.info(varDefinedInClassReassigned);
        logger.info(varDefinedInClassNotReassigned);
        logger.info(CONSTANT);
    }

    void classVariableReassignedBis() {
        varDefinedInClassReassigned = "2"; // method to avoid sonarqube error asking for moving class variable "varDefinedInClassReassigned" to local variable method
        myNonFinalAndReassignedObject = new Object();

        logger.info(varDefinedInClassReassigned);
        logger.info(myNonFinalAndReassignedObject.toString());
        logger.info(myFinalAndNotReassignedObject.toString());
    }

    void classVariableReassignedByThis() {
        this.varDefinedInClassReassignedByThis = "1";

        logger.info(this.varDefinedInClassReassignedByThis);
        logger.info(this.varDefinedInClassNotReassignedByThis);
    }

    void reassignedInMethod() {
        String varDefinedInMethodReassignedInMethod = "0"; // Compliant
        String varDefinedInMethodInFinalMethod = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        String varDefinedInMethodNotReassignedInMethod = "0"; // Compliant (the String was passed as a non-final parameter to the method)

        this.parameterReassigned(varDefinedInMethodReassignedInMethod);
        this.parameterReassigned(this.varDefinedInClassReassignedInMethod);
        this.parameterNotReassigned(varDefinedInMethodInFinalMethod);
        this.parameterNotReassigned(this.varDefinedInClassInFinalMethod);
        this.parameterNotReassignedNotFinal(varDefinedInMethodNotReassignedInMethod);
        this.parameterNotReassignedNotFinal(this.varDefinedInClassNotReassignedInMethod);
    }

    void reassignedInConstructor(){
        String varDefinedInMethodReassignedInConstructor = "0"; // Compliant
        String varDefinedInMethodInFinalConstructor = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
        String varDefinedInMethodNotReassignedInConstructor = "0"; // Compliant (the String was passed as a non-final parameter to the constructor)
        Object o = null;
        o = new reassignedInConstructor(varDefinedInMethodReassignedInConstructor);
        o = new reassignedInConstructor(this.varDefinedInClassReassignedInConstructor);
        o = new notReassignedInConstructor(varDefinedInMethodInFinalConstructor);
        o = new notReassignedInConstructor(this.varDefinedInClassInFinalConstructor);
        o = new notReassignedInConstructorNotFinal(varDefinedInMethodNotReassignedInConstructor);
        o = new notReassignedInConstructorNotFinal(this.varDefinedInClassNotReassignedInConstructor);
    }

}

class reassignedInConstructor{
    reassignedInConstructor(String reassignedInConstructor) {
        reassignedInConstructor = "10";
        System.out.println(reassignedInConstructor);
    }
}
class notReassignedInConstructor{
    notReassignedInConstructor(final String notReassignedInConstructor) {
        System.out.println(notReassignedInConstructor);
    }
}
class notReassignedInConstructorNotFinal{
    notReassignedInConstructorNotFinal(String notReassignedInConstructorNotFinal) { // Noncompliant {{The variable is never reassigned and can be 'final'}}
        System.out.println(notReassignedInConstructorNotFinal);
    }
}