package goals;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.text.ParseException;
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
        setPreferredSize(new Dimension(1000,600));
        
        addWindowListener(new WindowAdapter()
        {
           @Override
           public void windowClosing(WindowEvent e)
           {
               //if (JOptionPane.showConfirmDialog(null, "Would you like to save?", "Save", JOptionPane.YES_NO_OPTION)
               //        == JOptionPane.YES_OPTION) sfm.save(list, todaysEntry.getText());
               dispose();
               System.exit(0);
           }
        });
        
        todaysEntry = new JTextArea(5, 100);
        todaysEntry.setLineWrap(true);
        todaysEntry.setWrapStyleWord(true);
        todaysEntry.setFont(todaysEntry.getFont().deriveFont(14f));
        
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
        file.setMnemonic('F');
        
        JMenuItem load = new JMenuItem("Load");
        load.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                sfm.load(list, todaysEntry);
           }
        });
        load.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L,
                                           java.awt.Event.CTRL_MASK));
        file.add(load);
        
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               sfm.save(list, todaysEntry.getText());
           }
        });
        save.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                                           java.awt.Event.CTRL_MASK));
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
        
        menuBar.add(file);
        
        JMenu browse = new JMenu("Browse");
        browse.setMnemonic('B');
        
        JMenuItem goTo = new JMenuItem("Go to date...");
        goTo.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                DateJPanel dp = new DateJPanel(); 
                int result = JOptionPane.showConfirmDialog(GoalsWindow.this, dp,
                       "Enter date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) sfm.goToDate(dp.getValue().getDays());
           }
        });
        goTo.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G,
                                           java.awt.Event.CTRL_MASK));
        browse.add(goTo);
        
        JMenuItem back = new JMenuItem("Go back");
        back.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                int result = (int)sfm.goBack(list, todaysEntry);
                if (result != -1)
                {
                    date = new ShortDate(result);
                    refreshUI();
                }
           }
        });
        back.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B,
                                           java.awt.Event.CTRL_MASK));
        browse.add(back);
        
        JMenuItem fwd = new JMenuItem("Go forward");
        fwd.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
                int result = (int)sfm.goForward(list, todaysEntry);
                if (result != -1)
                {
                    date = new ShortDate(result);
                    refreshUI();
                }
           }
        });
        fwd.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F,
                                           java.awt.Event.CTRL_MASK));
        browse.add(fwd);    
        
        file.add(browse);
        
        menuBar.add(browse);
        
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
                    if (selectedGoals == 1)
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
        
        todaysEntry.setEditable(date.equals(new ShortDate()));
    }
    
    private void goToDate(ShortDate date)
    {
        //TODO
    }
}
