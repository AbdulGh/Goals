package goals;

import java.io.*;
import java.util.zip.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class SaveFileManager
{
    private String currentFile;
    private RandomAccessFile historySeeker;
    
    private static final char recordsep = (char)30;
    
    public SaveFileManager()
    {
        currentFile = null;
        historySeeker = null;
    }
    
    public boolean selectNewProfile()
    {
        JFileChooser saveDialog = new JFileChooser();
        saveDialog.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnVal = saveDialog.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            currentFile = saveDialog.getSelectedFile().getName();
            return true;
        }
        return false;
    }
    
    public boolean saveas(GoalJList list, String todaysEntry)
    {
        if (selectNewProfile()) return save(list, todaysEntry);
        return false;
    }
    
    public boolean save(GoalJList list, String todaysEntry)
    {
        if (currentFile == null && !selectNewProfile()) return false;
        
        todaysEntry.replaceAll("[\030\031\000]", "");
        
        extractProfile(currentFile);
        int today = new ShortDate().getDays();
        
        if (!list.saveGoalsToFile(".goals"))
        {
            JOptionPane.showMessageDialog(null,
                    "Could not save goal list.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try
        {
            File pointers = new File(".pointers");
            RandomAccessFile raf = new RandomAccessFile(pointers, "r");
            raf.seek(pointers.length());
            long[] data = SavePointerManager.readCurrentPointer(raf);
            raf.close();

            if (data[0] == today)//overwrite todays goalhistory and notes
            {
                //overwrite notes
                raf = new RandomAccessFile(".notes", "rw");
                raf.seek(data[1]);
                raf.writeBytes(todaysEntry);
                raf.setLength(raf.getFilePointer());
                raf.close();
                
                //overwrite goal history
                raf = new RandomAccessFile(".ghistory", "rw");
                raf.seek(data[2]);
                        
                raf.writeBytes(list.getEdits());
                raf.setLength(raf.getFilePointer());
                raf.close();
            }
            
            else //add new day
            {
                File notes = new File(".notes");
                raf = new RandomAccessFile(notes, "rw");
                raf.seek(notes.length());
                raf.writeChar(recordsep);
                long notesPos = raf.getFilePointer();
                raf.writeBytes(todaysEntry);
                raf.setLength(raf.getFilePointer());
                raf.close();
                
                File ghistory = new File(".ghistory");
                raf = new RandomAccessFile(ghistory, "rw");
                raf.seek(ghistory.length());
                raf.writeChar(recordsep);
                long ghistoryPos = raf.getFilePointer();
                System.out.println("gPos: " + ghistoryPos);
                raf.writeBytes(list.getEdits());
                raf.setLength(raf.getFilePointer());
                raf.close();
                
                raf = new RandomAccessFile(pointers, "rw");
                raf.seek(pointers.length());
                raf.writeChar(recordsep);
                
                raf.writeInt(today);
                raf.writeLong(notesPos);
                raf.writeLong(ghistoryPos);
                raf.setLength(raf.getFilePointer());
                raf.close();
            }
            
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        
        return zipProfile(currentFile);
    }
    
    public boolean load(GoalJList list, JTextArea todaysEntry)
    {
        JFileChooser chooseDialog = new JFileChooser(); 

        chooseDialog.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int returnVal = chooseDialog.showOpenDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION) return true;
        
        String filename = chooseDialog.getSelectedFile().getName();
        currentFile = filename;

        extractProfile(currentFile);
        
        try
        {
            if (!list.loadGoalsFromFile(".goals")) return false;
            
            File pointers = new File(".pointers");
            RandomAccessFile raf = new RandomAccessFile(pointers, "r");
            raf.seek(pointers.length());
            long[] data = SavePointerManager.readCurrentPointer(raf);
            raf.close();
            
            if (data[0] == new ShortDate().getDays()) //load notes and ghistory
            {
                File notes = new File (".notes");
                raf = new RandomAccessFile(notes, "r");
                raf.seek(data[1]);
                todaysEntry.setText(readString(raf));
                raf.close();
                                
                File ghistory = new File (".ghistory");
                raf = new RandomAccessFile(ghistory, "r");
                raf.seek(data[2]);
                list.loadEditsFromString(readString(raf));
                raf.close();
            }
            
            deleteSaveFiles();
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(null,
                    "This is not a valid profile.",
                    "Invalid Profile",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
       
        return true;
    }
    
    public long goBack(GoalJList list, JTextArea todaysEntry)
    {
        try
        {
            if (historySeeker == null)
            {
                extractProfile(currentFile);
                File pointers = new File(".pointers");
                historySeeker = new RandomAccessFile(pointers, "r");
                historySeeker.seek(pointers.length() - 1);
            }
            
            long[] data = SavePointerManager.readPreviousPointer(historySeeker);
                
            RandomAccessFile raf = new RandomAccessFile(".notes", "r");
            raf.seek(data[1]);
            todaysEntry.setText(readString(raf));
            raf.close();
            
            raf = new RandomAccessFile(".ghistory", "r");
            raf.seek(data[2]);
            list.loadEditsFromString(readString(raf));
            raf.close();
            
            return data[0];
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(null,
                "This is not a valid profile.",
                "Invalid Profile",
                JOptionPane.ERROR_MESSAGE);
            return -1;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }
    
    private String readString(RandomAccessFile raf) throws IOException
    {
        String value = "";
        int c;
        while ((c = raf.read()) != recordsep && c != -1)
            value += (char)c;
        return value;
    }
    
    private boolean extractProfile(String fileName)
    {
        File profile = new File(fileName);
        
        if (!profile.exists())
        {
            try
            {
                profile.createNewFile();
                createNewProfile();
                return true;
            }
            catch (IOException e)
            {
                System.err.println("IOException creating a new profile...");
                e.printStackTrace();
                return false;
            }
        }
        
        try
        {
            deleteSaveFiles();
            ZipInputStream zin = new ZipInputStream(new FileInputStream(profile));
            
            //extract the three files
            int i;
            ZipEntry e = null;       
            for (i = 4; i > 0; i--)
            {
                e = zin.getNextEntry();
                if (e == null) break;
                
                FileOutputStream fout = new FileOutputStream(e.getName());
                for (int c = zin.read(); c != -1; c = zin.read()) fout.write(c);
                zin.closeEntry();
                fout.close();
            }
            
            if (i != 0 || zin.getNextEntry() != null)
            {
                JOptionPane.showMessageDialog(null,
                    "This is not a valid profile.",
                    "Invalid Profile",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Profile disappeared...");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.err.println("IOException reading files...");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private boolean zipProfile(String fileName)
    {
        try
        {
            File profile = new File(fileName);
        
            if (!profile.exists())
            {
                try
                {
                    profile.createNewFile();
                    createNewProfile();
                    return true;
                }
                catch (IOException e)
                {
                    System.err.println("IOException creating a new profile...");
                    e.printStackTrace();
                    return false;
                }
            }
            
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(currentFile));
            byte[] buf = new byte[2048];

            FileInputStream inputStream = new FileInputStream(".goals");
            zout.putNextEntry(new ZipEntry(".goals"));

            int read;
            while ((read = inputStream.read(buf)) > 0)
                zout.write(buf, 0, read);
            inputStream.close();
            zout.closeEntry();

            inputStream = new FileInputStream(".notes");
            zout.putNextEntry(new ZipEntry(".notes"));

            while ((read = inputStream.read(buf)) > 0)
                zout.write(buf, 0, read);
            inputStream.close();
            zout.closeEntry();
            
            inputStream = new FileInputStream(".pointers");
            zout.putNextEntry(new ZipEntry(".pointers"));

            while ((read = inputStream.read(buf)) > 0)
                zout.write(buf, 0, read);
            inputStream.close();
            zout.closeEntry();
            
            inputStream = new FileInputStream(".ghistory");
            zout.putNextEntry(new ZipEntry(".ghistory"));

            while ((read = inputStream.read(buf)) > 0)
                zout.write(buf, 0, read);
            inputStream.close();
            zout.close();

            deleteSaveFiles();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void deleteSaveFiles()
    {
        new File(".goals").delete();
        new File(".notes").delete();
        new File(".ghistory").delete();
        new File(".pointers").delete();
    }
    
    private void createNewProfile() throws IOException
    {
        File pointers = new File(".pointers");
        pointers.createNewFile();
        
        DataOutputStream out = new DataOutputStream(new FileOutputStream(pointers));
        out.writeInt(new ShortDate().getDays());
        out.writeLong(0);
        out.writeLong(0);
        
        new File(".goals").createNewFile();
        new File(".notes").createNewFile();
        new File(".ghistory").createNewFile();
    }
    
}
