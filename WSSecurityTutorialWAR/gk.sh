#!/bin/bash

find . -name '*.jks' | xargs rm

# Set the values we'll use for the generation
serverkeyalias=myservicekey
serverkeypassword=skpass
serverstorepassword=sspass
serverkeystorename=src/main/springconfig/local/serviceKeystore.jks

clientkeyalias=myclientkey
clientkeypassword=ckpass
clientstorepassword=cspass
clientkeystorename=src/test/resources/clientKeystore.jks

# Generate the server and client keys
keytool -genkey -alias $serverkeyalias -keyalg RSA -sigalg SHA1withRSA -keypass $serverkeypassword -storepass $serverstorepassword -keystore $serverkeystorename -dname "cn=localhost" -validity 3650
keytool -genkey -alias $clientkeyalias -keyalg RSA -sigalg SHA1withRSA -keypass $clientkeypassword -storepass $clientstorepassword -keystore $clientkeystorename -dname "cn=clientuser" -validity 3650

# Export the client key and import it to the server keystore
keytool -export -rfc -keystore $clientkeystorename -storepass $clientstorepassword -alias $clientkeyalias -file $clientkeyalias.cer
keytool -import -trustcacerts -keystore $serverkeystorename -storepass $serverstorepassword -alias $clientkeyalias -file $clientkeyalias.cer -noprompt
rm $clientkeyalias.cer

# Export the server key and import it to the client keystore
keytool -export -rfc -keystore $serverkeystorename -storepass $serverstorepassword -alias $serverkeyalias -file $serverkeyalias.cer
keytool -import -trustcacerts -keystore $clientkeystorename -storepass $clientstorepassword -alias $serverkeyalias -file $serverkeyalias.cer -noprompt
rm $serverkeyalias.cer
