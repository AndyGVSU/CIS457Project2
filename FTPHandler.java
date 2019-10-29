public class FTPHandler {

	public static void main() {
		FTPClient client = new FTPClient();
		FTPServer server = new FTPServer();

		Thread clientThread = new Thread(client);
		Thread serverThread = new Thread(server);

		//run client/server in a thread for GUI
		clientThread.run();
		serverThread.run();
	}
}
