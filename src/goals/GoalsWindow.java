package goals;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class GoalsWindow extends JFrame
{
    private GoalJList list;
    private JTextArea todaysEntry;
    private SaveFileManager sfm;
    private ShortDate date;
    private JScrollPane notesContainer;
    
    public GoalsWindow(ShortDate date)
    {
        list = new GoalJList();
        sfm = new SaveFileManager();
        this.date = date;
        initUI();
    }
    
    private void initUI()
    {
        setTitle("Goals");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000,600));
        
        todaysEntry = new JTextArea(5, 100);
        todaysEntry.setLineWrap(true);
        todaysEntry.setWrapStyleWord(true);
        
        notesContainer = new JScrollPane(todaysEntry);
        notesContainer.setPreferredSize(new Dimension(640, 340));
        
        //list for goals
        JScrollPane listContainer = new JScrollPane(list);
        listContainer.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Ongoing goals:"));
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
                sfm.load(list, todaysEntry);
           }
        });
        file.add(load);
        
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               sfm.save(list, todaysEntry.getText());
           }
        });
        file.add(save);
        
        JMenuItem saveas = new JMenuItem("Save as");
        saveas.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                sfm.saveas(list, todaysEntry.getText());
           }
        });
        file.add(saveas);
        
        JMenuItem browse = new JMenuItem("Browse");
        browse.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                String dateString = JOptionPane.showInputDialog(GoalsWindow.this, "Go to which date? dd/mm/yyyy");
                if (dateString != null)
                {
                    try
                    {
                        goToDate(new ShortDate(dateString));
                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(GoalsWindow.this, "Please enter a date in the form dd/mm/yyyy!", 
                                "Misformed date", JOptionPane.ERROR_MESSAGE);
                    }
                }
           }
        });
        file.add(browse);
        
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
        
        JMenuItem del = new JMenuItem("Remove");
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
                    del.setEnabled(itemSelected);
                    
                    popupMenu.show(list, e.getX(), e.getY());
                }
            }
        });
        
        refreshUI();
        pack();
    }
    
    private void refreshUI()
    {
        notesContainer.setBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(Color.GRAY),
                "Notes for " + date +":"));
        
        list.refresh();
        
        todaysEntry.setEditable(date.equals(new ShortDate()));
    }
    
    private void goToDate(ShortDate date)
    {
        this.date = date;
        todaysEntry.setText(sfm.getEntryFor(date));
        refreshUI();
    }
}
