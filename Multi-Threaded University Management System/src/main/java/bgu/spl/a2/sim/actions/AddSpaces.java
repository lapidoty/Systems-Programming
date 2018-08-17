package bgu.spl.a2.sim.actions;

import com.google.gson.annotations.Expose;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;

public class AddSpaces extends Action<Boolean> {
	/*
	 * This action add available spaces at a course
	 * 
	 */
	@Expose
	private Integer toAdd;

	public AddSpaces(Integer toAdd) {
		this.toAdd = toAdd;

		setActionName("Add Spaces");
	}

	/*
	 * Add available spaces at a course.
	 * 
	 */
	@Override
	protected void start() {
		CoursePrivateState course = (CoursePrivateState) actorState;

		course.setAvailableSpots(course.getAvailableSpots() + toAdd);

		complete(true);
	}

}
