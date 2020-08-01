# About
This example shows how to persist thing features changes on an InfluxDB instance in a Spring Boot application. Throughout this guide you will deploy an InfluxDB instance (if you don't have one), configure your Ditto instance (creating a policy and a device for this demo) and run this project. Finally you will update the device features using the Ditto API and this changes will be saved in your InfluxDB instance.
# Prerequisites
* Ditto
* InfluxDB (>= 2.0)
> **Note**: you can run the docker version executing  `docker run --publish 9999:9999 quay.io/influxdb/influxdb:2.0.0-beta`. 
> After the deployment go to http://localhost:9999 in order to finish the installation.
* Maven (>= 3.6.0)
* Java 11
* HTTPie
# Setting up the environment
Configure the environment variables with your Ditto instance IP and port:
```bash
export DITTO_API_IP=localhost
export DITTO_API_PORT=8080
```
Create a policy in Ditto:
```bash
http -a ditto:ditto PUT $DITTO_API_IP:$DITTO_API_PORT/api/2/policies/org.acme:my-policy <<< '
{
  "entries": {
    "DEFAULT": {
      "subjects": {
        "{{ request:subjectId }}": {
           "type": "Ditto user authenticated via nginx"
        }
      },
      "resources": {
        "policy:/": {
          "grant": ["READ", "WRITE"],
          "revoke": []
        },
        "thing:/": {
          "grant": ["READ", "WRITE"],
          "revoke": []
        },
        "message:/": {
          "grant": ["READ", "WRITE"],
          "revoke": []
        }
      }
    }
  }
}'
```
Create a device:
```bash
http -a ditto:ditto PUT $DITTO_API_IP:$DITTO_API_PORT/api/2/things/org.acme:my-dev <<< '
{
  "policyId": "org.acme:my-policy",
  "attributes": {
    "location": "Device created for the demo"
  },  
  "features": {
    "temperature": {
      "properties": {
        "value": null
      }
    },
    "humidity": {
      "properties": {
        "value": null
      }
    },
    "pressure": {
      "properties": {
        "value": null
      }
    }    
  }
}'
```
Now Ditto is configured. 
Next it is required to download this project and configure its properties which are located at `src/main/resources/`.
First edit the `application.properties` file with your Ditto configuration:
> ditto.endpoint=ws://localhost:8080
> ditto.username=ditto
> ditto.password=ditto
> ditto.namespace=org.acme 

Then edit the `influx2.properties` file with your InfluxDB instance configuration:
> influx2.url=http://localhost:9999
> influx2.org=my-org
> influx2.bucket=my-bucket
> influx2.token=my-token

The token can be retrieved in the InfluxDB web admin interface in the `Data > Tokens` section.
Finally, install the project and run it:
```bash
mvn install
mvn spring-boot:run
```
# Testing
Now that everything is up and running we can start updating the device features:
```bash
http -a ditto:ditto PUT $DITTO_API_IP:$DITTO_API_PORT/api/2/things/org.acme:my-dev/features <<< '
{
   "temperature":{
      "properties":{
         "value":20
      }
   },
   "pressure":{
      "properties":{
         "value":1013
      }
   },
   "humidity":{
      "properties":{
         "value":89
      }
   }
}'
``` 
In the project logs you should see an output similar to this:
```
...
2020-07-30 19:07:30.115  INFO 4726 --- [-48768e37bb47-3] o.e.d.e.influxdb.service.DittoService    : Received features update from device 'org.acme:my-dev': {"temperature":{"properties":{"value":20}},"humidity":{"properties":{"value":89}},"pressure":{"properties":{"value":1013}}}
...
```
You can also explore the data in the InfluxDB web admin interface:

![](img/influxdb-dashboard.png?raw=true)

