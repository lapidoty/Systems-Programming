package bgu.spl.a2.sim;

import java.util.HashMap;
import java.util.LinkedList;


/**
 * represents a warehouse that holds a finite amount of computers
 *  and their suspended mutexes.
 * 
 */
public class Warehouse {
	HashMap<String , SuspendingMutex> computers;
	
	public void setList(LinkedList<Computer> computersList) {
		computers = new HashMap<String , SuspendingMutex>();
		
		for(Computer computer : computersList) 
			computers.put(computer.getType(), new SuspendingMutex(computer));
		
	}
	public HashMap<String , SuspendingMutex> getMap() {
		return computers;
	}
}
