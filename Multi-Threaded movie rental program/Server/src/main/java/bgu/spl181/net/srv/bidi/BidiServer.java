package bgu.spl181.net.srv.bidi;

import bgu.spl181.net.api.MessageEncoderDecoder;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections_Implentation;
import bgu.spl181.net.api.bidi.SharedProtocolData;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public interface BidiServer<T> extends Closeable {
    /**
     * The main loop of the server, Starts listening and handling new clients.
     */
    void serve();

    /**
     *This function returns a new instance of a thread per client pattern server
     * @param port The port for the server socket
     * @param supplier A factory that creats new MessagingProtocols
     * @param supplier2 A factory that creats new MessageEncoderDecoder
     * @param <T> The Message Object for the protocol
     * @return A new Thread per client server
     */
    public static <T> BidiServer<T>  threadPerClient(
            int port,
            Supplier<BidiMessagingProtocol<T>> supplier,
            Supplier<MessageEncoderDecoder<T>> supplier2) {
    	Connections_Implentation<T> connectionsImpl = new Connections_Implentation<T>();
    	AtomicInteger idCounter = new AtomicInteger(0);
		
        return new BidiBaseServer<T>(port, supplier, supplier2) {
            @Override
            protected void execute(BidiBlockingConnectionHandler<T>  handler) {
            	int id = idCounter.getAndIncrement(); //Generate new id for the handler
        		handler.setID(id); // sets the id
        		handler.setConnections(connectionsImpl); // set the connections at the handler
        		connectionsImpl.addConnection(id, handler); // adding the connection to the map
        		new Thread(handler).start(); // starts the thread loop
            }
        };

    }

    /**
     * This function returns a new instance of a reactor pattern server
     * @param nthreads Number of threads available for protocol processing
     * @param port The port for the server socket
     * @param protocolFactory A factory that creats new MessagingProtocols
     * @param encoderDecoderFactory A factory that creats new MessageEncoderDecoder
     * @param <T> The Message Object for the protocol
     * @return A new reactor server
     */
    public static <T> BidiServer<T> reactor(
            int nthreads,
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {
        return new BidiReactor<T>(nthreads, port, protocolFactory, encoderDecoderFactory);
    }



}
