package org.eddie.exec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * An executor service that schedules/executes PriorityTask instances
 * If 'forceHPI' is requested (*H*igh *P*riority *I*nterrupt), it is able to cancel a lower
 * priority Task in order to let a higher priority Task start running immediately
 * 
 * @author ehoning
 *
 * @param <TIN>
 * @param <TOUT>
 */
public class PriorityTaskExecutorService<TIN,TOUT> {
	
	Logger log = Logger.getLogger(PriorityTaskExecutorService.class);

	private static final int INITIAL_QUEUE_SIZE = 16;
	
	ExecutorService execService;
	BlockingQueue<Runnable> queue;
	private int poolSize;
	
	// flag to indicate whether the scheduler should perform a High Priority Interrupt (HPI)
	// when a Task is scheduled with a priority higher than any of the currently running Tasks
	private boolean forceHPI;
	
	// (values not vital, as we use a fixed size thread pool from the start => no reduction of pool size will occur)
	private long keepAliveTime = 1000;
	private TimeUnit unit = TimeUnit.MILLISECONDS;

	// (not synchronized => make sure to synchronize all access (read/write) functionality)
	List<PriorityTask<TIN,TOUT>> runningTasks = new ArrayList<PriorityTask<TIN,TOUT>>();

	public synchronized PriorityTask<TIN,TOUT> addRunningTask(PriorityTask<TIN,TOUT> task) {	
		runningTasks.add(task);
		return task;
	}
	public synchronized PriorityTask<TIN,TOUT> removeRunningTask(PriorityTask<TIN,TOUT> task) {
		runningTasks.remove(task);
		return task;
	}
	
	public synchronized int runningCount() {
		return runningTasks.size();
	}

	public PriorityTaskExecutorService(int poolSize) {
		this(poolSize, true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" }) // (needed to avoid warning around queue assignment)
	public PriorityTaskExecutorService(int poolSize, boolean forceHPI) {
		this.poolSize = poolSize;
		this.forceHPI = forceHPI;
		queue = new PriorityBlockingQueue(INITIAL_QUEUE_SIZE, new FutureTaskComparator());
		execService = new ThreadPoolExecutor(poolSize, poolSize, keepAliveTime, unit, queue);
	}

	/**
	 * Schedule a task for execution. This results in the Tasks's FutureTask getting queued
	 * (in priority order) on the queue that feeds a thread pool
	 * 
	 * @param task to be scheduled for execution
	 * @return task just scheduled for execution
	 */
	public synchronized PriorityTask<TIN,TOUT> execute(PriorityTask<TIN,TOUT> task) {
		task.execService = this; // back link
		// schedule task (execService.submit would  not work: callable gets wrapped in plain futuretask => no priority ordering
		execService.execute(task.futureTask);
		if(log.isDebugEnabled())
			log.debug(lgetStateRepresentation("after 'submit'"));
		// if the task we just scheduled is not running yet, we might need to free up capacity in the pool...
		if(forceHPI && runningTasks.size() == poolSize && !isRunning(task.getId())) {
			if(log.isDebugEnabled())
				log.debug("MAX LOADED");
			// need to free up capacity; find the lowest priority task
			PriorityTask<TIN,TOUT> lowest = getLowestPriorityTask();
			// if we got one, see if's priority is lower than the task we just scheduled and know is not running
			if (lowest != null && lowest.getPriority().compareTo(task.getPriority()) > 0) {
				if(log.isDebugEnabled())
					log.info( String.format("GOT %s PRIORITY TASK WITH ID %d; CANCEL %s PRIORITY TASK WITH ID %d ",
								task.getPriority(), task.getId(), lowest.getPriority(), lowest.getId()));
				// cancel the currently running lower priority task
				boolean cancelResult = lowest.cancel(true);
				if(!cancelResult && log.isDebugEnabled())
					log.error(String.format("COULD NOT CANCEL TASK WITH ID %d", lowest.id));
			}
		}

		return task;
	}
	
	public void shutdown() {
		execService.shutdown();
	}
	
	public boolean isRunning(int taskId) {
		for (PriorityTask<TIN,TOUT> runningTask : runningTasks ) {
			if (runningTask.getId() == taskId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get the youngest, lowest priority task that is currently running..
	 * 
	 * @return the youngest, lowest priority task in the running state
	 */
	public PriorityTask<TIN,TOUT> getLowestPriorityTask() {
		PriorityTask<TIN,TOUT> lowest = null;
		boolean first = true;
		for (PriorityTask<TIN,TOUT> task : runningTasks) {
			if(first) {
				lowest = task;
				if (!task.isDone())
					first = false;
				continue;
			}
			// note: use '>=' and not '>' to replace 'lowest' with most recent task if that task's
			// priority is equal to the current lowest, as this guarantees that we pick
			// the task that has performed the least amount of work (by virtue of it residing
			// further down the running task list)
			if(task.getPriority().compareTo(lowest.getPriority()) >= 0) {
				lowest = task;
			}
		}
		return lowest;
	}
	
	/**
	 * Get a string representation of the current 'state' (i.e., running and queued tasks)
	 * 
	 * @param msg a label to include at the start of the result string
	 * 
	 * @return the state representation as lists of task id - task priority pairs
	 */
	public synchronized String lgetStateRepresentation(String msg) {
		Iterator<Runnable> i = queue.iterator();
		StringBuffer buf = new StringBuffer();
			
		buf.append(msg).append(" Running: [");
		for(PriorityTask<?,?> t : runningTasks) {
			buf.append(String.format(" %d/%s", t.id, t.getPriority()));				
		}
		buf.append(" ]  Queued: [");
		while(i.hasNext()) {
			FutureTask<?> ft = (FutureTask<?>) i.next();
			if(ft instanceof PriorityFutureTask<?,? >) {
				PriorityFutureTask<?,?>pft = (PriorityFutureTask<?,?>)ft;
				buf.append(String.format(" %d/%s", pft.getId(), pft.getPriority()));
			}
		}
		buf.append(" ]");
		return buf.toString();
	}
}
