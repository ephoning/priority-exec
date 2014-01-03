package org.eddie.exec;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

/**
 * A task backed by a priority future task, allowing it to be used as a unit of work that can
 * be scheduled for execution in a priority-aware executor service
 * 
 * @author ehoning
 *
 * @param <TIN>
 * @param <TOUT>
 */
public abstract class PriorityTask<TIN,TOUT> implements Callable<TOUT> {

	Logger log = Logger.getLogger(PriorityTask.class);
	
	private static AtomicInteger nextId = new AtomicInteger(0);	
	public int id;
	public PriorityFutureTask<TIN,TOUT> futureTask;
	public PriorityTaskExecutorService<TIN,TOUT> execService;
	public TIN in;
	public TOUT out;

	public PriorityTask(TIN in, TOUT out, TaskPriority priority) {
		this.id = nextId.incrementAndGet();
		futureTask = new PriorityFutureTask<TIN,TOUT>(this, priority, id);
		this.in = in;
		this.out = out;
	}

	public int getId() { return id; }
	public TaskPriority getPriority() { return futureTask.getPriority(); }
	
	/**
	 * wrap actual task to keep count of number of running tasks
	 * and to throw appropriate
	 */
	public TOUT call() throws TaskException {
		try {
			execService.addRunningTask(this); // signal running	
			if(log.isDebugEnabled())
				log.debug(execService.lgetStateRepresentation("after 'call'"));
			run(); // run actual task
			return out;
		}
		finally {
			execService.removeRunningTask(this); // signal completion
		}
	}
	
	public abstract TOUT run() throws TaskException;

	public boolean cancel(boolean mayInterruptIfRunning) {
		return futureTask.cancel(mayInterruptIfRunning);
	}
	
	public TOUT get() throws TaskException {
		try {
			return futureTask.get();
		}
		catch (Exception e) {
			throw new TaskException("Task.get failure", e);
		}
	}
	
	public TOUT get(long timeout, TimeUnit unit)  throws TaskException {
		try {
			return futureTask.get(timeout, unit);
		}
		catch (Exception e) {
			throw new TaskException("Task.get(timeout,unit) failure", e);
		}
	}
	
	public boolean isCancelled() {
		return futureTask.isCancelled();
	}
	
	public boolean isDone() {
		return futureTask.isDone();
	}
}
