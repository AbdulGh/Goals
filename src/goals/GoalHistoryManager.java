package goals;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GoalHistoryManager
{
    private static final char recordsep = (char)30;
    private static final char unitsep = (char)31;
    
    private GoalHistoryManager(){}
    
    public static int goToStartOfDate(RandomAccessFile raf) throws IOException
    {
        long pointer = raf.getFilePointer();
        
        //seek to the start of the last date
        while ((char)raf.read() != recordsep)
        {
            raf.seek(pointer--);
            if (pointer < 0) break;
        }
        
        int date = raf.read() - '0';
        int nextDigit;
        while ((nextDigit = raf.read()) != unitsep)
        {
            if (nextDigit == -1) throw new EOFException();
            
            date *= 10;
            date += nextDigit - '0';
        }
        
        return date;
    }
    
    public static String getEditStringFor(int date, String filename) throws IOException
    {
        File inFile = new File(filename);
        RandomAccessFile raf = new RandomAccessFile(inFile, "r");
        
        raf.seek(inFile.length() - 2);
        int lastDate;
        long pointer = raf.getFilePointer();
        
        while ((lastDate = goToStartOfDate(raf)) > date && pointer >= 0)
        {
            raf.seek(pointer);
            pointer = raf.getFilePointer() - (int)Math.log10(lastDate) - 4;
        }
        
        if (lastDate != date) return "";
        
        char c; String editString = "";
        while ((c = (char)raf.read()) != recordsep) editString += c;
        return editString;
    }
    
    public static boolean takeGoalListTo(ArrayList<Goal> goalList, int date, String filename) throws IOException
    {
        File inFile = new File(filename);
        
        if (inFile.length() < 2) return false;
        
        RandomAccessFile raf = new RandomAccessFile(inFile, "r");
        raf.seek(inFile.length() - 2);
        
        int d;
        while ((d = goToStartOfDate(raf)) >= date)
        {
            updateWithCurrentEdits(goalList, raf);
            if (!goToPrev(raf))
            {
                goalList.clear();
                return false;
            }
            System.out.println();
        }
        
        raf.close();
        return true;
    }
    
    /**
    * must be called after goToStartOfDate
    */
    private static void updateWithCurrentEdits(ArrayList<Goal> goalList, RandomAccessFile raf) throws IOException
    {        
        String name = "";
        int c;
        while ((c = raf.read()) != '\0') //remove additions
        {
            if (c == -1) throw new EOFException();
            else if (c == unitsep)
            {
                Iterator<Goal> it = goalList.iterator();
                while (it.hasNext())
                {
                    Goal g = it.next();
                    if (g.getName().equals(name))
                    {
                        goalList.remove(g);
                        break;
                    }
                }
                name = "";
            }
            else name += (char)c;
        }

        //add removals
        String removals = "";
        Goal g = new Goal();
        while ((c=raf.read()) != recordsep)
        {
            if (c == -1) throw new EOFException();
            else removals += (char)c;
        }
        StringReader sr = new StringReader(removals);
        while (g.read(sr))
        {
            goalList.add(g);
            g = new Goal();
        }
        sr.close();
        
    }
    
    private static boolean goToPrev(RandomAccessFile raf) throws IOException
    {
        long pointer;
        if ((pointer = raf.getFilePointer() - 1) < 0) return false;
        raf.seek(pointer);
        //go to the end of the current date
        int c;
        while ((c = raf.read()) != unitsep)
        {
            if (c == -1) throw new EOFException();
            else if (--pointer < 0) return false;
            raf.seek(pointer);
        }
        
        //go to the start of the previous entry
        if (--pointer < 0) throw new EOFException();
        raf.seek(pointer);
        
        while ((c = raf.read()) != recordsep)
        {
            if (c == -1) throw new EOFException();
            else if (--pointer < 0) //first entry, seek to end 
            {
                return false;
            }
            else raf.seek(pointer);
        }
        
        //go to the end of the start of the previous entry
        if (--pointer < 0) return false;
        raf.seek(pointer);
        return true;
    }
    
}
