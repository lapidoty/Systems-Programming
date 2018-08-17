package bgu.spl181.net.api.bidi;

public abstract class BidiMessagingProtocol_Implentation<T> implements BidiMessagingProtocol<T> {
	/*
	 * This class is the first layer of any protocol in our server.
	 */
	protected volatile Connections<T> connections; // For transfer messages
	protected volatile int connectionId; // Client id
	protected volatile boolean shouldTerminate = false; // Indicates when to end the connection
	protected volatile SharedProtocolData<T> sharedProtocolData; // Holds the common data of the related protocols

	public void start(int connectionId, Connections<T> connections) {
		this.connectionId = connectionId;
		this.connections = connections;
	}

	public abstract void process(T message);

	@Override
	public boolean shouldTerminate() {
		return shouldTerminate;
	}

	@Override
	public void setSharedProtocolData(SharedProtocolData<T> sharedProtocolData) {
		this.sharedProtocolData = sharedProtocolData;

	}
}
