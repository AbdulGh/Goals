package goals;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class GoalJList extends JList
{
    private Map<String, Goal> goalList;
    
    public GoalJList()
    {
        goalList = new HashMap<String, Goal>();
        setFont(getFont().deriveFont(Font.PLAIN));
        refresh();
    }
    
    public void refresh()
    {
        ArrayList<Goal> tlist = new ArrayList<Goal>(goalList.values());
        
        //remove expired dates
        int today = new ShortDate().getDays();
        Iterator<Goal> it = tlist.iterator();
        while (it.hasNext())
        {
            ShortDate exp = it.next().getExpires();
            if (exp != null && exp.getDays() < today) it.remove();
        }
        
        Collections.sort(tlist);
        setListData(tlist.toArray());
    }
    
    public boolean addGoal(Goal inGoal)
    {
        if (goalList.containsKey(inGoal.getName()))
        {
            replaceGoal confirm = new replaceGoal(goalList.get(inGoal.getName()), inGoal);
            confirm.setVisible(true);
            if (!confirm.shouldReplace()) return false;
        }
        
        goalList.put(inGoal.getName(), inGoal);
        refresh();
        return true;
    }
    
    public void deleteSelectedFiles()
    {
        for (Goal x: (List<Goal>)getSelectedValuesList())
            goalList.remove(x.getName());
        
        refresh();
    }
    
    public void deleteGoal(Goal remove)
    {
        goalList.remove(remove);
        refresh();
    }
    
    public boolean saveToFile(String outFileName)
    {
        BufferedWriter output;
        
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
    
    public boolean loadFromFile(String inFileName)throws FileNotFoundException {return loadFromFile(inFileName, true);} 
    
    public boolean loadFromFile(String inFileName, boolean clearList) throws FileNotFoundException
    {
        BufferedReader reader = new BufferedReader(new FileReader(inFileName));
        List<Goal> readGoals = new ArrayList<Goal>();
        
        try
        {
            while (true) readGoals.add(new Goal(reader));
        }
        catch (EOFException e){} //this is supposed to happen
        catch (IOException e)
        {
            System.err.println("loadFromFile in GoalJList failed to read from the input file before EOF.");
            e.printStackTrace();
            return false;
        }
        
        try
        {
            reader.close();
        }
        catch (IOException e)
        {
            System.err.println("loadFromFile in GoalJList could not close the reader.");
            e.printStackTrace();
        }
        
        if (clearList) goalList.clear();
        
        for (Goal x: readGoals) addGoal(x);
        
        refresh();
        return true;
    }
}

class replaceGoal extends JDialog
{
    private boolean replace;
    
    public replaceGoal(Goal oldG, Goal newG)
    {
        setModal(true);
        replace = false;
        setTitle("Name Already Exists");
        setLocationRelativeTo(null);
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        
        JLabel one = new JLabel("Would you like to replace:");
        one.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(one);
        
        p.add (new JEditorPane("text/html", oldG.toString()));
        
        JLabel two = new JLabel("With:");
        two.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(two);
        
        p.add (new JEditorPane("text/html", newG.toString()));
        
        JButton yes = new JButton("Yes"), no = new JButton("No");
        
        JPanel bp = new JPanel(new FlowLayout());
        yes.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                replace = true;
                setVisible(false);
            }
        });
        bp.add(yes);
        
        no.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                replace = false;
                setVisible(false);
            }
        });
        bp.add(no);
        p.add(bp);
        
        add(p);
        pack();
        setResizable(false);
    }
    
    public boolean shouldReplace()
    {
        return replace;
    }
}