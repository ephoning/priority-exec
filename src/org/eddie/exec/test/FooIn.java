package org.eddie.exec.test;

import java.util.Random;

/**
 * Input data for a FooTask
 * 
 * @author ehoning
 *
 */
public class FooIn {
	
	static Random r;
	
	static {
		r = new Random();
		r.setSeed(42);
	}
	
	public int times = 10;
	public long sleepDelay = 0;

	public FooIn() {
		this(r.nextInt(500));
	}
	public FooIn(long sleepDelay) {
		this.sleepDelay = sleepDelay;
	}
}
