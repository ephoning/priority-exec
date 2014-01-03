package org.eddie.exec.test;

import org.eddie.exec.PriorityTask;
import org.eddie.exec.TaskPriority;
import org.eddie.exec.TaskException;

import org.apache.log4j.Logger;

/**
 * An example of a priority task
 * 
 * @author ehoning
 *
 */
public class FooTask extends PriorityTask<FooIn,FooOut> {

	Logger log = Logger.getLogger(FooTask.class);
	
	boolean terminateWithException = false;
	
	public FooTask(FooIn fIn, FooOut fOut) {
		super(fIn, fOut, TaskPriority.MEDIUM);
		this.terminateWithException = false;
	}
	public FooTask(FooIn fIn, FooOut fOut, TaskPriority priority) {
		super(fIn, fOut, priority);
		this.terminateWithException = false;
	}
	public FooTask(FooIn fIn, FooOut fOut, TaskPriority priority, boolean terminateWithException) {
		super(fIn, fOut, priority);
		this.terminateWithException = terminateWithException;
	}
	
	public FooOut run() throws TaskException {
		boolean cancelRequested = false;
		log.info(String.format("\tFooTask %d STARTED; running at: %s priority", getId(), getPriority()));					
		for(int i = 0; i < in.times; i++) {
			log.info(String.format("\t\tFooTask %d busy...", id));
			/// break overall sleep delay in small portions to allow frequent cancellation request checking
			long sleepDelayFragment = in.sleepDelay / 100;
			for(int f = 0; f < 100; f++) {
				try {
					Thread.sleep(sleepDelayFragment);
				}
				catch (InterruptedException e) { }
				if(isCancelled()) {
					// do resource cleanup, etc. here....
					cancelRequested = true; // indicate break outer loop
					break; // break inner loop
				}
				if(terminateWithException && f >= 50) {
					// force an exception
					log.info(String.format("\t\tFooTask %d terminating with an exception...", id));
					out.result = Integer.toString(42/0);
				}
			}
			if (cancelRequested)
				break; // break outer loop
		}
		if(isCancelled()) {
			log.info(String.format("\tFooTask %d CANCELLED", id));
			out.result = String.format("FooTask %d got cancelled", id);
		}
		else {
			log.info(String.format("\tFooTask %d DONE", id));		
			out.result = String.format("FooTask %d completed", id);
		}
		return out;
	}
}
