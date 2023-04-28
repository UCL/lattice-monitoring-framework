# Lattice Monitoring Controller


### Installation
As soon as the Build process is completed, a controller instance (e.g, ZMQController) can be started as follows:
```sh
$ cd jars/
$ java -cp monitoring-bin-controller.jar mon.lattice.control.controller.json.ZMQController controller.properties
```
The `controller.properties` file contains the configuration settings for the controller (example files are under `conf/`)

### Configuration
```
info.localport = 6699
``` 
is the local port used by the Controller when connecting to the Information Plane. Other Lattice entities (e.g., Data Sources) will remotely connect to this port once started.

```
restconsole.localport = 6666
```
is the port where the controller will listen for HTTP control requests.

```
deployment.enabled = true
```
Can be set either to `true` or `false` and enables/disables respectively the automated Data Sources deployment functionality to a remote host (current implementation is based on SSH with public key authentication)

```
deployment.localJarPath = /Users/lattice
deployment.jarFileName = monitoring-bin-core.jar
deployment.remoteJarPath = /tmp
deployment.ds.className = mon.lattice.appl.datasources.ZMQDataSourceDaemon
deployment.dc.className = mon.lattice.appl.dataconsumers.ZMQControllableDataConsumerDaemon
```
The above settings allow to specify (in order):
- the path where the jar (to be used for the Data Sources / Consumers automated remote deployment) is located
- the file name of the above jar file
- the path where the jar will be copied on the remote machine where the Data Source is being deployed
- the class name of the Data Source to be started (it must exist in the specified jar)
- the class name of the Data Consumer to be started (it must exist in the specified jar)
