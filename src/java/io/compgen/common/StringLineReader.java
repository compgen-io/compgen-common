package io.compgen.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class StringLineReader extends AbstractLineReader<String> {
    public StringLineReader(InputStream is) {
        super(is);
    }
    public StringLineReader(InputStream is, FileChannel channel, String name) {
        super(is, channel, name);
    }

    public StringLineReader(String filename) throws IOException {
        super(filename);
    }

    protected String convertLine(String line) {
        return line;
    }
}
