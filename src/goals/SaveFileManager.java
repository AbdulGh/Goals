package goals;

import java.io.*;
import java.util.zip.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class SaveFileManager
{
    private String currentFile;
    
    public SaveFileManager()
    {
        currentFile = null;
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
        
        todaysEntry.replace("\0", "");
        extractProfile(currentFile);
        
        if (!list.saveToFile("goals"))
        {
            JOptionPane.showMessageDialog(null,
                    "Could not save goal list.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try
        {
            File outFile = new File("notes");
            RandomAccessFile raf = new RandomAccessFile(outFile, "rw");

            if (outFile.length() == 0)
            {
                raf.write(String.valueOf(new ShortDate().getDays()).getBytes());
                raf.write('\0');
            }

            else
            {
                raf.seek(outFile.length() - 2);
                int lastDate = readPreviousDate(raf);
                long t;
                if ((t = new ShortDate().getDays()) != lastDate) //add new date
                {
                    raf.seek(outFile.length());
                    raf.write(String.valueOf(t).getBytes());
                    raf.write('\0');
                } //otherwise we're in the right position to start writing
            }

            raf.write(todaysEntry.getBytes());
            raf.write('\0');
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
            if (!list.loadFromFile("goals")) return false;
            File notes = new File("notes");
            
            if (notes.length() != 0)
            {
                RandomAccessFile raf = new RandomAccessFile(notes, "rw");
                raf.seek(notes.length() - 2);
                int lastDate = readPreviousDate(raf), today = new ShortDate().getDays();
                if (lastDate > today )
                {
                    System.out.println(lastDate + " " + today);
                    JOptionPane.showMessageDialog(null,
                        "You have notes from the future, something is wrong.",
                        "Time travel",
                        JOptionPane.ERROR_MESSAGE);
                }
                else if (lastDate == today)
                    todaysEntry.setText(raf.readLine());

                raf.close();
            }
            
            new File("goals").delete();
            notes.delete();
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
            return false;
        }
        return true;
    }
    
    public String getEntryFor(ShortDate date)
    {
        if (currentFile == null && !selectNewProfile() || !extractProfile(currentFile)) return "";
        
        try
        {
            File notes = new File("notes");
            RandomAccessFile raf = new RandomAccessFile(notes, "rw");
            raf.seek(notes.length() - 2);
            
            int prevDate, desiredDate = date.getDays();
            
            while ((prevDate = readPreviousDate(raf)) >= desiredDate)
            {
                System.out.print("PrevDate: " + prevDate + "  Desired: " + desiredDate);
                
                if (prevDate == desiredDate)
                {
                    String entry = "";
                    char c;
                    while ((c = (char)raf.readChar()) != '\0') entry += c;
                    raf.close();
                    return entry;
                }
                
                long prevEntry = raf.getFilePointer() - (int)Math.log10(prevDate) - 3;
                
                System.out.println("   PrevEntry: " + prevEntry);
                
                if (prevEntry <= 0) break;
                raf.seek(prevEntry);
            }
            System.out.println(" - fin, PrevDate: " + prevDate);
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
    private int readPreviousDate(RandomAccessFile raf) throws IOException
    {
        //seek to end of most recent date
        long pointer;
        for (pointer = raf.getFilePointer(); pointer >= 0; pointer--)
        {
            raf.seek(pointer);
            if (raf.read() == '\0') 
            {
                raf.seek(--pointer);
                break;
            }
        }
        
        //go to the start of the date
        for (; pointer >= 0 && raf.read() != '\0'; pointer--)
            raf.seek(pointer);
        //read the date
        int date = raf.read() - '0';
        char c;
        while ((c = (char)raf.read()) != '\0')
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
            ZipInputStream zin = new ZipInputStream(new FileInputStream(profile));
            
            //extract the two files
            int i = 2;
            ZipEntry e = null;       
            for (i = 2; i > 0; i--)
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
            zout.close();

            new File("goals").delete();
            new File("notes").delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
