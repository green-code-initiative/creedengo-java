package org.greencodeinitiative.creedengo.java.checks;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class OptimizeReadFileExceptionCheck5 {

    Logger logger = Logger.getLogger("");

    OptimizeReadFileExceptionCheck5(OptimizeReadFileExceptionCheck5 readFile) {
    }

    public void readPreferences(String filename) {
        //...
        try (InputStream in = new FileInputStream(filename)) { // Noncompliant {{Optimize Read File Exceptions}}
            logger.info("my log");
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
        //...
    }
}
