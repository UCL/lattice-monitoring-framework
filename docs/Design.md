# Design

A number of existing monitoring systems such as Ganglia, Nagios , MonaLisa , and GridICE have addressed monitoring of large distributed systems, but they have not addressed the rapidly changing and dynamic infrastructure seen in service clouds.

The monitoring system for a virtualized systems needs to feed data into a Service Manager / Orchestrator so that it can manage the services deployed. Over time, it is expected that the management capabilities of the Service Manager  / Orchestrator will expand to include new functions. As a consequence we need the monitoring system to be adaptable, flexible, and extensible in order to support the expanding functionality.
To address all of the requirements and functionality of the virtualized environment we have determined that the main features for monitoring which need to be taken account of are:

- *scalability* — to ensure that the monitoring can cope with a large number of probes
- *elasticity* — so that virtual resources created and destroyed by expanding and contracting
networks are monitored correctly
- *migration* — so that any virtual resource which moves from one physical host to another is monitored correctly
- *adaptability* — so that the monitoring framework can adapt to varying computational and network loads in order not to be invasive
- *autonomic* — so that the monitoring framework can keep running without intervention and reconfiguration
- *federation* — so that any virtual resource which resides on another domain is monitored correctly
- *isolation* — so that monitoring of VEEs from different services are not mixed and are not visible to other services

To establish such features in a monitoring framework requires careful architecture and design. The following sections present the Lattice monitoring framework which we have designed and built for the purpose of monitoring dynamic environments.


In many systems, **probes** are used to collect data for system management. In this regard, Lattice also relies on probes. However, to increase the power and flexibility of the monitoring we introduce the concept of a **data dource**. A data source represents an interaction and control point within the system that encapsulates one or more probes. A probe sends a well defined set of attributes and values to a data consumer at a predefined interval.

The goal for the monitoring system is to have fully dynamic data sources, in which each one can have multiple probes, with each probe returning its own data. The data sources will be able to turn on and turn off probes, or change their sending rate dynamically at run time. A further useful facility for a data source is the ability to add new probes to a data source at run-time. By using this approach we will be able to instrument components of the system without having to restart them in order to get new information.

Such an approach for data sources and probes is important because in many systems that need monitoring , the Management / Orchestration needs to grow and adapt for new requirements over time. If the monitoring system is a fixed point then such Management / Orchestration will be difficult to adapt and amend.
To meet all the criteria outlined requires careful architecture and design. Many monitoring systems rely on simple data transmission. From a design point of view, this approach is successful, although limited.

For Lattice, we believe it is better if the monitoring framework design encapsulates separate planes for data, for meta-data, and for control. This allows us to build a system that has the desired behaviour and meets the requirements.
In Lattice, the separate planes for connecting the monitoring framework are:

- the *data plane* — for distributing measurements from the Probes and Data Sources to the consumers. This is the same monitoring data plane shown in figures 3, 4, and 5.
- the *control plane* — for distributing control messages to the Data Sources and the Probes.
- the *information plane* — which holds all the meta-data relating to measurements sent by Data Sources and Probes.

![Monitoring Planes](images/mon_planes.png)

###Data Source

A Data Source can manage several Probes, and has plugins for the monitoring data plane, the control plane, and the information plane, so that it is possible to change the implementation of each plane easily and independently of the other planes. This will allow users to choose the best solution for different setup scenarios. We have a control interface so that Data Sources can be controlled from a Manager, via the control plane, and Probes can also be controlled via the Data Source. We can then develop control strategies so that it is possible to manage the lifecycle of a Probe from a Manager rather than having everything programmed-in.

As there could be hundreds or thousands of probes in a Lattice system, it is important that each probe has a unique identity. Without an identity, it is not possible to identify individual probes. Using the identity it is possible for the Data Source to address the probe in order to turn it on, turn it off, change its rate of sending, or find its status. It is the probe’s identity that also allows the combination of its data with other probe’s data to create the complex information.

###Probes

In many systems, the Probes collect data at a given data rate, and transmit measurements immediately, at exactly the same data rate. In Lattice we can decouple the collection rate and the transmission rate in order to implement strategies which aid in efficiency.

 
The collection of measurement data is a fundamental aspect of a Probe. The collection strategies will be:

- at data rate, which collects some measurement data at a regular data rate.
- on event, where a measurement is not collected at a specified rate, but is passed to the
probe as an event from another entity. 

The transmission strategies are:

- at data rate, this is the main strategy, which transmits measurements at a regular data rate.
- on change, where a measurement is only transmitted when the data that is read is different from the previous data snapshot. This can be elaborated so that only a specific set of attributes are included when determining if there has been a change.
- filtering, where a measurement is only transmitted if the filter passes the value. Examples of such filtering strategies are:

	* above threshold, where a measurement is only transmitted when an attribute of the data that is read is above a specified threshold. Otherwise, nothing is transmitted.
	* below threshold, where a measurement is only transmitted when an attribute of the data that is read is below a specified threshold. Otherwise, nothing is transmitted.
	* in band, where a measurement is only transmitted when an attribute of the data that is read is between an upper bound and a lower bound. Otherwise, nothing is transmitted.
	* out of band, where a measurement is only transmitted when an attribute of the data that is read is above an upper bound or below a lower bound. Otherwise, nothing is transmitted.
	
	although any kind of filter can be defined.
	

For a probe to be part of Lattice we can either write the probe from scratch or use existing sensors and instrumentation and adapt them for Lattice. The probes can be implemented in various ways to get the data they need to send. They can:

- read the data directly from the relevant place
- be an adaptor for an existing instrument by wrapping the existing one
- act as a bridge for an existing instrument by allowing the existing one to send it data.

in order to gather the required information.



### Probe Data Dictionary

One of the important aspects of this monitoring design is the specification of a Data Dictionary
for each probe. The Data Dictionary defines the attributes as the names, the types and the
units of the measurements that the probe will be sending out. This is important because the
consumers of the data can collect this information in order to determine what will be received.
At present many monitoring systems have fixed data sets, with a the format of measurements being pre-defined.  The advantage here is that as new probes are to be added to the system or embedded in an application, it will be possible to introspect what is being measured. This is important in a service cloud system such as RESERVOIR, because many of the Probes will not be known in advance.

The measuremdefinntes tthehattribautrees thsaetnittcawn siellndhinaavesetvoaf PluroebefiAtetrlibduste tohbjeacts,rtehalat stpecdifyirthectly to the data dictionary. To determine which field is which, the consumer can lookup in the data dictionary to elaborate the full attribute value set.

![Probe Data Dictionary](images/impl-model.png)


