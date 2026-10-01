package org.greencodeinitiative.creedengo.java.checks;

// No "import lombok.*" here on purpose : Lombok annotations are only used in their fully qualified form,
// which is valid Java and used to be a false positive (the rule required an explicit import to be found).

class MakeNonReassignedVariablesConstantsWithoutLombokImport {

    @lombok.Setter
    private String myLombokManagedString = "initialValue"; // Compliant

    private String myPlainNotReassignedString = "initialValue"; // Noncompliant {{The variable is never reassigned and can be 'final'}}

    @lombok.Setter(lombok.AccessLevel.NONE) // Noncompliant {{The variable is never reassigned and can be 'final'}}
    private String mySetterNoneString = "initialValue";

}

@lombok.Setter
class myClassWithFullyQualifiedLombokSetterAndNoImport {
    private String myClassString = "initialValue"; // Compliant
    private final String myClassFinalString = "initialValue"; // Compliant
}

@lombok.Data
class myClassWithFullyQualifiedLombokDataAndNoImport {
    private String myClassString = "initialValue"; // Compliant
    private final String myClassFinalString = "initialValue"; // Compliant
}

class myExtraClassWithFullyQualifiedFieldSetterAndNoImport {
    @lombok.Setter
    private String myFullyQualifiedSetterString = "initialValue"; // Compliant

    private String myPlainNotReassignedString = "initialValue"; // Noncompliant {{The variable is never reassigned and can be 'final'}}
}
