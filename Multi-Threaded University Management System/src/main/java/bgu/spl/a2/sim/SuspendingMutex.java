package bgu.spl.a2.sim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import bgu.spl.a2.Promise;

/**
 * 
 * this class is related to {@link Computer} it indicates if a computer is free
 * or not
 * 
 * Note: this class can be implemented without any synchronization. However,
 * using synchronization will be accepted as long as the implementation is
 * blocking free.
 *
 */
public class SuspendingMutex {
	private Computer computer;
	private AtomicBoolean isBlocked;
	private ConcurrentLinkedQueue<Promise<Computer>> queueOfPromises;
	/**
	 * Constructor
	 * 
	 * @param computer
	 */
	public SuspendingMutex(Computer computer) {
		this.computer = computer;
		isBlocked = new AtomicBoolean(false);
		queueOfPromises = new ConcurrentLinkedQueue<Promise<Computer>>();
	}

	/**
	 * Computer acquisition procedure Note that this procedure is non-blocking and
	 * should return immediatly
	 * 
	 * @return a promise for the requested computer
	 */
	public Promise<Computer> down() {
		Promise<Computer> toReturn = new Promise<Computer>();

		if (isBlocked.compareAndSet(false, true)) {
			toReturn.resolve(computer);
		} else {
			queueOfPromises.add(toReturn);
		}

		return toReturn;
	}

	/**
	 * Computer return procedure releases a computer which becomes available in the
	 * warehouse upon completion
	 */
	public void up() {
		if(!queueOfPromises.isEmpty()) {
			queueOfPromises.poll().resolve(computer);
		}	
		else
			isBlocked.set(false);
	}

	
}
