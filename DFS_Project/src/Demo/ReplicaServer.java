package Demo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReplicaServer implements ReplicaServerClientInterface, ReplicaMasterInterface, Remote{

	public int id;
	public String dir;
	private static Registry registry;
	private String readContent;
	public static String encodedString;
	public static String decodedString;

	
	
	private Map<Long, String> activeTxn; 
	private Map<Long, Map<Long, byte[]>> txnFileMap;
	private Map<String,	 List<ReplicaInterface> > filesReplicaMap;
	private Map<Integer, ReplicaLoc> replicaServersLoc;
	private Map<Integer, ReplicaInterface> replicaServersStubs; 
	private ConcurrentMap<String, ReentrantReadWriteLock> locks; 

	public ReplicaServer(int id, String dir) {
		this.id = id;
		this.dir = dir+"/Replica_"+ id +"/";
		
		txnFileMap = new TreeMap<Long, Map<Long, byte[]>>();
		activeTxn = new TreeMap<Long, String>();
		filesReplicaMap = new TreeMap<String, List<ReplicaInterface>>();
		replicaServersLoc = new TreeMap<Integer, ReplicaLoc>();
		replicaServersStubs = new TreeMap<Integer, ReplicaInterface>();
		locks = new ConcurrentHashMap<String, ReentrantReadWriteLock>();
		
		File file = new File(this.dir);
		if (!file.exists()){
			file.mkdir();
		}
		
		
	}
	/*public static String encryptedFileName(SecretKey secretKey, String fileInputPath)
			   throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
			   IllegalBlockSizeException, BadPaddingException {
		
		byte[] raw = secretKey.getEncoded();

		  SecretKey key = new SecretKeySpec(raw, "AES");
		  Cipher cipher = Cipher.getInstance("AES");
		  cipher.init(Cipher.ENCRYPT_MODE, key);
		//inputfile = fileInputPath.getBytes();	  
	    //cipher.update(inputfile);
		//return inputfile;
		  //byte[] inputfile = fileInputPath.getBytes();	  
		   //cipher.update(inputfile);
		   //byte[] cipherText = cipher.doFinal();
		   //String s = new String(cipherText, StandardCharsets.UTF_8);
		   String encodedString = Base64.getEncoder().encodeToString(fileInputPath.getBytes());

		return encodedString;
		

		   //return s;
		
	}
	public static String decryptedFileName(SecretKey secretKey)
			   throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
			   IllegalBlockSizeException, BadPaddingException {
		
		//byte[] raw = secretKey.getEncoded();

		//  SecretKey key = new SecretKeySpec(raw, "AES");
		  //Cipher cipher = Cipher.getInstance("AES");
		  //cipher.init(Cipher.DECRYPT_MODE, key);
	    //cipher.update(encodedString);
	    //byte[] cipherText = cipher.doFinal();
		  // return cipherText;
		byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
		   String decodedString = new String(decodedBytes);
		   return decodedString;
		   
		
	}*/
	public void encryptedFile(SecretKey secretKey, String encryptFileName)
			   throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
			   IllegalBlockSizeException, BadPaddingException {
			  byte[] raw = secretKey.getEncoded();

			  SecretKey key = new SecretKeySpec(raw, "AES");
			  Cipher cipher = Cipher.getInstance("AES");
			  cipher.init(Cipher.ENCRYPT_MODE, key);
			  Path currentRelativePath = Paths.get("");
			  String s = currentRelativePath.toAbsolutePath().toString();
			  //Path path = FileSystems.getDefault().getPath(fileInputPath);
			  //String s = path.toString();
			  var fileInput = new File(s+"/Replica_"+ id +"/"+encryptFileName) ;
			  var inputStream = new FileInputStream(fileInput);
			  var inputBytes = new byte[(int) fileInput.length()];
			  inputStream.read(inputBytes);
			  //inputfile = fileInputPath.getBytes();	  
		      //cipher.update(inputfile);


			  var outputBytes = cipher.doFinal(inputBytes);
			  var fileEncryptOut = new File(s+"/Replica_"+ id +"/"+encryptFileName);
			  var outputStream = new FileOutputStream(fileEncryptOut);
			  outputStream.write(outputBytes);

			  inputStream.close();
			  outputStream.close();
			  
			  System.out.println("File successfully encrypted!");
		     			  //System.out.println("New File: " + fileOutPath);
			 }
	

	public void decryptedFile(SecretKey secretKey, String fileOutPath)
			   throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
			   IllegalBlockSizeException, BadPaddingException {
				 byte[] raw = secretKey.getEncoded();
				 SecretKey key = new SecretKeySpec(raw, "AES");
				 var cipher = Cipher.getInstance("AES");
				 cipher.init(Cipher.DECRYPT_MODE, key);
				 Path currentRelativePath = Paths.get("");
				  String s = currentRelativePath.toAbsolutePath().toString();
				  //byte[] input = fileInputPath.getBytes();	  
			      //cipher.update(input);


			  var fileInput = new File(s+"/Replica_"+ id +"/"+fileOutPath);
			  var inputStream = new FileInputStream(fileInput);
			  var inputBytes = new byte[(int) fileInput.length()];
			  inputStream.read(inputBytes);

			  byte[] outputBytes = cipher.doFinal(inputBytes);
              
			  var fileEncryptOut = new File(s+"/Replica_"+ id +"/"+fileOutPath);
			  var outputStream = new FileOutputStream(fileEncryptOut);
			  outputStream.write(outputBytes);

			  inputStream.close();
			  outputStream.close();
			  
			  System.out.println("File successfully decrypted!");
			  //System.out.println("New File: " + "\"C:\\Users\\sai jahnavi\\Desktop\\textdecrypt.txt\"");
			 }
	
	public void createFile(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		/*KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		SecureRandom random = new SecureRandom(); // cryptograph. secure random 
		keyGen.init(random); 
		SecretKey secretKey = keyGen.generateKey();*/
		
		File file = new File(dir+fileName);
		
		locks.putIfAbsent(fileName, new ReentrantReadWriteLock());
		ReentrantReadWriteLock lock = locks.get(fileName);
		
		lock.writeLock().lock();
		file.createNewFile();
		lock.writeLock().unlock();
		System.out.println("File created successfully");
//		Encryption.encryptedFile(secretKey, fileName, fileName);


	}
	
	public String read(String fileName) throws FileNotFoundException, RemoteException, IOException {
		
		try {
			File f = new File(dir+fileName);
			Scanner sc = new Scanner(f);
			sc.useDelimiter("\\Z");
			readContent = sc.next();
			System.out.println("File read successfully");
			
			sc.close();
			}
			
			catch(FileNotFoundException e) {
				System.out.println("File not found");
				readContent = "";
			}
		
		return readContent;
		
	}
	public boolean delete(String fileName) throws RemoteException {
		File f = new File(dir+fileName);
		locks.putIfAbsent(fileName, new ReentrantReadWriteLock());
		if(f.delete()) {
			System.out.println("File deleted");
			return true;
		}
		else {
			System.out.println("Cannot delete file");
			return false;
		}
		
	}
	
	@Override
	public String rename(String oldfileName, String newFileName) throws IOException {
		File oldf = new File(dir+oldfileName);
		File newf = new File(dir+newFileName);
		String s = null;
		if(!oldf.exists()) {
			s = "File doesnot exists";
			System.out.println("File does not exists");
		}
		if (newf.exists()) {
			s = "File name already exists";
			System.out.println("File name already exists");
		}
		boolean success = oldf.renameTo(newf);
		if(success) {
			System.out.println("Rename successful");
			s = "Rename action done successfully";
		}
		return s;
		
	}
	
	public ChunkAck write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		if (!txnFileMap.containsKey(txnID)){
			txnFileMap.put(txnID, new TreeMap<Long, byte[]>());
			activeTxn.put(txnID, data.getFileName());
		}

		Map<Long, byte[]> chunkMap =  txnFileMap.get(txnID);
		chunkMap.put(msgSeqNum, data.getData());
		return new ChunkAck(txnID, msgSeqNum);
	}
	
	public boolean commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException, IOException {
		
		
		Map<Long, byte[]> chunkMap = txnFileMap.get(txnID);
		if (chunkMap.size() < numOfMsgs)
			throw new MessageNotFoundException();
		
		String fileName = activeTxn.get(txnID);
		System.out.println("FileName "+fileName);
		
		BufferedOutputStream bw =new BufferedOutputStream(new FileOutputStream(dir+fileName, true));
		
		locks.putIfAbsent(fileName, new ReentrantReadWriteLock());
		ReentrantReadWriteLock lock = locks.get(fileName);
		
		lock.writeLock().lock();
		for (Iterator<byte[]> iterator = chunkMap.values().iterator(); iterator.hasNext();) 
			bw.write(iterator.next());
		bw.close();
		lock.writeLock().unlock();
		
		activeTxn.remove(txnID);
		txnFileMap.remove(txnID);
		
		return false;
	}
	
	public void takeCharge(String fileName, List<ReplicaLoc> slaveReplicas) throws AccessException, RemoteException, NotBoundException {
		List<ReplicaInterface> slaveReplicasStubs = new ArrayList<ReplicaInterface>(slaveReplicas.size());
		
		for (ReplicaLoc loc : slaveReplicas) {
			if (loc.getId() == this.id) {
				continue;
			}
			if (!replicaServersLoc.containsKey(loc.getId())){
				replicaServersLoc.put(loc.getId(), loc);
				ReplicaInterface stub = (ReplicaInterface) registry.lookup("ReplicaClient"+loc.getId());
				replicaServersStubs.put(loc.getId(), stub);
			}
			ReplicaInterface replicaStub = replicaServersStubs.get(loc.getId());
			slaveReplicasStubs.add(replicaStub);
		}
		
		filesReplicaMap.put(fileName, slaveReplicasStubs);
	}
	
	public boolean isAlive() {
		return true;
	}
	
	public static void main(String[] args) {
//		run the below line for each replica
//		ReplicaServer rs = new ReplicaServer(1, "./");
		ReplicaServer rs = new ReplicaServer(0, "./");
		ReplicaInterface stub = null;
		try {
//			System.setProperty("java.rmi.server.hostname", "10.200.152.195");
//			change the port number for each replica according the port given in repServer.txt file
			LocateRegistry.createRegistry(50005);
			registry = LocateRegistry.getRegistry(50005);
			stub = (ReplicaInterface) UnicastRemoteObject.exportObject(rs, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
//			run the below line for each replica by changing the ReplicaClient value
//			registry.rebind("ReplicaClient1", stub);
			registry.rebind("ReplicaClient0", stub);
			System.out.println("Replica server ready");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	

}
