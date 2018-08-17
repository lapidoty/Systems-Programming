package bgu.spl.a2.sim.actions;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;

public class RegisterPreferences extends Action<Boolean> {
	/*
	 * This action try to register a student to his preferences list, by send a new
	 * Participate In Course message to the Thread pool. If succeed, stops trying to
	 * register to the next course at the preferences list, otherwise, keep trying.
	 * 
	 */
	@Expose
	private String[] courses;
	@Expose
	private Integer[] grades;
	private Integer currentCourse = 0;
	private Boolean nextCourse = true;

	public RegisterPreferences() {
		nextCourse = true;

	}

	public RegisterPreferences(String[] courses, Integer[] grades) {
		this.courses = courses;
		this.grades = grades;

		setActionName("Register With Preferences");
	}

	/*
	 * Try to register a student to his preferences list, by send a new Participate
	 * In Course message to the Thread pool. If succeed, stops trying to register to
	 * the next course at the preferences list, otherwise, keep trying.
	 */
	@Override
	protected void start() {
		if (nextCourse) {
			nextCourse = false;
			ArrayList<Action<Boolean>> actions = new ArrayList<Action<Boolean>>();
			ParticipateInCourse toAdd = new ParticipateInCourse(actorId, grades[currentCourse]);
			toAdd.setActionName("Try to register - Register With Preferences");
			actions.add(toAdd);
			sendMessage(toAdd, courses[currentCourse], new CoursePrivateState());
			then(actions, () -> {

				if (actions.get(0).getResult().get())
					complete(true);
				else {
					if (currentCourse < courses.length - 1) {
						nextCourse = true;
						currentCourse++;
						resumed.set(false);
						pool.submit(this, actorId, actorState);
					} else
						complete(false);

				}
			});
		} else
			pool.submit(this, actorId, actorState);

	}

}
