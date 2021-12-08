# Networks Course Project
This project implements an IoT device that communicates between multiple devices. Below are instructions to compile and run the source code. 

\### NOTE ### 

Currently all software is configured such that the server is run on isengard. Its IP address is what the android application and the Raspberry Pi attempt to connect to.

\###

An additional dummy client is supplied with this software, as access to all hardware is 
## Raspberry Pi and Sensor
On a raspberry pi device, simply run 
```bash
make pi
```
to compile the source code for the raspberry pi.

In order to run this code, one must have superuser privileges on the Raspberry Pi. Simply run 
``` bash
sudo ./pi
```

This will start the software, allowing for data to eb obtained from the sensors, and will automatically connect to the server, and start sending packets if it is able.

Additionally, a dummy client is supplied with this software that sends packets every 5 seconds. This can similarly be run using 
``` bash
make dummy && ./dummy
```

## Server
Similarly, on the server, simply run 
``` bash
make server
```

to compile the source code for the server.

This will create an executable: server in the parent directory.

Running this executable simply with:

``` bash
./server
```
This will start the server, which will in turn start listening for devices. this runs on port 8888, so this port must not be in use to run the server.
## Android
In order to install the android application on to a mobile smartphone, Android Studio must be used, and the device wired into the computer running Android Studio. From here, there will be an option to run it on the attached device. this will install the application to the device.