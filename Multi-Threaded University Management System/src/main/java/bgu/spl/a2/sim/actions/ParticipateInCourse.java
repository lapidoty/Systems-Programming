package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class ParticipateInCourse extends Action<Boolean> {
	/*
	 * This action try to register a student to specified course, it check the
	 * prerequisites of the student, the available spots at the course , and if gets
	 * positive answer from both, register the student to the course, means add the student to the
	 * students list of the course, and set the available spaces.
	 */
	@Expose
	private String studentID;
	@Expose
	private Integer grade;

	public ParticipateInCourse(String student, Integer grade) {
		this.studentID = student;
		this.grade = grade;

		setActionName("Participate In Course");
	}

	/*
	 * Try to register a student to specified course, it check the prerequisites of
	 * the student, the available spots at the course , and if gets positive answer
	 * from both, register the student to the course, means add the student to the
	 * students list of the course, and set the available spaces.
	 * 
	 */
	@Override
	protected void start() {

		ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();
		CoursePrivateState course = (CoursePrivateState) actorState;
		List<String> prereq = course.getPrequisites();
		CheckAndSetGrade checkAndRegister = new CheckAndSetGrade(prereq, this.actorId, grade);

		if (course.getAvailableSpots() > 0) {
			actions.add(checkAndRegister);
			sendMessage(checkAndRegister, studentID, new StudentPrivateState());
			then(actions, () -> {
				Boolean passResult = checkAndRegister.getResult().get();

				complete(passResult);
			});
		} else {
			complete(false);
		}
	}

	public String getId() {
		return studentID;
	}

}
