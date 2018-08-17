package bgu.spl181.net.api.bidi.UserServiceTextProtocol;


import java.util.LinkedList;
import java.util.Map.Entry;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol_Implentation;
import bgu.spl181.net.api.bidi.DataObject;

public abstract class UserServiceText__MessagingProtocol extends BidiMessagingProtocol_Implentation<String> {
	/*
	 * This class defines the User Service Text Based Protocol This class defines
	 * the connection with the client that his number is the connection id.
	 */

	private volatile String UserNameLoggedIn = ""; // This field Indicates about the user that logged in by the
													// clientId. If ="", indicates no user is logged by this clientId.
	private volatile boolean firstTime = true;

	/*
	 * This function checks the kind of input from the user, then calls the right
	 * function to handle.
	 */
	@Override
	public void process(String message) {
		if (firstTime) {

			firstTime = false;
		}
		String firstWord = "";

		if (message.contains(" ")) {
			firstWord = message.substring(0, message.indexOf(" "));
		} else
			firstWord = message;

		switch (firstWord) {
		case "REGISTER":
			REGISTER(message);
			break;
		case "LOGIN":
			LOGIN(message);
			break;
		case "SIGNOUT":
			SIGNOUT(message);
			break;
		case "REQUEST":
			REQUEST(message);
			break;

		}

	}

	/// REGISTER management functions ///

	/*
	 * This function checks if conditions of registration fulfilled, then register
	 * the user by adding him to the Registerd_Users map and calls
	 * REGISTER_at_Service to at the service protocol(BlockBuster) in our case.
	 * 
	 * If Succeeded - calls the ACK_REGISTERSucceeded to send message to the client
	 * that indicates the registration succeeded.
	 */
	protected void REGISTER(String message) {
		String TheMessageWithOutTheType = message.substring(message.indexOf(" ") + 1);

		String username = retriveUserName(TheMessageWithOutTheType);

		String password = retrivePassword(TheMessageWithOutTheType);

		String[] dataBlocks = retriveDataBlocks(TheMessageWithOutTheType);

		// We explain our synchronized as always:
		// When adds a new user to the map, we don't want any thread to Read/add/change
		// data. Furthermore, we don't want the conditions to be change after checking.
		synchronized (sharedProtocolData.getData("Registerd_Users")) {

			if (UserNameLoggedIn != "" || userNameAlreadyTaken(username) || missingInfo(dataBlocks)) {
				ERROR_REGISTERFailed();

			} else {
				AddUser(username, password, dataBlocks);
				REGISTER_at_Service(username, password, dataBlocks);
				ACK_REGISTERSucceeded();
			}
		}
	}

	/*
	 * Adds a User to the USTP users map.
	 */
	protected void AddUser(String username, String password, String[] dataBlocks) {
		UserServiceText_User user = new UserServiceText_User(password, username, dataBlocks);
		sharedProtocolData.getData("Registerd_Users").putIfAbsent(username, user);
	}

	/*
	 * Return the username part of the registration message.
	 */
	protected String retriveUserName(String theMessageWithOutTheType) {
		String username = theMessageWithOutTheType.split(" ")[0];

		return username;
	}

	/*
	 * Return the password part of the registration message.
	 */
	protected String retrivePassword(String theMessageWithOutTheType) {
		String password = "";
		if (theMessageWithOutTheType.split(" ").length > 0)
			password = theMessageWithOutTheType.split(" ")[1];

		return password;
	}

	/*
	 * Return the data optional part of the registration message.
	 */
	protected String[] retriveDataBlocks(String theMessageWithOutTheType) {
		String AllData = theMessageWithOutTheType.substring(theMessageWithOutTheType.indexOf(" ") + 1);
		AllData = AllData.substring(AllData.indexOf(" ") + 1);
		LinkedList<String> dataList = new LinkedList<String>();
		boolean split = true;
		int messageSize = AllData.length();
		int nextIndex = 0;

		for (int i = 0; i < messageSize; i++) {
			if (AllData.charAt(i) == '"')
				split = !split;

			if (AllData.charAt(i) == ' ' && split) {
				String temp = AllData.substring(nextIndex, i);
				dataList.add(temp);
				nextIndex = i + 1;
			}

			if (i == messageSize - 1) {
				String temp = AllData.substring(nextIndex);
				dataList.add(temp);
			}
		}

		String[] dataBlocks = new String[dataList.size()];

		int addIndex = 0;
		for (String toAdd : dataList) {
			dataBlocks[addIndex] = toAdd;
			addIndex++;
		}
		return dataBlocks;
	}

	/*
	 * Check if missing some info at the registration(a password, a username).
	 */
	protected boolean missingInfo(String[] dataBlocks) {
		if (dataBlocks.length < 1) {
			return true;
		}
		return false;

	}

	/*
	 * Checks if the username already in the registered users map.
	 */
	protected boolean userNameAlreadyTaken(String username) {

		if (sharedProtocolData.getData("Registerd_Users").containsKey(username))
			return true;

		return false;

	}

	/*
	 * Calls when REGISTER fails.
	 */
	protected void ERROR_REGISTERFailed() {
		connections.send(connectionId, "ERROR registration failed");

	}

	/*
	 * Calls when REGISTER Succeeded.
	 */
	protected void ACK_REGISTERSucceeded() {
		connections.send(connectionId, "ACK registration succeeded");
	}

	/*
	 * By extends this function of any service(BlockBuster etc), the registration
	 * continues by the service demands.
	 */
	protected abstract void REGISTER_at_Service(String username, String password, String[] dataBlock);

	/// LOGIN management functions ///
	protected void LOGIN(String message) {

		String TheMessageWithOutTheType = message.substring(message.indexOf(" ") + 1);

		String username = retriveUserName(TheMessageWithOutTheType);

		String password = retrivePassword(TheMessageWithOutTheType);

		// We explain our synchronized as always:
		// When a user wants log in, we don't want any thread to Read/add/change
		// data. Furthermore, we don't want the conditions to be change after checking.
		synchronized (sharedProtocolData.getData("Registerd_Users")) {
			if (!(UserNameLoggedIn.equals("")) || !sharedProtocolData.getData("Registerd_Users").containsKey(username)
					|| !((UserServiceText_User) sharedProtocolData.getData("Registerd_Users").get(username))
							.getPassword().equals(password)
					|| AlreadyLoggedIn(username))

				ERROR_LOGINFailed();
			else {
				UserServiceText_User user = (UserServiceText_User) sharedProtocolData.getData("Registerd_Users")
						.get(username);
				UserNameLoggedIn = user.getUsername();
				user.setLoggedIn(connectionId);
				ACK_LOGINSucceeded();
			}
		}
	}

	/*
	 * Checks if the user field that indicates if logged-In already is not -1, which
	 * means he logged in by the client that his id is that number.
	 */
	private boolean AlreadyLoggedIn(String username) {
		UserServiceText_User user = (UserServiceText_User) sharedProtocolData.getData("Registerd_Users").get(username);
		return user.getLoggedIn() != -1;
	}

	/*
	 * Calls when LOGIN Succeeded.
	 */
	protected void ERROR_LOGINFailed() {
		connections.send(connectionId, "ERROR login failed");
	}

	/*
	 * Calls when LOGIN Succeeded.
	 */
	protected void ACK_LOGINSucceeded() {
		connections.send(connectionId, "ACK login succeeded");
	}

	/// SIGNOUT management functions ///

	/*
	 * Sign out the user from the service and the server , by change his fields and
	 * removes it from the connections map, if the conditions fit.
	 */
	private void SIGNOUT(String message) {

		if (!UserNameLoggedIn.equals("")) {
			UserServiceText_User user = (UserServiceText_User) sharedProtocolData.getData("Registerd_Users")
					.get(UserNameLoggedIn);
			user.LoggedInClient = -1;
			UserNameLoggedIn = "";
			shouldTerminate = true;

			ACK_SIGNOUTSucceeded();
			connections.disconnect(connectionId);
		} else
			ERROR_SIGNOUTFailed();

	}

	/*
	 * Calls when SIGNOUT Succeeded.
	 */
	protected void ERROR_SIGNOUTFailed() {
		connections.send(connectionId, "ERROR signout failed");
	}

	/*
	 * Calls when SIGNOUT Succeeded.
	 */
	protected void ACK_SIGNOUTSucceeded() {
		connections.send(connectionId, "ACK signout succeeded");
	}

	/// REQUEST management functions ///

	/*
	 * Calls the above-protocol service function to act the correct request, if the
	 * conditions fit.
	 */
	private void REQUEST(String message) {
		String TheMessageWithOutTheType = message.substring(message.indexOf(" ") + 1);

		String serviceName = retrieveServiceName(TheMessageWithOutTheType);

		String[] parameters = retrieveParameters(TheMessageWithOutTheType);

		if (UserNameLoggedIn.equals(""))
			ERROR_REQUESTFailed("ERROR request " + serviceName + " failed");
		else
			REQUEST_at_Service(serviceName, parameters);

	}

	/*
	 * Calls when REQUEST Succeeded.
	 */
	protected void ERROR_REQUESTFailed(String outputMessage) {
		connections.send(connectionId, outputMessage);
	}

	/*
	 * Calls when REQUEST Succeeded.
	 */
	protected void ACK_REQUESTSucceeded(String outputMessage) {
		connections.send(connectionId, outputMessage);
	}

	/*
	 * Implements at the above protocol.
	 */
	protected abstract void REQUEST_at_Service(String requestName, String[] parameters);

	/*
	 * Retrieve user input kind of message.
	 */
	protected String retrieveServiceName(String theMessageWithOutTheType) {
		String service = theMessageWithOutTheType.split(" ")[0];

		return service;
	}

	/*
	 * Retrieve user input parameters.
	 */
	protected String[] retrieveParameters(String messageWithoutType) {
		String messageWithoutService = messageWithoutType.substring(messageWithoutType.indexOf(" ") + 1,
				messageWithoutType.length());
		String[] output = messageWithoutService.replaceAll("^\"", "").split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?");

		return output;
	}

	protected String getUserNameLoggedIn() {
		return UserNameLoggedIn;
	}

	protected void setUserNameLoggedIn(String userNameLoggedIn) {
		UserNameLoggedIn = userNameLoggedIn;
	}

	/*
	 * Calls when needed BROADCAST, this functions goes throw the Registered Users
	 * map, gets the field that indicates if the user logged in, then calls
	 * connections to send the message with the proper connection id.
	 */
	protected void BROADCAST(String message) {
		for (Entry<String, DataObject> item : sharedProtocolData.getData("Registerd_Users").entrySet()) {
			UserServiceText_User user = (UserServiceText_User) item.getValue();
			Integer clientToSend = user.getLoggedIn();

			if (clientToSend != -1)
				connections.send(clientToSend, message);

		}
	}
}
