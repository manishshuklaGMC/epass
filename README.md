Dependencies:

Redis
Kafka
Postres (database setup is given in setup.sql inside the project code)
Elasticsearch + logstash 


SSL certificate:
Generate the certificate using https://certbot.eff.org/lets-encrypt/ubuntubionic-apache
Keystore.p12


Generating Private public key pair for signature:

openssl dsaparam -genkey 2048 | openssl dsa -out privatekey
openssl dsa -in privatekey -pubout -outform DER -out public_key.der
openssl pkcs8 -topk8 -inform PEM -outform DER -in privatekey -out private_key.der -nocrypt


Running the code:

mvn -e spring-boot:run
or
mvn -Dmaven.test.skip=true package and then run java -jar target/ecurfew-1.0-SNAPSHOT.jar


Postman:https://www.getpostman.com/collections/70d9457dcb51b81deeae

Onboarding a new state:
https://{{host}}/addState
{
    "authToken":”tokne",
    "stateName":"GOA",
    "stateConfig":{"emailFromId":"no-reply@goagov.org",
 "helplineFooter":"In case of any queries, please contact 0832-2740178", "passTitleImageFileURL":"https://www.goavidhansabha.gov.in/images/header-banner.pn", "emailFromName":"Goa Home Department", "issuingAuthorityDisclaimer":"Issued under the authority of Additional Chief Secretary to Govt of Goa, Home Department, Goa"
}
}
Restart the service service




To add a new admin for a state use the api https://{{host}}/createAdminAccount given in postman collection




Contributers:
1. Manish Shukla
2. Mayank Natani
3. Vibhav Shrivastava

