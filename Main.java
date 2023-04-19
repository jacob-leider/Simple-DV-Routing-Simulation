import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Main class for this program. The main method:
 * 1. Creates a server which is initialized based on the network topology specified in "topology.txt"
 * 2. Runs the server on its own thread
 * 3. Creates a list of clients: one for each node specified in "topology.txt"
 * 4. Runs each client on it's own thread. Clients will execute the Bellman-Ford DV algorithm once all have joined
 * 5. When all nodes in the network have stabilized and terminated their threads, all threads join and the parent
 *    process terminates.
 */
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
            if (Integer.parseInt(args[1]) < 1024) {
                throw new RuntimeException("ERROR: Privilaged port number");
            } else {
                serverPort = Integer.parseInt(args[1]);
                serverAddress = new InetSocketAddress(InetAddress.getByName(args[0]), serverPort);
            }
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
