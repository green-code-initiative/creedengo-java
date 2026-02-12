package org.greencodeinitiative.creedengo.java.checks;

import java.io.*;

class FreeResourcesOfAutoCloseableInterface {
    FreeResourcesOfAutoCloseableInterface(FreeResourcesOfAutoCloseableInterface mc) {

    }

    public void foo1() {
        String fileName = "./FreeResourcesOfAutoCloseableInterface.java";
        try (FileReader fr = new FileReader(fileName);
             BufferedReader br = new BufferedReader(fr)) {
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void foo2() throws IOException {
        String fileName = "./FreeResourcesOfAutoCloseableInterface.java";
        FileReader fr = null;
        BufferedReader br = null;
        try { // Noncompliant
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            System.out.println(br.readLine());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            if (fr != null) {
                fr.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * The first method adds a "try" in the stack used to follow if the code is in a try
     */
    public void callingMethodWithTheTry() throws IOException {
        try { // Compliant
            calledMethodWithoutTry();
        } finally {
            // Empty block of code
        }
    }

    /**
     * The "try" should have been popped from the stack before entering here
     */
    private void calledMethodWithoutTry() throws IOException {
        FileWriter myWriter = new FileWriter("somefilepath");
        myWriter.write("something");
        myWriter.flush();
        myWriter.close();
    }
}
