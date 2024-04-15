#!/bin/sh
cp /ssefs/server.crt .
keytool -importcert -trustcacerts -file server.crt -keystore /opt/java/openjdk/lib/security/cacerts -storepass \'$KS_PW\' -noprompt -alias server
java -jar /app.jar