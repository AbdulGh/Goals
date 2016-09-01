package goals;

import javax.swing.*;
import java.text.ParseException;
import java.util.Calendar;

public class DateJPanel extends JPanel
{
    private JComboBox day;
    private JComboBox month;
    private JComboBox year;
    
    public DateJPanel()
    {
        day = new JComboBox();
        for (int i = 1; i <= 31; i++)
            day.addItem(i);
        
        month = new JComboBox();
        for (int i = 1; i <= 12; i++)
            month.addItem(i);
        
        year = new JComboBox();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear + 10; i++)
            year.addItem(i);
        
        setDate(new ShortDate());
        
        add(day);
        add(month);
        add(year);
    }
    
    public void setDate(ShortDate date)
    {
        String[] dmy = date.toString().split("/");
        day.setSelectedItem(Integer.parseInt(dmy[0]));
        month.setSelectedItem(Integer.parseInt(dmy[1]));
        year.setSelectedItem(Integer.parseInt(dmy[2]));
    }
    
    public void setEnabled(boolean enable)
    {
        day.setEnabled(enable);
        month.setEnabled(enable);
        year.setEnabled(enable);
    }
    
    public ShortDate getValue()
    {
        if (!day.isEnabled()) return null;
        
        try
        {
            return new ShortDate(day.getSelectedItem() + "/" + month.getSelectedItem() + "/" + year.getSelectedItem());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
