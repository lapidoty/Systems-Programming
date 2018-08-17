package bgu.spl181.net.impl.BBreactor;

import bgu.spl181.net.api.MessageEncoderDecoder_Implentation;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol.BlockBusterService_MessagingProtocol;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol.BlockBusterService_SharedData;
import bgu.spl181.net.srv.bidi.BidiServer;

public class ReactorMain {

	public static void main(String[] args) {
		BlockBusterService_SharedData sharedProtocolData = new BlockBusterService_SharedData();
		sharedProtocolData.Parse();
		BidiServer.reactor(8, Integer.valueOf(args[0]),
				() -> new BlockBusterService_MessagingProtocol(sharedProtocolData),
				() -> new MessageEncoderDecoder_Implentation()).serve();

	}

}
