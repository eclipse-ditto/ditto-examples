# Example: Device Simulator for communication with Azure IoT Hub for integration with Ditto
This example uses the Azure IoT Hub device SDK to build a simple client which publishes an example event in Ditto-protocol
format to the IoT Hub and listens for messages and direct method invocations.

## Prerequisites

- Running Ditto instance (e.g. locally or on an Azure Kubernetes Service (AKS))
- Azure subscription.
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) to set up Azure IoT Hub.
- OpenJDK 8 or 11 and Maven 3 to build and run the example.

## How to run the example

### Set up Azure IoT Hub

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

To register the connections to the IoT Hub in your instance, follow Ditto's [Manage Connection documentation](https://www.eclipse.org/ditto/connectivity-manage-connections.html).

**Telemetry connection:**

Retrieve the following from the primary connection string to the policy "service" in your hub view in the
Azure Portal under "Shared access policies":

- `${userName}`: the SharedAccessKeyName
- `${password}`: the SharedAccessKey

From the menu "Built in endpoints":

- `${endpoint}`: Event Hub-compatible endpoint -> Endpoint
- `${entitiyPath}`: Event Hub-compatible endpoint -> EntityPath

A payload could look like this:

```json
{
  "targetActorSelection": "/system/sharding/connection",
  "headers": {},
  "piggybackCommand": {
    "type": "connectivity.commands:createConnection",
    "connection": {
      "id": "azure-example-connection-telemetry",
      "connectionType": "amqp-10",
      "connectionStatus": "open",
      "failoverEnabled": false,
      "uri": "amqps://${userName}:${password}@${endpoint}:5671",
      "source": [
        {
          "addresses": [
            "${entityPath}/ConsumerGroups/$Default/Partitions/0",
            "${entityPath}/ConsumerGroups/$Default/Partitions/1"
          ],
          "authorizationContext": ["ditto"],
          "enforcement": {
            "input": "{{ header:iothub-connection-device-id }}",
            "filters": [
              "{{ thing:id }}"
            ]
          }
        }
      ]
    }
  }
}
```

**Messages connection:**

- `${policyName}`: "service"
- `${hostName}`: ${hubName}.azure-devices.net
- `${username}`: ${policy_name}@sas.root.${hubName}
- `${encoded_SAS_token}`: URI-encoded token retrieved via: 
  ```bash
  az iot hub generate-sas-token -n $hubName -du $duration -policy $policyName
  ```

```json
{
  "targetActorSelection": "/system/sharding/connection",
  "headers": {},
  "piggybackCommand": {
    "type": "connectivity.commands:createConnection",
    "connection": {
      "id": "azure-example-connection-messages",
      "connectionType": "amqp-10",
      "connectionStatus": "open",
      "failoverEnabled": false,
      "uri": "amqps://${username}:${encoded_SAS_token}@${hostName}:5671",
      "target": [
        {"address": "/messages/devicebound",
          "topics": [
            "_/_/things/live/commands",
            "_/_/things/live/messages"
          ],
          "authorizationContext": ["ditto"],
          "headerMapping": {
            "message_id": "{{header:correlation-id}}",
            "to": "/devices/{{ header:ditto-message-thing-id }}/messages/deviceInbound"
          }
        }
      ]
    }
  }
}
```

**Thing creation:**

Create a new Thing with the same ID as the chosen `$deviceId` to represent the digital twin of this device and
reflect changes through events from the device.

PUT /things/`deviceId`
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

- `connection_string`: the connection string found under "Shared access policies" -> "device".
- `namespace`: the namespace of the created thing.
- `thingName`: the thingId without namespace.
- `connection_protocol`: the protocol the device uses to connect to Azure IoT Hub.
- Proxy config: leave empty if no proxy is required.


### Invoking message example
The message example can be invoked by [sending a live message](https://www.eclipse.org/ditto/protocol-specification-things-messages.html) 
to the corresponding thing.

*Note: At the time of creating this example, the Azure IoT Hub Device client can not correctly process AMQP messages, 
which contain AMQPValue as body (Which all unmapped Ditto messages do). Thus, an [outgoing payload mapping](https://www.eclipse.org/ditto/connectivity-mapping.html) 
to a byte message has to be done.*

Example payload-mapping:
```javascript
function mapFromDittoProtocolMsg(
  namespace,
  id,
  group,
  channel,
  criterion,
  action,
  path,
  dittoHeaders,
  value,
  status,
  extra
) {
  
  let headers = dittoHeaders;
  let textPayload = null;
  let bytePayload = Ditto.stringToArrayBuffer(Ditto.buildDittoProtocolMsg(namespace, id, group, channel, criterion, action, path, dittoHeaders, value).toString());
  let contentType = 'application/octet-stream';

  return Ditto.buildExternalMsg(
    headers,
    textPayload,
    bytePayload,
    contentType
  );
}
```

### Invoking direct method example

This section shows how to perform direct method invocation.

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

*Note: At the time of creating this example, Ditto Http Push connections don't allow for SAS authentication, 
thus the request has to be executed directly to the Azure IoT Hub.*
