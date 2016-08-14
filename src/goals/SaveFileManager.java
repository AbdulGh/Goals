package goals;

import java.io.*;
import java.util.zip.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class SaveFileManager
{
    private String currentFile;
    private GoalHistoryManager GHM;
        
    private static final char recordsep = (char)30;
    private static final char unitsep = (char)31;
    
    public SaveFileManager()
    {
        currentFile = null;
        GHM = new GoalHistoryManager();
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
        
        todaysEntry.replaceAll("[\030\031]", "");
        
        extractProfile(currentFile);
        
        if (!list.saveGoalsToFile("goals"))
        {
            JOptionPane.showMessageDialog(null,
                    "Could not save goal list.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try
        {
            //write notes
            File outFile = new File("notes");
            RandomAccessFile raf = new RandomAccessFile(outFile, "rw");
            
            int today = new ShortDate().getDays();

            if (outFile.length() == 0)
            {
                raf.write(String.valueOf(today).getBytes());
                raf.write(unitsep);
            }

            else
            {
                raf.seek(outFile.length() - 2);

                if (goToStartOfDate(raf) != today) //add new date
                {
                    raf.seek(outFile.length());
                    raf.write(String.valueOf(today).getBytes());
                    raf.write(unitsep);
                } //otherwise we're in the right position to start writing
            }

            raf.write(todaysEntry.getBytes());
            raf.write(recordsep);
            raf.setLength(raf.getFilePointer());
            raf.close();
            
            //append goal changes
            //TODO: find out why date is being written again and again
            outFile = new File("ghistory");
            raf = new RandomAccessFile(outFile, "rw");
            
            if (outFile.length() == 0)
            {
                raf.write(String.valueOf(today).getBytes());
                raf.write(unitsep);
            }
            
            else //check if we need to overwrite todays entry
            {
                raf.seek(outFile.length() - 2);

                if (GHM.goToStartOfDate(raf) != today)
                {
                    raf.seek(outFile.length());
                    raf.write(String.valueOf(today).getBytes());
                    raf.write(unitsep);
                    System.out.println("Meme!");
                }
            }
            
            raf.write(list.getEdits().getBytes());
            raf.setLength(raf.getFilePointer());
            raf.close();
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
            if (!list.loadGoalsFromFile("goals")
                || !list.loadEditsFromFile("ghistory")) return false;
            
            todaysEntry.setText(getEntryFor(new ShortDate()));
            
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
       
        return true;
    }
    
    public String getEntryFor(ShortDate date)
    {
        return getEntryFor(date.getDays());
    }
    
    
    public String getEntryFor(int desiredDate)
    {
        if (currentFile == null && !selectNewProfile() || !extractProfile(currentFile)) return null;
        
        try
        {
            File notes = new File("notes");
            RandomAccessFile raf = new RandomAccessFile(notes, "rw");
            raf.seek(notes.length() - 2);
            
            int prevDate;
            
            while ((prevDate = goToStartOfDate(raf)) >= desiredDate)
            {   
                if (prevDate == desiredDate)
                {
                    String entry = "";
                    char c;
                    while ((c = (char)raf.read()) != recordsep) entry += c;
                    raf.close();
                    return entry;
                }
                
                long prevEntry = raf.getFilePointer() - (int)Math.log10(prevDate) - 4;
                
                if (prevEntry <= 0) break;
                raf.seek(prevEntry);
            }
            raf.close();
        }
        catch (IOException e)
        {
            System.err.println("IOException in getEntryFor()...");
            e.printStackTrace();
        }
        
        return "";
    }
    
    /**
    * @returns the date prior to the cursor, while moving the cursor to the first character of the entry
    */
    private int goToStartOfDate(RandomAccessFile raf) throws IOException
    {
        //seek to start of the most recent date
        for (long pointer = raf.getFilePointer(); pointer >= 0 && raf.read() != recordsep; pointer--)
            raf.seek(pointer);
        
        //read the date
        int date = raf.read() - '0';
        char c;
        while ((c = (char)raf.read()) != unitsep)
        {
            date *= 10;
            date += c - '0';
        }
        
        return date;
    }
    
    private boolean extractProfile(String fileName)
    {
        File profile = new File(fileName);
        
        if (!profile.exists())
        {
            try
            {
                profile.createNewFile();
                new File("goals").createNewFile();
                new File("notes").createNewFile();
                new File("ghistory").createNewFile();
            }
            catch (IOException e)
            {
                System.err.println("IOException creating a new profile...");
                e.printStackTrace();
                return false;
            }
            return true;
        }
        
        try
        {
            deleteSaveFiles();
            ZipInputStream zin = new ZipInputStream(new FileInputStream(profile));
            
            //extract the three files
            int i = 3;
            ZipEntry e = null;       
            for (i = 3; i > 0; i--)
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
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(currentFile));
            byte[] buf = new byte[2048];

            FileInputStream inputStream = new FileInputStream("goals");
            zout.putNextEntry(new ZipEntry("goals"));

            int read;
            while ((read = inputStream.read(buf)) > 0)
                zout.write(buf, 0, read);
            inputStream.close();
            zout.closeEntry();

            inputStream = new FileInputStream("notes");
            zout.putNextEntry(new ZipEntry("notes"));

            while ((read = inputStream.read(buf)) > 0)
                zout.write(buf, 0, read);
            inputStream.close();
            zout.closeEntry();
            
            inputStream = new FileInputStream("ghistory");
            zout.putNextEntry(new ZipEntry("ghistory"));

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
        new File("goals").delete();
        new File("notes").delete();
        new File("ghistory").delete();
    }
}
