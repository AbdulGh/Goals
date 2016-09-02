package goals;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class GoalJList extends JList
{
    private Map<String, Goal> goalList;
    
    private List<String> todaysAdditions;
    private List<Goal> todaysRemovals;
    
    private static final char unitsep = (char)31;
    
    public GoalJList()
    {
        goalList = new HashMap<String, Goal>();
        
        todaysAdditions = new ArrayList<String>();
        todaysRemovals = new ArrayList<Goal>();
        
        setFont(getFont().deriveFont(Font.PLAIN));
        setCellRenderer(new CellBorderRenderer());
        refresh();
    }
    
    public void refresh()
    {
        ArrayList<Goal> tlist = new ArrayList<Goal>(goalList.values());
        
        //remove expired dates
        int today = new ShortDate().getDays();
        ArrayList old = new ArrayList<Goal>();
        
        for (Map.Entry<String, Goal> e: goalList.entrySet())
        {
            Goal x = e.getValue();
            ShortDate exp = x.getExpires();
            if (exp != null && exp.getDays() < today)
                old.add(x);
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
        
        todaysAdditions.add(inGoal.getName());
        goalList.put(inGoal.getName(), inGoal);
        refresh();
        return true;
    }
    
    public void deleteSelectedFiles()
    {
        deleteAll((List<Goal>)getSelectedValuesList());
    }
    
    public void deleteAll(List<Goal> old)
    {
        Iterator<Goal> it = old.iterator();
        while (it.hasNext()) deleteGoal(it.next());
        refresh();
    }
    
    public void setDispList(List<Goal> newList)
    {
        setListData(newList.toArray());
    }
    
    public void deleteGoal(Goal x)
    {
        if (todaysAdditions.contains(x.getName()))
            todaysAdditions.remove(x.getName());
        else if (goalList.remove(x.getName()) != null) todaysRemovals.add(x);
        
    }
    
    public Goal[] getGoals()
    {
        return (Goal[])goalList.values().toArray();
    }
    
    public boolean saveGoalsToFile(String outFileName)
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
    
    public boolean loadGoalsFromFile(String inFileName)throws FileNotFoundException {return loadGoalsFromFile(inFileName, true);} 
    
    public boolean loadGoalsFromFile(String inFileName, boolean clearList) throws FileNotFoundException
    {
        FileReader reader = new FileReader(inFileName);
        List<Goal> readGoals = new ArrayList<Goal>();
        
        try
        {
            Goal newGoal = new Goal();
            while (newGoal.read(reader))
            {
                readGoals.add(newGoal);
                newGoal = new Goal();
            }
            
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (clearList) goalList.clear();
        for (Goal x: readGoals) addGoal(x);
        
        refresh();
        return true;
    }
    
    public String getEdits()
    {
        String todaysEdits = "";
        
        for (String x: todaysAdditions) 
            todaysEdits += x + unitsep;
        
        todaysEdits += "\0"; //used to seperate additions and removals
        
        if (!todaysRemovals.isEmpty())
            for (Goal g : todaysRemovals)
                todaysEdits += g.getSaveString();
        
        return todaysEdits;
    }
    
    public boolean loadEditsFromString(String inString) throws FileNotFoundException
    {
        StringReader sr = new StringReader(inString);
        
        int i;
        String readAddition = "";
        try
        {
            //read additions
            todaysAdditions.clear();
            while ((i = sr.read()) != '\0')
            {
                if (i == -1) return false;

                if (i == unitsep)
                {
                    todaysAdditions.add(readAddition);
                    readAddition = "";
                }
                else readAddition += (char)i;
            }

            todaysRemovals.clear();
            Goal newGoal = new Goal();
            while (newGoal.read(sr))
            {
                todaysRemovals.add(newGoal);
                newGoal = new Goal();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return true;
    }
}

class CellBorderRenderer implements ListCellRenderer
{
    private final Border fullBorder;
    private final Border partBorder;
    private final DefaultListCellRenderer defaultRenderer;
    
    public CellBorderRenderer()
    {
        fullBorder = new LineBorder(Color.GRAY);
        partBorder = new MatteBorder(0,1,1,1,Color.GRAY);
        defaultRenderer = new DefaultListCellRenderer();
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) 
    {
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                                                                                isSelected, cellHasFocus);
        renderer.setBorder((index == 0) ? fullBorder : partBorder);
        return renderer;
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