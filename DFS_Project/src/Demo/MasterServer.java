package Demo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.*;
import java.rmi.*;

public class MasterServer implements MasterReplicaInterface, MasterServerClientInterface, Remote{
	
	static Registry registry ;
	
	private int nextTID;
	private Random randomGen;
	private int replicationN = 1; //number of replicas
	private Map<String,	 List<ReplicaLoc> > filesLocationMap;
	private Map<String,	 ReplicaLoc> primaryReplicaMap;
	private List<ReplicaLoc> replicaServersLocs;
	private List<ReplicaMasterInterface> replicaServersStubs; 
	
	public MasterServer() {
		filesLocationMap = new HashMap<String, List<ReplicaLoc>>();
		primaryReplicaMap = new HashMap<String, ReplicaLoc>();
		replicaServersLocs = new ArrayList<ReplicaLoc>();
		replicaServersStubs = new ArrayList<ReplicaMasterInterface>();
		nextTID = 0;
		randomGen = new Random();
		
	}
	
	private void createNewFile(String fileName) {
		System.out.println("Creating new file initiated");
		int luckyReplicas[] = new int[replicationN];
		List<ReplicaLoc> replicas = new ArrayList<ReplicaLoc>();
		Set<Integer> chosenReplicas = new TreeSet<Integer>();
		
		for(int i=0; i<luckyReplicas.length;i++) {
			do {
				luckyReplicas[i] = randomGen.nextInt(replicationN);
			}while(!replicaServersLocs.get(luckyReplicas[i]).isAlive() || chosenReplicas.contains(luckyReplicas[i]));
			
			chosenReplicas.add(luckyReplicas[i]);
			replicas.add(replicaServersLocs.get(luckyReplicas[i]));
		
			try {
				replicaServersStubs.get(luckyReplicas[i]).createFile(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		int primary = luckyReplicas[0];
		try {
			replicaServersStubs.get(primary).takeCharge(fileName, replicas);			
		}catch(RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
		filesLocationMap.put(fileName, replicas);
		primaryReplicaMap.put(fileName, replicaServersLocs.get(primary));
	}
	
	public List<ReplicaLoc> read(String fileName) throws FileNotFoundException, IOException, RemoteException{
		List<ReplicaLoc> replicaLocs = filesLocationMap.get(fileName);
		if(replicaLocs == null) {
			throw new FileNotFoundException();
		}
		return replicaLocs;
		}
	
	public List<ReplicaLoc> readReplicas() throws FileNotFoundException, IOException, RemoteException{
		List<ReplicaLoc> replicaLocs = replicaServersLocs;
		
		return replicaLocs;
		}
	
	public WriteAck write(String fileName) throws RemoteException, IOException{
		System.out.println("Write request initiated");
		long timeStamp = System.currentTimeMillis();
		
		List<ReplicaLoc> replicaLocs = filesLocationMap.get(fileName);
		int tid = nextTID+1;
		if (replicaLocs == null) {
			createNewFile(fileName);
		}
		
		ReplicaLoc primaryReplicaLoc = primaryReplicaMap.get(fileName);
		
		if (primaryReplicaLoc == null)
			throw new IllegalStateException("No primary replica found");
		
		return new WriteAck(tid, timeStamp,primaryReplicaLoc);
	}
	
	
	public void registerReplicaServer(ReplicaLoc replicaLoc, ReplicaInterface replicaStub) {
		replicaServersLocs.add(replicaLoc);
		replicaServersStubs.add((ReplicaMasterInterface) replicaStub);
		
	}
	
	public static MasterServer startMaster() throws AccessException, RemoteException{
		MasterServer master = new MasterServer();
		MasterServerClientInterface stub = 
				(MasterServerClientInterface) UnicastRemoteObject.exportObject(master, 0);
		registry.rebind("MasterServerClientInterface", stub);
		System.out.println("Server ready");
		return master;
	}
	
	public static void connectToReplicaServers(MasterServer master)throws IOException, NotBoundException{
		System.out.println("Contacting replica servers ");
		BufferedReader br = new BufferedReader(new FileReader("ReplicaDetails.txt"));
		int n = Integer.parseInt(br.readLine().trim());
		ReplicaLoc replicaLoc;
		String s;
		String[] s1;
		String port;

		for (int i = 0; i < n; i++) {
			s = br.readLine().trim();
			s1 = s.split(":");
			port = s1[1];
			replicaLoc = new ReplicaLoc(i, s1[0] ,Integer.parseInt(port), true);
//			Registry registry = LocateRegistry.getRegistry(s1[0],Integer.parseInt(port));
			Registry registry = LocateRegistry.getRegistry(Integer.parseInt(port));
			ReplicaMasterInterface stub1 = (ReplicaMasterInterface) registry.lookup("ReplicaClient"+i);

			master.registerReplicaServer(replicaLoc, stub1);

			System.out.println("replica server state: "+stub1.isAlive());
		}
		br.close();
	}
	
	public static void main(String[] args) throws IOException, NotBoundException {
		try {
			int regPort = 59218;
//			System.setProperty("java.rmi.server.hostname", "10.200.152.195");
			LocateRegistry.createRegistry(regPort);
			registry = LocateRegistry.getRegistry(regPort);
//			registry = LocateRegistry.getRegistry("10.200.152.195", regPort);
			MasterServer master = startMaster();
			connectToReplicaServers(master);
			
			
		}catch(RemoteException e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
		

}
