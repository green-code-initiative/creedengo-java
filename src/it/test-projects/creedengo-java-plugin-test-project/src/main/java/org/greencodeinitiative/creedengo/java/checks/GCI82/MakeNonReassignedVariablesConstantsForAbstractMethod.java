package org.greencodeinitiative.creedengo.java.checks;

@FunctionalInterface
interface EventListenerSample<T> {
    /**
     * Callback method when an event occurs
     * @param value Event data
     * @return False if this event must stop at this treatment.
     */
    boolean onEvent(T value); // Compliant
}

interface InterfaceWithMultipleMethods {
    void abstractMethod(String param); // Compliant

    default void defaultMethod(String param) { // Noncompliant {{The variable is never reassigned and can be 'final'}}
        System.out.println(param);
    }

    static void staticMethod(String param) { // Noncompliant {{The variable is never reassigned and can be 'final'}}
        System.out.println(param);
    }
}
