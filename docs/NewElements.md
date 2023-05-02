# How to write new elements 


### How to write a Probe

This section describes how to write a Probe.

Add section on Random Probe.

Define ProbeAttributes

Add Probe Values

Create a measurement and set type

Clock driven vs. event driven
setDataRate() turns on clock driven

 /**
     * Inform the Probe of an object.
     * This turns the Probe on so it calls collect().
     */
    public Object inform(Object obj) 

Always define  public abstract ProbeMeasurement collect();

###How to write a DataSource

This section describes how to write a DataSource.

Add Probe

Probe Lifecycle

Extending BasicDataSource

### How to write a Consumer

BasicConsumer

Reporter