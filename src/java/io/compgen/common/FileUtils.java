package io.compgen.common;

import java.io.BufferedOutputStream;
import java.io.File;
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
	/**
	 * Replace ~/foo/bar filenames with $HOME/foo/bar
	 * 
	 * Usually this is done by the shell, but in some cases, filenames get passed in alternative ways, so we need to manually adjust.
	 * 
	 * @param path
	 * @return
	 */
	public static String expandUserPath(String path) {
		if (path.startsWith("~" + File.separator)) {
		    path = System.getProperty("user.home") + path.substring(1);
		} else if (path.startsWith("~")) {
		    throw new UnsupportedOperationException("Path expansion for other user home-directories is not supported.");
		}
		return path;
	}

}
