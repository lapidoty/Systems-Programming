package bgu.spl.a2.sim.actions;

import java.util.List;
import bgu.spl.a2.Action;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;

public class AskForSpace extends Action<Boolean>{
	/*This action checks if there is available space in a course.
	 * 
	 */
	private String studentID;
	
	public AskForSpace(String studentID) {
		this.studentID = studentID ;
		setActionName("Ask For Space");
	}
	
	/*
	 * Checks if there is available space in a course.
	 * 
	 */
	@Override
	protected void start() {
		CoursePrivateState course = (CoursePrivateState)actorState;
		List<String> registeredStudents = course.getRegStudents();

		if (course.getAvailableSpots() > 0){
			registeredStudents.add(studentID);
			course.setAvailableSpots(course.getAvailableSpots()-1);
			course.setRegistered(course.getRegistered()+1);
			
			complete(true);
		}
		else
			complete(false);
			
	}
	

}
