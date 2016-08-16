package goals;

import java.io.*;

public class GoalHistoryManager
{
    private static final char recordsep = (char)30;
    private static final char unitsep = (char)31;
    
    public int goToStartOfDate(RandomAccessFile raf) throws IOException
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
    
    public String getEditStringFor(int date, String filename) throws IOException
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
}
