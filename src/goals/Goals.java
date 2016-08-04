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
        dformatter = new SimpleDateFormat("dd/MM/yyyy");
                
        initUI();
    }
    
    private void initUI()
    {
        setTitle("Goals");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(640,480));
        
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener()
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
        
        file.add(save);
        
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
                            "Could not load from '" + filename + "'!",
                            "IOException",
                            JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch (FileNotFoundException ex)
                    {
                        JOptionPane.showMessageDialog(Goals.this,
                            "Could not find '" + filename + "'!",
                            "FileNotFoundException",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
           }
        });
        file.add(load);
        menuBar.add(file);
        
        setJMenuBar(menuBar);
        
        Container pane = getContentPane();
        GridBagLayout gbLayout = new GridBagLayout();
        pane.setLayout(gbLayout);
        
        class WGBC extends GridBagConstraints
        {
            public WGBC()
            {
                super();
                this.weightx = 1;
                this.weighty = 1;
                this.fill = GridBagConstraints.BOTH;
                this.insets = new Insets(3,3,3,3);
            }
        }
        
        GridBagConstraints componentSettings;
        
        //text entry for notes
        JTextArea todaysEntry = new JTextArea(5, 100);
        todaysEntry.setLineWrap(true);
        todaysEntry.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(todaysEntry);
        
        scrollPane.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Notes for " + dformatter.format(currentDate) +":"));
        
        componentSettings = new WGBC();
        componentSettings.gridx = 0;
        componentSettings.gridy = 0;
        componentSettings.gridwidth = 4;
        componentSettings.gridheight = 1;
        componentSettings.ipady = 220;
        pane.add(scrollPane, componentSettings);
        
        //list for goals
        JPanel listContainer = new JPanel(new BorderLayout());
        listContainer.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Ongoing goals:"));
        
        componentSettings = new WGBC();
        componentSettings.gridx = 0;
        componentSettings.gridy = 1;
        componentSettings.gridwidth = 3;
        componentSettings.gridheight = 1;
        componentSettings.ipadx = 250;
        listContainer.add(list);
        pane.add(listContainer, componentSettings);
        
        //Menu
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
        
        pane.setLayout(gbLayout);
        pack();
    }
}