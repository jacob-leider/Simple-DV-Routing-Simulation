# Simple-DV-Routing-Simulation

A simple simulation of a distance-vector routing protocol implemented in java. This program is built to run on UNIX-like operating systems (linux, macOS, etc.).
This program simulates a network of routers whose topology is specified in the file "topology.txt". A router (called a "node" in the network) is initialized with 
a router ID and distances to other routers adjacent to it within the network. Through communication of adjacent nodes in the network, each node iteratively updates
distance estimates (in a distance vector (DV)) to all other routers in the network. When every node in the network has stable distance estimates for all other 
nodes in the network, each node's process terminates after yielding it's final distance vector.


****Clone From github:**** &nbsp;&nbsp; $ git clone https://github.com/jacob-leider/Simple-DV-Routing-Simulation.git


## Compilation

To compile the project once your working directory is the project directory, submit the following command into a UNIX shell:

****Main Application:**** &nbsp;&nbsp; $ javac Main


## Running

In any UNIX environment, run the program by specifying an IP address and port for the server to run on. 

****Main Application**** &nbsp;&nbsp; $ java Main <SERVER-IP> <SERVER-PORT>

For example, to run the server on a local host recipient to port 9999, enter

****Main Application**** &nbsp;&nbsp; $ java Main 127.0.0.1 9999



## Use

The network topology on which the DV algorithm is run is specified in the file "topology.txt". This file can be edited to accomadate a network topology of the user's
choosing, so long as it is formatted as follows: For each node (router) in the network, a line must be added to "topology.txt" with the node's router id followed by 
<id, distance> pairs to all other nodes in the network. For a network of N router's the line should be formatted as follows (whitespace/padding within the same line should 
be ignored during network topology construction):

[router id]:<[router 1 id], [distance estimate]>: ... <[last router id], [distance estimate]>:

All entries are separated by a colon ":". If nodes are not adjacent in the network, their starting distance should be NEGATIVE. Conversely, if a distance is specified as negative, it will be assumed that the nodes
are node adjacent. The distance from node to itself is specified as ZERO. Line comments are acceptable and will be ignored during network topology construction. A line
comment begins with "//".



## Command Line Arguments

****SERVER-PORT:**** Run the server on a port greater than or equal to 1024, as ports 0-1023 are "privilaged" in the sense that they are reserved for common TCP/IP applications. If 
the port submitted is within the privilaged range, an error will be thrown

****SERVER-IP:**** The server's IP address should be specified in standard dotted-decimal format "xxx.xxx.xxx.xxx".








