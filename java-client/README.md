# Eclipse Ditto Java Client examples

These example shows how to use the Client to manage Things, Attributes/Features, register for changes on your
 Things and send messages.
You can find more information about the Client in our [Documentation](https://www.eclipse.org/ditto/client-sdk-java.html).

## Prerequisites

The following background knowledge is required for this example
- Java 8 (In order to use Java 9 and above, please use the [Build and run for Java 9 and above](#Build-and-run-for-Java-9-and-above) section)
- Maven
- Docker
- Publish–subscribe pattern
- Eclipse Ditto


## Configure
The examples are preconfigured to work with a local Eclipse Ditto running in Docker. Find more information on
 [GitHub](https://github.com/eclipse/ditto/tree/master/deployment/docker).

You can change the configuration to your liking by editing `src/main/resources/config.properties`.
The configured user names and passwords must be added to the nginx.htpasswd of Eclipse Ditto.
```bash
htpasswd nginx.htpasswd user1
```

## Build and run for Java 8
Start Eclipse Ditto:
```bash
docker-compose up -d
```

Build and run an Example (e.g. `RegisterForChanges`):
```bash
mvn compile exec:java -Dexec.mainClass="org.eclipse.ditto.examples.changes.RegisterForChanges"
```

## Build and run for Java 9 and above
Start Eclipse Ditto:
```bash
docker-compose up -d
```

Add the following dependencies to your `pom.xml`
```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.2.11</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-core</artifactId>
    <version>2.2.11</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>2.2.11</version>
</dependency>
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1.1</version>
</dependency>
```

Build and run an Example (e.g. `RegisterForChanges`):
```bash
mvn compile exec:java -Dexec.mainClass="org.eclipse.ditto.examples.changes.RegisterForChanges"
```
