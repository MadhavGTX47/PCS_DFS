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
import java.util.List;
import java.util.Scanner;

public class Client {
	
	static MasterServerClientInterface masterStub;
	static Registry registry;
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
				String permission = fetchPermissions(fileName);
				if (permission.contains("r") || permission == "owner"){
					System.out.println("File Content: ");
					System.out.println(fileContent);
					System.out.println("Read action completed successfully");
					}
				else {
					System.out.println("Access denied");
				}
			}
	}
	public void delete(String fileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		String permission = fetchPermissions(fileName);
		if(permission.contains("x") || permission == "owner") {
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
		else {
			System.out.println("Access denied");
		}
	}
		
	public void rename(String oldfileName, String newfileName) throws IOException, NotBoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		String permission = fetchPermissions(oldfileName);
		if (permission.contains("x") || permission == "owner") {
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
		else {
			System.out.println("Access denied");
		}
	}
	
	@SuppressWarnings("unused")
	public void write(String fileName, byte[] data) throws IOException, NotBoundException, MessageNotFoundException{
		List<ReplicaLoc> replicalocations = masterStub.readReplicas();
		String permission = fetchPermissions(fileName);
		if (permission.contains("w") || permission == "owner") {
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
		else {
			System.out.println("Access denied");
		}
	}
	
	public boolean r1(String username, String password) {
		try {
			boolean flag = masterStub.registerNewUser(username,password);
			return flag;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean l1(String username, String password) {
		try {
			if( masterStub.loginUser(username, password)) {
				userloggedIn = username;
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	// code for registering new user 
	public void registeruser(String username, String password) throws IOException {
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
//		 ReadWriteXMLfile xml = new ReadWriteXMLfile();
//		 xml.WriteXML(username, password);
	}
	// code for login user
	public boolean loginUser(String username, String password) throws IOException {
		boolean loginResult = masterStub.loginUser(username, password);
		System.out.println("Login result is:"+loginResult);
		if (loginResult){
			System.out.println("User logged in successfully");
		}
		else {
			System.out.println("Error in login");
		}
		userloggedIn = username;
		return loginResult;
	}
	// call master to create permission for a file
	public void createPermissions(String filename, String owner, String permissions) throws IOException {
		masterStub.setPermission(filename, owner, permissions);
	}
	//call master to get permission details for operations
	public static String fetchPermissions(String filename) throws RemoteException {
		String permission = masterStub.fetchPermission(filename, userloggedIn);
//		System.out.println("Permission value line 190: " + permission);
//		if(permission.equals("owner")){
//			return "owner";
//			}
//		else {
//			return permission;
//		}
		return permission;
	}
	
	public boolean Operations(boolean cont, Client c, Scanner input) {
		try {
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
					File tempfile = new File(".//Replica_0/"+fwname);
					if(!tempfile.exists()) {
						System.out.println("Define permissions for the file:");
						String permissions = input.nextLine();
//						System.out.println("User "+userloggedIn);
						c.createPermissions(fwname, userloggedIn, permissions);
					}
					System.out.println("Enter the content to be written: ");
					String content = input.nextLine();	
					byte[] bcontent = content.getBytes(StandardCharsets.UTF_8);
					c.write(fwname,bcontent);						
					break;
				case "rename":
					System.out.print("Enter the file name to be changed: ");
					String oldfname = input.nextLine();
//					permission = fetchPermissions(oldfname);
//					if (permission.contains("write") || permission.equals("owner")) {
						System.out.print("Enter the new file name: ");
						String newfname = input.nextLine();
						c.rename(oldfname, newfname);
//					}
//					else {
//						System.out.println("Access denied");
//					}
					break;		
				case "delete":
					System.out.print("Enter the file name: ");
					String fdname = input.nextLine();
//					permission = fetchPermissions(fdname);
//					if (permission.contains("write") || permission.equals("owner")) {
						c.delete(fdname);
//					}
//					else {
//						System.out.println("Access denied");
//					}
					break;
				default:
					System.out.println("Invalid Operation");
					break;
				}
				System.out.println("Do you still want to continue?yes/no: ");
				String con = input.nextLine();
				if(con.contentEquals("no")) {
					cont = false;
					System.out.println("User logged out successfully!!");
					break;
				}
			}while(cont == true);
//			input.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return cont;
	}
	
	
	
	public static void main(String arg[]) {
		
		try {
			String con = "yes";
			Client c = new Client();
			Scanner input = new Scanner(System.in);
			boolean cont = true;
			boolean flag = false;
			do {
				System.out.println("SignIn or SignUp?");
				String login = input.nextLine().toLowerCase();
				switch(login) {
				case "signup":
					System.out.println("Enter username:");
					String uname = input.nextLine();
					System.out.println("Enter password:");
					String password = input.nextLine();
//					c.registeruser(uname, password);
					if (c.r1(uname, password)) {
						System.out.println("User registered successfully");
//						flag = c.Operations(cont, c, input);
					}
					else {
						System.out.println("Invalid inputs");
						flag = true;
					}
//					break;
				case "signin":
					System.out.println("Sign in");
					System.out.println("Enter username:");
					String username = input.nextLine();
					System.out.println("Enter password:");
					String pwd = input.nextLine();
					boolean access = c.l1(username, pwd);
					if(access) {
						System.out.println("Logged in Successfully");
						flag = c.Operations(cont,c,input);
					}else {
						System.out.println("Incorrect username or password");
						flag = true;
					}
					break;
				
				default:
					System.out.println("Invalid input");
					flag = true;
					break;
				}
				cont = flag;
//				System.out.println(flag);
//				System.out.println("Do you still want to continue?yes/no: ");
//				con = input.nextLine();
//				if(con.contentEquals("no")) {
//					cont = false;
//					System.out.println("Task completed");
//					break;
//			}
				}while(cont);
			input.close();
			
			
//			do {
//				System.out.println("Select the operation to be performed:\n1.Read\n2.Write\n3.Rename\n4.Delete");
//				
//				String operation = input.nextLine().toLowerCase();
//				switch(operation) {
//				case "read":
//					System.out.print("Enter file name: ");
//					String fname = input.nextLine();
//					c.read(fname);
//					break;
//				case "write":
//					System.out.println("Enter file name: ");
//					String fwname = input.nextLine();
//					File tempfile = new File(".//Replica_0/"+fwname);
//					boolean fileexists = tempfile.exists();
//					if(fileexists != true) {
//						System.out.println("Define permissions for the file:");
//						String permissions = input.nextLine();
//						Client.createPermissions(fwname, userloggedIn, permissions);
//					}
//					System.out.println("Enter the content to be written: ");
//					String content = input.nextLine();	
//					byte[] bcontent = content.getBytes(StandardCharsets.UTF_8);
//					c.write(fwname,bcontent);
//					break;
//				case "rename":
//					System.out.print("Enter the file name to be changed: ");
//					String oldfname = input.nextLine();
//					System.out.print("Enter the new file name: ");
//					String newfname = input.nextLine();
//					c.rename(oldfname, newfname);
//					break;		
//				case "delete":
//					System.out.print("Enter the file name: ");
//					String fdname = input.nextLine();
//					c.delete(fdname);
//					break;
//				case "registeruser":
//					System.out.println("Enter username:");
//					Scanner u1 = new Scanner(System.in);
//					String uname = u1.nextLine();
//					System.out.println("Enter password:");
//					Scanner p1 = new Scanner(System.in);
//					String pword = p1.nextLine();
//					Client.registeruser(uname, pword);
//					break;
//				case "loginuser":
//					System.out.println("Enter username:");
//					Scanner u2 = new Scanner(System.in);
//					String usname = u2.nextLine();
//					System.out.println("Enter password:");
//					Scanner p2 = new Scanner(System.in);
//					String paword = p2.nextLine();
//					Client.loginUser(usname, paword);
//					break;
//				default:
//					System.out.println("Invalid Operation");
//					break;
//				}
//				System.out.println("Do you still want to continue?yes/no: ");
//				con = input.nextLine();
//				if(con.contentEquals("no")) {
//					cont = false;
//					System.out.println("Task completed");
//					break;
//				}
//			}while(cont == true);
//			input.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}

