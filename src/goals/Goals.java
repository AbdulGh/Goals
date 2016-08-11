package goals;

import java.awt.EventQueue;

public class Goals
{    
    public static void main(String[] args) 
    {        
        EventQueue.invokeLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                GoalsWindow window = new GoalsWindow(new ShortDate());
                window.setVisible(true);
            }
        });
    }
}

