package goals;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.*;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Goals extends JFrame
{
    GoalJList list;
    SimpleDateFormat dformatter;
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
        dformatter = new SimpleDateFormat("dd/MM/yyyy");
                
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
                "Notes for " + dformatter.format(new Date()) +":"));
        
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
                JFileChooser chooseDialog = new JFileChooser(); 

                chooseDialog.setCurrentDirectory(new File(System.getProperty("user.dir")));
                int returnVal = chooseDialog.showOpenDialog(Goals.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) 
                {
                    String filename = chooseDialog.getSelectedFile().getName();
                    
                    try
                    {
                        if (!list.loadFromFile(filename))
                        {
                            JOptionPane.showMessageDialog(Goals.this,
                            "Could not load from '" + filename + "'.",
                            "IOException",
                            JOptionPane.ERROR_MESSAGE);
                        }
                        
                        else currentFile = filename;
                    }
                    catch (FileNotFoundException ex)
                    {
                        JOptionPane.showMessageDialog(Goals.this,
                            "Could not find '" + filename + "'.",
                            "FileNotFoundException",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
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
        }
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
            JOptionPane.showMessageDialog(Goals.this,
                    "Please create a file first!",
                    "No profile loaded",
                    JOptionPane.ERROR_MESSAGE);
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

            //see if we need to overwrite todays notes, navigate to cursor to correct position
            //seek to end of most recent date
            long pointer;
            for (pointer = outFile.length() - 1; pointer >= 0; pointer--)
            {
                raf.seek(pointer);
                if (raf.read() == '\0') 
                {
                    raf.seek(--pointer);
                    break;
                }
            }

            if (pointer >= 0) 
            {
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

                long t;
                if ((t = new Date().getTime() / 100000L) != date) //add new date
                {
                    raf.seek(outFile.length());
                    raf.write(String.valueOf(t).getBytes());
                    raf.write('\0');
                } //otherwise we're in the right position to start writing
            }
            else// otherwise the file was empty, add first date
            {
                raf.write(String.valueOf(new Date().getTime() / 100000L).getBytes());
                raf.write('\0');
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
}

