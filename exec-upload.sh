#ssh root@37.187.153.76 -p 10422

#echo "Compiling project"
#mvn clean install -Pprod -DskipTests=true

echo "Uploading war"
scp -P10822 ./biller-web/target/*.war  root@37.187.153.76:/tmp/

