
package core;

import javax.crypto.spec.*;
import gui.ExceptionDialog;
import gui.ExceptionDecryption;
import gui.ExceptionEncryption;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;


/**
 *
 * @author Arlene
 */
public class FileEncryptorAndDecryptor
{
    private File destinationFile;
    private double accumulator=0;
    
    
    private boolean HashEqual(byte[] keyHash_1,byte[] keyHash_2)
    {
        if(keyHash_1==keyHash_2)
            return true;
        if(Arrays.equals(keyHash_1, keyHash_2))
            return true;
        return false;
    }
    
    private byte[] hashCode(String key) throws NoSuchAlgorithmException
    {
        byte[] keyHash;
        final MessageDigest md = MessageDigest.getInstance("SHA-512");
        keyHash = md.digest(key.getBytes());
        return keyHash;
        
    }
    
    private byte[] hashCode(byte[] key) throws NoSuchAlgorithmException
    {
        byte[] keyHash;
        final MessageDigest md = MessageDigest.getInstance("SHA-512");
        keyHash = md.digest(key);
        return keyHash;
        
    }
    
    private byte[] divide(byte[]a, int from,int to)
    {
        int len_a = a.length;
        if (from>len_a) return null;
        if(to>len_a)
            to = len_a;
        byte[] d = new byte[to-from];
        for(int i=from,j=0;i<to;i++,j++)
        {
            d[j]=a[i];
        }
        return d;
    }
    
    public void encrypt(File file,String key, JTextArea progressTextField)
    {
        byte[] keyHash;
                
        if(!file.isDirectory())
        {
            try
            {
                keyHash=hashCode(key);
                
                destinationFile=new File(file.getAbsolutePath().concat(".enc"));
                if(destinationFile.exists())
                {
                    destinationFile.delete();
                    destinationFile=new File(file.getAbsolutePath().concat(".enc"));
                }
                
                BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
                byte[] plaintext = new byte[(int) file.length()];
                fileReader.read(plaintext);                
                fileReader.close();
                
                //generate key
                SecretKey aesKey = new SecretKeySpec(key.getBytes(),"AES");
                // create cipher
                Cipher aesCipher;
                aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                
                //create cipher
                aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
                
                //encrypt
                byte[] ciphertext = aesCipher.doFinal(plaintext);
          
                //hash file to guarantee integrity
                byte[] hashText = hashCode(plaintext);
                                
                //write to file.enc
                FileOutputStream fileWriter=new FileOutputStream (destinationFile);
                
                fileWriter.write(keyHash,0,64);
                fileWriter.write(ciphertext,0,ciphertext.length);
                fileWriter.write(hashText,0,64);
                
                fileWriter.close();
                
                if(!file.delete())
                {
                    new ExceptionEncryption(new javax.swing.JFrame(), true, file.getAbsolutePath()).setVisible(true);
                }
                
            } 
            catch (NoSuchAlgorithmException e)
            {
                new ExceptionDialog("NoSuchAlgorithmException!", "Something hugely badly unexpectadly went awfully wrong", e).setVisible(true);
                Logger.getLogger(FileEncryptorAndDecryptor.class.getName()).log(Level.SEVERE, null, e);
            }
            catch (SecurityException e)
            {
                new ExceptionDialog("File Security Error!!!", file+" doesn't allow you to do that!", e).setVisible(true);
            }
            catch (FileNotFoundException e)
            {
                new ExceptionDialog("File Not Found!!!", file+" not found!", e).setVisible(true);
            }
            catch (IOException e)
            {
                new ExceptionDialog("Can Not Read or Write file!!!", file+" can not be read or written!", e).setVisible(true);
            }
            catch (Exception e)
            {
                 new ExceptionDialog("Unexpected System Error!", "Something hugely badly unexpectadly went awfully wrong", e).setVisible(true);
            }
            
        }
    }
    
    public void decrypt(File file, String key, JTextArea progressTextField)
    {
        byte[] keyHash;
        if(!file.isDirectory())
        {
            try
            {
                keyHash=hashCode(key);
                                
                BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
                byte[] bodyFile = new byte[(int) file.length()];
                fileReader.read(bodyFile);                
                fileReader.close();                
                
                byte[] keyHashinFile = divide(bodyFile,0,64);
                byte[] cipherText = divide(bodyFile,64,bodyFile.length-64);
                
                if(HashEqual(keyHash, keyHashinFile))
                {
                    

                    //generate key
                    SecretKey aesKey = new SecretKeySpec(key.getBytes(),"AES");
                    // create cipher
                    Cipher aesCipher;
                    aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
                    
                    //encrypt
                    byte[] plaintext = aesCipher.doFinal(cipherText);
                    


                    //read hash_text
                    byte [] hashTextinFile = hashCode(plaintext);
                    byte [] hashText = divide(bodyFile,bodyFile.length-64,bodyFile.length);

                    if(!HashEqual(hashText,hashTextinFile))
                    {
                        new ExceptionDialog("NoSuchAlgorithmException!", "Something hugely badly unexpectadly went awfully wrong",null).setVisible(true);                        
                    }
                    
                    //write plaintext to file
                    destinationFile=new File(file.getAbsolutePath().toString().substring(0, file.getAbsolutePath().toString().length()-4));
                    FileOutputStream fileWriter=new FileOutputStream (destinationFile);
                    fileWriter.write(plaintext,0,plaintext.length);
                    fileWriter.close();
                    
                    
                    if(!file.delete())
                    {
                        new ExceptionDecryption(new javax.swing.JFrame(), true, file.getAbsolutePath()).setVisible(true);
                    }
                }
                else
                {
                    progressTextField.append("\nWrong Key. Try it again !!!\n");
                }
                
            }
            catch (NoSuchAlgorithmException e)
            {
                new ExceptionDialog("NoSuchAlgorithmException!", "Something hugely badly unexpectadly went awfully wrong", e).setVisible(true);
                Logger.getLogger(FileEncryptorAndDecryptor.class.getName()).log(Level.SEVERE, null, e);
            }
            catch (SecurityException e)
            {
                new ExceptionDialog("File Security Error!!!", file+" doesn't allow you to do that!", e).setVisible(true);
            }
            catch (FileNotFoundException e)
            {
                new ExceptionDialog("File Not Found!!!", file+" not found!", e).setVisible(true);
            }
            catch (IOException e)
            {
                new ExceptionDialog("Can Not Read or Write file!!!", file+" can not be read or written!", e).setVisible(true);
            }            
            catch (Exception e)
            {
                 new ExceptionDialog("Unexpected System Error!", "Something hugely badly unexpectadly went awfully wrong", e).setVisible(true);
            }            
        }
    }
    
    
}
