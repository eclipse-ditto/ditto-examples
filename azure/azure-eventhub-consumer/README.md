# Sample: Eclipse Ditto event and message consumption with Azure Event Hubs

This sample demonstrates consuming Eclipse Ditto events and messages with a Spring Boot app and Spring Cloud Stream Kafka integration.

## Prerequisites

- Running Ditto instance (e.g. locally or on an Azure Kubernetes Service (AKS))
- Azure subscription.
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) to setup Azure Event Hubs.
- OpenJDK 8 or 11 and Maven 3 to build and run the sample.

## Howto run the sample

### SetUp Azure Events Hubs

First create a Azure resource group if you have not done so yet.

```bash
export resourceGroupName=myresourcegroup
az group create --name $resourceGroupName --location westeurope
```

Now create your [Azure Event Hub's](https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-about) namespace and hub. The namespace needs to be globally unique as it is used as DNS name as well.

Note: this sample leverages Azure Event Hub's Kafka support as an option for consuming Ditto thing updates.

```bash
export namespace=MY_DITTO_NAMESPACE
export eventHubName=ditto
export consumerGroup=dittocg

az eventhubs namespace create --name $namespace --resource-group $resourceGroupName --enable-kafka

az eventhubs eventhub create --name $eventHubName --resource-group $resourceGroupName --namespace-name $namespace --message-retention 1 --partition-count 2

az eventhubs eventhub consumer-group create --eventhub-name $eventHubName --resource-group $resourceGroupName --namespace-name $namespace --name $consumerGroup
```

Now we can create users for Ditto with `Send` permission and for the sample app to `Listen`.

```bash
export ditto_user_sas_key_name=dittouser
export sample_app_user_sas_key_name=samplereader

az eventhubs eventhub authorization-rule create --resource-group $resourceGroupName --namespace-name $namespace --eventhub-name $eventHubName  --name $ditto_user_sas_key_name --rights Send
az eventhubs eventhub authorization-rule create --resource-group $resourceGroupName --namespace-name $namespace --eventhub-name $eventHubName  --name $sample_app_user_sas_key_name --rights Listen
```

Now the CLI again to retrieve the key necessary in the REST call to Ditto below:

```bash
az eventhubs eventhub authorization-rule keys list --resource-group $resourceGroupName --namespace-name $namespace --eventhub-name $eventHubName --name $ditto_user_sas_key_name --query primaryKey
```

To register the connection to the Event Hub in your Ditto instance. Follow Ditto's [Manage Connection documentation](https://www.eclipse.dev/ditto/connectivity-manage-connections.html).

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
      "uri": "amqps://YOUR_SAS_KEY_NAME:YOUR_SAS_KEY@YOUR_NAMESPACE_NAME.servicebus.windows.net:5671",
      "targets": [
        {
          "address": "YOUR_HUB_NAME",
          "topics": ["_/_/things/twin/events", "_/_/things/live/messages"],
          "authorizationContext": ["ditto"]
        }
      ]
    }
  }
}
```

Now it is time to compile and run our sample:

```bash
mvn clean install

export sample_app_user_sas_key=`az eventhubs eventhub authorization-rule keys list --resource-group $resourceGroupName --namespace-name $namespace --eventhub-name $eventHubName --name $sample_app_user_sas_key_name --query primaryConnectionString --output tsv`

java -jar target/azure-eventhub-consumer-0.0.1-SNAPSHOT.jar --azure.event-hubs.namespace=$namespace --azure.event-hubs.connection-string=$sample_app_user_sas_key --azure.event-hubs.hubname=$eventHubName --azure.event-hubs.consumer-group=$consumerGroup
```

Now you should see on the console events comming in as you change data in Ditto, e.g. as described in the [Ditto hello world](https://www.eclipse.dev/ditto/intro-hello-world.html).
