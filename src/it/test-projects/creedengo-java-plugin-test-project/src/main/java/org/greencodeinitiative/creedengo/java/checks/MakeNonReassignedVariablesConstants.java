import java.util.logging.Logger;
import lombok.Setter;
import lombok.Data;
import lombok.AccessLevel;

public class MakeNonReassignedVariablesConstants {

    private final Logger logger = Logger.getLogger(""); // Compliant

    @Setter
    private String myLombokManagedString = "initialValue"; // Compliant

    private Object myNonFinalAndNotReassignedObject = new Object(); // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private Object myNonFinalAndReassignedObject = new Object(); // Compliant
    private final Object myFinalAndNotReassignedObject = new Object(); // Compliant

    private static final String CONSTANT = "toto";  // Compliant
    private String varDefinedInClassNotReassigned = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassNotUsed = "0"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String varDefinedInClassReassigned = "0"; // Compliant
    private String varDefinedInConstructorReassigned = "1"; // Compliant

    public MakeNonReassignedVariablesConstants() {
        varDefinedInConstructorReassigned = "3";
        logger.info(varDefinedInConstructorReassigned);
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

}

@Setter
class myExtraClassWithLombokSetter {
    private String myExtraClassString = "initialValue"; // Compliant
    private final String myExtraClassFinalString = "initialValue"; // Compliant

    @Setter(AccessLevel.NONE) //  Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String myExtraClassSetterNoneString = "initialValue";
}

@Data
class myExtraClassWithLombokData {
    private String myExtraClassString = "initialValue"; // Compliant
    private final String myExtraClassFinalString = "initialValue"; // Compliant

    @Setter(AccessLevel.NONE) //  Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String myExtraClassSetterNoneString = "initialValue";
}
