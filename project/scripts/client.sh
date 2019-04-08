# cd ..
# javac **/*.java

if [ $2 = "BACKUP" ]
  then
      java connection.Client localhost/$1 BACKUP $3 $4
elif [ $2 = "RESTORE" ]
  then
      java connection.Client localhost/$1 RESTORE $3
elif [ $2 = "DELETE" ]
  then
      java connection.Client localhost/$1 DELETE $3
elif [ $2 = "RECLAIM" ]
  then
      java connection.Client localhost/$1 RECLAIM $3
elif [ $2 = "STATE" ]
  then
      java connection.Client localhost/$1 STATE
fi
