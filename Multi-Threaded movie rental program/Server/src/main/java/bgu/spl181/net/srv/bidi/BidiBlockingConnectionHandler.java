package bgu.spl181.net.srv.bidi;

import bgu.spl181.net.api.MessageEncoderDecoder;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.api.bidi.Connections_Implentation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BidiBlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

	private final BidiMessagingProtocol<T> protocol;
	private final MessageEncoderDecoder<T> encdec;
	private final Socket sock;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private volatile boolean connected = true;
	private volatile int ID; //added to identify what client defines this handler
	private volatile Connections<T> connections;// added to send to the bidi protocol

	public BidiBlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader,
			BidiMessagingProtocol<T> protocol) {
		this.sock = sock;
		this.encdec = reader;
		this.protocol = protocol;
	}

	@Override
	public void run() {
		try (Socket sock = this.sock) { // just for automatic closing
			int read;

			in = new BufferedInputStream(sock.getInputStream());
			out = new BufferedOutputStream(sock.getOutputStream());
			protocol.start(ID, connections);

			while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
				T nextMessage = encdec.decodeNextByte((byte) read);

				if (nextMessage != null) {

					protocol.process(nextMessage);

				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("connection closed");
	}

	@Override
	public void close() throws IOException {
		connected = false;
		sock.close();
	}
/*
 * This function sends a message to the client that connected to this handler
 * 
 */
	@Override
	public void send(T msg) {
		try {
			out.write(encdec.encode(msg));
			out.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public void setID(int id) {
		this.ID = id;

	}

	public void setConnections(Connections_Implentation<T> connectionsImpl) {
		this.connections = connectionsImpl;

	}

	public BidiMessagingProtocol<T> getProtocol() {
		return protocol;
	}
}
