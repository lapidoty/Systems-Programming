package bgu.spl.a2.sim.actions;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import bgu.spl.a2.Action;
import bgu.spl.a2.Promise;
import bgu.spl.a2.sim.Computer;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

public class CheckAdmin extends Action<Boolean> {
	/*
	 * This action gets a computer type, a students list, and conditions course
	 * list, and checks if the student passed all the mentioned courses. If yes, set
	 * a success signature at the student private state of the specified computer.
	 */
	@Expose
	private Warehouse warehouse;
	@Expose
	private String computerType;
	@Expose
	private List<String> students;
	@Expose
	private List<String> conditions;
	private Promise<Computer> promiseComputer;
	private boolean firstTime;

	public CheckAdmin(String computerType, List<String> students, List<String> conditions) {
		this.conditions = conditions;
		this.students = students;
		this.computerType = computerType;

		setActionName("Administrative Check");

	}

	/*
	 * Gets a computer type, a students list, and conditions course list, and checks
	 * if the student passed all the mentioned courses. If yes, set a success
	 * signature at the student private state of the specified computer.
	 * 
	 */
	@Override
	protected void start() {
		ArrayList<Action<?>> actions = new ArrayList<Action<?>>();
		if (firstTime)
			promiseComputer = warehouse.getMap().get(computerType).down();

		if (promiseComputer.isResolved()) {
			for (int i = 0; i < students.size(); i++) {
				StudentCheckAndSign msg = new StudentCheckAndSign(promiseComputer.get(), conditions);
				actions.add(msg);

				sendMessage(msg, students.get(i), new StudentPrivateState());
			}
			then(actions, () -> {
				complete(true);
				warehouse.getMap().get(computerType).up();

			});

		}

		else {
			firstTime = false;
			pool.submit(this, actorId, actorState);
		}

	}

	public void setWareHouse(Warehouse warehouse) {
		this.warehouse = warehouse;

	}

	public void setFisrtTime(boolean b) {
		this.firstTime = b;

	}

}
