
package core;

import gui.ProgressBar;
import gui.ExceptionDialog;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.*;

/**
 *
 * @author Arlene
 */
public class EncryptorAndDecryptor extends SwingWorker <Boolean,Boolean>
{
    File[] listFile;
    String encryptOrDecrypt;
    String key;
    FileEncryptorAndDecryptor fileEncryptorAndDecryptor;
    boolean completedTask;
    JProgressBar progressBar;
    JTextArea progressTextField;
    JLabel progressLabel;
    JButton oKButton;
    ProgressBar progressFrame;
    public EncryptorAndDecryptor(File[] listFile, String encryptOrDecrypt, String key)
    {
        this.listFile = listFile;
        this.encryptOrDecrypt = encryptOrDecrypt;
        this.key=key;
        progressFrame= new ProgressBar(encryptOrDecrypt);
        progressFrame.setVisible(true);
        fileEncryptorAndDecryptor = new FileEncryptorAndDecryptor();
        progressBar = progressFrame.getProgressBar();
        progressTextField = progressFrame.getProgressOfFilesTextField();
        progressLabel = progressFrame.getProgressPercentLabel();
        oKButton=progressFrame.getoKButton();
    }
    
    @Override
    protected Boolean doInBackground() 
    {
        try
        {
            if(encryptOrDecrypt.equalsIgnoreCase("encrypt"))
            {
                encrypt();
            }
            else if(encryptOrDecrypt.equalsIgnoreCase("decrypt"))
            {
                decrypt();            
            }
        }
        catch (Exception e)
        {
              new ExceptionDialog("Unexpected System Error!", "Something hugely badly unexpectadly went awfully wrong", e).setVisible(true);         
        }
        finally
        {
            return true;
        }
    }
    protected void done()
    {
        try
        {
        
            progressFrame.setCompletedTask(true);
            Toolkit.getDefaultToolkit().beep();
            oKButton.setVisible(true);
            oKButton.setEnabled(true);
            oKButton.setText("OK");
                    
        }
        catch (Exception e)
        {
            new ExceptionDialog("Unexpected System Error!", "Something hugely badly unexpectadly went awfully wrong", e).setVisible(true);
        }
    }
    
    
    private void encrypt()
    {
        for(File file:listFile)
        {
            encrypt(file);
        }
        progressBar.setValue(progressBar.getMaximum());
        progressLabel.setText("100%");
    }
    private void encrypt(File file)
    {
        long percent = 100/file.length();
        int totalPercent = progressBar.getValue();
        if(!file.isDirectory() && file.exists())
        {
            progressTextField.append("Encrypting "+file.getAbsolutePath()+"\n");
            fileEncryptorAndDecryptor.encrypt(file, key, progressTextField);
            progressTextField.append("Done!\n\n");
        }
        else if(file.isDirectory() && file.exists())
        {
            for(File f:file.listFiles())
            {
                encrypt(f);
                totalPercent += percent;
                progressBar.setValue((int)totalPercent);
                progressLabel.setText(String.valueOf(totalPercent)+"%");
            }
        }
    }
    
    private void decrypt()
    {
        for(File file:listFile)
        {
                decrypt(file);
        }
        progressBar.setValue(progressBar.getMaximum());
        progressLabel.setText("100%");
    }
    private void decrypt(File file)
    {
        long percent = 100/file.length();
        int totalPercent = progressBar.getValue();
        if(!file.isDirectory() && file.exists() && file.getName().substring(file.getName().length()-4, file.getName().length()).equalsIgnoreCase(".enc"))
        {
            progressTextField.append("Decrypting "+file.getAbsolutePath()+"\n");
            fileEncryptorAndDecryptor.decrypt(file, key, progressTextField);
            progressTextField.append("Done!\n\n");
        }
        else if(file.isDirectory() && file.exists())
        {
            for(File f:file.listFiles())
            {
                
                decrypt(f);
                totalPercent += percent;
                progressBar.setValue((int)totalPercent);
                progressLabel.setText(String.valueOf(totalPercent)+"%");
            }
        }
    }
    
}