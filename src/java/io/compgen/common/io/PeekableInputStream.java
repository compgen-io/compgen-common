package io.compgen.common.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class wraps another inputstream. It will let you peek at [bufferSize]
 * bytes from the stream without removing them from the stream. This lets one
 * read from an arbitrary stream (file, stdin, network) and identify a file type
 * based upon magic bytes. You can then pass this stream to an appropriate
 * handler, but keep the magic bytes as part of the inputstream.
 * 
 * @author mbreese
 */
public class PeekableInputStream extends InputStream {
    public static final int DEFAULT_BUFFERSIZE = 64*1024;
    protected final int bufferSize;
    protected final InputStream parent;

    private boolean closed = false;
    private byte[] buffer = null;

    // position in buffer
    private int pos = 0;
    // size of buffer (amount read from parent stream)
    private int buflen = 0;
    
    private int peekpos = 0;
    
    public PeekableInputStream(InputStream parent, int bufferSize) throws IOException {
        this.parent = parent;
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];
    }
    
    public PeekableInputStream(InputStream parent) throws IOException {
        this(parent, DEFAULT_BUFFERSIZE);
    }

    private void fillBuffer() throws IOException {
    	if (parent == null) {
    		throw new IOException("Parent InputStream is null?");
    	}
        buflen = parent.read(buffer, 0, buffer.length);
        pos = 0;
        peekpos = 0;
    }

    private void resetBuffer() throws IOException {
        if (pos >= buflen || buflen == 0) { 
            fillBuffer();
        } else if (pos > 0) {
        	// copy from pos to new buffer.
            byte[] tmp = new byte[buffer.length];
            for (int i=0; i<buflen - pos; i++) {
                tmp[i] = buffer[pos + i];
            }

            buflen = buflen - pos;
            pos = 0;
            peekpos = 0;
        }
    }
    
    @Override
    public int read() throws IOException {
        if (closed) {
            throw new IOException("Attempted to read from closed stream!");
        }
        if (pos >= buflen) { 
            fillBuffer();
        }
        
        if (buflen == -1) {
            return -1;
        }
        
        return buffer[pos++] & 0xff;
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        parent.close();
        closed = true;
    }
    
    /**
     * Preview a few bytes from the stream before actually "reading" them.
     * 
     * This will let you read a few bytes from a stream (which might be a file, 
     * network, or whatever) without removing the bytes from the stream. This way
     * you can pre-identify a file with magic bytes without removing the magic bytes.
     * 
     * @param bytes
     * @return
     * @throws IOException
     */
    public byte[] peek(int bytes) throws IOException {
        if (closed) {
            throw new IOException("Attempted to read from closed stream!");
        }
        if (pos > 0 || buflen == 0) {
        	// we don't peek into an empty buffer, or one where the current pos is > 0.
            resetBuffer();
        }
        
        byte[] out = new byte[bytes];
        
        for (int i=0; i<bytes; i++) {
        	// we will grow the buffer as needed to fulfill the the request... 
            out[i] = peek();
        }
        
        return out;
    }
    
    public byte peek() throws IOException {
        if (peekpos >= buflen) {
        	if (buffer.length > (Integer.MAX_VALUE - bufferSize)) {
        		throw new IOException("Attempted to peek beyond buffer!");
        	}
        	
        	// grow the buffer in bufferSize chunks
//        	System.err.println(" -- growing buffer by " + bufferSize );
        	byte[] newbuf = new byte[buffer.length + bufferSize];
        	for (int i=0; i<buflen; i++) {
        		newbuf[i] = buffer[i];
        	}
        	// fill in the new buffer.
            int newbuflen = parent.read(newbuf, buflen, bufferSize);
            if (newbuflen == -1) {
        		throw new IOException("Attempted to peek beyond parent inputstream!");
            }
            buflen += newbuflen;
            buffer = newbuf;

        }
        return buffer[peekpos++];
    }
    
    public void resetPeek() {
        peekpos = 0;
    }
}
