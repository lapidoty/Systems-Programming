package bgu.spl181.net.api.bidi;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SharedProtocolData<T> {
	/*
	 * This class is the first layer of any kind of shared data that the protocols
	 * have.
	 */

	public SharedProtocolData() {
		super();
	}

	public abstract void addData(T map, T key, DataObject value);

	public abstract ConcurrentHashMap<T, DataObject> getData(T map);

}
