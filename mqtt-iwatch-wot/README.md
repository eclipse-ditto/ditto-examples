# Eclipse-Ditto-MQTT-iWatch
This example presents how to configure Ditto to be able update things via MQTT. In this example we will create a iWatch from a WoT TM (Web of Things Thing Model). Our Digital Twin it will be updated via MQTT, using synthetic data.

# Requirements
1. Clone Ditto: ```git clone https://github.com/eclipse-ditto/ditto.git```

2. Pull Mosquitto: ```docker pull eclipse-mosquitto```

3. Clone Eclipse-Ditto-MQTT-iWatch: ```git clone https://github.com/bernar0507/Eclipse-Ditto-MQTT-iWatch.git```

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
docker run -it --name mosquitto --network docker_default -p 1883:1883 -v $(pwd)/mosquitto:/mosquitto/ eclipse-mosquitto
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

# Create a MQTT Connection
We need to get the Mosquitto Ip Adress from the container running Mosquitto. 
For that we need to use this to get the container ip:
```
mosquitto_ip=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' mosquitto)
```

Before we can use MQTT, we have to open a MQTT connection in Eclipse Ditto. We can do this by using DevOps Commands. In this case we need the Piggyback Commands to open a new connection (this is gonna use the `$mosquitto_ip`, defined previously).
To use these commands we have to send a `POST Request` to the URL `http://localhost:8080/devops/piggyback/connectivity?timeout=10`.

## Create the connection:
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
            "uri": "tcp://ditto:ditto@'"$mosquitto_ip"':1883",
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
This will be handled in the `Dockerfile.iwatch`, so we don't need to install anything locally.
Just do the following:

```
cd iwatch/dockerfile
```

```
docker build --no-cache  -t iwatch_image -f Dockerfile.iwatch .
```

```
docker run -it --name iwatch-container --network docker_default iwatch_image
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
