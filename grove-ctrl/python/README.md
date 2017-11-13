The python scripts are to be used on a Raspberry Pi with a GrovePi+ board.
They aim to frequently provide and consume property changes of sensor values
and also allow to activate or deactive the buzzer through a message.

For information on how to run the complete setup, please refer to
the base [README.md](../README.md). This instruction will only give
a short intro on the python part of the demo setup.

# Prerequisites

* Python 3.x installed on the Raspberry
* Raspberry with installed GrovePi libraries
* Raspberry and PC/Notebook running the same network
* Running instance of Ditto on you PC/Notebook

# Hardware Setup

* GrovePi+
* Raspberry Pi Model B+ v1.2
* Grove Buzzer v1.1b
* Grove Light Sensor v1.0

# Quick start

When you have followed the base [README.md](../README.md), you only need
to fix the address of your running Ditto instance. This can be found
in `ditto_grove_demo.py`:

```python
DITTO_IP = "192.168.1.100"
DITTO_PORT = "8080"
```

To start the script, call it using python3:

```bash
$ python3 ./grovepi_ditto.py
```

It will connect to the Websocket provided by Ditto and start sending
sensor values to it.

# Possible adaptions

Read here how to adapt the code if your setup differs from the demo setup.

## Using another user/password to authenticate against Ditto
You can change those properties in `raspberry_thing.py`:
```python
# User and password needed for providing new sensor values
THING_USER = "raspberry"
THING_PASSWORD = "raspberry"
```

Have a look at the base [README.md](../README.md) for instructions
on how to add a new user to the Ditto nginx instance.

## Using other digital and analog ports for your sensors
You can change those properties in `raspberry_thing.py`:
```python
# Digital Port D8 on the GrovePi+ is connected to the buzzer
BUZZER_PORT = 8
# Analog Port A0 on the GrovePi+ is connected to the light sensor
LIGHT_SENSOR_PORT = 0
# Digital Port D4 on the GrovePi+ is connected to the temp sensor
TEMP_SENSOR_PORT = 4
```

## Testing the script without a GrovePi+

You can run the scripts without having the GrovePi+ connected to your
Raspberry Pi. Though your Ditto instance has to be up and running.

Just replace the import from `import grovepi` inside `raspberry_thing.py`
to the mock import:
```python
# To 'mock' the calls to grovepi script, change this line to 'import grovepi_mock as grovepi'
import grovepi_mock as grovepi
```


# Troubleshooting

Read here for common problems and how to solve them.

## Can't connect to Ditto

* can you curl onto the running Ditto instance?

    -> verify Ditto is up and running
* did you create the raspberry user?

    -> see base [README.md](../README.md) on how to create the raspberry:raspberry

* did you open your Ditto port in the firewall?

    -> this is necessary to be able to connect from the raspberry to your locally running Ditto instance

* did you set the correct ip address and port in the python script?

    -> verify by looking at the network information

* if nothing else helps...

    -> have you tried plugging out and in again? ;)

