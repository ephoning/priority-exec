this is a working impl of a prioritize task scheduling framework
testing
- prelim DONE
- add low priority tasks right after saturating with medium tasks
needs verification of whether a high priority task can truly preempt a currently running lower priority task   DONE
more work needs to be done on generic method, etc. type signatures   DONE
also: some type simplification is likely in order (e.g., potentially remove the task in/out marker interfaces) DONE
it is not properly documented ONGOING
testing logic needs to be formed into TestNG test case instances DONE

need to clean up exception throwing/handling

also: do we need Runnable in Task? try to remove it... DONE
