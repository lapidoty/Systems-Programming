package bgu.spl181.net.api.bidi;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl181.net.srv.bidi.ConnectionHandler;

public class Connections_Implentation<T> implements Connections<T> {

	ConcurrentHashMap<Integer, ConnectionHandler<T>> map;

	public Connections_Implentation() {
		super();
		this.map = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
	}

	/*
	 * This function searches for the client handler, and if finds- send him the
	 * message. Return true if finds it.
	 */
	@Override
	public boolean send(int connectionId, T msg) {
		if (!map.containsKey(connectionId))
			return false;

		else
			map.get(connectionId).send(msg);
		return true;
	}

	/*
	 * This function sends a message to all active clients, by iterate all he
	 * connection handlers on the map.
	 */
	@Override
	public void broadcast(T msg) {
		for (Entry<Integer, ConnectionHandler<T>> item : map.entrySet()) {
			ConnectionHandler<T> connectionHandler = item.getValue();
			connectionHandler.send(msg);
		}

	}

	/*
	 * This function removes from the active client map the client by his id number.
	 */
	@Override
	public void disconnect(int connectionId) {
		map.remove(connectionId);

	}

	/*
	 * This function adds to the map an active client by his id number and handler.
	 */
	public void addConnection(int id, ConnectionHandler<T> handle) {
		map.putIfAbsent(id, handle);
	}

}
