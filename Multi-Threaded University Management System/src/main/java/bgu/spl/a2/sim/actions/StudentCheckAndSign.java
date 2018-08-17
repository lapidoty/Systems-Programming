package bgu.spl.a2.sim.actions;

import java.util.List;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.Computer;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class StudentCheckAndSign extends Action<Boolean> {
	/*
	 * This action gets a computer and courses list, and send to the specified
	 * computer to check if he passed those courses, and set a signature as the
	 * result.
	 */
	Computer computer;
	private List<String> conditions;

	public StudentCheckAndSign(Computer computer, List<String> conditions) {
		this.computer = computer;
		this.conditions = conditions;
		setActionName("Student Check And Sign");

	}

	/*
	 * Gets a computer and courses list, and send to the specified computer to check
	 * if he passed those courses, and set a signature as the result.
	 */
	@Override
	protected void start() {

		((StudentPrivateState) actorState)
				.setSignature(computer.checkAndSign(conditions, ((StudentPrivateState) actorState).getGrades()));

		complete(true);

	}

}
