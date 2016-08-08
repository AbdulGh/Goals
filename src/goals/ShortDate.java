package goals;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Just stores UNIX time in days
 */
public class ShortDate implements Comparable
{
    private static final long MILLISINDAY = 0x5265c00;
    private static final SimpleDateFormat dformat = new SimpleDateFormat("dd/MM/yyyy");
    private int days;
    
    public ShortDate()
    {
        days = (int)(new Date().getTime() / MILLISINDAY);
    }
    
    public ShortDate(int days)
    {
        this.days = days;
    }
    
    public ShortDate(String inString) throws ParseException
    {
        days = (int)(dformat.parse(inString).getTime() / MILLISINDAY) + 1;
    }
    
    public int getDays()
    {
        return days;
    }
    
    public boolean inRange(ShortDate o, int range)
    {
        return Math.abs(o.getDays() - days) <= range;
    }
    
    @Override
    public String toString()
    {
        return dformat.format(new Date(days * MILLISINDAY));
    }
    
    @Override
    public int compareTo(Object o)
    {
        ShortDate other = (ShortDate)o;
        return other.getDays() - days;
    }
    
    @Override
    public boolean equals(Object o)
    {
        ShortDate other = (ShortDate)o;
        return other.getDays() == days;
    }
}
