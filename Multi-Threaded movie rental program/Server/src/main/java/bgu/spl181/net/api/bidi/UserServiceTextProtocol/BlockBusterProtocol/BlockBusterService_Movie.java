package bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import bgu.spl181.net.api.bidi.DataObject;

public class BlockBusterService_Movie extends DataObject {
	/*
	 * This class describes a BlockBuster Service Movie.
	 */

	@Expose
	@SerializedName("id")
	Integer id;
	@Expose
	@SerializedName("name")
	String name;
	@Expose
	@SerializedName("price")
	Integer price;
	@Expose
	@SerializedName("bannedCountries")
	LinkedList<String> bannedCountries;
	@Expose
	@SerializedName("availableAmount")
	AtomicInteger availableAmount;
	@Expose
	@SerializedName("totalAmount")
	AtomicInteger totalAmount;

	public BlockBusterService_Movie(String name, Integer totalAmount, Integer price,
			LinkedList<String> bannedCountries) {
		super();
		this.name = name;
		this.price = price;
		this.bannedCountries = bannedCountries;
		this.totalAmount = new AtomicInteger(totalAmount);
		this.availableAmount = new AtomicInteger(totalAmount);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public LinkedList<String> getBannedCountries() {
		return bannedCountries;
	}

	public void setBannedCountries(LinkedList<String> bannedCountries) {
		this.bannedCountries = bannedCountries;
	}

	public AtomicInteger getAvailableAmount() {
		return availableAmount;
	}

	public AtomicInteger getTotalAmount() {
		return totalAmount;
	}

}
