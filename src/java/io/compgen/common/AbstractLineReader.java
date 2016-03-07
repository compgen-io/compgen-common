package io.compgen.common;

import io.compgen.common.progress.FileChannelStats;
import io.compgen.common.progress.ProgressMessage;
import io.compgen.common.progress.ProgressUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public abstract class AbstractLineReader<T> implements Iterable<T> {
    final private Reader reader;
    final protected FileChannel channel;
	final protected String name;

	private BufferedReader iteratorReader = null;

	public AbstractLineReader(String filename) throws IOException {
        if (filename.equals("-")) {
            this.reader = new InputStreamReader(System.in);
            this.channel = null;
            this.name = "<stdin>";
        } else {
        	this.name = filename;
            FileInputStream fis = new FileInputStream(filename);
            this.channel = fis.getChannel();
            if (filename.endsWith(".gz")) {
                this.reader = new InputStreamReader(new GZIPInputStream(fis));
            } else {
                this.reader = new InputStreamReader(fis);
            }
        }
    }

    public AbstractLineReader(InputStream is) {
        this(is, null, null);
    }
    
    public AbstractLineReader(InputStream is, FileChannel channel, String name) {
        this.reader = new InputStreamReader(is);
        this.channel = channel;
        this.name = name;
    }
    
    public void close() throws IOException {
    	if (this.iteratorReader != null) {
    		this.iteratorReader.close();
    	} else {
    		this.reader.close();
    	}
    }
    
    protected abstract T convertLine(String line);
    
    @Override
    public Iterator<T> iterator() {
    	iteratorReader = new BufferedReader(reader);
        return new Iterator<T>() {
            String next = readnext();
            
            private String readnext() {
            	String line = null;
                
//                while (line == null) {
                    try {
                        line = iteratorReader.readLine();
                    } catch (IOException e) {
                        line = null;
                    }
//                    if (line == null) {
//                        break;
//                    }
//                }
                if (line == null) {
                    try {
                    	iteratorReader.close();
                    } catch (IOException e) {
                    }
                }
                return line;
            }
            
            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public T next() {
                T out = null;
                while (out == null) {
                    out = convertLine(next);
                    next = readnext();     
                }
                return out;
            }

            @Override
            public void remove() {
            }
            
        };
    }

    public Iterator<T> progress() {
    	return progress(null);
    }
    public Iterator<T> progress(ProgressMessage<T> msg) {
    	return ProgressUtils.getIterator(name, iterator(), new FileChannelStats(channel), msg);
    }
}
