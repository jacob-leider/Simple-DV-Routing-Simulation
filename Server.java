import org.w3c.dom.Node;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public class Server implements Runnable {
    private final DatagramSocket socket;                              // the socket for this server
    private final HashMap<String, Utils.Node> distanceTable;          // stores node info for each router ID


    // constructor: creates a server bound to the specified address, representing the
    // network topology specified in the filename "netTopologyFP"
    public Server(InetSocketAddress serverAddress, String netTopologyFP) {
        try {
            this.socket = new DatagramSocket(serverAddress);
            this.distanceTable = new HashMap<>();
            initializeDistanceTable(netTopologyFP);
        } catch (SocketException ex) {
            throw new RuntimeException("ERROR: Failed to initialize server: Failed to initialize a socket");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("ERROR: Failed to initialize server: Invalid Network Topology Filepath");
        }
    }


    @Override
    public void run() {
        byte[] buff = new byte[Utils.MAXLINE], responseBuff;
        DatagramPacket outgoingPacket, incomingPacket = new DatagramPacket(buff, Utils.MAXLINE);
        String[] data;
        String routerID, msgType;
        int activeNodes = 0;

        // wait for all clients to join the network
        while (activeNodes < getNumNodes()) {
            try {
                socket.receive(incomingPacket);
                SocketAddress address = incomingPacket.getSocketAddress();
                data = new String(incomingPacket.getData(), StandardCharsets.UTF_8).split(":", 3);
                msgType = data[0];
                routerID = data[1];
                if (msgType.equals("JOIN")) {
                    if (!distanceTable.get(routerID).isActive) {
                        distanceTable.get(routerID).isActive = true;
                        distanceTable.get(routerID).address = address;
                        activeNodes++;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Server socket failure");
            }
        }

        // assign a distance vector to each client (router)
        for (String id : distanceTable.keySet()) {
            try {
                responseBuff = Utils.buildDV(id, distanceTable.get(id).dv).getBytes();
                outgoingPacket = new DatagramPacket(responseBuff, responseBuff.length, distanceTable.get(id).address);
                socket.send(outgoingPacket);
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Server socket failure");
            }
        }

        // main loop: facilitate Bellman-Ford distance vector algorithm
        do {
            try {
                socket.receive(incomingPacket);
                data = new String(incomingPacket.getData(), StandardCharsets.UTF_8).split(":", 3);
                msgType = data[0];
                routerID = data[1];
                switch (msgType) {
                    case "UPDATE" -> {
                        for (String id : distanceTable.get(routerID).neighbors) {
                            incomingPacket.setSocketAddress(distanceTable.get(id).address);
                            socket.send(incomingPacket);
                        }
                    }
                    case "EXIT" -> activeNodes--;
                }
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Server socket failure");
            }
        } while (activeNodes > 0);
    }


    // return router ids of all routers that have joined the network
    String[] getNodes() {
        return distanceTable.keySet().toArray(new String[0]);
    }


    // return the number of nodes in the network
    int getNumNodes() {
        return distanceTable.keySet().size();
    }


    // initializes the network topology from the file given by "filepath"
    public void initializeDistanceTable(String filepath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filepath));
        String line, id, srcID;
        String[] pair, entries, entry;
        int dist;

        while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();
            if (!line.startsWith("//")) {   // if line is not a comment
                Utils.Node srcInfo = new Utils.Node(new HashMap<String, Integer>());
                srcInfo.neighbors = new ArrayList<>();
                pair = line.split(":", 2);
                srcID = pair[0];

                // add distance vector
                entries = pair[1].split(":");
                for (String chunk : entries) {
                    entry = chunk.replaceAll("[<>]", " ")
                            .trim()
                            .split(",");
                    id = entry[0];
                    try {
                        dist = Integer.parseInt(entry[1]);
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("ERROR: Bad format in initialization file");
                    }
                    if (dist > 0) { // if distance is finite and nonzero, this is a neighboring node
                        srcInfo.neighbors.add(id);
                    }
                    srcInfo.dv.put(id, dist);
                }
                distanceTable.put(srcID, srcInfo);  // point node id to node info
            }
        }
    }
}
