package goals;

import java.io.*;


public class SavePointerManager
{
    private static final char recordsep = (char)30;
    
    public static long[] readCurrentPointer(RandomAccessFile raf) throws IOException
    {
        long[] data = new long[3];
        
        long pointer = raf.getFilePointer();
        while (--pointer >= 0 && raf.read() != recordsep)
            raf.seek(pointer);
        
        data[0] = readLong(raf); //date
        data[1] = readLong(raf); //notes pointer
        data[2] = readLong(raf); //goal history pointer
        return data;
    }
    
    public static long[] readPreviousPointer(RandomAccessFile raf) throws IOException
    {
        long pointer = raf.getFilePointer();
        while (--pointer > 1)
        {
            raf.seek(pointer);
            if (raf.read() == recordsep) break;
        }
        
        raf.seek(pointer - 1);
        return readCurrentPointer(raf);
    }
    
    private static long readLong(RandomAccessFile raf) throws IOException
    {
        long returnval = raf.read() - '0';
        char charRead;
        
        while (Character.isDigit(charRead = (char)raf.read()))
        {
            returnval *= 10;
            returnval += charRead - '0';
        }
        return returnval;
    }
    
}
