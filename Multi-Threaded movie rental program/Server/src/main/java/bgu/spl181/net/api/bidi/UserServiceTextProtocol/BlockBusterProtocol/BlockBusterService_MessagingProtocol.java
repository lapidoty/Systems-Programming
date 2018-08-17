package bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl181.net.api.bidi.DataObject;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.UserServiceText__MessagingProtocol;

public class BlockBusterService_MessagingProtocol extends UserServiceText__MessagingProtocol {
	/*
	 * This class defines the Block Buster protocol
	 * 
	 */
	public BlockBusterService_MessagingProtocol(BlockBusterService_SharedData sharedProtocolData) {
		super();
		this.sharedProtocolData = sharedProtocolData;
	}

	/*
	 * Adds the given status of users (at start time) to the shared protocol data,
	 * means a map by the key username and the value is the username password. After
	 * updates the map of register users, we will write it to the Json file, since
	 * its part of the block buster protocol.
	 */
	@Override
	protected void REGISTER_at_Service(String username, String password, String[] country) {
		String onlyCountry = country[0].substring(country[0].indexOf("=") + 1);
		onlyCountry = onlyCountry.substring(1, onlyCountry.length() - 1);

		BlockBusterService_User user = new BlockBusterService_User(username, password, onlyCountry);
		sharedProtocolData.addData("Service_Clients", username, user);

		((BlockBusterService_SharedData) sharedProtocolData).updateUsersJsonFile();
	}

	/*
	 * Check if missing some info at the registration(A password, a username, or
	 * country)
	 */
	@Override
	protected boolean missingInfo(String[] dataBlocks) {
		if (dataBlocks.length != 1) {
			return true;
		} else if (!dataBlocks[0].contains("country=")) {
			return true;

		}
		return false;
	}

	/*
	 * Fits the request to the proper request, and activate the proper
	 * function.
	 */
	@Override
	protected void REQUEST_at_Service(String requestName, String[] parameters) {
		switch (requestName) {
		case "balance":
			balanceRequest(parameters);
			break;
		case "info":
			infoRequest(parameters);
			break;
		case "rent":
			rentRequest(parameters);
			break;
		case "return":
			returnRequest(parameters);
			break;
		case "addmovie":
			addMovieRequest(parameters);
			break;
		case "remmovie":
			remMovieRequest(parameters);
			break;
		case "changeprice":
			changePriceRequest(parameters);
			break;
		}
	}

	/*
	 * This function gets the user object from the protocol data map , and retrieve
	 * the balance from it if the conditions fit.
	 */
	private void balanceRequest(String[] parameters) {

		BlockBusterService_User user = (BlockBusterService_User) sharedProtocolData.getData("Service_Clients")
				.get(getUserNameLoggedIn());
		String action = parameters[0]; // should be "info" or "add". any other word would do nothing.

		switch (action) {
		case "info":
			ACK_REQUESTSucceeded("ACK balance " + user.getBalance());
			break;
		case "add":
			Integer currentBalance = user.getBalance();
			Integer amount = Integer.parseInt(parameters[1]);
			user.setBalance(currentBalance + amount);
			((BlockBusterService_SharedData) sharedProtocolData).updateUsersJsonFile();
			ACK_REQUESTSucceeded("ACK balance " + user.getBalance() + " added " + amount);
			break;
		}
	}

	/*
	 * This function gets all the movies info from the map if the conditions fit,
	 * and print it to the user if success.
	 * 
	 */
	private void infoRequest(String[] parameters) {
		// We explain our synchronized as always:
		// When reading the info from the map, we don't want any thread to add/change
		// data.
		synchronized (sharedProtocolData.getData("Movies")) {
			if (parameters[0].equals("info")) {
				// If no movie name was entered, the only parameter will also == "info" as the
				// service name.
				String output;
				output = moviesToString(sharedProtocolData.getData("Movies"));

				ACK_REQUESTSucceeded("ACK info" + output); // output string already starts with a space.
			} else {
				BlockBusterService_Movie movie = (BlockBusterService_Movie) sharedProtocolData.getData("Movies")
						.get(parameters[0]);
				if (movie != null) {
					String movieInfo = "\"" + movie.getName() + "\" " + movie.getAvailableAmount().get() + " "
							+ movie.getPrice() + bannedCountriesToString(movie.getBannedCountries());
					ACK_REQUESTSucceeded("ACK info " + movieInfo);
				} else
					ERROR_REQUESTFailed("ERROR request info failed");
			}
		}
	}

	/*
	 * This function rents a movie if the conditions fit , means add it to the
	 * movies list of the user , and updates the balance field at the user and the
	 * available amount of the movie.
	 *
	 */
	private void rentRequest(String[] parameters) {

		BlockBusterService_User user = (BlockBusterService_User) sharedProtocolData.getData("Service_Clients")
				.get(getUserNameLoggedIn());
		BlockBusterService_Movie movie = (BlockBusterService_Movie) sharedProtocolData.getData("Movies")
				.get(parameters[0]);
		// We explain our synchronized as always:
		// Due to 'available amount' that can be modified by couple of threads, we want
		// to condition regarding the amount will not change during the checking.

		synchronized (sharedProtocolData.getData("Movies")) {
			// We check if the movie is found, and THEN if it follows the assignment rules.
			if (movie == null || movie.getAvailableAmount().get() == 0 || user.getMovies().contains(movie)
					|| movie.getBannedCountries().contains(user.getCountry()) || user.getBalance() < movie.getPrice())
				ERROR_REQUESTFailed("ERROR request rent failed");

			else {
				user.setBalance(user.getBalance() - movie.getPrice());
				user.getMovies().add(movie);
				movie.getAvailableAmount().decrementAndGet();

				((BlockBusterService_SharedData) sharedProtocolData).updateUsersJsonFile();
				((BlockBusterService_SharedData) sharedProtocolData).updateMoviesJsonFile();

				ACK_REQUESTSucceeded("ACK rent " + "\"" + movie.getName() + "\"" + " success");
				BROADCAST("BROADCAST" + " \"" + movie.getName() + "\" " + movie.getAvailableAmount() + " "
						+ movie.getPrice());
			}
		}
	}

	/*
	 * This function return a movie if the conditions fit , means remove it from the
	 * movies list of the user , and updates the balance field at the user and the
	 * available amount of the movie.
	 */
	private void returnRequest(String[] parameters) {
		BlockBusterService_User user = (BlockBusterService_User) sharedProtocolData.getData("Service_Clients")
				.get(getUserNameLoggedIn());
		BlockBusterService_Movie movie = (BlockBusterService_Movie) sharedProtocolData.getData("Movies")
				.get(parameters[0]);

		if (movie == null || !user.getMovies().contains(movie))
			ERROR_REQUESTFailed("ERROR request return failed");
		else {
			user.getMovies().remove(movie);
			movie.getAvailableAmount().incrementAndGet();

			((BlockBusterService_SharedData) sharedProtocolData).updateUsersJsonFile();
			((BlockBusterService_SharedData) sharedProtocolData).updateMoviesJsonFile();

			ACK_REQUESTSucceeded("ACK return " + "\"" + movie.getName() + "\"" + " success");
			BROADCAST("BROADCAST" + " \"" + movie.getName() + "\" " + movie.getAvailableAmount() + " "
					+ movie.getPrice());
		}
	}

	/*
	 * Create a String of movie names ready to be sent to the client.
	 */
	private String moviesToString(ConcurrentHashMap<String, DataObject> movies) {

		String output = "";

		for (String movie : movies.keySet())
			output += " \"" + movie + "\"";

		return output;
	}

	/*
	 * Creates a String of banned countries ready to be sent to the client.
	 */
	private String bannedCountriesToString(LinkedList<String> countries) {

		String output = "";

		for (String country : countries)
			output += " \"" + country + "\"";

		return output;
	}

	//// ADMIN REQUESTS////

	/*
	 * This function adds a movie if the conditions fit , means add it to the movies
	 * map.
	 */
	private void addMovieRequest(String[] parameters) {
		// We explain our synchronized as always:
		// We don't want any thread to read or write data from the map, while we adding
		// a new movie,
		// since it's can change the conditions of the "if" after checking, and we will
		// get wrong result(ERROR or SUCCSESS).

		synchronized (sharedProtocolData.getData("Movies")) {
			ConcurrentHashMap<String, DataObject> users = sharedProtocolData.getData("Service_Clients");
			BlockBusterService_User user = (BlockBusterService_User) users.get(getUserNameLoggedIn());
			ConcurrentHashMap<String, DataObject> movies = sharedProtocolData.getData("Movies");

			String movieName = parameters[0];
			Integer amount = Integer.parseInt(parameters[1]);
			Integer price = Integer.parseInt(parameters[2]);

			if (!user.getType().equals("admin") || movies.get(parameters[0]) != null || amount <= 0 || price <= 0)
				ERROR_REQUESTFailed("ERROR request addmovie failed");
			else {

				LinkedList<String> bannedCountries = new LinkedList<String>();
				for (int i = 3; i < parameters.length; i++)
					bannedCountries.add(parameters[i]);

				BlockBusterService_Movie movie = new BlockBusterService_Movie(movieName, amount, price,
						bannedCountries);
				movie.setId(highestMovieID(movies));
				movies.putIfAbsent(movieName, movie);
				((BlockBusterService_SharedData) sharedProtocolData).updateMoviesJsonFile();
				ACK_REQUESTSucceeded("ACK addmovie " + movieName + " success");
				BROADCAST(
						"BROADCAST movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + movie.getPrice());

			}
		}
	}

	/*
	 * This method returns an ID to set to a new movie while calculating the current
	 * highest ID.
	 */
	private Integer highestMovieID(ConcurrentHashMap<String, DataObject> movies) {

		Integer output = 0;
		for (Entry<String, DataObject> entry : movies.entrySet()) {
			BlockBusterService_Movie movie = (BlockBusterService_Movie) entry.getValue();
			if (movie.getId() > output)
				output = movie.getId();
		}
		return output + 1;
	}

	/*
	 * This function removes a movie if the conditions fit , means remove it from
	 * the movies map.
	 */
	private void remMovieRequest(String[] parameters) {
		// We explain our synchronized as always:
		// We don't want any thread to read or write data from the map, while we
		// removing a movie,since it's can change the conditions of the "if" after
		// checking, and we will
		// get wrong result(ERROR or SUCCSESS).

		synchronized (sharedProtocolData.getData("Movies")) {
			ConcurrentHashMap<String, DataObject> users = sharedProtocolData.getData("Service_Clients");
			BlockBusterService_User user = (BlockBusterService_User) users.get(getUserNameLoggedIn());
			ConcurrentHashMap<String, DataObject> movies = sharedProtocolData.getData("Movies");
			String movieName = parameters[0];
			BlockBusterService_Movie movie = (BlockBusterService_Movie) movies.get(movieName);

			if (!user.getType().equals("admin") || movie == null
					|| movie.getTotalAmount().get() != movie.getAvailableAmount().get())// checks that movie is not
																						// rented.
				ERROR_REQUESTFailed("ERROR request remmovie failed");
			else {
				movies.remove(movieName);
				((BlockBusterService_SharedData) sharedProtocolData).updateMoviesJsonFile();
				ACK_REQUESTSucceeded("ACK remmovie " + movieName + " success");
				BROADCAST("BROADCAST movie \"" + movieName + "\" removed");
			}
		}
	}

	/*
	 * This function changes the price of a movie if the conditions fit , means get
	 * the specific movie from the map and change his field.
	 */
	private void changePriceRequest(String[] parameters) {
		// We explain our synchronized as always:
		// We don't want any thread to read or write data from the map, while we
		// change a movie price ,since it's can change the conditions of the "if" after
		// checking, and we will get wrong result(ERROR or SUCCSESS).

		synchronized (sharedProtocolData.getData("Movies")) {
			ConcurrentHashMap<String, DataObject> users = sharedProtocolData.getData("Service_Clients");
			BlockBusterService_User user = (BlockBusterService_User) users.get(getUserNameLoggedIn());
			ConcurrentHashMap<String, DataObject> movies = sharedProtocolData.getData("Movies");

			// We assign the parameters in their order as described in the assignment page.
			String movieName = parameters[0];
			Integer newPrice = Integer.parseInt(parameters[1]);
			BlockBusterService_Movie movie = (BlockBusterService_Movie) movies.get(movieName);

			if (!user.getType().equals("admin") || movie == null || newPrice <= 0)
				ERROR_REQUESTFailed("ERROR request changeprice failed");
			else {
				movie.setPrice(newPrice);
				((BlockBusterService_SharedData) sharedProtocolData).updateMoviesJsonFile();
				ACK_REQUESTSucceeded("ACK changeprice " + movieName + " success");
				BROADCAST("BROADCAST movie \"" + movieName + "\" " + movie.getAvailableAmount() + " " + newPrice);
			}
		}
	}
}
