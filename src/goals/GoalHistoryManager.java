package goals;

import java.io.*;

public class GoalHistoryManager
{
    public int goToStartOfDate(RandomAccessFile raf) throws IOException
    {
        long pointer = raf.getFilePointer();
        boolean dateFound = false;
        
        while (!dateFound && pointer >= 0)
        {
            while (raf.read() != '\0') raf.seek(--pointer);

            if (Character.isDigit((char)raf.read()))
            {
                raf.seek(pointer + 1);
                dateFound = true;
            }
            
            else pointer--;
        }
        
        if (pointer < 0) throw new EOFException();
        
        int date = raf.read() - '0';
        int nextDigit;
        while ((nextDigit = raf.read() - '0') != '\0')
        {
            date *= 10;
            date += nextDigit;
        }
        
        return date;
    }
}
