package io.compgen.common;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class TabWriter {
    final private OutputStream out;
    final private Charset charset;
    final private String delim = "\t";
    private String line = "";
    
    
    public TabWriter() {
        this.out = System.out;
        this.charset = Charset.defaultCharset();
    }

    public TabWriter(String filename) throws FileNotFoundException {
        this.out = new BufferedOutputStream(new FileOutputStream(filename));
        this.charset = Charset.defaultCharset();
    }

    public TabWriter(OutputStream out) {
        this.out = new BufferedOutputStream(out);
        this.charset = Charset.defaultCharset();
    }

    public TabWriter(Charset charset) {
        this.out = System.out;
        this.charset = charset;
    }

    public TabWriter(OutputStream out, Charset charset) {
        this.out = new BufferedOutputStream(out);
        this.charset = charset;
    }

    public void write_line(String line) throws IOException {
        out.write((line+"\n").getBytes(charset));
    }
    
    public void write(String...vals) {
        for (String val:vals) {
            if (line.equals("")) {
                line = ""+val;
            } else {
                line = line + delim + val;
            }
        }
    }

    public void write(int val) {
        write(Integer.toString(val));
    }
    
    public void write(long val) {
        write(Long.toString(val));
    }
    
    public void write(double val) {
        write(Double.toString(val));
    }
    
    public void write(float val) {
        write(Float.toString(val));
    }
    
    public void eol() throws IOException {
        if (!line.equals("")){
            line = line + "\n";
            out.write(line.getBytes(charset));
            line = "";
        }
    }
    
    public void close() throws IOException {
        eol();
        if (out != System.out) {
            out.close();
        }
    }

	public void write(List<String> vals) {
        for (String val:vals) {
            if (line.equals("")) {
                line = val;
            } else {
                line = line + delim + val;
            }
        }
	}
}
