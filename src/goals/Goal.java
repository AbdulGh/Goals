package goals;

import java.io.*;

public class Goal implements Comparable
{
    private String name;
    private String details;
    private ShortDate set;
    private ShortDate expires;
    
    public Goal(String name, String details, ShortDate set, ShortDate expires)
    {
        this.name = name.replace("\0", "");
        this.details = details.replace("\0", "");
        this.set = set;
        this.expires = expires;
    }
    
    public Goal(BufferedReader in) throws IOException, EOFException
    {
        int readValue = in.read();
        if (readValue == -1) throw new EOFException();
        
        if (readValue == 0) name = "";
        else
        {
            name = Character.toString((char)readValue);
            while ((readValue = in.read()) != '\0') name += (char)readValue;
        }
        
        details = "";
        while ((readValue = in.read()) != '\0') details += (char)readValue;

        if ((readValue = in.read()) != '\0')
        {
            int setDays = readValue - '0';

            while ((readValue = in.read()) != '\0')
            {
                setDays *= 10;
                setDays += readValue - '0';
            }
            
            set = new ShortDate(setDays);
        }
        else set = null;
        
        if ((readValue = in.read()) != '\0')
        {
            int expireDays = readValue - '0';

            while ((readValue = in.read()) != '\0')
            {
                expireDays *= 10;
                expireDays += readValue - '0';
            }
            
            expires = new ShortDate(expireDays);
        }
        else expires = null;
    }
    
    public String getSaveString()
    {
        String output = name + '\0';
        output += details + '\0';
        
        if (set != null) output += String.valueOf(set.getDays());
        output += '\0';

        if (expires != null) output += String.valueOf(expires.getDays());
        output += '\0';
        
        return output;
    }
    
    @Override
    public String toString()
    {
        String output = "<html><b>" + name + "</b>";
        if (set != null) output += "&emsp Set: " + set;
        
        output += "&emsp Expires: ";
        if (expires == null) output += "Never";
        else
        {
            if (expires.inRange(new ShortDate(), 1))
                output += "<font color='red'>" + expires + "</font>";
            else
                output += expires;
        }
        
        output += "<br/>" + details + "</html>";
        return output;
    }
    
    @Override
    public int compareTo(Object o)
    {
        Goal other = (Goal)o;
        
        if (expires == null)
        {
            if (other.getExpires() == null) return 0;
            return 1;
        }
        if (other.getExpires() == null) return -1;
        
        return -expires.compareTo(other.getExpires());
    }
    
    public String getName() 
    {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getDetails() 
    {
        return details;
    }

    public void setDetails(String details) 
    {
        this.details = details;
    }

    public ShortDate getSet() 
    {
        return set;
    }

    public void setSet(ShortDate set) 
    {
        this.set = set;
    }

    public ShortDate getExpires() 
    {
        return expires;
    }

    public void setExpires(ShortDate expires) 
    {
        this.expires = expires;
    }
}
