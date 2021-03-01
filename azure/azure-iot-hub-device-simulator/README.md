# Sample: Device Simulator for communication with Azure IoT Hub for integration with Ditto
This sample uses the Azure IoT Hub device sdk to build a simple client which publishes a sample event in ditto-protocol
format to the IoT Hub and listens for messages and direct method invocations.

## Prerequisites

- Running Ditto instance (e.g. locally or on an Azure Kubernetes Service (AKS))
- Azure subscription.
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) to setup Azure IoT Hub.
- OpenJDK 8 or 11 and Maven 3 to build and run the sample.

## Howto run the sample

### SetUp Azure IoT Hub

First create an Azure resource group if you have not done so yet.

```bash
az group create --name $resourceGroupName --location westeurope
```

Add the azure-iot extension to your cli.

```bash
az extension add --name azure-iot
```

Now create your [Azure IoT Hub](https://docs.microsoft.com/de-de/azure/iot-hub/) and new device.

```bash
az iot hub create --resource-group $resourceGroupName --name $iotHubName
```

*Note: the device should be in [namespaced-id notation](https://www.eclipse.org/ditto/basic-namespaces-and-names.html#namespaced-id).*

```bash
az iot hub device-identity create --device-id $deviceId --hub-name $iotHubName
```

To register the connection to the IoT Hub in your Ditto instance. Follow Ditto's [Manage Connection documentation](https://www.eclipse.org/ditto/connectivity-manage-connections.html).

Retrieve the following from the primary connection string to the policy "service" in your hub view in the 
Azure Portal under "Shared access policies":

- ```${hostName}``` = the HostName
- ```${userName}``` = the SharedAccessKeyName
- ```${password}``` = the SharedAccessKey

From the menu "Built in endpoints":

- ```${endpointName}``` = Event Hub-compatible name

A payload could look like this:

```json
{
  "targetActorSelection": "/system/sharding/connection",
  "headers": {},
  "piggybackCommand": {
    "type": "connectivity.commands:createConnection",
    "connection": {
      "id": "azure-example-connection",
      "connectionType": "amqp-10",
      "connectionStatus": "open",
      "failoverEnabled": false,
      "uri": "amqps://${userName}:${password}@${hostName}.servicebus.windows.net:5671",
      "source": [
        {
          "addresses": [
          "${endpointName}/ConsumerGroups/$Default/Partitions/0",
          "${endpointName}/ConsumerGroups/$Default/Partitions/1"
        ],
          "authorizationContext": ["ditto"]
        }
      ]
    }
  }
}
```

Create a new Thing with the same ID as the chosen ```$deviceId``` to represent the digital twin of this device and
reflect changes through events from the device.

PUT /things/```deviceId```
```JSON
{
   "features": {
      "Sensor": {
         "properties": {}
      }
   }
}
```

### SetUp this example

Fill in the config in /src/main/resources/config.properties

- ```connection_string``` = the connection string found under "Shared access policies" -> "device".
- ```namespace``` = the namespace of the created thing.
- ```thingId``` = the thingId without the namespace part.
- ```connection_protocol``` = the protocol with which the device should be connected to Azure IoT Hub.
- ```Proxy config``` = leave empty if no proxy is required.

### Invoking message sample

For invoking the message callback execute:

Generate Shared Access Signature:

```bash
az iot hub generate-sas-token -n $iotHubName -du $duration
```

Execute method invocation:

```bash
curl -X POST \
  https://$iotHubName.azure-devices.net/devices/$deviceId/messages/devicebound \
  -H 'Authorization: SharedAccessSignature sr=${sharedAccessSignature}' \
  -H 'Content-Type: application/json' \
  -d '{
    "message": "hello"
    }'
```

*Note: At the time of creating this example, Ditto HttpPush connections don't allow for SAS authentication,
thus the request has to be executed directly to the Azure IoT Hub*

### Invoking direct method sample

For invoking the direct method callback execute:

Generate Shared Access Signature:

```bash
az iot hub generate-sas-token -n $iotHubName -du $duration
```

Execute method invocation:

```bash
curl -X POST \
  https://$iotHubName.azure-devices.net/twins/$deviceId/methods?api-version=2018-06-30 \
  -H 'Authorization: SharedAccessSignature sr=${sharedAccessSignature}' \
  -H 'Content-Type: application/json' \
  -d '{
    "methodName": "reboot",
    "responseTimeoutInSeconds": 200,
    "payload": {}
}'
```

*Note: At the time of creating this example, Ditto HttpPush connections don't allow for SAS authentication, 
thus the request has to be executed directly to the Azure IoT Hub*
