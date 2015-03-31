package io.compgen.common;

public class MonitoredThread extends Thread {
	private boolean done = false;
	final private Runnable target;
	
	public MonitoredThread(Runnable target) {
		this.target = target;
	}
	
	final public void run() {
		target.run();
		this.done = true;
	}
	
	public boolean isDone() {
		return done;
	}
}
