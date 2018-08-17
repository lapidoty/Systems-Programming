package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.DepartmentPrivateState;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class AddStudent extends Action<Boolean> {
	/*
	 * This action adds a student to the department students list, and initialize
	 * the student actor.
	 */
	@Expose
	String toRegister;

	public AddStudent(String id) {
		this.toRegister = id;
		setActionName("Add Student");
	}

	/*
	 * Adds a student to the department students list, and initialize the student
	 * actor.
	 * 
	 */
	@Override
	protected void start() {
		DepartmentPrivateState department = (DepartmentPrivateState) actorState;
		List<String> studentList = department.getStudentList();
		Action<Boolean> action = new Action<Boolean>() {

			@Override
			protected void start() {
				complete(true);

			}
		};
		studentList.add(toRegister);
		action.setActionName("Initialize student");
		sendMessage(action, toRegister, new StudentPrivateState());
		then(new ArrayList<>(), () -> {
			complete(true);
		});

	}

}
