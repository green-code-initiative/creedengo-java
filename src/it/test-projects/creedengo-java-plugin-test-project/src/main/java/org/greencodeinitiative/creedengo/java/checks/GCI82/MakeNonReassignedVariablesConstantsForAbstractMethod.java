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

    default void defaultMethod(String param) { // Compliant : 'final' on a parameter brings no optimization
        System.out.println(param);
    }

    static void staticMethod(String param) { // Compliant
        System.out.println(param);
    }
}
