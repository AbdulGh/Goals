package goals;
import java.util.Date;


/**
 * Just stores the day, month, year by storing UNIX time in days
 */
public class ShortDate implements Comparable
{
    private static final int MILLISINDAY = 0x5265c00;
    private int days;
    
    public ShortDate()
    {
        days = (int)(new Date().getTime() / MILLISINDAY);
    }
    
    public int getDays()
    {
        return days;
    }
    
    @Override
    public String toString()
    {
        return String.valueOf(days);
    }
    
    public int compareTo(Object o)
    {
        ShortDate other = (ShortDate)o;
        return other.getDays() - days;
    }
}
