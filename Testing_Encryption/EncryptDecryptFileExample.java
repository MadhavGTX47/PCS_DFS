import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecryptFileExample {

 public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
   IllegalBlockSizeException, BadPaddingException, IOException {
  //var key = "jackrutorial.com";
  KeyGenerator keyGen = KeyGenerator.getInstance("AES");
  SecureRandom random = new SecureRandom(); // cryptograph. secure random 
  keyGen.init(random); 
  SecretKey secretKey = keyGen.generateKey();
  
  System.out.println("File input: " + "\"C:\\Users\\sai jahnavi\\Desktop\\text1.txt\"");

  //encryptedFile
  encryptedFile(secretKey, "\"C:\\Users\\sai jahnavi\\Desktop\\text1.txt\"", "\"C:\\Users\\sai jahnavi\\Desktop\\textencrypt.txt\"");
  
  //decryptedFile
  decryptedFile(secretKey, "\"C:\\Users\\sai jahnavi\\Desktop\\textencrypt.txt\"", "\"C:\\Users\\sai jahnavi\\Desktop\\textdecrypt.txt\"");
 }

 public static void encryptedFile(SecretKey secretKey, String fileInputPath, String fileOutPath)
   throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
   IllegalBlockSizeException, BadPaddingException {
  byte[] raw = secretKey.getEncoded();

  SecretKey key = new SecretKeySpec(raw, "AES");
  Cipher cipher = Cipher.getInstance("AES");
  cipher.init(Cipher.ENCRYPT_MODE, key);

  var fileInput = new File("C:\\Users\\sai jahnavi\\Desktop\\text1.txt");
  var inputStream = new FileInputStream(fileInput);
  var inputBytes = new byte[(int) fileInput.length()];
  inputStream.read(inputBytes);

  var outputBytes = cipher.doFinal(inputBytes);

  var fileEncryptOut = new File("C:\\Users\\sai jahnavi\\Desktop\\textencrypt.txt");
  var outputStream = new FileOutputStream(fileEncryptOut);
  outputStream.write(outputBytes);

  inputStream.close();
  outputStream.close();
  
  System.out.println("File successfully encrypted!");
  System.out.println("New File: " + "C:\\\\Users\\\\sai jahnavi\\\\Desktop\\\\textencrypt.txt");
 }

 public static void decryptedFile(SecretKey secretKey, String fileInputPath, String fileOutPath)
   throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
   IllegalBlockSizeException, BadPaddingException {
	 byte[] raw = secretKey.getEncoded();
	 SecretKey key = new SecretKeySpec(raw, "AES");
	 var cipher = Cipher.getInstance("AES");
	 cipher.init(Cipher.DECRYPT_MODE, key);

  var fileInput = new File("C:\\\\Users\\\\sai jahnavi\\\\Desktop\\\\textencrypt.txt");
  var inputStream = new FileInputStream(fileInput);
  var inputBytes = new byte[(int) fileInput.length()];
  inputStream.read(inputBytes);

  byte[] outputBytes = cipher.doFinal(inputBytes);

  var fileEncryptOut = new File("C:\\Users\\sai jahnavi\\Desktop\\textdecrypt.txt");
  var outputStream = new FileOutputStream(fileEncryptOut);
  outputStream.write(outputBytes);

  inputStream.close();
  outputStream.close();
  
  System.out.println("File successfully decrypted!");
  System.out.println("New File: " + "\"C:\\Users\\sai jahnavi\\Desktop\\textdecrypt.txt\"");
 }
}