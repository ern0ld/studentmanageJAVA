package org.utu.studentcare.javafx;

import java.io.PrintStream;

/**
 * Command line interface for communication.
 * Can be also implemented as a GUI component.
 * <p>
 * Default OOMKit operation:
 * A main window automatically implements a console.
 * The stdout (System.out) is also redirected to the default console and to the stdout.
 */
interface Console {
    /**
     * Clear the console.
     */
    void clearConsole();

    /**
     * A default PrintStream for writing text to the console.
     * <p>
     * Example: this.stream().println("Hello world");
     */
    PrintStream stream();


    /**
     * Closes the console.
     */
    void close();


    /**
     * Toggles between command line and GUI console.
     *
     * Can be adjusted if the app doesn't want to display every message in the GUI.
     */
    void toggleStdout();
}
