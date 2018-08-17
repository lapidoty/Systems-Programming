package bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol;

import java.util.LinkedList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.UserServiceText_User;

public class BlockBusterService_User extends UserServiceText_User {
	/*
	 * This class describes a BlockBuster Service Client.
	 */

	@Expose
	@SerializedName("type")
	String type;
	@Expose
	@SerializedName("country")
	String country;
	@Expose
	@SerializedName("movies")
	protected LinkedList<BlockBusterService_Movie> movies;
	@Expose
	@SerializedName("balance")
	Integer balance;

	public BlockBusterService_User() {
		super();
		dataBlocks[0] = country;

	}

	public BlockBusterService_User(String username, String password, String country) {
		super(password, username, new String[1]);
		this.dataBlocks[0] = country;
		this.country = country;
		this.balance = 0;
		this.type = "normal";
		this.movies = new LinkedList<>();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.dataBlocks[0] = country;
	}

	public LinkedList<BlockBusterService_Movie> getMovies() {
		return movies;
	}

	public void setMovies(LinkedList<BlockBusterService_Movie> movies) {
		this.movies = movies;
	}

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

}
