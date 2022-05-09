package net.codejava.crypto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * A tester for the CryptoUtils class.
 * @author www.codejava.net
 *
 */
public class CryptoUtilsTest {
	public static void main(String[] args) {
		
		String key= "0123456789abcdef0123456789abcdef";
		
		File inputFile = new File("inputFile");
		File encryptedFile = new File("document.encrypted");

	
		try {
			CryptoUtils.encrypt(key, inputFile, encryptedFile);
			CryptoUtils.decrypt(key, encryptedFile, inputFile);
		} catch (CryptoException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		
		
		
		  FileWriter fw = null;
	        BufferedWriter bw = null;
	        PrintWriter pw = null;
		
		
		try {
			fw = new FileWriter("inputFile", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        bw = new BufferedWriter(fw);
        pw = new PrintWriter(bw);

        pw.println("second text");
       
        

        System.out.println("Data Successfully appended into file");
        pw.flush();
		
        

        
        
        try {
			CryptoUtils.encrypt(key, inputFile, encryptedFile);
			CryptoUtils.decrypt(key, encryptedFile, inputFile);
		} catch (CryptoException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		
		
		
		
		
	}
}
