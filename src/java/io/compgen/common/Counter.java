package io.compgen.common;

public class Counter {
	private long value = 0;
	public void incr() {
		this.value ++;
	}
	
	public long getValue() {
		return value;
	}
}
