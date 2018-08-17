/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import bgu.spl.a2.ActorThreadPool;
import bgu.spl.a2.PrivateState;
import bgu.spl.a2.sim.actions.AddSpaces;
import bgu.spl.a2.sim.actions.AddStudent;
import bgu.spl.a2.sim.actions.CheckAdmin;
import bgu.spl.a2.sim.actions.CloseCourse;
import bgu.spl.a2.sim.actions.OpenCourse;
import bgu.spl.a2.sim.actions.ParticipateInCourse;
import bgu.spl.a2.sim.actions.RegisterPreferences;
import bgu.spl.a2.sim.actions.Unregister;
import bgu.spl.a2.sim.privateStates.CoursePrivateState;
import bgu.spl.a2.sim.privateStates.DepartmentPrivateState;
import bgu.spl.a2.sim.privateStates.StudentPrivateState;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {

	public static ActorThreadPool actorThreadPool;
	private static String json;
	private static Warehouse warehouse;
	private static AtomicInteger count; 

	/**
	 * Begin the simulation Should not be called before attachActorThreadPool()
	 */
	public static void start() {
		// Initialize the proper Objects

		warehouse = new Warehouse();
		count = new AtomicInteger();
		LinkedList<Computer> computers = new LinkedList<>();
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(json);
		JsonObject jsonObject = jsonTree.getAsJsonObject();
		actorThreadPool.start();

		// Computers actions handling(Creates new list of computers as specified in the
		// input file)
		JsonArray ArrayOfAllTheComputers = (JsonArray) jsonObject.get("Computers");
		for (int i = 0; i < ArrayOfAllTheComputers.size(); i++) {
			JsonObject TheCurrentComputer = (JsonObject) ArrayOfAllTheComputers.get(i);
			ParseComputer(TheCurrentComputer, computers);
		}

		// Setting the computer list as a data member of the warehouse
		warehouse.setList(computers);

		// Deserialize the actions in Phase1 via 'ParseAction' method
		JsonArray ArrayOfAllThPhaseOneActions = (JsonArray) jsonObject.get("Phase 1");
		for (int i = 0; i < ArrayOfAllThPhaseOneActions.size(); i++) {

			JsonObject TheCurrentAction = (JsonObject) ArrayOfAllThPhaseOneActions.get(i);
			ParseAction(TheCurrentAction);
		}

		// Waits until all the actions in Phase1 be completed before proceeding to
		// Phase2
		while (count.get() != 0)
			;

		// Deserialize the actions in Phase2 via 'ParseAction' method
		JsonArray ArrayOfAllThPhaseTwoActions = (JsonArray) jsonObject.get("Phase 2");
		for (int i = 0; i < ArrayOfAllThPhaseTwoActions.size(); i++) {
			JsonObject TheCurrentAction = (JsonObject) ArrayOfAllThPhaseTwoActions.get(i);
			ParseAction(TheCurrentAction);
		}

		// Waits until all the actions in Phase2 be completed before proceeding to
		// Phase3
		while (count.get() != 0)
			;

		// Deserialize the actions in Phase3 via 'ParseAction' method
		JsonArray ArrayOfAllThPhaseThreeActions = (JsonArray) jsonObject.get("Phase 3");
		for (int i = 0; i < ArrayOfAllThPhaseThreeActions.size(); i++) {
			JsonObject TheCurrentAction = (JsonObject) ArrayOfAllThPhaseThreeActions.get(i);
			ParseAction(TheCurrentAction);
		}

		// Waits until all the actions in Phase3 be completed before return
		while (count.get() != 0)
			;
	}

	/*
	 * This method provides a deserialize of the content of an computers in the JSON
	 * String. It checks which computer type is about, creates new JSON string to
	 * match the exact fields, and set up the fields correctly of the new computer
	 * object. Then, it added the computer to the warehouse.
	 * 
	 */
	private static void ParseComputer(JsonObject theCurrentComputer, LinkedList<Computer> computers) {
		JsonElement TheTypeOfTheCurrentComputer = theCurrentComputer.get("Type");
		JsonElement TheSigSuccessOfTheCurrentComputer = theCurrentComputer.get("Sig Success");
		JsonElement TheSigFailOfTheCurrentComputer = theCurrentComputer.get("Sig Fail");
		String computerType = TheTypeOfTheCurrentComputer.toString();
		String failSig = TheSigFailOfTheCurrentComputer.toString();
		String successSig = TheSigSuccessOfTheCurrentComputer.toString();
		String jsonInStringComputer = "{'computerType' : " + computerType + "," + "'failSig' : " + failSig + ","
				+ "'successSig' : " + successSig + "}";
		Gson gson = new Gson();
		Computer toAdd = gson.fromJson(jsonInStringComputer, Computer.class);
		computers.add(toAdd);

	}

	/*
	 * This method provides a deserialize of the content of an action in the JSON
	 * String. It checks which action type is about, creates new JSON string to
	 * match the exact fields, and set up the fields correctly of the new action
	 * object. Then, it submit the action to the proper actor.
	 * 
	 */
	private static void ParseAction(JsonObject theCurrentAction) {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

		JsonElement TheTypeOfTheCurrentAction = theCurrentAction.get("Action");
		String actionNameAsField = TheTypeOfTheCurrentAction.toString();
		String actionName = TheTypeOfTheCurrentAction.getAsString();
		switch (actionName) {

		case "Open Course":
			JsonElement OpenCourse_Json_Department = theCurrentAction.get("Department");
			JsonElement OpenCourse_Json_Course = theCurrentAction.get("Course");
			JsonElement OpenCourse_Json_Space = theCurrentAction.get("Space");
			JsonElement OpenCourse_Json_Prerequisites = theCurrentAction.get("Prerequisites");
			String OpenCourse_Department = OpenCourse_Json_Department.toString();
			String OpenCourse_Course = OpenCourse_Json_Course.toString();
			String OpenCourse_Space = OpenCourse_Json_Space.toString();
			String OpenCourse_Prerequisites = OpenCourse_Json_Prerequisites.toString();
			String OpenCourse_Json_InString = "{'courseName' : " + OpenCourse_Course + "," + "'spots' : "
					+ OpenCourse_Space + "," + "'prerequisites' : " + OpenCourse_Prerequisites + "," + "'name' : "
					+ actionNameAsField + "}";

			OpenCourse toAdd_OpenCourse = gson.fromJson(OpenCourse_Json_InString, OpenCourse.class);
			toAdd_OpenCourse.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();

			actorThreadPool.submit(toAdd_OpenCourse, OpenCourse_Json_Department.getAsString(),
					new DepartmentPrivateState());
			break;

		case "Add Student":
			JsonElement AddStudent_Json_Department = theCurrentAction.get("Department");
			JsonElement AddStudent_Json_Student = theCurrentAction.get("Student");
			String AddStudent_Department = AddStudent_Json_Department.toString();
			String AddStudent_Student = AddStudent_Json_Student.toString();
			String AddStudent_Json_InString = "{'toRegister' : " + AddStudent_Student + "," + "'name' : "
					+ actionNameAsField + "}";
			AddStudent toAdd_AddStudent = gson.fromJson(AddStudent_Json_InString, AddStudent.class);
			toAdd_AddStudent.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();
			actorThreadPool.submit(toAdd_AddStudent, AddStudent_Json_Department.getAsString(),
					new DepartmentPrivateState());
			break;

		case "Participate In Course":
			JsonElement ParticipateInCourse_Json_Student = theCurrentAction.get("Student");
			JsonElement ParticipateInCourse_Json_Course = theCurrentAction.get("Course");
			JsonArray ParticipateInCourse_Json_Grade = (JsonArray) theCurrentAction.get("Grade");

			String ParticipateInCourse_Student = ParticipateInCourse_Json_Student.toString();
			String ParticipateInCourse_Course = ParticipateInCourse_Json_Course.toString();
			String ParticipateInCourse_Grade = ParticipateInCourse_Json_Grade.get(0).toString();

			if (ParticipateInCourse_Grade.indexOf("-") == 1) {

				ParticipateInCourse_Grade = '"' + "-1" + '"';
			}
			String ParticipateInCourse_Json_InString = "{'studentID' : " + ParticipateInCourse_Student + ","
					+ "'grade' : " + ParticipateInCourse_Grade + "," + "'name' : " + actionNameAsField + "}";

			ParticipateInCourse toAdd_ParticipateInCourse = gson.fromJson(ParticipateInCourse_Json_InString,
					ParticipateInCourse.class);
			toAdd_ParticipateInCourse.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();

			actorThreadPool.submit(toAdd_ParticipateInCourse, ParticipateInCourse_Json_Course.getAsString(),
					new CoursePrivateState());
			break;

		case "Unregister":
			JsonElement Unregister_Json_Student = theCurrentAction.get("Student");
			JsonElement Unregister_Json_Course = theCurrentAction.get("Course");
			String Unregister_Student = Unregister_Json_Student.toString();
			String Unregister_Course = Unregister_Json_Course.toString();
			String Unregister_Json_InString = "{'studentID' : " + Unregister_Student + "," + "'name' : "
					+ actionNameAsField + "}";
			Unregister toAdd_Unregister = gson.fromJson(Unregister_Json_InString, Unregister.class);
			toAdd_Unregister.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();
			actorThreadPool.submit(toAdd_Unregister, Unregister_Json_Course.getAsString(), new CoursePrivateState());
			break;

		case "Close Course":
			JsonElement CloseCourse_Json_Department = theCurrentAction.get("Department");
			JsonElement CloseCourse_Json_Course = theCurrentAction.get("Course");
			String CloseCourse_Department = CloseCourse_Json_Department.toString();
			String CloseCourse_Course = CloseCourse_Json_Course.toString();
			String CloseCourse_Json_InString = "{'course' : " + CloseCourse_Course + "," + "'name' : "
					+ actionNameAsField + "}";
			CloseCourse toAdd_CloseCourse = gson.fromJson(CloseCourse_Json_InString, CloseCourse.class);
			toAdd_CloseCourse.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();
			actorThreadPool.submit(toAdd_CloseCourse, CloseCourse_Json_Department.getAsString(),
					new DepartmentPrivateState());
			break;

		case "Add Spaces":
			JsonElement AddSpaces_Json_Course = theCurrentAction.get("Course");
			JsonElement AddSpaces_Json_Number = theCurrentAction.get("Number");
			String AddSpaces_Number = AddSpaces_Json_Number.toString();
			String AddSpaces_Course = AddSpaces_Json_Course.toString();
			String AddSpaces_Json_InString = "{'toAdd' : " + AddSpaces_Number + "," + "'name' : " + actionNameAsField
					+ "}";
			AddSpaces toAdd_AddSpaces = gson.fromJson(AddSpaces_Json_InString, AddSpaces.class);
			toAdd_AddSpaces.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();
			actorThreadPool.submit(toAdd_AddSpaces, AddSpaces_Json_Course.getAsString(), new CoursePrivateState());
			break;

		case "Register With Preferences":
			JsonElement RegisterWithPreferences_Json_Student = theCurrentAction.get("Student");
			JsonElement RegisterWithPreferences_Json_Preferences = theCurrentAction.get("Preferences");
			JsonElement RegisterWithPreferences_Json_Grade = theCurrentAction.get("Grade");
			String RegisterWithPreferences_Student = RegisterWithPreferences_Json_Student.toString();
			String RegisterWithPreferences_Preferences = RegisterWithPreferences_Json_Preferences.toString();
			String RegisterWithPreferences_Grade = RegisterWithPreferences_Json_Grade.toString();
			if (RegisterWithPreferences_Grade.contains("-")) {
				RegisterWithPreferences_Grade = RegisterWithPreferences_Grade.replaceAll("-", "-1");
			}
			String RegisterWithPreferences_Json_InString = "{'courses' : " + RegisterWithPreferences_Preferences + ","
					+ "'grades' : " + RegisterWithPreferences_Grade + "," + "'name' : " + actionNameAsField + "}";
			RegisterPreferences toAdd_RegisterPreferences = gson.fromJson(RegisterWithPreferences_Json_InString,
					RegisterPreferences.class);
			toAdd_RegisterPreferences.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();
			actorThreadPool.submit(toAdd_RegisterPreferences, RegisterWithPreferences_Json_Student.getAsString(),
					new StudentPrivateState());
			break;

		case "Administrative Check":
			JsonElement AdministrativeCheck_Json_Department = theCurrentAction.get("Department");
			JsonElement AdministrativeCheck_Json_Students = theCurrentAction.get("Students");
			JsonElement AdministrativeCheck_Json_Computer = theCurrentAction.get("Computer");
			JsonElement AdministrativeCheck_Json_Conditions = theCurrentAction.get("Conditions");
			String AdministrativeCheck_Department = AdministrativeCheck_Json_Department.toString();
			String AdministrativeCheck_Students = AdministrativeCheck_Json_Students.toString();
			String AdministrativeCheck_Computer = AdministrativeCheck_Json_Computer.toString();
			String AdministrativeCheck_Conditions = AdministrativeCheck_Json_Conditions.toString();
			String AdministrativeCheck_Json_InString = "{'computerType' : " + AdministrativeCheck_Computer + ","
					+ "'students' : " + AdministrativeCheck_Students + "," + "'conditions' : "
					+ AdministrativeCheck_Conditions + "," + "'name' : " + actionNameAsField + "}";

			CheckAdmin toAdd_AdministrativeCheck = gson.fromJson(AdministrativeCheck_Json_InString, CheckAdmin.class);
			toAdd_AdministrativeCheck.getResult().subscribe(() -> {
				count.getAndDecrement();
			});
			count.getAndIncrement();
			toAdd_AdministrativeCheck.setWareHouse(warehouse);
			toAdd_AdministrativeCheck.setFisrtTime(true);
			actorThreadPool.submit(toAdd_AdministrativeCheck, AdministrativeCheck_Json_Department.getAsString(),
					new DepartmentPrivateState());
			break;
		}

	}

	/**
	 * attach an ActorThreadPool to the Simulator, this ActorThreadPool will be used
	 * to run the simulation
	 * 
	 * @param myActorThreadPool
	 *            - the ActorThreadPool which will be used by the simulator
	 */
	public static void attachActorThreadPool(ActorThreadPool myActorThreadPool) {
		actorThreadPool = myActorThreadPool;
	}

	/**
	 * shut down the simulation returns list of private states
	 */
	public static HashMap<String, PrivateState> end() {
		try {
			actorThreadPool.shutdown();
		} catch (InterruptedException e) {
		} 

		HashMap<String, PrivateState> toReturn = new HashMap<>(actorThreadPool.getActors());
		return toReturn;
	}

	public static int main(String[] args) { 

		// Creates JSON String from the TXT file
		String content = null;
		File file = new File(args[0]);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			System.out.println("File not found");
		}

		// Pass the content of the JSON file to a field inside the Simulator
		json = content;

		// Parse the number of threads should be in the ActorThreadPool
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(content);
		JsonObject jsonObject = jsonTree.getAsJsonObject();
		JsonElement NumberOfThreads = jsonObject.get("threads");

		// Creates a new ActorThreadPool and attach it, then start
		attachActorThreadPool(new ActorThreadPool(NumberOfThreads.getAsInt()));
		start();

		HashMap<String, PrivateState> SimulationResult;
		SimulationResult = end();

		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream("result.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(SimulationResult);
		} catch (IOException e) {
		}
		
		return 0;
	}
	

}
