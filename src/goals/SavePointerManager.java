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
        
        data[0] = raf.readInt(); //date
        data[1] = raf.readLong(); //notes pointer
        data[2] = raf.readLong(); //goal history pointer
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
    
    public static long[] readNextPointer(RandomAccessFile raf) throws IOException
    {
        int c;
        while ((c = raf.read()) != recordsep)
        {
            if (c == -1) return readCurrentPointer(raf); //eof
        }
        
        long[] data = new long[3];
        data[0] = raf.readInt();
        data[1] = raf.readLong();
        data[2] = raf.readLong();
        return data;
        
    }
}
