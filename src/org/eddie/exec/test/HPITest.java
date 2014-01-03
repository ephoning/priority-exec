package org.eddie.exec.test;

import org.apache.log4j.Logger;
import org.eddie.exec.PriorityTaskExecutorService;
import org.eddie.exec.TaskException;
import org.eddie.exec.TaskPriority;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import java.util.concurrent.TimeUnit;

import static org.eddie.exec.TaskPriority.*;


public class HPITest {
	
	Logger log = Logger.getLogger(HPITest.class);
	
	PriorityTaskExecutorService<FooIn, FooOut> tes;
	FooTask[] tasks;
	
  @BeforeMethod
  public void beforeMethod() {
	  System.out.println("=======================");
	  tasks = new FooTask[] {
			  new FooTask( new FooIn(1000), new FooOut()),
			  new FooTask( new FooIn(1000), new FooOut(), LOW),
			  new FooTask( new FooIn(1000), new FooOut()),
			  new FooTask( new FooIn(1000), new FooOut()),
			  new FooTask( new FooIn(), new FooOut(), TaskPriority.LOW),
			  new FooTask( new FooIn(), new FooOut(), LOW),
			  new FooTask( new FooIn(), new FooOut(), HIGH)
		};
  	}

  	@AfterMethod
  	public void afterMethod() {
		  tes.shutdown();
  	}
  	
	@Test
	public void test0_noPriorityTaskInterruption	() throws Exception {
		  tes = new PriorityTaskExecutorService<FooIn, FooOut>(3,false); // no HPI
		executeTasks(true); // fetch tasks results using blocking 'get' 
	}

	@Test
	public void test1_lowPriorityTaskInterruption() throws Exception {
		  tes = new PriorityTaskExecutorService<FooIn, FooOut>(3); // HPI
		  executeTasks(false); // fetch tasks results using timeout-based 'get'
	}
	
	@Test
	public void test2_lowPriorityTaskInterruption	() throws Exception {
		  tes = new PriorityTaskExecutorService<FooIn, FooOut>(3); // HPI
		executeTasks(true); // fetch tasks results using blocking 'get' 
	}

	@Test(expectedExceptions = TaskException.class)
	public void test3_taskFailure() throws Exception {
		tes = new PriorityTaskExecutorService<FooIn, FooOut>(3, false); // no HPI
		FooTask ft = new FooTask( new FooIn(1000), new FooOut(), TaskPriority.MEDIUM, true);
		tes.execute(ft);
		ft.get();
	}
	
	private void executeTasks(boolean blockingGet) throws Exception {
		 // start all but the last task
		for(int i = 0; i < 6; i++)
			tes.execute(tasks[i]);
		System.out.println("SLEEPING FOR 3 SECS.");
		Thread.sleep(3000);
		// now start the last task
		tes.execute(tasks[6]);
		
		log.info( String.format("%d threads scheduled; %d threads running - wait until done...",
				tes.runningCount(), tes.runningCount()));
		
		if (blockingGet) {
			for(FooTask t : tasks) {
				try {
					t.get(); // block until tasks completes...
					log.info(String.format("Result for task %d: %s", t.id, t.out.result));
				}
				catch (Exception e) {
					log.info(String.format("Result for task %d not available due to: %s", t.id, e));
				}
			}
		}
		else {
			System.out.println("SLEEPING FOR 10 SECS.");
			Thread.sleep(10000);
			for(FooTask t : tasks) {
				try {
					t.get(3, TimeUnit.SECONDS);
					System.out.println(String.format("Result for task %d: %s", t.id, t.out.result));
				}
				catch (Exception e) {
					System.out.println(String.format("Result for task %d not available due to: %s", t.id, e));
				}
			}
		}
	}
}
