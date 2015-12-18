package io.compgen.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class wraps another InputStream. It will let you read data from that
 * input stream. In addition to reading bytes from the stream, it will also
 * write the bytes to a connected OutputStream.
 * 
 * An example would be where you are reading a file, and want to process the
 * data and write the data to a new file (unaltered).
 * 
 * This is functionally equivalent to the *nix program tee. 
 * 
 * @author mbreese
 */
public class PassthruInputStream extends InputStream {
    protected final InputStream source;
    protected final OutputStream sink;

    private boolean closed = false;
    
    public PassthruInputStream(InputStream source, OutputStream sink) throws IOException {
    	this.source =source;
    	this.sink = sink;
    }
    
    @Override
    public int read() throws IOException {
    	int b = source.read();
    	sink.write(b);
    	return b;
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        source.close();
        sink.close();
    }
}
