package Demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Client{
	
	static MasterServerClientInterface masterStub;
	static Registry registry;
	static String decodedFile;
  static String userloggedIn;

	
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
		String result;
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		String permission = Client.fetchPermissions(fileName);
		if (permission.contains("r") || permission == "owner"){
			result = "Proceed";
		}
		else {
			result = "Deny";
		}
		if(result == "Proceed"){
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
		else {
			System.out.println("Access denied");
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
	public void write(String fileName, byte[] data) throws IOException, NotBoundException, MessageNotFoundException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		
		
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
	// code for registering new user 
	public static void registeruser(String username, String password) throws IOException {
		String userdetails = username+":"+password;
		//BufferedReader br = new BufferedReader(new FileReader("UserDetails.txt"));
		 Path fileName = Path.of(
		            "UserDetails.txt");
		 BufferedWriter bw = Files.newBufferedWriter(fileName, StandardOpenOption.APPEND);
		 bw.write(userdetails);
		 bw.newLine();
		 //Files.writeString(fileName, userdetails,StandardOpenOption.APPEND);
		 bw.close();
		 System.out.println("User registered successfully");
	}
	// code for login user
	public static void loginUser(String username, String password) throws IOException {
		String loginResult = masterStub.loginUser(username, password);
		System.out.println("LOgin result is:"+loginResult);
		if (loginResult != ""){
			System.out.println("User logged in successfully");
		}
		else {
			System.out.println("Error in login");
		}
		userloggedIn = username;
	}
	// call master to create permission for a file
	public static void createPermissions(String filename, String owner, String permissions) throws IOException {
//		JSONObject permissionobject = new JSONObject();
//		JSONArray permissiondetails = new JSONArray();  
//		permissiondetails.add(owner);
//		permissiondetails.add(permissions);
//		permissionobject.put(filename, permissiondetails);
//		File file=new File("Permissions.json");  
//        file.createNewFile();  
//        FileWriter fileWriter = new FileWriter(file);  
//        System.out.println("Writing JSON object to file");  
//        System.out.println("-----------------------");  
//        System.out.print(permissionobject);  
//
//        fileWriter.write(permissionobject.toJSONString());
//        fileWriter.flush();  
//        fileWriter.close(); 
		masterStub.setPermission(filename, owner, permissions);
		
	}
	//call master to get permission details for operations
	public static String fetchPermissions(String filename) throws RemoteException {
		 String permission = masterStub.fetchPermission(filename, userloggedIn);
		 if(permission == "owner"){
			 return "owner";
			 }
		 else {
			 return permission;
		 }
	}
	
	public static void main(String arg[]) throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		SecureRandom random = new SecureRandom(); // cryptograph. secure random 
		keyGen.init(random); 
		SecretKey secretKey = keyGen.generateKey();
		
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
					
					//ReplicaServer.decryptfileName(secretKey);
					ReplicaServer replicaServer = new ReplicaServer(0, null);

					//String decryptFileName=replicaServer.decryptedFileName(secretKey);
					//byte[] decodedBytes = Base64.getDecoder().decode(decodedFile);
					//String decodedFileName = new String(decodedBytes);
					//System.out.println("file name: "+ decodedFileName);
					System.out.print("Enter file name: ");
					String fname = input.nextLine();
					String encodedFileName = Base64.getEncoder().encodeToString(fname.getBytes());
					replicaServer.decryptedFile(secretKey, encodedFileName);
					c.read(encodedFileName);
					break;
				case "write":
					System.out.println("Enter file name: ");
					String fwname = input.nextLine();

					File tempfile = new File(".//Replica_0/"+fwname);
					boolean fileexists = tempfile.exists();
					if(fileexists != true) {
						System.out.println("Define permissions for the file:");
						String permissions = input.nextLine();
						Client.createPermissions(fwname, userloggedIn, permissions);
					}
					System.out.println("Enter the content to be written: ");

                    ReplicaServer replicaServer1 = new ReplicaServer(0, null);

					//String encryptFileName=replicaServer1.encryptedFileName(secretKey,fwname);
                	String encodedFileName1 = Base64.getEncoder().encodeToString(fwname.getBytes());
                	System.out.println("Enter the content to be written: ");

					String content = input.nextLine();	
					byte[] bcontent = content.getBytes(StandardCharsets.UTF_8);
					c.write(encodedFileName1,bcontent);
					replicaServer1.encryptedFile(secretKey, encodedFileName1);
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
				case "registeruser":
					System.out.println("Enter username:");
					Scanner u1 = new Scanner(System.in);
					String uname = u1.nextLine();
					System.out.println("Enter password:");
					Scanner p1 = new Scanner(System.in);
					String pword = p1.nextLine();
					Client.registeruser(uname, pword);
					break;
				case "loginuser":
					System.out.println("Enter username:");
					Scanner u2 = new Scanner(System.in);
					String usname = u2.nextLine();
					System.out.println("Enter password:");
					Scanner p2 = new Scanner(System.in);
					String paword = p2.nextLine();
					Client.loginUser(usname, paword);
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

