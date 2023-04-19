import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Client implements Runnable {
    private final DatagramSocket socket;                                              // this client's socket
    private final HashMap<String, Integer> distanceVector = new HashMap<>();          // distance vector for this node
    private final SocketAddress serverAddress;                                        // the server's Inet address
    private  String id;                                                               // represents a router ID

    /**
     * Create a client bound to the specified port, recipient to the specified server
     * @param routerID a string representation of this simulated router's ID
     * @param serverAddress the server's address specified in the command line
     * @param the port this client runs on. Clients are bound to the host on which they are run
     */
    public Client(String routerID, SocketAddress serverAddress, int clientPort) {
        try {
            this.id = routerID;
            this.socket = new DatagramSocket(new InetSocketAddress(clientPort));
            this.serverAddress = serverAddress;
            socket.setSoTimeout(500);
        } catch (SocketException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Socket Exception: Failed to create client.");
        }
    }

    /**
     * Runs this client once initialized.
     * 1. Attempts to join the server.
     * 2. Initializes this node's distance vector
     * 3. Runs the Bellman-Ford DV algorithm until all distance estimates are stable
     * 4. Prints this nodes finalized distance vector
     * 5. Terminates
     */
    @Override
    public void run() {
        DatagramPacket incomingPacket = new DatagramPacket(new byte[Utils.MAXLINE], Utils.MAXLINE);
        String data;
        int cyclesSinceUpdate = 0;

        try {
            sendJoinRequest();
            socket.receive(incomingPacket);
            data = new String(incomingPacket.getData());
            initDV(data);
            sendUpdateRequest();
        } catch (IOException e) {
            throw new RuntimeException("ERROR: Client node initialization failed");
        }
        do {
            try {
                incomingPacket.setData(new byte[Utils.MAXLINE]);
                socket.receive(incomingPacket);
                data = new String(incomingPacket.getData());
                cyclesSinceUpdate++;
                if (updateDV(data)) {            // if the distance vector was updated, forward changes to server
                    sendUpdateRequest();
                    cyclesSinceUpdate = 0;
                }
            } catch (SocketTimeoutException ex) {
                cyclesSinceUpdate++;            // increment number of iterations since any distances changed
            } catch (IOException ex) {
                throw new RuntimeException("ERROR: Socket failure in client [" + id + "]");
            }
        } while (cyclesSinceUpdate <= 5);
        try {
            sendExitRequest();
        } catch (IOException e) {
            throw new RuntimeException("ERROR: Socket exception in client [" + id + "]");
        }

        System.out.println("NODE [" + this.id + "]: distance vector: " + distanceVector);
    }

    /**
     * Sends an EXIT request to the server
     * @throws IOException
     */
    private void sendExitRequest() throws IOException {
        String dv = "EXIT:" + id + ":";
        byte[] responseBuff = dv.getBytes();
        DatagramPacket outgoingPacket = new DatagramPacket(responseBuff, responseBuff.length, serverAddress);
        socket.send(outgoingPacket);
    }

    /**
     * Sends a JOIN request to the server
     * @throws IOException
     */
    private void sendJoinRequest() throws IOException {
        String dv = "JOIN:" + id + ":";
        byte[] responseBuff = dv.getBytes();
        DatagramPacket outgoingPacket = new DatagramPacket(responseBuff, responseBuff.length, serverAddress);
        socket.send(outgoingPacket);
    }

    /**
     * Sends an UPDATE request to the server with the most recent values stored in this
     * node's distance vector
     * @throws IOException
     */
    private void sendUpdateRequest() throws IOException {
        String dv = "UPDATE:" + Utils.buildDV(id, distanceVector);
        byte[] responseBuff = dv.getBytes();
        DatagramPacket outgoingPacket = new DatagramPacket(responseBuff, responseBuff.length, serverAddress);
        socket.send(outgoingPacket);
    }


    /**
     * Initializes this node's distance vector given a set of initial distance estimates.
     * This method is called once at initialization, and never thereafter
     * @param data the server's response to a node's JOIN request
     */
    public void initDV(String data) {
        // compare and update. If anything is changed, forward to server.
        String[] pair, entries, entry;
        String id;
        pair = data.trim().split(":", 2);
        this.id = pair[0];
        entries = pair[1].split(":");
        for (String chunk : entries) {
            entry = chunk.replaceAll("[<>]", " ").trim().split(",");
            id = entry[0];
            int dist = Integer.parseInt(entry[1]);
            distanceVector.put(id, dist);
        }
    }

    /**
     * updates this node's distance vector upon receiving an update from an adjacent node (through the server)
     * @param data an adjacent node's updated distance vector
     * @return true if this node's distance vector was updated; false if all distances remained the same
     */
    public boolean updateDV(String data) {
        // compare and update. If anything is changed, forward to server.
        String[] triplet, entries, entry;
        int oldDist, newDist, distToTgt;
        boolean updated = false;
        String tgtID, id;
        triplet = data.trim().split(":", 3);
        tgtID = triplet[1];
        distToTgt = distanceVector.get(tgtID);
        entries = triplet[2].split(":");
        for (String chunk : entries) {
            entry = chunk.replaceAll("[<>]", " ").trim().split(",");
            id = entry[0];
            try {
                int dist = Integer.parseInt(entry[1]);
                oldDist = distanceVector.get(id);
                newDist = dist + distToTgt;
                if (dist > 0 && (newDist < oldDist || oldDist < 0)) {   // update distance estimate if Bellman-Ford condition is true
                    distanceVector.put(id, newDist);
                    updated = true;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("Exception in client [" + id + "]");
            }
        }
        return updated;
    }
}