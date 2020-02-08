package org.utu.studentcare.javafx;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

// Oheisluokka, EI tarvitse ymmärtää, muokata eikä käyttää omassa koodissa työn tekemiseksi

/**
 * Basic GUI console.
 * <p>
 * This class is final. Modification is not necessary.
 */
public class FXConsole extends BorderPane implements Console {
    private volatile boolean active = true;
    private final TextArea textArea;
    private final TextField inputField = new TextField();
    private final PrintStream originalOut;
    private PrintStream printStream;

    private void updateText(String newText) {
        if (newText.startsWith("\033c")) {
            textArea.setText(newText.substring(2));
        } else {
            textArea.appendText(newText);
        }

        // enforce a maximum content length
        // otherwise the control would become unresponsive
        String s = textArea.getText();
        if (s.length()>8000) {
            textArea.setText(s.substring(s.length()-8000));
        }

        //auto-scroll
        textArea.setScrollTop(Double.MAX_VALUE);
        textArea.positionCaret(textArea.getLength());
    }

    private final OutputStream stream = new OutputStream() {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(200);
        private final StringBuffer buffer2 = new StringBuffer();
        boolean wasESC;

        private int push(String s) {
            synchronized (buffer2) {
                int oldLen = buffer2.length();
                buffer2.append(s);
                return oldLen;
            }
        }

        private int clearAndPush(String s) {
            synchronized (buffer2) {
                int oldLen = buffer2.length();
                buffer2.setLength(0);
                buffer2.append(s);
                return oldLen;
            }
        }

        private String pop2() {
            synchronized (buffer2) {
                String s = buffer2.toString();
                buffer2.setLength(0);
                return s;
            }
        }
        private String pop() {
            synchronized (buffer) {
                String s = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                buffer.reset();
                return s;
            }
        }

        /**
         * Process the received data, i.e. print to the GUI component.
         *
         * @see OutputStream
         * <p>
         * You can call this from any thread.
         */
        @Override
        public void write(int b) {
            synchronized (buffer) {
                // handle ANSI console reset
                if (wasESC && b == 'c') {
                    buffer.reset();
                    if (clearAndPush("\033c") == 0)
                        Platform.runLater(() -> updateText(pop2()));
                    return;
                }
                wasESC = b == '\033';
                buffer.write(b);

                if (b == '\n')
                    if (push(pop()) == 0)
                        Platform.runLater(() -> updateText(pop2()));
            }
        }
    };

    /**
     * Clears the console. You can call this from any thread.
     */
    @Override
    public final void clearConsole() {
        Platform.runLater(textArea::clear);
    }

    /**
     * Provide a print stream. Thread-safe.
     */
    @Override
    public PrintStream stream() {
        return printStream;
    }

    public final LinkedBlockingQueue<String> commands = new LinkedBlockingQueue<>(10);

    public void close() {
        active = false;
    }

    void collectInput() {
        inputField.setOnKeyReleased(k -> {
            if (k.getCode() == KeyCode.ENTER) {
                commands.offer(inputField.getText());
                inputField.clear();
            }
        });

        inputField.requestFocus();
        setBottom(inputField);

        new Thread(() -> {
            Scanner s = new Scanner(System.in);
            while(active) {
                    commands.offer(s.nextLine());


                Thread.yield();
            }
        }).start();
    }

    public FXConsole() {
        this(false, true,  new TextArea());
    }

    public FXConsole(TextArea textArea) {
        this(false, true,  textArea);
    }

    public void toggleStdout() {
        printStream = System.out == originalOut ?
                new PrintStream(new MergedStream(originalOut, stream)) :
                originalOut;

        System.setOut(printStream);
    }

    public FXConsole(boolean collectsInput, boolean connectToStdout, TextArea textArea) {
        this.textArea = textArea;
        printStream = originalOut = System.out;

        Font f;
        f = Font.font("Source Code pro", FontWeight.BOLD, 14);
        if (f == null)
            f = Font.font("Courier New", FontWeight.BOLD, 14);
        if (f != null)
            textArea.setFont(f);

        setCenter(textArea);
        textArea.setEditable(false);
        textArea.setFocusTraversable(false);

        if (connectToStdout) toggleStdout();
        if (collectsInput) collectInput();
    }
}