package org.greencodeinitiative.creedengo.java.checks;

import lombok.Setter;
import lombok.Data;
import lombok.AccessLevel;

class myExtraClassWithLombokAttributeSetter {

    @Setter
    private String myLombokManagedString = "initialValue"; // Compliant

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

// fully qualified annotations : valid Java, and the only available form when there is no lombok import
@Setter
class myExtraClassWithFullyQualifiedLombokSetter {
    private String myExtraClassString = "initialValue"; // Compliant

    @Setter(value = AccessLevel.NONE) //  Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String myNamedArgumentSetterNoneString = "initialValue";

    @Setter(lombok.AccessLevel.NONE) //  Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String myFullyQualifiedSetterNoneString = "initialValue";
}

@Data
class myExtraClassWithFullyQualifiedLombokData {
    private String myExtraClassString = "initialValue"; // Compliant
}

class myExtraClassWithFullyQualifiedFieldSetter {
    @Setter
    private String myFullyQualifiedSetterString = "initialValue"; // Compliant

    private String myPlainNotReassignedString = "initialValue"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
}
