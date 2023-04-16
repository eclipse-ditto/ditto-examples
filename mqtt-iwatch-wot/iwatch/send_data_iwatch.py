import paho.mqtt.client as mqtt
import json
import iwatch_simulator
import time
import os
import socket

# Replace with your own values
MQTT_BROKER_PORT = 1883
THING_ID = "org.Iotp2c:iwatch"
MQTT_TOPIC = f"{THING_ID}/things/twin/commands/modify"


def on_connect(client, userdata, flags, rc):
    print("Connected to MQTT broker with result code "+str(rc))

def on_disconnect(client, userdata, rc):
    print("Disconnected from MQTT broker with result code "+str(rc))

def on_publish(client, userdata, mid):
    print("Message published to "+MQTT_TOPIC)

def send_data_to_ditto(iwatch_data):
    # Create a MQTT client instance
    client = mqtt.Client()

    # Set the callbacks
    client.on_connect = on_connect
    client.on_disconnect = on_disconnect
    client.on_publish = on_publish
    
    # Get the IP address of the MQTT broker
    broker_ip = socket.gethostbyname("mosquitto")

    # Connect to the MQTT broker
    client.username_pw_set(username='ditto', password='ditto')
    client.connect(broker_ip, MQTT_BROKER_PORT, 60)

    # Prepare the Ditto command payload
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
    # Convert the dictionary to a JSON string
    ditto_data_str = json.dumps(ditto_data)

    # Publish the message to the MQTT topic
    client.publish(MQTT_TOPIC, payload=ditto_data_str)

    # Disconnect from the MQTT broker
    client.disconnect()

    print("Data sent to Ditto: " + json.dumps(ditto_data))


properties = ['heart_rate', 'timestamp', 'longitude', 'latitude']
dict_dt = {property: None for property in properties}
# Example usage
while True:
    iwatch_data = next(iwatch_simulator.iwatch(dict_dt))
    send_data_to_ditto(iwatch_data)
    time.sleep(1)
