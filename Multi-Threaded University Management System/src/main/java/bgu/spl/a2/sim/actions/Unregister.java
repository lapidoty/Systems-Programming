package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class Unregister extends Action<Boolean> {
	/*
	 * This action unregister a student from a specified course. It deletes the
	 * student from the students list of the course and delete the course from the
	 * grades list of the student.
	 */
	@Expose
	private String studentID;

	public Unregister(String studentID) {
		this.studentID = studentID;
		setActionName("Unregister");
	}

	/*
	 * Unregister a student from a specified course. It deletes the student from the
	 * students list of the course and delete the course from the grades list of the
	 * student.
	 */
	@Override
	protected void start() {
		CoursePrivateState course = (CoursePrivateState) this.actorState;
		List<String> registeredStudents = course.getRegStudents();
		if (!registeredStudents.contains(studentID))
			complete(true);
		else {
			registeredStudents.remove(studentID);
			course.setAvailableSpots(course.getAvailableSpots() + 1);
			course.setRegistered(course.getRegistered() - 1);

			ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();
			RemoveGrade removeGrade = new RemoveGrade(this.actorId);

			actions.add(removeGrade);
			sendMessage(removeGrade, studentID, new StudentPrivateState());
			then(actions, () -> {
				complete(actions.get(0).getResult().get());
			});
		}
	}

}
