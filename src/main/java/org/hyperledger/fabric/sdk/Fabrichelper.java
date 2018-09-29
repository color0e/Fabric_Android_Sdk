package org.hyperledger.fabric.sdk;

import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <h1>HFJavaSDKBasicExample</h1>
 * <p>
 * Simple example showcasing basic fabric-ca and fabric actions.
 * The demo required fabcar fabric up and running.
 * <p>
 * The demo shows
 * <ul>
 * <li>connecting to fabric-ca</li>
 * <li>enrolling admin to get new key-pair, certificate</li>
 * <li>registering and enrolling a new user using admin</li>
 * <li>creating HF client and initializing channel</li>
 * <li>invoking chaincode query</li>
 * </ul>
 *
 * @author lkolisko
 */
public class Fabrichelper {

    private static final Logger log = Logger.getLogger(Fabrichelper.class);


    /**
     * Invoke blockchain query
     *
     * @param client The HF Client
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public static String queryBlockChain(HFClient client,String[] args) throws ProposalException, InvalidArgumentException {
        // get channel instance from client
        Channel channel = client.getChannel("mychannel");
        // create chaincode request
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        // build cc id providing the chaincode name. Version is omitted here.
        ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName("fabcar").build();
        qpr.setChaincodeID(fabcarCCId);
        // CC function to be called
        qpr.setFcn("queryAllCars");
        qpr.setArgs(args);
        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);
        // display response
        String stringResponse=null;
        for (ProposalResponse pres : res) {
            stringResponse = new String(pres.getChaincodeActionResponsePayload());
        //    log.info(stringResponse);
        }
        return stringResponse;
    }
	 /**
     * Invoke blockchain query
     *
     * @param client The HF Client
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public static boolean invokeBlockChain(HFClient client,String[] args) throws ProposalException, InvalidArgumentException,Exception {
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        boolean fresult=true;
    	
    	// get channel instance from client
        Channel channel = client.getChannel("mychannel");
        // create chaincode request
        TransactionProposalRequest tpr = client.newTransactionProposalRequest();
        // build cc id providing the chaincode name. Version is omitted here.
        ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName("fabcar").build();
        tpr.setChaincodeID(fabcarCCId);
        // CC function to be called
        tpr.setFcn("createCar");
        //String[] strs = new String[]{"CAR10","JANG","JANG","JANG","JANG"};
        tpr.setArgs(args);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes());
        tm2.put("method", "TransactionProposalRequest".getBytes());
        tm2.put("result", ":)".getBytes());
        tpr.setTransientMap(tm2);
        Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(tpr, channel.getPeers());
        for (ProposalResponse response : transactionPropResp) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                System.out.println("Successful transaction proposal response Txid: "+response.getTransactionID() + " from peer " +response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
                fresult=false;
            }
        }
        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp);
        if (proposalConsistencySets.size() != 1) {
            System.err.println("Expected only one set of consistent proposal responses but got "+proposalConsistencySets.size());
        }

        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
            System.err.println("Not enough endorsers for invoke (move a,b,100):" + failed.size() + " endorser error: " +
                    firstTransactionProposalResponse.getMessage() +
                    ". Was verified: " + firstTransactionProposalResponse.isVerified());
        }
        
        ProposalResponse resp = successful.iterator().next();
        byte[] x = resp.getChaincodeActionResponsePayload(); // This is the data returned by the chaincode.
        String resultAsString = null;
        if (x != null) {
            resultAsString = new String(x, "UTF-8");
        }
        System.out.println("Result returned from chaincode: "+resultAsString);
        System.out.println("Chaincode Status: "+resp.getChaincodeActionResponseStatus());
        System.out.println("Chaincode ID: "+resp.getChaincodeID());
        System.out.println("Chaincode PATH: "+resp.getChaincodeID().getPath());
        System.out.println("Chaincode NAME: "+resp.getChaincodeID().getName());
        System.out.println("Chaincode Version: "+resp.getChaincodeID().getVersion());
        

        /*channel.sendTransaction(successful).thenApply(transactionEvent -> {
            System.out.print("Response <transactionEvent>: "+transactionEvent.isValid());
            System.out.print("Response <transactionEvent TXID >: "+transactionEvent.getTransactionID());
            if(transactionEvent.isValid()==false) {
            	return false;
            }
            return true;
        });*/
        BlockEvent.TransactionEvent transactionEvent=channel.sendTransaction(successful).get(100, TimeUnit.SECONDS);
        System.out.println("isvalid:"+transactionEvent.isValid());
        System.out.println("transaction id:"+transactionEvent.getTransactionID());
        if(transactionEvent.isValid()==false) {
        	return false;
        }else {
        	return true;
        }
        

    }

    /**
     * Initialize and get HF channel
     *
     * @param client The HFC client
     * @return Initialized channel
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    public static Channel getChannel(HFClient client) throws InvalidArgumentException, TransactionException {
        // initialize channel
        // peer name and endpoint in fabcar network
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://220.123.29.45:7051");
        // eventhub name and endpoint in fabcar network
        EventHub eventHub = client.newEventHub("eventhub01", "grpc://220.123.29.45:7053");
        // orderer name and endpoint in fabcar network
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://220.123.29.45:7050");
        // channel name in fabcar network
        Channel channel = client.newChannel("mychannel");
        channel.addPeer(peer);
        channel.addEventHub(eventHub);
        channel.addOrderer(orderer);
        channel.initialize();
        return channel;
    }

    /**
     * Create new HLF client
     *
     * @return new HLF client instance. Never null.
     * @throws CryptoException
     * @throws InvalidArgumentException
     */
    public static HFClient getHfClient() throws Exception {
        // initialize default cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }


    /**
     * Register and enroll user with userId.
     * If AppUser object with the name already exist on fs it will be loaded and
     * registration and enrollment will be skipped.
     *
     * @param caClient  The fabric-ca client.
     * @param registrar The registrar to be used.
     * @param userId    The user id.
     * @return AppUser instance with userId, affiliation,mspId and enrollment set.
     * @throws Exception
     */
    public static AppUser getUser(HFCAClient caClient, AppUser registrar, String userId) throws Exception {
		AppUser appUser = null;
        if (appUser == null) {
            RegistrationRequest rr = new RegistrationRequest(userId, "org1");
            String enrollmentSecret = caClient.register(rr, registrar);
            Enrollment enrollment = caClient.enroll(userId, enrollmentSecret);
            appUser = new AppUser(userId, "org1", "Org1MSP", enrollment);
        }
        return appUser;
    }

    /**
     * Enroll admin into fabric-ca using {@code admin/adminpw} credentials.
     * If AppUser object already exist serialized on fs it will be loaded and
     * new enrollment will not be executed.
     *
     * @param caClient The fabric-ca client
     * @return AppUser instance with userid, affiliation, mspId and enrollment set
     * @throws Exception
     */
    public static AppUser getAdmin(HFCAClient caClient) throws Exception {
		AppUser admin = null;
        if (admin == null) {
            Enrollment adminEnrollment = caClient.enroll("admin", "adminpw");
            admin = new AppUser("admin", "org1", "Org1MSP", adminEnrollment);
        }
        return admin;
    }

    /**
     * Get new fabic-ca client
     *
     * @param caUrl              The fabric-ca-server endpoint url
     * @param caClientProperties The fabri-ca client properties. Can be null.
     * @return new client instance. never null.
     * @throws Exception
     */
    public static HFCAClient getHfCaClient(String caUrl, Properties caClientProperties) throws Exception {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient caClient = HFCAClient.createNewInstance(caUrl, caClientProperties);
        caClient.setCryptoSuite(cryptoSuite);
        return caClient;
    }


    // user serialization and deserialization utility functions
    // files are stored in the base directory

    /**
     * Serialize AppUser object to file
     *
     * @param appUser The object to be serialized
     * @throws IOException
     */
    static void serialize(AppUser appUser) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                Paths.get(appUser.getName() + ".jso")))) {
            oos.writeObject(appUser);
        }
    }

    /**
     * Deserialize AppUser object from file
     *
     * @param name The name of the user. Used to build file name ${name}.jso
     * @return
     * @throws Exception
     */
    static AppUser tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get(name + ".jso"))) {
            return deserialize(name);
        }
        return null;
    }

    static AppUser deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get(name + ".jso")))) {
            return (AppUser) decoder.readObject();
        }
    }
}
