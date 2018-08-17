package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.DepartmentPrivateState;

public class OpenCourse extends Action<Boolean> {
	/*
	 * This action opens a course, which means add the course to the department
	 * course list , and initialize the private state of the specified course.
	 */
	@Expose
	private String courseName;

	@Expose
	private Integer spots;

	@Expose
	private List<String> prerequisites;

	public OpenCourse(String courseName, Integer spots, List<String> prereq) {
		this.courseName = courseName;
		this.spots = spots;
		this.prerequisites = prereq;

		setActionName("Open Course");
	}

	/*
	 * Opens a course, which means add the course to the department course list ,
	 * and initialize the private state of the specified course.
	 */
	@Override
	protected void start() {
		DepartmentPrivateState department = (DepartmentPrivateState) actorState;
		List<String> departmentCourses = department.getCourseList();
		departmentCourses.add(courseName);

		ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();
		Action<Boolean> initCourse = new InitCourse(spots, prerequisites);

		actions.add(initCourse);
		sendMessage(initCourse, courseName, new CoursePrivateState());
		then(actions, () -> {
			complete(actions.get(0).getResult().get());
		});

	}

}
