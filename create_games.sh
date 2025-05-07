#!/bin/bash
for i in {1..20}
do
  echo "Creating game $i"
  curl -X POST -H "Content-Type: application/json" -d "{\"name\":\"Test Game $i\",\"platform\":\"PC\",\"genre\":\"Action\",\"releasedYear\":2023}"  http://localhost:8080/games
  echo -e "\n"
  sleep 0.5
done
