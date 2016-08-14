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
        while ((char)raf.read() != recordsep && pointer-- > 0)
        {
            raf.seek(pointer);
            if (pointer == 0) break;
        }
        
        int date = raf.read() - '0';
        
        int nextDigit;
        while ((nextDigit = raf.read()) != unitsep)
        {
            if (nextDigit == -1) throw new EOFException();
            
            date *= 10;
            date += nextDigit - '0';
        }
        
        System.out.println(date);
        return date;
    }
}
