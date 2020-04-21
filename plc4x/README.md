# Simple Example of how to use Apache PLC4X together with Eclipse Ditto

This example shows how to build a minimal digital twin for a PLC.

## Setup

* Before starting, you need to start the docker-compose based local Ditto deployment (with all default settings).
* To build the project simply run `mvn clean install`.
* Then you are good to go and can execute `Example`.

## Configuration

### Mocked

Two sets of configurations are provided.
If you use the mock
```
    private static final String PLC4X_FIELD_NAME = "pressure";
    private static final String PLC4X_PLC_ADDRESS = "mock:plc";
    private static final String PLC4X_FIELD_ADDRESS = "%DB:xxx";
```
the program will start a "mocked" plc which always returns a random value between 0 and 100.

### Siemens S7

If you have a Siemens S7 at hand you can also use this set of configurations

```
    private static final String PLC4X_FIELD_NAME = "pressure";
    private static final String PLC4X_PLC_ADDRESS = "s7://192.168.167.210/1/1";
    private static final String PLC4X_FIELD_ADDRESS = "%DB:xxx";
```
and enter a valid PLC IP as well as a valid field address (in PLC4X syntax).