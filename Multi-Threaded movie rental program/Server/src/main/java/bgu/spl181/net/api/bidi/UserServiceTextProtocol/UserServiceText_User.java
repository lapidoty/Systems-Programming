package bgu.spl181.net.api.bidi.UserServiceTextProtocol;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import bgu.spl181.net.api.bidi.DataObject;

public class UserServiceText_User extends DataObject {
	/*
	 * This class describes a USTP Service Client.
	 */
	@Expose
	@SerializedName("password")
	protected String password;
	@Expose
	@SerializedName("username")
	protected String username;
	protected String[] dataBlocks;
	protected int LoggedInClient = -1;

	public UserServiceText_User() {
		super();
		this.dataBlocks = new String[10];
		this.LoggedInClient = -1;
	}

	public UserServiceText_User(String password, String username, String[] dataBlocks) {
		super();
		this.password = password;
		this.username = username;
		this.dataBlocks = dataBlocks;

	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getLoggedIn() {
		return LoggedInClient;
	}

	public void setLoggedIn(Integer loggedIn) {
		LoggedInClient = loggedIn;
	}

}
