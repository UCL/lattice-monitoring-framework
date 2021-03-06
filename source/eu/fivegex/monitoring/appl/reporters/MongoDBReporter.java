package eu.fivegex.monitoring.appl.reporters;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import mon.lattice.appl.reporters.ReporterException;
import mon.lattice.core.AbstractControllableReporter;
import mon.lattice.core.Measurement;
import mon.lattice.core.ProbeValue;
import mon.lattice.core.ProbeValueWithName;
import mon.lattice.core.Timestamp;
import mon.lattice.distribution.ConsumerMeasurementWithMetaDataAndProbeName;

public class MongoDBReporter extends AbstractControllableReporter {
    private final String mongoDBAddress;
    private final int mongoDBPort;
    private final String mongoDBName;
    private final String mongoDBCollectionName;
    
    private MongoDatabase db;
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    
    
    public MongoDBReporter(String address, String port, String dbName, String collectionName) throws ReporterException {
        this(address, Integer.valueOf(port), dbName, collectionName);
    }
    
    public MongoDBReporter(String address, int port, String dbName, String collectionName) throws ReporterException {
        super("mongoDB-reporter");
        this.mongoDBAddress = address;
        this.mongoDBPort = port;
        this.mongoDBName = dbName;
        this.mongoDBCollectionName = collectionName;
        
        this.MongoDBConnect();
    }
    
    private void MongoDBConnect() throws ReporterException { 
        System.out.println("Connecting to MongoDB Server...");

        this.mongoClient = new MongoClient(new ServerAddress(mongoDBAddress, mongoDBPort), MongoClientOptions.builder().serverSelectionTimeout(4000).build());
        try {
            this.db=mongoClient.getDatabase(mongoDBName);
            this.collection = db.getCollection(mongoDBCollectionName);
            // this should raise an exception if the above connection failed
            collection.count();
        } catch (Exception e) {
            throw new ReporterException(e);
        }
            
        System.out.println("Connected!");
    }
    
    @Override
    public void report(Measurement m) {
        String probeName = ((ConsumerMeasurementWithMetaDataAndProbeName)m).getProbeName();

        Timestamp t = ((ConsumerMeasurementWithMetaDataAndProbeName)m).getTimestamp();

        Document attributes = new Document();
        for (ProbeValue attribute : m.getValues()) {
            attributes.append(((ProbeValueWithName)attribute).getName(), attribute.getValue());
        }

        Bson doc1 = new Document("$set",
                                new Document(probeName + "." + t.toString(),       
                                // first arg was probeName      
                                // new Document().append(((ProbeValueWithName)m.getValues().get(0)).getName(), m.getValues().get(0).getValue())));
                                attributes));

        collection.updateOne(new Document("_id", m.getServiceID().toString()), doc1);
    }


}
