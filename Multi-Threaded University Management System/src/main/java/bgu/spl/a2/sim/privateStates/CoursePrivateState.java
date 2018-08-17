package bgu.spl.a2.sim.privateStates;


import java.util.ArrayList;
import java.util.List;

import bgu.spl.a2.PrivateState;

/**
 * this class describe course's private state
 */
public class CoursePrivateState extends PrivateState{
	private Integer availableSpots;
	private Integer registered;
	private List<String> regStudents;
	private List<String> prequisites;
	
	/**
 	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 */
	public CoursePrivateState() {
		registered=0 ;
		availableSpots=0;
		regStudents = new ArrayList<String>();
		prequisites = new ArrayList<String>(); 
	}

	public Integer getAvailableSpots() {
		return availableSpots;
	}

	public Integer getRegistered() {
		return registered;
	}

	public List<String> getRegStudents() {
		return regStudents;
	}

	public List<String> getPrequisites() {
		return prequisites;
	}
	
	public void setRegistered(Integer registered){
		this.registered = registered ;
	}
	
	public void setPrequisites(List<String> prerequisites) {
		for (String s : prerequisites)
			this.prequisites.add(s);
	}

	public void setAvailableSpots(Integer availableSpots) {
		this.availableSpots = availableSpots;
	}

}
