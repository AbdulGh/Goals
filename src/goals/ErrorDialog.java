package goals;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ErrorDialog extends JDialog
{
    public static void ErrorMessageDialog(String e)
    {
        JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public ErrorDialog(String message, Throwable e)
    {        
        setPreferredSize(new Dimension(600,400));
        
        JPanel panel = new JPanel(new BorderLayout());
        
        panel.add(new JLabel(message + ":"), BorderLayout.NORTH);
        
        JTextArea ta = new JTextArea();
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setEditable(false);
        ta.setText(e.toString());
        panel.add(new JScrollPane(ta));
        
        JButton close = new JButton("Close");
        
        close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
        
        setContentPane(panel);
        pack();
        setVisible(true);
    }
}
