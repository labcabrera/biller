#ssh root@37.187.153.76 -p 10422

#echo "Compiling project"
#mvn clean install -Pprod -DskipTests=true

echo "Uploading war"
scp -P10422 ./biller-web/target/*.war  root@37.187.153.76:/home/tomcat7/biller.war



