# Sample: Eclipse Ditto event and message consumption with Azure Service Bus

This sample demonstrates consuming Eclipse Ditto events and messages with a Spring Boot app and Spring JMS integration based on [Apache Qpid JMS is an AMQP 1.0 Java Message Service 2.0 client](https://qpid.apache.org/components/jms/index.html).

Note: we use Qpid JMS as of version 1.X the [Microsoft Azure Service Bus Client for Java](https://github.com/Azure/azure-service-bus-java) does not support reading text payload by AMQP value as sent by Ditto.

## Prerequisites

- Running Ditto instance (e.g. locally or on an Azure Kubernetes Service (AKS))
- Azure subscription.
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) to setup Azure Service Bus.
- OpenJDK 8 or 11 and Maven 3 to build and run the sample.

## Howto run the sample

### SetUp Azure Service Bus

First create a Azure resource group if you have not done so yet.

```bash
export resourceGroupName=YOUR_RESOURCE_GROUP
az group create --name $resourceGroupName --location westeurope
```

Now create your [Azure Service Bus](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-messaging-overview) namespace, topics and queues. The namespace needs to be globally unique as it is used as DNS name as well.

```bash
export topic=dittooutbound
export queue=dittoinbound
export responseQueue=dittoresponses
export subscription=fromditto
export namespace=YOUR_NAMESPACE

az servicebus namespace create --resource-group $resourceGroupName \
    --name $namespace

az servicebus topic create --resource-group $resourceGroupName \
    --namespace-name $namespace \
    --name $topic

az servicebus queue create --resource-group $resourceGroupName \
    --namespace-name $namespace \
    --name $queue

az servicebus queue create --resource-group $resourceGroupName \
    --namespace-name $namespace \
    --name $responseQueue

az servicebus topic subscription create --resource-group $resourceGroupName  \
    --namespace-name ${namespace} --topic-name ${topic} \
    --name ${subscription}
```

Now we can create users for Ditto and for the sample app to with `Send` as well as `Listen` permission.

```bash
export ditto_user_sas_key_name=dittouser
export sample_app_user_sas_key_name=sampleapp

az servicebus namespace authorization-rule create --resource-group $resourceGroupName \
        --namespace-name $namespace  --name $ditto_user_sas_key_name --rights {Send,Listen}
az servicebus namespace authorization-rule create --resource-group $resourceGroupName \
        --namespace-name $namespace --name $sample_app_user_sas_key_name --rights {Send,Listen}
```

Now the CLI again to retrieve the key necessary in the REST call to Ditto below:

```bash
az servicebus namespace authorization-rule keys list --resource-group $resourceGroupName \
        --namespace-name $namespace --name $ditto_user_sas_key_name --query primaryKey
```

To register the connection to the Service Bus in your Ditto instance. Follow Ditto's [Manage Connection documentation](https://www.eclipse.dev/ditto/connectivity-manage-connections.html).

A payload could look like this:

```json
{
  "targetActorSelection": "/system/sharding/connection",
  "headers": {},
  "piggybackCommand": {
    "type": "connectivity.commands:createConnection",
    "connection": {
      "id": "azure-servicebus-topic-connection",
      "connectionType": "amqp-10",
      "connectionStatus": "open",
      "failoverEnabled": true,
      "uri": "amqps://YOUR_SAS_KEY_NAME:YOUR_SAS_KEY@YOUR_NAMESPACE_NAME.servicebus.windows.net:5671",
      "targets": [
        {
          "address": "topic://dittooutbound",
          "topics": ["_/_/things/twin/events", "_/_/things/live/messages"],
          "authorizationContext": ["ditto"]
        }
      ],
      "sources": [
        {
          "addresses": ["dittoinbound"],
          "authorizationContext": ["ditto"]
        }
      ],
      "specificConfig": {
        "amqp.idleTimeout": 120000
      }
    }
  }
}
```

Now it is time to compile and run our sample:

```bash
mvn clean install

export sample_app_user_sas_key=`az servicebus namespace authorization-rule keys list --resource-group $resourceGroupName --namespace-name $namespace --name $sample_app_user_sas_key_name --query primaryKey --output tsv`

java -jar target/azure-servicebus-amqp10-0.0.1-SNAPSHOT.jar \
    --amqphub.amqp10jms.remote-url=amqps://$namespace.servicebus.windows.net \
    --amqphub.amqp10jms.username=$sample_app_user_sas_key_name \
    --amqphub.amqp10jms.password=$sample_app_user_sas_key
```

Now you should see on the console events send to Ditto including the response. In addition similar to the Azure Event Hubs: [Azure Event Hubs example](../azure-eventhub-consumer/) events coming in as you change data in Ditto, e.g. as described in the [Ditto hello world](https://www.eclipse.dev/ditto/intro-hello-world.html).
