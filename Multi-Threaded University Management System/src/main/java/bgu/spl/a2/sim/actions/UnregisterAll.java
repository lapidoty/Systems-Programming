package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class UnregisterAll extends Action<Boolean> {
	/*
	 * This action do an unregister for all the students in the specified course,
	 * called when close course take action.
	 */
	public UnregisterAll() {
		setActionName("UnregisterAll");
	}

	/*
	 * Do an unregister for all the students in the specified course,
	 * called when close course take action.
	 */
	@Override
	protected void start() {

		CoursePrivateState course = (CoursePrivateState) actorState;
		ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();

		List<String> students = course.getRegStudents();
		Boolean success = false;
		while (!success) {
			try {
				for (String student : students) {
					RemoveGrade removeGrade = new RemoveGrade(this.actorId);
					actions.add(removeGrade);
					sendMessage(removeGrade, student, new StudentPrivateState());
				}
				success = true;
			} catch (java.util.ConcurrentModificationException e) {
			}
		}
		then(actions, () -> {
			students.clear();
			course.setAvailableSpots(-1);
			course.setRegistered(0);
			complete(true);
		});

	}

}
