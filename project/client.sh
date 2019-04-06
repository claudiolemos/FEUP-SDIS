# javac *.java

if [ $2 = "BACKUP" ]
  then
      java Client localhost/Peer$1 BACKUP $3 $4
elif [ $2 = "RESTORE" ]
  then
      java Client localhost/Peer$1 RESTORE $3
elif [ $2 = "DELETE" ]
  then
      java Client localhost/Peer$1 DELETE $3
elif [ $2 = "RECLAIM" ]
  then
      java Client localhost/Peer$1 RECLAIM $3
elif [ $2 = "STATE" ]
  then
      java Client localhost/Peer$1 STATE
fi
