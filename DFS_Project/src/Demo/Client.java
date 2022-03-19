package Demo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class Client {
	
	static MasterServerClientInterface masterStub;
	static Registry registry;
	
	public Client() {
		try {
//			registry = LocateRegistry.getRegistry("10.200.12.99",59216);
			registry = LocateRegistry.getRegistry(59218);
			try {
				masterStub = (MasterServerClientInterface) registry.lookup("MasterServerClientInterface");
			} catch (NotBoundException | RemoteException e) {
				
				e.printStackTrace();
			}
			System.out.println("Connection established with master");
			
		}catch(RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void read(String fileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		ReplicaLoc replicaLoc = replicalocations.get(0);
//		registry = LocateRegistry.getRegistry(replicaLoc.getAddress(),replicaLoc.getPort());
		registry = LocateRegistry.getRegistry(replicaLoc.getPort());
		ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaLoc.getId());
		String fileContent = replicaStub.read(fileName);
		if(fileContent.isEmpty()) {
			System.out.println("File not found");
		}	
		else {
			System.out.println("File Content: ");
			System.out.println(fileContent);
			System.out.println("Read action completed successfully");
		}
		
	}
	public void delete(String fileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		for(int i = 0; i< replicalocations.size();i++) {
			ReplicaLoc replicaLoc = replicalocations.get(i);
			int replicaID = replicaLoc.getId();
//			registry = LocateRegistry.getRegistry(replicaLoc.getAddress(),replicaLoc.getPort());
			registry = LocateRegistry.getRegistry(replicaLoc.getPort());
			ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaID);
			boolean status  = replicaStub.delete(fileName);
			if (status) {
				System.out.println("Delete operation completed successfully");
			}
			else {
				System.out.println("File not found");
			}
			
			
		}
	}
		
	public void rename(String oldfileName, String newfileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		for(int i = 0; i< replicalocations.size();i++) {
			ReplicaLoc replicaLoc = replicalocations.get(i);
			int replicaID = replicaLoc.getId();
//			registry = LocateRegistry.getRegistry(replicaLoc.getAddress(), replicaLoc.getPort();
			registry = LocateRegistry.getRegistry(replicaLoc.getPort());
			ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaID);
			String result = replicaStub.rename(oldfileName, newfileName);
			System.out.println(result);
				
			}	
	}
	
	@SuppressWarnings("unused")
	public void write(String fileName, byte[] data) throws IOException, NotBoundException, MessageNotFoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		for(int i = 0; i< replicalocations.size();i++) {
			ReplicaLoc replicaLoc = replicalocations.get(i);
			int replicaID = replicaLoc.getId();
//			registry = LocateRegistry.getRegistry(replicaLoc.getAddress(),replicaLoc.getPort());
			registry = LocateRegistry.getRegistry(replicaLoc.getPort());
			ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaID);
			FileContent fileContent = new FileContent(fileName);
			fileContent.setData(data);
			ChunkAck chunkAck;
			chunkAck = replicaStub.write(i+1, 1, fileContent);
			System.out.println("Write action completed successfully");
			replicaStub.commit(i+1, 1);
			
		}
	}
	
	public static void main(String arg[]) {
		
		try {
			String con = "yes";
			Client c = new Client();
			Scanner input = new Scanner(System.in);
			boolean cont = true;
			do {
				System.out.println("Select the operation to be performed:\n1.Read\n2.Write\n3.Rename\n4.Delete");
				
				String operation = input.nextLine().toLowerCase();
				switch(operation) {
				case "read":
					System.out.print("Enter file name: ");
					String fname = input.nextLine();
					c.read(fname);
					break;
				case "write":
					System.out.println("Enter file name: ");
					String fwname = input.nextLine();
					System.out.println("Enter the content to be written: ");
					String content = input.nextLine();	
					byte[] bcontent = content.getBytes(StandardCharsets.UTF_8);
					c.write(fwname,bcontent);
					break;
				case "rename":
					System.out.print("Enter the file name to be changed: ");
					String oldfname = input.nextLine();
					System.out.print("Enter the new file name: ");
					String newfname = input.nextLine();
					c.rename(oldfname, newfname);
					break;		
				case "delete":
					System.out.print("Enter the file name: ");
					String fdname = input.nextLine();
					c.delete(fdname);
					break;
				default:
					System.out.println("Invalid Operation");
					break;
				}
				System.out.println("Do you still want to continue?yes/no: ");
				con = input.nextLine();
				if(con.contentEquals("no")) {
					cont = false;
					System.out.println("Task completed");
					break;
				}
			}while(cont == true);
			input.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}

