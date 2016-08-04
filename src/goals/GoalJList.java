package goals;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import javax.swing.*;
import java.awt.Font;


public class GoalJList extends JList
{
    List<Goal> goalList;
    
    public GoalJList()
    {
        goalList = new ArrayList();
        setListData(goalList.toArray());
        setFont(getFont().deriveFont(Font.PLAIN));
    }
    
    public void refresh()
    {
        setListData(goalList.toArray());
    }
    
    public void addGoal(Goal inGoal)
    {
        goalList.add(inGoal);
        refresh();
    }
    
    public void deleteSelectedFiles()
    {
        goalList.removeAll(getSelectedValuesList());
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
            
            for (Goal x : goalList)
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
        catch (EOFException e){System.out.printf("Done reading");} //this is supposed to happen
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
        goalList.addAll(readGoals);
        refresh();
        return true;
    }
}
