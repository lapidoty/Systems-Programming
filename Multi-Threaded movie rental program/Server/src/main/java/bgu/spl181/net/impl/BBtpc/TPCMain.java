package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.api.MessageEncoderDecoder_Implentation;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol.BlockBusterService_MessagingProtocol;
import bgu.spl181.net.api.bidi.UserServiceTextProtocol.BlockBusterProtocol.BlockBusterService_SharedData;
import bgu.spl181.net.srv.bidi.BidiServer;

public class TPCMain {

	public static void main(String[] args) {
		BlockBusterService_SharedData sharedProtocolData = new BlockBusterService_SharedData();
		sharedProtocolData.Parse();
		BidiServer.threadPerClient(Integer.valueOf(args[0]),
				() -> new BlockBusterService_MessagingProtocol(sharedProtocolData),
				() -> new MessageEncoderDecoder_Implentation()).serve();

	}

}
