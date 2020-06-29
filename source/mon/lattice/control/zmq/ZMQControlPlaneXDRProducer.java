/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mon.lattice.control.zmq;

import mon.lattice.control.ControlPlaneConsumerException;
import mon.lattice.core.plane.ControlPlaneMessage;
import mon.lattice.core.plane.ControlOperation;
import mon.lattice.distribution.MetaData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mon.lattice.control.ControlServiceException;
import mon.lattice.core.ID;
import mon.lattice.core.Measurement;
import mon.lattice.core.Rational;
import mon.lattice.core.Timestamp;
import mon.lattice.im.delegate.DCNotFoundException;
import mon.lattice.im.delegate.DSNotFoundException;
import mon.lattice.im.delegate.ProbeNotFoundException;
import mon.lattice.im.delegate.ReporterNotFoundException;
import mon.lattice.im.delegate.ZMQControlEndPointMetaData;


public class ZMQControlPlaneXDRProducer extends AbstractZMQControlPlaneProducer {    
    protected ZMQXDRRequester requester;
    
    /**
     * Creates a Producer without announce/deannounce management capabilities
     */
    public ZMQControlPlaneXDRProducer(int port) {
        super(port);
        try {
            requester = new ZMQXDRRequester(zmqRouter.getContext());
        } catch (IOException ioe) {
             LOGGER.error("Error while connecting " + ioe.getMessage());
        }
    }
    
    
    @Override
    public boolean connect() {
        return super.connect();
    }

    @Override
    public boolean disconnect() {
        return super.disconnect();
    }
    
    
    
    @Override
    public ID loadProbe(ID dataSourceID, String probeClassName, Object ... probeArgs) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(dataSourceID);
        args.add(probeClassName);
        args.add(probeArgs);
        
        ID probeID = null;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.LOAD_PROBE, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromID(dataSourceID);
            
            MetaData mData;
            if (dstAddr.getType().equals("zmq")) {
                mData = new ZMQControlMetaData(dataSourceID.toString());                
                probeID = (ID) requester.synchronousTransmit(m, mData);
                
                // should wait until the info plane message for that probe is received
                infoPlaneDelegate.waitForAddedProbe(probeID, 5000);
            }  
        }
          catch (InterruptedException e) {
            LOGGER.error("Interrupted while loading probe: " + e.getMessage());
            throw new ControlServiceException(e);
          }
          catch (IOException | DSNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing load probe command: " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        return probeID;
    }

    @Override
    public boolean unloadProbe(ID probeID) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.UNLOAD_PROBE, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData);
            
            infoPlaneDelegate.waitForRemovedProbe(probeID, 5000);
        }
        catch (InterruptedException e) {
            LOGGER.error("Interrupted while unloading probe: " + e.getMessage());
            throw new ControlServiceException(e);
          }
        catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing unload probe command " + ex.getMessage());
            throw new ControlServiceException(ex);
        }
        
        return result;
    }
    
    @Override
    public String getProbeName(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeName(ID probeID, String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeServiceID(ID probeID, ID id) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        args.add(id);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_PROBE_SERVICE_ID, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData);
        }
        catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing set probe service ID command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }        
        return result;
        
    }
    
    
    @Override
    public ID getProbeServiceID(ID probeID) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        ID result;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.GET_PROBE_SERVICE_ID, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (ID) requester.synchronousTransmit(m, mData);
        }
        catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing get probe Service ID command " + ex.getMessage());
            throw new ControlServiceException(ex);
        }
        
        return result;
        
    }
    

    @Override
    public ID getProbeGroupID(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setProbeGroupID(ID probeID, ID id) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        args.add(id);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_PROBE_GROUP_ID, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData);
        }        
        catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing set probe group ID command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        
        return result;    
    }
    
    @Override
    public Rational getProbeDataRate(ID probeID) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        Rational result;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.GET_PROBE_DATA_RATE, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Rational) requester.synchronousTransmit(m, mData);
        
        }        
        catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing get probe data rate  command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        
        return result;
        
    }

    @Override
    public boolean setProbeDataRate(ID probeID, Rational dataRate) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        args.add(dataRate);
        Boolean result = false;

        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.SET_PROBE_DATA_RATE, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData);
        }
          catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing set probe data rate command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        
        return result;
    }

    @Override
    public Measurement getProbeLastMeasurement(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Timestamp getProbeLastMeasurementCollection(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean turnOnProbe(ID probeID) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.TURN_ON_PROBE, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData);
            return result;
        
        }  
         catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing turn on probe command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        }

    @Override
    public boolean turnOffProbe(ID probeID) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(probeID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.TURN_OFF_PROBE, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromProbeID(probeID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData); 
            return result;
        }
        
        catch (IOException | DSNotFoundException | ProbeNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing turn off probe command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        }
    

    @Override
    public boolean isProbeOn(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean activateProbe(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deactivateProbe(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isProbeActive(ID probeID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDataSourceInfo(ID id) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(id);
        
        String name = null;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.GET_DS_NAME, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDSAddressFromID(id);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            name = (String) requester.synchronousTransmit(m, mData);
            return name;
        }
        catch (IOException | DSNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing getDataSourceName command " + ex.getMessage());
            throw new ControlServiceException(ex);
        }
    }

    @Override
    public boolean setDataSourceName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    /* DC Control Service methods */
    
    @Override
    public Rational getDCMeasurementsRate(ID dcId) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(dcId);
        
        Rational rate;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.GET_DC_RATE, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDCAddressFromID(dcId);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            rate = (Rational) requester.synchronousTransmit(m, mData);
        }

        catch (IOException | DCNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing getDCMeasurementsRate command " + ex.getMessage());
            throw new ControlServiceException(ex);
        }
        return rate;
    }

    @Override
    public ID loadReporter(ID dataConsumerID, String reporterClassName, Object... reporterArgs) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(dataConsumerID);
        args.add(reporterClassName);
        args.add(reporterArgs);
        
        ID reporterID = null;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.LOAD_REPORTER, args);
        
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDCAddressFromID(dataConsumerID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            reporterID = (ID) requester.synchronousTransmit(m, mData);
        }
        catch (IOException | DCNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing loadReporter command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        return reporterID;
    }
    
    
    @Override
    public boolean unloadReporter(ID reporterID) throws ControlServiceException {
        List<Object> args = new ArrayList();
        args.add(reporterID);
        Boolean result = false;
        
        ControlPlaneMessage m=new ControlPlaneMessage(ControlOperation.UNLOAD_REPORTER, args);
        try {
            ZMQControlEndPointMetaData dstAddr = (ZMQControlEndPointMetaData)infoPlaneDelegate.getDCAddressFromReporterID(reporterID);
            MetaData mData = new ZMQControlMetaData(dstAddr.getId().toString());
            result = (Boolean) requester.synchronousTransmit(m, mData);
        }
        catch (IOException | DCNotFoundException | ReporterNotFoundException | ControlPlaneConsumerException ex) {
            LOGGER.error("Error while performing unloadReporter command " + ex.getMessage());
            throw new ControlServiceException(ex);
          }
        return result;
    }
}
