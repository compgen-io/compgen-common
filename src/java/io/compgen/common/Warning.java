package io.compgen.common;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class Warning {
	private static final Warning singleton = new Warning();
	private Set<String> done = new HashSet<String>();
	private PrintStream out;
	
	public Warning(OutputStream out) {
		this.out = new PrintStream(out);
	}
	
	public Warning(PrintStream out) {
		this.out = out;
	}
	
	public Warning() {
		this(System.err);
	}
	
	public void once(String s) {
		if (done.contains(s)) {
			return;
		}
		
		out.println(s);
	}

	public void warn(String s) {
		out.println(s);
	}

	public static void warnStatic(String s) {
		Warning.singleton.warn(s);
	}
	public static void onceStatic(String s) {
		Warning.singleton.once(s);
	}
}
