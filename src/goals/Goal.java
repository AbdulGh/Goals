package goals;

import java.io.*;

public class Goal implements Comparable
{
    private String name;
    private String details;
    private ShortDate set;
    private ShortDate expires;
    
    private static final char recordsep = (char)30;
    private static final char unitsep = (char)31;
    
    public Goal()
    {
        name = details = null;
        set = expires = null;
    }
    
    public Goal(String name, String details, ShortDate set, ShortDate expires)
    {
        this.name = name.replaceAll("[\030\031]", "");
        this.details = details.replaceAll("[\030\031]", "");
        this.set = set;
        this.expires = expires;
    }
    
    public boolean read(Reader in) throws IOException, EOFException
    {
        int readValue = in.read();
        if (readValue == -1) return false;
        
        if (readValue == recordsep) return false;
        if (readValue == unitsep) name = "";
        else
        {
            name = Character.toString((char)readValue);
            while ((readValue = in.read()) != unitsep) name += (char)readValue;
        }
        
        details = "";
        while ((readValue = in.read()) != unitsep) details += (char)readValue;

        if ((readValue = in.read()) != unitsep)
        {
            int setDays = readValue - '0';

            while ((readValue = in.read()) != unitsep)
            {
                setDays *= 10;
                setDays += readValue - '0';
            }
            
            set = new ShortDate(setDays);
        }
        else set = null;
        
        if ((readValue = in.read()) != unitsep)
        {
            int expireDays = readValue - '0';

            while ((readValue = in.read()) != unitsep)
            {
                expireDays *= 10;
                expireDays += readValue - '0';
            }
            
            expires = new ShortDate(expireDays);
        }
        else expires = null;
        
        return true;
    }
    
    public String getSaveString()
    {
        String output = name + unitsep;
        output += details + unitsep;
        
        if (set != null) output += String.valueOf(set.getDays());
        output += unitsep;

        if (expires != null) output += String.valueOf(expires.getDays());
        output += unitsep;
        
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
    
    @Override
    public boolean equals(Object o)
    {
        return ((Goal)o).getName().equals(name);
    }
}
