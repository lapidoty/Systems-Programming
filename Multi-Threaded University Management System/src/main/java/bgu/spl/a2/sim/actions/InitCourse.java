package bgu.spl.a2.sim.actions;

import java.util.List;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;

public class InitCourse extends Action<Boolean> {
	/*
	 * This action initialize a course, means set the initially spaces and the
	 * prerequisites.
	 */
	private Integer spots;
	private List<String> prereq;

	public InitCourse(Integer spots, List<String> prereq) {
		this.spots = spots;
		this.prereq = prereq;
		this.setActionName("Init Course");
	}

	/*
	 * Initialize a course, means set the initially spaces and the
	 * prerequisites.
	 */
	@Override
	protected void start() {
		CoursePrivateState course = (CoursePrivateState) actorState;
		course.setAvailableSpots(spots);
		course.setPrequisites(prereq);
		course.setRegistered(0);
		complete(true);
	}

}
