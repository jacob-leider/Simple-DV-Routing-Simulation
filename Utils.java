import java.net.SocketAddress;
import java.util.*;

public class Utils {
    protected static final int MAXLINE = 1024;                      // buffer length for an arbitrary datagram
    protected static final int DEFAULT_PORT = 4060;                 // default port for the first client

    /**
     * contains information for a node:
     *      this node's distance vector,
     *      this client's Inet address,
     *      adjacent nodes' router IDs,
     *      whether this node has joined the network
     */

    protected static class Node {
        public Node(HashMap<String, Integer> dv) {
            this.dv = dv;
        }
        public HashMap<String, Integer> dv;
        public SocketAddress address;
        public List<String> neighbors;
        public boolean isActive;
    }

    /**
     * builds a string representation of a distance vector given a <router id, distance> hashmap and a router id
     * similar to the format specifications for "topology.txt"
     */

    protected static String buildDV(String id, HashMap<String, Integer> distanceVector) {
        StringBuilder t = new StringBuilder(id + ":");
        String entry;
        for (String name : distanceVector.keySet()) {
            entry = "<" + name + "," + distanceVector.get(name) + ">";
            t.append(entry);
            t.append(':');
        }
        return t.toString();
    }
}
