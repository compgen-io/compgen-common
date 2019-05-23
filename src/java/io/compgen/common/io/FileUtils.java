package io.compgen.common.io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils {
	public FileUtils() {
	}
    public static OutputStream openOutputStream(String outputName) throws IOException {
    	return openOutputStream(outputName, false);
    }

    public static OutputStream openOutputStream(String outputName, boolean forceCompress) throws IOException {
    	if (forceCompress) {
        	if (outputName == null || outputName.equals("-")) {
                return new GZIPOutputStream(System.out);
            } else {
                return new GZIPOutputStream(new FileOutputStream(outputName));
            }
    	} else {
	    	if (outputName == null || outputName.equals("-")) {
	            return System.out;
	        } else if (outputName.endsWith(".gz")) {
	            return new GZIPOutputStream(new FileOutputStream(outputName));
	        } else {
	            return new BufferedOutputStream(new FileOutputStream(outputName));
	        }
    	}
    }

}
