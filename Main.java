import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // ensure valid command line args
        if (args.length != 2) {
            throw new RuntimeException("Usage: <Server IP> <Server Port>");
        }

        // resolve port number and initialize server address
        InetSocketAddress serverAddress;
        int serverPort;
        try {
            serverPort = Integer.parseInt(args[1]);
            serverAddress = new InetSocketAddress(InetAddress.getByName(args[0]), serverPort);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("ERROR: Invalid port number");
        } catch (UnknownHostException ex) {
            throw new RuntimeException("ERROR: Unknown Host");
        }

        // initialize and run server
        Server server = new Server(serverAddress, "topology.txt");
        Thread serverThread = new Thread(server);
        serverThread.start();

        // initialize and run clients (routers)
        int clientPort = Utils.DEFAULT_PORT;
        String[] clientIDs = server.getNodes();
        Thread[] clientThreads = new Thread[server.getNumNodes()];
        for (int i = 0; i < server.getNumNodes(); i++) {
            Client client = new Client(clientIDs[i], serverAddress, clientPort);
            clientThreads[i] = new Thread(client);
            clientThreads[i].start();
            clientPort += 1;
        }

        // join server, client threads when network has stabilized
        for (Thread client : clientThreads) client.join();
        serverThread.join();
    }
}
