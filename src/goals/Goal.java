package goals;

import java.util.Date;
import java.io.*;
import java.text.SimpleDateFormat;

public class Goal
{
    private String name;
    private String details;
    private Date set;
    private Date expires;
    
    public Goal(String name, String details, Date set, Date expires)
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
            int setMills = readValue - '0';

            while ((readValue = in.read()) != '\0')
            {
                setMills *= 10;
                setMills += readValue - '0';
            }
            
            set = new Date(setMills * 100000L);
        }
        else set = null;
        
        if ((readValue = in.read()) != '\0')
        {
            int expireMills = readValue - '0';

            while ((readValue = in.read()) != '\0')
            {
                expireMills *= 10;
                expireMills += readValue - '0';
            }
            
            expires = new Date(expireMills * 100000L);
        }
        else expires = null;
    }

    @Override
    public String toString()
    {
        SimpleDateFormat dFormatter = new SimpleDateFormat("dd/MM/yyyy");
        String output = "<html><b>" + name + "</b>";
        if (set != null) output += "&emsp Set: " + dFormatter.format(set);
        output += "&emsp Expires: " + ((expires == null) ? "Never" : dFormatter.format(expires));
        output += "<br/>" + details + "</html>";
        return output;
    }
    
    public String getSaveString()
    {
        String output = name + '\0';
        output += details + '\0';
        
        if (set != null) output += String.valueOf(set.getTime() / 100000L);
        output += '\0';

        if (expires != null) output += String.valueOf(expires.getTime() / 100000L);
        output += '\0';
        
        return output;
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

    public Date getSet() 
    {
        return set;
    }

    public void setSet(Date set) 
    {
        this.set = set;
    }

    public Date getExpires() 
    {
        return expires;
    }

    public void setExpires(Date expires) 
    {
        this.expires = expires;
    }
}
