package org.greencodeinitiative.creedengo.java.checks;

import java.util.logging.Logger;

public class MakeNonReassignedVariablesConstantsForArray {

    private static final Logger LOGGER = Logger.getLogger("GCI82");

    private final int size;

    private byte[] fieldBuffer = new byte[16]; // Compliant : an array created by its size is a buffer, its content is not constant

    public MakeNonReassignedVariablesConstantsForArray(final int size) {
        this.size = size;
    }

    public void arrayCreatedWithFixedSize() {
        byte[] buffer = new byte[1024]; // Compliant
        LOGGER.info(buffer.length + " " + fieldBuffer.length);
    }

    public void arrayCreatedWithComputedSize() {
        int[] values = new int[this.size]; // Compliant
        LOGGER.info(String.valueOf(values.length));
    }

    public void multiDimensionalArrayCreatedWithSize() {
        int[][] matrix = new int[3][4]; // Compliant
        LOGGER.info(String.valueOf(matrix.length));
    }

    public void emptyArrayCreatedWithSize() {
        int[] empty = new int[0]; // Compliant
        LOGGER.info(String.valueOf(empty.length));
    }

    public void arrayFilledAfterCreation() {
        int[] squares = new int[10]; // Compliant
        for (int i = 0; i < squares.length; i++) { // Compliant : 'i' is reassigned
            squares[i] = i * i;
        }
        LOGGER.info(String.valueOf(squares[9]));
    }

    public void finalArrayCreatedWithSize() {
        final byte[] buffer = new byte[1024]; // Compliant
        LOGGER.info(String.valueOf(buffer.length));
    }

    public void arrayReassigned() {
        int[] reassigned = new int[2]; // Compliant
        reassigned = new int[4];
        LOGGER.info(String.valueOf(reassigned.length));
    }

    // an array declared with its values is not a compile-time constant either

    public void arrayInitializedWithValues() {
        String[] literal = {"a", "b", "c"}; // Compliant : an array is never a compile-time constant
        LOGGER.info(literal[0]);
    }

    public void arrayCreatedWithValues() {
        String[] withNew = new String[]{"a", "b"}; // Compliant
        LOGGER.info(withNew[0]);
    }

    public void arrayCreatedWithEmptyInitializer() {
        int[] emptyInitializer = new int[]{}; // Compliant
        LOGGER.info(String.valueOf(emptyInitializer.length));
    }

}
