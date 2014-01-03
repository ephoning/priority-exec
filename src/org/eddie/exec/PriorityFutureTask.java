package org.eddie.exec;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * A FutureTask that carries a priority
 * It is suitable to be used in a PriorityBlockingQueue that has been instantiated
 * with a FutureTaskComparator
 * 
 * @author ehoning
 *
 * @param <TIN>
 * @param <TOUT>
 */
public class PriorityFutureTask<TIN,TOUT> extends FutureTask<TOUT>
{

	private int id;
	private TaskPriority priority;
	
	public PriorityFutureTask(Callable<TOUT> callable, int id) {
		this(callable, TaskPriority.MEDIUM, id);
	}
	
	public PriorityFutureTask(Callable<TOUT> callable, TaskPriority priority, int id) {
		super(callable);
		this.id = id;
		this.priority = priority;
	}

	public int getId() {
		return id;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}
}
