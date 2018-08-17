package bgu.spl.a2.sim.actions;

import java.util.HashMap;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class RemoveGrade extends Action<Boolean> {
	/*
	 * This action removes a grade from the student grade list.
	 */
	private String course;

	public RemoveGrade(String course) {
		this.course = course;

		setActionName("Remove Grade");
	}

	/*
	 * Removes a grade from the student grade list.
	 */
	@Override
	protected void start() {
		StudentPrivateState student = (StudentPrivateState) actorState;
		HashMap<String, Integer> gradesList = student.getGrades();
		gradesList.remove(course);

		complete(true);
	}

}
