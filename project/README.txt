Open at least for terminals: one to start rmiregistry, one for the client and two for the peers.

In each terminal, change directory to the project folder and compile (javac **/*.java).

RMI

  To start the RMI, in one of the terminals execute the command rmiregistry

Peer

  To start a peer, run 'sh scripts/peer {id} {version}' where id is the number of the peer (in this example it would be 1 and 2) and version is the version of the protocol being used (for this project we can use version 1 and 2).

  So to run peer 1 with version 1.0, execute the commnand sh scripts/peer 1 1.

Client

  To send a message from the client you can execute the following commands:

  sh scripts/client 1 BACKUP test/tux.svg 1: peer 1 will backup test/tux.svg with replication degree of 1

  sh scripts/client 1 RESTORE test/tux.svg: peer 1 will restore test/tux.svg from the other peer

  sh scripts/client 1 DELETE test/tux.svg: peer 1 will delete test/tux.svg from the other peer

  sh scripts/client 2 RECLAIM 0: peer 2 will reclaim all the space being used

  sh scripts/client 2 STATE: peer 2 will show info about its files, backup chunks and storage
