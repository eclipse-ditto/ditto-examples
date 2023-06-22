# Eclipse-Ditto-MQTT-iWatch-SSL
In this tutorial, we'll illustrate how to configure Ditto to securely update 'Things' via MQTT with SSL, forming a robust IoT system. For demonstration purposes, we'll create an 'iWatch' from a WoT TM (Web of Things Thing Model). Our Digital Twin will then be updated using synthetic data relayed through MQTT.

The crux of this configuration is its security, achieved through dual authentication mechanisms: `Username and Password`, and `Certificates`. The details of these mechanisms are as follows:

* `Username and Password`: This is the first layer of our dual authentication process. It involves the traditional method of using a known username and password to gain access.

* `Certificates`: This involves using two types of SSL certificates:

	* A `self-signed server certificate for the MQTT broker`: This certificate is used to establish a TLS encrypted connection with the MQTT broker. Since it is self-signed, a special configuration is needed to 'trust' this certificate, a process we will guide you through.
	* A `client certificate for Ditto`: This certificate authenticates the Ditto connection to the MQTT broker, ensuring a secure interaction between Ditto and the broker.
By using dual authentication mechanisms, we add an extra layer of security, enhancing the reliability of our IoT system. By the conclusion of this tutorial, you'll understand how these mechanisms work in harmony to facilitate secure updates to your Digital Twin via MQTT.

# Requirements
1. Clone Ditto: ```git clone https://github.com/eclipse-ditto/ditto.git```

2. Pull Mosquitto: ```docker pull eclipse-mosquitto```

3. Clone Eclipse-Ditto-MQTT-iWatch-SSL: ```git clone https://github.com/bernar0507/Eclipse-Ditto-MQTT-iWatch-SSL.git```

# Start Ditto and Mosquitto

### Ditto: 
```
cd ditto
```

```
git checkout tags/3.0.0
```

```
cd deployment/docker
```

```
docker compose up -d
```

### Mosquitto: 
```
docker run -it --name mosquitto --network docker_default -p 8883:8883 -v $(pwd)/mosquitto:/mosquitto/   eclipse-mosquitto   mosquitto -c /mosquitto/config/mosquitto.conf
```

# Create the Policy
```
curl -X PUT 'http://localhost:8080/api/2/policies/org.Iotp2c:policy' -u 'ditto:ditto' -H 'Content-Type: application/json' -d '{
    "entries": {
        "owner": {
            "subjects": {
                "nginx:ditto": {
                    "type": "nginx basic auth user"
                }
            },
            "resources": {
                "thing:/": {
                    "grant": [
                        "READ","WRITE"
                    ],
                    "revoke": []
                },
                "policy:/": {
                    "grant": [
                        "READ","WRITE"
                    ],
                    "revoke": []
                },
                "message:/": {
                    "grant": [
                        "READ","WRITE"
                    ],
                    "revoke": []
                }
            }
        }
    }
}'
```


# Create the Thing
We will use a WoT (Web of Things) Thing model to create our Digital Twin:
```
curl --location --request PUT -u ditto:ditto 'http://localhost:8080/api/2/things/org.Iotp2c:iwatch' \
  --header 'Content-Type: application/json' \
  --data-raw '{
      "policyId": "org.Iotp2c:policy",
      "definition": "https://raw.githubusercontent.com/bernar0507/Eclipse-Ditto-MQTT-iWatch/main/iwatch/wot/iwatch.tm.jsonld"
  }'
```

# Create the certificates and get them signed
So for this tutorial we are gonna use self-signed certificates. We need to create certificates on the broker and client side.
We also need a CA (Certificate Authority) to sign the broker certificate and client certificates, so that the broker can trust the client, and the client can trust the broker.

* First step: Get inside the mosquitto container
```
docker exec -it mosquitto bin/sh
```

When inside the container we need to go to the folder mosquitto/conf:
```
cd mosquitto/conf
```

After this we can create the  CA certificate:
* First define the following variables:
```
COUNTRY="PT" 
STATE="MAFRA"
CITY="LISBON"
ORGANIZATION="My Company"
ORG_UNIT="IT Department"
COMMON_NAME="CA"
```

Install openssl:
```
apk add openssl
```

* After that we can run this command to create the `ca.key` and `ca.crt`:
```
openssl req -new -x509 -days 3650 -extensions v3_ca -keyout ca.key -out ca.crt -nodes -subj "/C=$COUNTRY/ST=$STATE/L=$CITY/O=$ORGANIZATION/OU=$ORG_UNIT/CN=$COMMON_NAME"
```

Now that the `ca.key` and `ca.crt` are create lets create the `server.crt` and `server.key`

Inside the folder we will run the bash script `generate_openssl_config.sh` (this will create a file openssl.cnf): 
```
sh generate_openssl_config.sh
```

* Now lets define the following variables:
```
COUNTRY="PT" 
STATE="MAFRA"
CITY="LISBON"
ORGANIZATION="My Company"
ORG_UNIT="IT Department"
COMMON_NAME="MQTT Broker"
```

* After that we can run this command to create the `server.key` and `server.crt`:
```
openssl req -new -out server.csr -keyout server.key -nodes -subj "/C=$COUNTRY/ST=$STATE/L=$CITY/O=$ORGANIZATION/OU=$ORG_UNIT/CN=$COMMON_NAME" -config openssl.cnf
```

* And now lets sign them using our CA:
```
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 3650 -extensions v3_req -extfile openssl.cnf
```

Now that we have our certificates we have to restart the container:
* first type exit to exit the container:
```
exit
```

And then:
```
docker restart mosquitto 
```

This will start the broker with the certificates we just created. 

Now we have to start the iwatch container we create the certificates for the client and sign them with our CA.

Start the iwatch container:
```
cd iwatch/dockerfile
```

```
docker build --no-cache  -t iwatch_image -f Dockerfile.iwatch .
```

```
docker run -it -v <PATH_TO_FOLDER>/Eclipse-Ditto-MQTT-iWatch-SSL/mosquitto:/app/Eclipse-Ditto-MQTT-iWatch-SSL/mosquitto --name iwatch-container --network docker_default iwatch_image
```

When inside the container we need to go to the folder mosquitto/conf:
```
cd Eclipse-Ditto-MQTT-iWatch-SSL/mosquitto/conf
```

After this we can create the  client certificate:
* First define the following variables:
```
COUNTRY="PT" 
STATE="MAFRA"
CITY="LISBON"
ORGANIZATION="My Company"
ORG_UNIT="IT Department"
COMMON_NAME="iwatch Client"
```

* Create the client key and certificate request files:
```
openssl req -new -out client.csr -keyout client.key -nodes -subj "/C=$COUNTRY/ST=$STATE/L=$CITY/O=$ORGANIZATION/OU=$ORG_UNIT/CN=$COMMON_NAME" -config openssl.cnf
```

* After that we can sign them with the CA certificate:
```
openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt -days 3650 -extensions v3_req -extfile openssl.cnf
```

# Create a MQTT Connection
We need to get the Mosquitto Ip address from the container running Mosquitto. 
For that we need to use this to get the container ip:
```
mosquitto_ip=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mosquitto)
```

Before we can use MQTT, we have to open a MQTT connection in Eclipse Ditto. We can do this by using DevOps Commands. In this case we need the Piggyback Commands to open a new connection (this is gonna use the `$mosquitto_ip`, defined previously).
To use these commands we have to send a `POST Request` to the URL `http://localhost:8080/devops/piggyback/connectivity?timeout=10`.

Here's how you get `<CA_CERT>`, `<CLIENT_CERT>`, `<CLIENT_KEY>`:

* Save in a variable:
```
ca_crt=$(cat ca.crt)
```

* Print the variable:
```
echo $ca_crt
```

* Output:
```
-----BEGIN CERTIFICATE----- <CA_CERT> -----END CERTIFICATE-----
```

* Now just copy the `<CA_CERT>` part (dont select the blank spaces), and save in a e.g. text editor

- You will have to repeat these steps for `<CLIENT_CERT>` and `<CLIENT_KEY>`

## Create the connection:
Now that you have the values for `<CA_CERT>`, `<CLIENT_CERT>` and `<CLIENT_KEY>`, lets replace them in the following command
```
curl -X POST \
  'http://localhost:8080/devops/piggyback/connectivity?timeout=10' \
  -H 'Content-Type: application/json' \
  -u 'devops:foobar' \
  -d '{
    "targetActorSelection": "/system/sharding/connection",
    "headers": {
        "aggregate": false
    },
    "piggybackCommand": {
        "type": "connectivity.commands:createConnection",
        "connection": {
  "id": "mqtt-connection-iwatch",
  "connectionType": "mqtt",
  "connectionStatus": "open",
  "failoverEnabled": true,
  "uri": "ssl://ditto:ditto@'"$mosquitto_ip"':8883",
  "validateCertificates": true,
  "ca": "-----BEGIN CERTIFICATE-----\n<CA_CERT>\n-----END CERTIFICATE-----",
  "credentials": {
    "type": "client-cert",
    "cert": "-----BEGIN CERTIFICATE-----\n<CLIENT_CERT>\n-----END CERTIFICATE-----",
    "key": "-----BEGIN PRIVATE KEY-----\n<CLIENT_KEY>\n-----END PRIVATE KEY-----"
  },
  "sources": [{
                "addresses": ["org.Iotp2c:iwatch/things/twin/commands/modify"],
                "authorizationContext": ["nginx:ditto"],
                "qos": 0,
                "filters": []
            }],
  "targets": [{
                "address": "org.Iotp2c:iwatch/things/twin/events/modified",
                "topics": [
                "_/_/things/twin/events",
                "_/_/things/live/messages"
                ],
                "authorizationContext": ["nginx:ditto"],
                "qos": 0
            }]
	}
    }
}'
```

## If you need to delete the connection:
```
curl -X POST \
  'http://localhost:8080/devops/piggyback/connectivity?timeout=10' \
  -H 'Content-Type: application/json' \
  -u 'devops:foobar' \
  -d '{
    "targetActorSelection": "/system/sharding/connection",
    "headers": {
        "aggregate": false
    },
    "piggybackCommand": {
        "type": "connectivity.commands:deleteConnection",
        "connectionId": "mqtt-connection-iwatch"
    }
}'
```

# Send data to Eclipse Ditto from iWatch
Now that we have the connection create and everything else ready (certificates from both sides), we can send data to Ditto:

* Go to the iwatch tab and run in the folder app/Eclipse-Ditto-MQTT-iWatch-SSL/iwatch:
```
python3 send_data_iwatch.py
```

# Test if the digital twin is being updated
To see if the twin is being updated with the data send by script we can run the following:
```
curl -u ditto:ditto -X GET 'http://localhost:8080/api/2/things/org.Iotp2c:iwatch'
```

# Payload mapping
Depending on your IoT-Device, you may have to map the payload that you send to Eclipse Ditto. Because IoT-Devices are often limited due to their memory, it's reasonable not to send fully qualified Ditto-Protocol messages from the IoT-Device. 
In this case, the function that simulates the data generated from an iWatch sends a dictionary with the data from iWatch.
After that we will map this payload so it is according to the Ditto-Protocol format.

Ditto-Protocol format (in the `send_data_iwatch.py`):
```
ditto_data = {
    "topic": "org.Iotp2c/iwatch/things/twin/commands/modify",
    "path": "/",
    "value":{
      "thingId":"org.Iotp2c:iwatch",
      "policyId":"org.Iotp2c:policy",
      "definition":"https://raw.githubusercontent.com/bernar0507/Eclipse-Ditto-MQTT-iWatch/main/iwatch/wot/iwatch.tm.jsonld",
         "attributes":{
            "heart_rate":iwatch_data['heart_rate'],
            "timestamp":iwatch_data['timestamp'],
            "longitude":iwatch_data['longitude'],
            "latitude":iwatch_data['latitude']
        }
    }
}
```

`topic`: This is the topic to which the message will be published. In this case, the topic is "org.Iotp2c/iwatch/things/twin/commands/modify", which suggests that the message is intended to modify a twin (digital representation) of an iWatch device in an IoT platform.

`path`: This is the path within the twin where the value will be updated. In this case, the path is "/", indicating that the value should be updated at the root level of the iWatch twin.

`value`: This is the data payload that will be updated in the twin.

`thingId`: This is the unique identifier of the iWatch device within the IoT platform. In this example, the thingId is "org.Iotp2c:iwatch".

`policyId`: This is the identifier of the policy that governs the access control of the iWatch device. In this example, the policyId is "org.Iotp2c:policy".

`definition`: This is a URI referencing the JSON-LD file that contains the Thing Model for the iWatch device. In this example, the definition is "https://raw.githubusercontent.com/bernar0507/Eclipse-Ditto-MQTT-iWatch/main/iwatch/wot/iwatch.tm.jsonld".

`attributes`: This is a dictionary of key-value pairs that represent metadata about the iWatch device. In this example, the attributes include the heart rate, timestamp, longitude, and latitude data retrieved from the iwatch_data variable.
