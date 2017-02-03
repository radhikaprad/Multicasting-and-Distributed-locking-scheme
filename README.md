# Multicasting and Distributed locking scheme

The distributed system uses logical clock to timestamp messages sent/received between nodes. To start the distributed
system, each node synchronizes their logical clocks to the same initial value, based onwhich the ordering of events can be determined among the machines. Berkeley Algorithm is used to obtain an initial agreement on the logical clocks. Totally ordered multicasting is implemented using either Lamport’s algorithm.

# Totally Ordered Multicasting

Created two threads for each process, one for sending the multicast message to other nodes and one for listening to its
communication port. 
