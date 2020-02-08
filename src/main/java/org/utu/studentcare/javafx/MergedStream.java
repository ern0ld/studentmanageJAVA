package org.utu.studentcare.javafx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Internal API for redirecting all (output) stream writes to many (output) streams.
 * No need to understand how this works. Let's pretend it does not exist.
 */
class MergedStream extends OutputStream {
    private final List<OutputStream> streams;

    public MergedStream(List<OutputStream> streams) {
        this.streams = streams;
    }

    public MergedStream(OutputStream a, OutputStream b) {
        streams = Arrays.asList(a, b);
    }

    @Override
    public void write(int i) throws IOException {
        for (OutputStream os : streams) os.write(i);
    }
}