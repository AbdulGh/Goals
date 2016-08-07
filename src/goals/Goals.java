package goals;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.io.*;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Goals extends JFrame
{
    GoalJList list;
    Date currentDate;
    SimpleDateFormat dformatter;
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
        currentDate = new Date();
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
        
        JTextArea todaysEntry = new JTextArea(5, 100);
        todaysEntry.setLineWrap(true);
        todaysEntry.setWrapStyleWord(true);
        JScrollPane notesContainer = new JScrollPane(todaysEntry);
        
        notesContainer.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Notes for " + dformatter.format(currentDate) +":"));
        
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
        save.addActionListener(new SaveButtonListener());
        file.add(save);
        
        JMenuItem saveas = new JMenuItem("Save as");
        saveas.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                JFileChooser saveDialog = new JFileChooser();
                saveDialog.setCurrentDirectory(new File(System.getProperty("user.dir")));
                
                int returnVal = saveDialog.showSaveDialog(Goals.this);

                if (returnVal == JFileChooser.APPROVE_OPTION)
                    list.saveToFile(saveDialog.getSelectedFile().getName());
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
    
    class SaveButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
             if (currentFile == null)
             {
                 JOptionPane.showMessageDialog(Goals.this,
                         "Please create a file first!",
                         "No profile loaded",
                         JOptionPane.ERROR_MESSAGE);
             }
             else if (!list.saveToFile("goals"))
             {
                 JOptionPane.showMessageDialog(Goals.this,
                         "Could not save goal list.",
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }

             try
             {
                 File outFile = new File(outFileName);
                 outFile.createNewFile(); //does nothing if the file already exists
                 output = new BufferedWriter(new FileWriter(outFile.getAbsoluteFile(), false));

                 for (Goal x : goalList.values())
                     output.write(x.getSaveString());

                 output.close();

             }
             catch (Exception e)
             {
                 e.printStackTrace();
                 return false;
             }

             return true;
         }
    }
}

