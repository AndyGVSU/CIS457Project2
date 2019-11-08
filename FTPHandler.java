public class FTPHandler {
	private FTPClient client;

	public FTPHandler() {
		client = new FTPClient();
		FTPServer server = new FTPServer();

		//run client/server in a thread for GUI
		client.start();
		server.start();
	}

    public FTPClient getClient() {
		return client;
    }
    
}
