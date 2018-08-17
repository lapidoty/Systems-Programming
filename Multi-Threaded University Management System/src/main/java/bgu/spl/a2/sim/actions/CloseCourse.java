package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.DepartmentPrivateState;

public class CloseCourse extends Action<Boolean> {
	/*
	 * This action close a course, means remove the course from the department
	 * courses list, and remove all the students from the course, using unregister
	 * action.
	 */
	@Expose
	private String course;

	public CloseCourse(String course) {
		this.course = course;

		setActionName("Close Course");
	}

	/*/
	 * Close a course, means remove the course from the department
	 * courses list, and remove all the students from the course, using unregister
	 * action. 
	 */
	@Override
	protected void start() {

		DepartmentPrivateState department = (DepartmentPrivateState) actorState;
		List<String> courseList = department.getCourseList();

		ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();
		UnregisterAll unregisterAll = new UnregisterAll();

		actions.add(unregisterAll);
		sendMessage(unregisterAll, course, new CoursePrivateState());

		then(actions, () -> {
			complete(courseList.remove(course));
		});
	}

}
