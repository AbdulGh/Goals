package goals;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.*;

public class Goals extends JFrame
{
    GoalJList list;
    JTextArea todaysEntry;
    String currentFile;
    
    public static void main(String[] args) 
    {        
        EventQueue.invokeLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                Goals window = new Goals();
                window.setVisible(true);
            }
        });
    }
    
    public Goals()
    {
        list = new GoalJList();
        currentFile = null;
                
        initUI();
    }
    
    private void initUI()
    {
        setTitle("Goals");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(640,480));
        
        todaysEntry = new JTextArea(5, 100);
        todaysEntry.setLineWrap(true);
        todaysEntry.setWrapStyleWord(true);
        JScrollPane notesContainer = new JScrollPane(todaysEntry);
        
        notesContainer.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Notes for " + new ShortDate() +":"));
        
        //list for goals
        JScrollPane listContainer = new JScrollPane(list);
        listContainer.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Ongoing goals:"));
        
        notesContainer.setPreferredSize(new Dimension(640, 340));
        listContainer.setPreferredSize(new Dimension(640, 140));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, notesContainer, listContainer);
        split.setResizeWeight(0.6);
        getContentPane().add(split);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        
        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                load();
           }
        });
        file.add(load);
        
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               save();
           }
        });
        file.add(save);
        
        JMenuItem saveas = new JMenuItem("Save as");
        saveas.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                saveas();
           }
        });
        file.add(saveas);
        
        menuBar.add(file);
        setJMenuBar(menuBar);
        
        //List right click menu
        JMenuItem newG = new JMenuItem("New");
        newG.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NewGoalDialog ngd = new NewGoalDialog();
                ngd.setVisible(true);
                
                if (ngd.getCreatedGoal() != null)
                    list.addGoal(ngd.getCreatedGoal());
                
                ngd.dispose();
                
            }
        });
        
        JMenuItem accomplished = new JMenuItem("Finished");
        
        JMenuItem edit = new JMenuItem("Edit");
        edit.addActionListener(new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent e)
           {
                Goal remove = (Goal)list.getSelectedValue(); 

                NewGoalDialog ngd = new NewGoalDialog(remove);
                ngd.setVisible(true);

                if (ngd.getCreatedGoal() != null)
                {
                    list.deleteGoal(remove);
                    list.addGoal(ngd.getCreatedGoal());
                }
                ngd.dispose();
           }
        });
        
        JMenuItem del = new JMenuItem("Delete");
        del.addActionListener(new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent e)
           {
               list.deleteSelectedFiles();
           }
        });
        
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(newG);
        popupMenu.add(accomplished);
        popupMenu.add(edit);
        popupMenu.add(del);
        
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    JList list = (JList)e.getSource();
                    
                    int selectedGoals = list.getSelectedIndices().length;
                    if (selectedGoals < 2)
                    {
                        int row = list.locationToIndex(e.getPoint());
                        list.setSelectedIndex(row);
                    }
                    
                    boolean itemSelected = selectedGoals > 0;
                    edit.setEnabled(itemSelected);
                    accomplished.setEnabled(itemSelected);
                    del.setEnabled(itemSelected);
                    
                    popupMenu.show(list, e.getX(), e.getY());
                }
            }

        });
        
        pack();
    }
    
    public void saveas()
    {
        JFileChooser saveDialog = new JFileChooser();
        saveDialog.setCurrentDirectory(new File(System.getProperty("user.dir")));

        int returnVal = saveDialog.showSaveDialog(Goals.this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            currentFile = saveDialog.getSelectedFile().getName();
            save();
        }
    }
    
    public void save()
    {
        if (currentFile == null)
        {
            saveas();
            return;
        } else if (!list.saveToFile("goals"))
        {
            JOptionPane.showMessageDialog(Goals.this,
                    "Could not save goal list.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try
        {
            File outFile = new File("notes");
            outFile.createNewFile();
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

            raf.write(todaysEntry.getText().getBytes());
            raf.write('\0');
            raf.setLength(raf.getFilePointer());
            raf.close();
            
            //zip the two files into one 'profile' then delete
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
            //new File("notes").delete();
            
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public boolean load()
    {
        JFileChooser chooseDialog = new JFileChooser(); 

        chooseDialog.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int returnVal = chooseDialog.showOpenDialog(Goals.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            String filename = chooseDialog.getSelectedFile().getName();
            currentFile = filename;
        }
        
        File profile = new File(currentFile);
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
                JOptionPane.showMessageDialog(Goals.this,
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
        }
        catch (IOException e)
        {
            System.err.println("IOException reading files...");
            e.printStackTrace();
        }
        
        try
        {
            if (!list.loadFromFile("goals")) return false;
            File notes = new File("notes");
            RandomAccessFile raf = new RandomAccessFile(notes, "rw");
            raf.seek(notes.length() - 2);
            int lastDate = readPreviousDate(raf), today = new ShortDate().getDays();
            if (lastDate > today )
            {
                System.out.println(lastDate + " " + today);
                JOptionPane.showMessageDialog(Goals.this,
                    "You have notes from the future, something is wrong.",
                    "Time travel",
                    JOptionPane.ERROR_MESSAGE);
            }
            else if (lastDate == today)
                todaysEntry.setText(raf.readLine());
            
            raf.close();
        }
        catch (FileNotFoundException e)
        {
            JOptionPane.showMessageDialog(Goals.this,
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
    
    /**
    * @returns the date prior to the cursor
    */
    public int readPreviousDate(RandomAccessFile raf) throws IOException
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
}

