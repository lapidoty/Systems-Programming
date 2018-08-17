package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gson.annotations.Expose;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class CheckAndSetGrade extends Action<Boolean> {
	/*
	 * A test for the student to check if he can register to a course with
	 * prerequisites. Initially submitted to the student's actor.
	 */

	private List<String> prereq;
	@Expose
	private String course;
	@Expose
	private Integer grade;

	public CheckAndSetGrade(List<String> prereq, String course, Integer grade) {
		this.prereq = prereq;
		this.course = course;
		this.grade = grade;
		setActionName("Check And Set Grade");
	}

	/*
	 * A test for the student to check if he can register to a course with
	 * prerequisites. Initially submitted to the student's actor.
	 * 
	 */
	@Override
	protected void start() {

		StudentPrivateState student = (StudentPrivateState) actorState;
		Boolean passResult = true;
		Iterator courseList = prereq.iterator();
		while (courseList.hasNext() && passResult) {
			String course = (String) courseList.next();
			if (!student.getGrades().containsKey(course))
				passResult = false;
		}
		if (passResult) {
			ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();
			AskForSpace spaceRequest = new AskForSpace(actorId);
			actions.add(spaceRequest);
			sendMessage(spaceRequest, course, new CoursePrivateState());

			then(actions, () -> {
				Boolean successfulSpaceRequest = spaceRequest.getResult().get();
				if (successfulSpaceRequest) {
					HashMap<String, Integer> gradesList = student.getGrades();
					gradesList.put(course, grade);
					complete(true);
				} else
					complete(false);
			});
		} else
			complete(false);

	}

}
