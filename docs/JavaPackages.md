## Java Packages for Lattice

The following packages are used within Lattice.

Package	| Description
------------- | -------------
**mon.lattice.core** | This package provides classes that are at the core of the monitoring framework, including: Data Source, Probe, Probe Attribute, Measurement, and Probe Value.
mon.lattice.core.plane | This package provides classes that define the Planes of the monitoring framework.
mon.lattice.core.data | This package provides sub-packages for specific data types within a Measurement.
mon.lattice.core.data.list| This package provides classes that are used for embedding List data within a Measurement.
mon.lattice.core.data.map| This package provides classes that are used for embedding Map data within a Measurement.
mon.lattice.core.data.table| This package provides classes that are used for embedding Table data within a Measurement.
mon.lattice.core.datarate | This package provides classes that are used to specify data rates for Probes in different ways.
**mon.lattice.distribution** | This package provides classes and sub-packages that are used for distributing measurement data with the framework.
mon.lattice.distribution.udp | This package provides classes for the implementation of the Data Plane that utilizes UDP for transmission.
mon.lattice.distribution.multicast | This package provides classes for the implementation of the Data Plane that utilizes IP Multicast for transmission.
mon.lattice.distribution.zmq | This package provides classes for the implementation of the Data Plane that utilizes ZeroMQ for transmission.
mon.lattice.distribution.rest | This package provides classes for the implementation of the Data Plane that utilizes REST for transmission.
mon.lattice.distribution.ws | This package provides classes for the implementation of the Data Plane that utilizes Websocket for transmission.
**mon.lattice.xdr** | This package provides classes that are used for XDR encoding and decoding of data.
**mon.lattice.management** | This package provides the sub-packages that are used for management within the framework.
mon.lattice.management.deployment | This package provides classes and sub-packages that are used for deploying management facets within the framework.
mon.lattice.management.deployment.ssh | This package provides classes  that are used for deploying ssh within the framework.
mon.lattice.management.deployment.console | This package provides classes  that are used for deploying a console within the framework.
**mon.lattice.control** | 
mon.lattice.control.udp | 
mon.lattice.control.agents | 
mon.lattice.control.agents.appl | 
mon.lattice.control.controller | 
mon.lattice.control.controller.json | 
mon.lattice.control.im | 
mon.lattice.control.zmq | 
mon.lattice.control.console | 
**mon.lattice.im** | 
mon.lattice.im.dht | This package provides classes for the implementation of the Info Plane that utilizes a Distributed Hash Table (DHT) 
mon.lattice.im.dht.tomp2p | This package provides classes for the implementation of the Info Plane that utilizes TomP2P
mon.lattice.im.dht.planx | This package provides classes for the implementation of the Info Plane that utilizes PlanX
mon.lattice.im.zmq | This package provides classes for the implementation of the Info Plane that utilizes ZeroMQ
**mon.lattice.appl** | This package provides classes that are at the application level.
mon.lattice.appl.datasources | 
mon.lattice.appl.demo | 
mon.lattice.appl.demo.iot | 
mon.lattice.appl.demo.iot.process | 
mon.lattice.appl.demo.reporters | 
mon.lattice.appl.demo.reporters.riemann | 
mon.lattice.appl.probes | 
mon.lattice.appl.probes.openstack | 
mon.lattice.appl.probes.docker | 
mon.lattice.appl.probes.vlsp | 
mon.lattice.appl.probes.host | 
mon.lattice.appl.probes.host.linux | This package provides classes that are used for Probes that monitor Linux hosts.
mon.lattice.appl.probes.delay | 
mon.lattice.appl.probes.delay.bidirectional | 
mon.lattice.appl.probes.delay.unidirectional | 
mon.lattice.appl.probes.hypervisor | 
mon.lattice.appl.probes.hypervisor.libvirt | 
mon.lattice.appl.reporters | 
mon.lattice.appl.reporters.vlsp | 
mon.lattice.appl.reporters.im | 
mon.lattice.appl.dataconsumers | 

