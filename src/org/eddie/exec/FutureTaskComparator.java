package org.eddie.exec;

import java.util.Comparator;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;

/**
 * A comparator that can compare instances of PriorityFutureTasks
 * 
 * @author ehoning
 *
 */
public class FutureTaskComparator implements Comparator<FutureTask<?>>{
	
	Logger log = Logger.getLogger(FutureTaskComparator.class);
	
	@Override
	public int compare(FutureTask<?> o1, FutureTask<?> o2) {
		if (o1 instanceof PriorityFutureTask<?,?>  && o2 instanceof PriorityFutureTask<?,?>) {
			PriorityFutureTask<?,?> hpift1 = (PriorityFutureTask<?,?>)o1;
			PriorityFutureTask<?,?> hpift2 = (PriorityFutureTask<?,?>)o2;
			int result =  hpift1.getPriority().compareTo(hpift2.getPriority());
			return result;
		}
		else {
			log.error("CANNOT COMPARE BASE FUTURETASKS!!!");
			return 0;			
		}
	}

}
