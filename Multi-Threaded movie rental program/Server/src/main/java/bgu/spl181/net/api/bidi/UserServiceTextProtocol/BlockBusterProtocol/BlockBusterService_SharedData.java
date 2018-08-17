package bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;

import bgu.spl181.net.api.bidi.DataObject;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.UserServiceText_SharedData;

public class BlockBusterService_SharedData extends UserServiceText_SharedData {
	/*
	 * This class defines the BlockBuster Service shared data.
	 */
	protected ConcurrentHashMap<String, DataObject> Movies; // map that holds all the register usernames
	protected ConcurrentHashMap<String, DataObject> Service_Clients; // map that holds all the register usernames
	private Object lockUsers;
	private Object lockMovies;

	public BlockBusterService_SharedData() {
		super();
		this.Movies = new ConcurrentHashMap<String, DataObject>(); // map that holds all the register usernames
		this.Service_Clients = new ConcurrentHashMap<String, DataObject>();
		this.lockUsers = new Object();
		this.lockMovies = new Object();

	}

	@Override
	public void addData(String map, String key, DataObject value) {
		if (map == "Movies")
			Movies.putIfAbsent(key, (BlockBusterService_Movie) value);

		if (map == "Service_Clients") {
			Service_Clients.putIfAbsent(key, (BlockBusterService_User) value);

		}

	}

	@Override
	public ConcurrentHashMap<String, DataObject> getData(String map) {
		if (map == "Movies")
			return Movies;

		if (map == "Service_Clients")
			return Service_Clients;

		if (map == "Registerd_Users")
			return Registerd_Users;

		return null;
	}

	/*
	 * This function updates the users Json file, read the users info from the map,
	 * and then write it to the Json file, using a new serialize class we made to
	 * fit the requirements.
	 *
	 */
	protected void updateUsersJsonFile() {
		synchronized (lockUsers) {
			LinkedList<BlockBusterService_User> users = new LinkedList<BlockBusterService_User>();
			for (Entry<String, DataObject> item : Service_Clients.entrySet()) {
				BlockBusterService_User user = (BlockBusterService_User) item.getValue();
				if (user.getType().contains("admin")) {
					users.addFirst(user);

				} else
					users.addLast(user);

			}

			try {
				Serializer serializer = new Serializer();
				serializer.setIfUser(true);
				Gson gson = new GsonBuilder().disableHtmlEscaping()
						.registerTypeHierarchyAdapter(DataObject.class, serializer)
						.excludeFieldsWithoutExposeAnnotation().create();
				JsonWriter writer = new JsonWriter(new FileWriter("Database" + File.separator + "Users.json"));

				writer.beginObject().name("users");
				writer.beginArray();
				for (BlockBusterService_User blockBusterService_User : users) {

					gson.toJson(blockBusterService_User, blockBusterService_User.getClass(), writer);
				}
				writer.endArray();
				writer.endObject();
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * This function updates the movies Json file, read the movies info from the
	 * map, and then write it to the Json file, using a new serialize class we made
	 * to fit the requirements.
	 *
	 */
	protected void updateMoviesJsonFile() {
		synchronized (lockMovies) {
			LinkedList<BlockBusterService_Movie> movies = new LinkedList<BlockBusterService_Movie>();
			for (Entry<String, DataObject> item : Movies.entrySet()) {
				BlockBusterService_Movie movie = (BlockBusterService_Movie) item.getValue();
				movies.add(movie);

			}

			try {
				Serializer serializer = new Serializer();
				Gson gson = new GsonBuilder().disableHtmlEscaping()
						.registerTypeHierarchyAdapter(DataObject.class, serializer)
						.excludeFieldsWithoutExposeAnnotation().create();
				JsonWriter writer = new JsonWriter(new FileWriter("Database" + File.separator + "Movies.json"));

				writer.beginObject().name("movies");
				writer.beginArray();
				for (BlockBusterService_Movie blockBusterService_Movie : movies) {

					gson.toJson(blockBusterService_Movie, blockBusterService_Movie.getClass(), writer);
				}
				writer.endArray();
				writer.endObject();
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * This function parses the input users Json file, as well the movies json file,
	 * and restart the maps accordingly.
	 *
	 */
	public void Parse() {

		Gson gson = new GsonBuilder().create();
		JsonParser parser = new JsonParser();
		JsonObject jsonObjectUsers = new JsonObject();
		JsonObject jsonObjectMovies = new JsonObject();
		try {
			jsonObjectMovies = (JsonObject) parser.parse(new FileReader("Database" + File.separator + "Movies.json"));

			JsonArray Array = (JsonArray) jsonObjectMovies.getAsJsonArray("movies");
			for (int i = 0; i < Array.size(); i++) {
				BlockBusterService_Movie movie = gson.fromJson(Array.get(i), BlockBusterService_Movie.class);
				Movies.put(movie.getName(), movie);
			}

			jsonObjectUsers = (JsonObject) parser.parse(new FileReader("Database" + File.separator + "Users.json"));

			JsonArray ArrayUsers = (JsonArray) jsonObjectUsers.getAsJsonArray("users");
			for (int i = 0; i < ArrayUsers.size(); i++) {
				BlockBusterService_User user = gson.fromJson(ArrayUsers.get(i), BlockBusterService_User.class);
				Service_Clients.put(user.getUsername(), user);
				Registerd_Users.put(user.getUsername(), user);
			}

			// Restart the correct movie reference inside the service client arrays
			for (Entry<String, DataObject> item : Service_Clients.entrySet()) {
				BlockBusterService_User user = (BlockBusterService_User) item.getValue();
				LinkedList<BlockBusterService_Movie> movies = user.getMovies();
				LinkedList<BlockBusterService_Movie> toReplace = new LinkedList<>();
				for (int i = 0; i < movies.size(); i++) {
					toReplace.add((BlockBusterService_Movie) Movies.get(movies.get(i).getName()));
				}
				user.setMovies(toReplace);

			}
		} catch (Exception e) {
			System.out.println("File not found");
		}



	}

	/*
	 * New serialize class we made to fit the requirements of the writing in the files.
	 *
	 */
	public class Serializer implements JsonSerializer<DataObject> {

		private boolean isUserSerialized;

		@Override
		public JsonElement serialize(DataObject src, java.lang.reflect.Type typeOfSrc,
									 JsonSerializationContext context) {
			JsonObject jObj = new JsonObject();
			if (isUserSerialized && src instanceof BlockBusterService_Movie) {
				jObj.addProperty("id", String.valueOf(((BlockBusterService_Movie) src).getId()));
				jObj.addProperty("name", ((BlockBusterService_Movie) src).getName());

			} else if (isUserSerialized && src instanceof BlockBusterService_User) {
				Serializer serializer = new Serializer();
				serializer.setIfUser(true);
				Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(DataObject.class, serializer)
						.excludeFieldsWithoutExposeAnnotation().create();
				jObj = new JsonObject();

				jObj.addProperty("username", ((BlockBusterService_User) src).getUsername());
				jObj.addProperty("type", ((BlockBusterService_User) src).getType());
				jObj.addProperty("password", ((BlockBusterService_User) src).getPassword());
				jObj.addProperty("country", ((BlockBusterService_User) src).getCountry());
				JsonArray movies = new JsonArray();
				for (BlockBusterService_Movie blockBusterService_Movie : ((BlockBusterService_User) src).getMovies()) {
					JsonObject tmp = new JsonObject();
					tmp.addProperty("id", String.valueOf(blockBusterService_Movie.getId()));
					tmp.addProperty("name", blockBusterService_Movie.getName());
					movies.add(tmp);
				}

				jObj.add("movies", movies);
				jObj.addProperty("balance", String.valueOf(((BlockBusterService_User) src).getBalance()));
			} else if (src instanceof BlockBusterService_Movie) {
				Serializer serializer = new Serializer();
				serializer.setIfUser(true);
				Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(DataObject.class, serializer)
						.excludeFieldsWithoutExposeAnnotation().create();
				jObj = new JsonObject();

				jObj.addProperty("id", String.valueOf(((BlockBusterService_Movie) src).getId()));
				jObj.addProperty("name", ((BlockBusterService_Movie) src).getName());
				jObj.addProperty("price", String.valueOf(((BlockBusterService_Movie) src).getPrice()));

				JsonArray bannedCountries = new JsonArray();
				for (String s : ((BlockBusterService_Movie) src).getBannedCountries()) {
					bannedCountries.add(s);
				}

				jObj.add("bannedCountries", bannedCountries);
				jObj.addProperty("availableAmount",
						String.valueOf(((BlockBusterService_Movie) src).getAvailableAmount()));
				jObj.addProperty("totalAmount", String.valueOf(((BlockBusterService_Movie) src).getTotalAmount()));
			}

			return jObj;
		}

		public void setIfUser(boolean bool) {
			this.isUserSerialized = bool;
		}

	}
}
