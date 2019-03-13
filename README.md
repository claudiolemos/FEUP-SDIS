# FEUP - Sistemas Distribuidos 2018/2019

Change directory to the prefered lab folder and compile using `javac Server.java` and `javac Client.java`

## Lab 1 (UDP)

### Server
To open server, use command `java Server 4445`

### Client
To send client requests use `java Client localhost 4445 REGISTER 12-12-12 Claudio` and `java Client localhost 4445 LOOKUP 12-12-12`

## Lab 2 (UDP Multicast)

### Server
To open server, use command `java Server 8080 225.0.0 8000`

### Client
To send client requests use `java Client 225.0.0.0 8000 REGISTER 12-12-12 Claudio` and `java Client 225.0.0.0 8000 LOOKUP 12-12-12`

## Lab 3 (UDP Multicast)

### Server

### Client
