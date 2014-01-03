package org.eddie.exec;

/**
 * Task priorities
 * Note: make sure to order priorities from high to low in order to guarantee proper
 * priority-based task scheduling preemption
 * Additional granularity can be created by adding or replacing the values below
 * Feel free to rename the defined values (no direct reference is made to these values in
 * task scheduling code)
 * 
 * @author ehoning
 *
 */
public enum TaskPriority {
	HIGH, MEDIUM, LOW;
}
