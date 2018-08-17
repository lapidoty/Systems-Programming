#include <stdlib.h>
#include <connectionHandler.h>
#include <boost/thread.hpp>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
using namespace std;

using boost::asio::ip::tcp;

int main(int argc, char *argv[]) {
	if (argc < 3) {
		std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
		return -1;
	}
	std::string host = argv[1];
	short port = atoi(argv[2]);
	std::condition_variable cv;
	ConnectionHandler connectionHandler(host, port);

	if (!connectionHandler.connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	boost::thread runner(boost::bind(&ConnectionHandler::ReadFromSocket, &connectionHandler));
	while (!connectionHandler.Terminate()) {
		const short bufsize = 1024;
		char buf[bufsize];

		std::cin.getline(buf, bufsize);
		std::string line(buf);
		if (!connectionHandler.sendLine(line)) {
			break;
		}

	}
	runner.join();
	return 0;
}




