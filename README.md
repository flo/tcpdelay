tcpdelay
========

A proxy server that can delay TCP packages between the real client and server

You can compile the source code with Maven by calling:

mvn package

Afterwards you can start the program via:

java -jar target/tcpdelay-1.0.jar targetAddress targetPort port delay

The first two arguments are the address and port of the real server. 
The third argument is the port at which the tcpdelay server will listen. 
All clients that connect to that port will get their tcp packages delayed.
The fourth argument is the amount of delay in milliseconds
