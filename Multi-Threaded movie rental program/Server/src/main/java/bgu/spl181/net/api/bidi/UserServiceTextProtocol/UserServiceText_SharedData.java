package bgu.spl181.net.api.bidi.UserServiceTextProtocol;

import java.util.concurrent.ConcurrentHashMap;

import bgu.spl181.net.api.bidi.DataObject;
import bgu.spl181.net.api.bidi.SharedProtocolData;

public class UserServiceText_SharedData extends SharedProtocolData<String> {
	/*
	 * This class defines the User Service Text shared data.
	 */
	protected ConcurrentHashMap<String, DataObject> Registerd_Users; // map that holds all the register usernames

	public UserServiceText_SharedData() {
		super();
		Registerd_Users = new ConcurrentHashMap<String, DataObject>();

	}

	@Override
	public void addData(String map, String key, DataObject value) {
		if (map == "Registerd_Users")
			Registerd_Users.putIfAbsent(key, value);

	}

	@Override
	public ConcurrentHashMap<String, DataObject> getData(String map) {
		return Registerd_Users;
	}

}
