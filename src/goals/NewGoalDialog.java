package goals;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class NewGoalDialog extends JDialog
{
    private Goal createdGoal;
    
    public JTextField name;
    private JTextArea desc;
    private JTextField set;
    private JTextField expires;
    
    private SimpleDateFormat dateFormat;
    
    public NewGoalDialog(Goal old)
    {
        createdGoal = old;
        setModal(true);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        initDialog();
    }
    
    public NewGoalDialog()
    {
        setModal(true);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        initDialog();
    }
    
    private void initDialog()
    {
        setTitle("New Goal");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints quick = new GridBagConstraints();
        quick.insets = new Insets(2,2,2,2);
        
        quick.gridx = 0;
        quick.gridy = 0;
        panel.add(new JLabel("Name:"), quick);
        
        quick.gridy = 1;
        panel.add(new JLabel("Description:"), quick);
        
        quick.gridy = 2;
        panel.add(new JLabel("Start:"), quick);
        
        quick.gridy = 3;
        panel.add(new JLabel("Expires:"), quick);
        
        JButton ok = new JButton("Add");
        quick.gridx = 1;
        quick.gridy = 4;
        ok.addActionListener(new CreateGoal());
        panel.add(ok, quick);
        
        JButton cancel = new JButton("Cancel");
        quick.gridx = 2;
        quick.gridy = 4;
        
        cancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                createdGoal = null;
                setVisible(false);
            }
        });
        
        panel.add(cancel, quick);
        
        quick.gridwidth = 2;
        quick.gridx = 1;
        
        name = new JTextField(10);
        quick.gridy = 0;
        panel.add(name, quick);
        
        desc = new JTextArea(3,10);
        desc.setLineWrap (true);
        JScrollPane scrollPane = new JScrollPane(desc);
        quick.gridy = 1;
        panel.add(scrollPane, quick);
        
        set = new JTextField(dateFormat.format(new Date()), 10);
        quick.gridy = 2;
        panel.add(set, quick);
        
        expires = new JTextField("dd/mm/yyyy", 10);
        quick.gridy = 3;
        panel.add(expires, quick);
        
        if (createdGoal != null)
        {
            name.setText(createdGoal.getName());
            desc.setText(createdGoal.getDetails());
            
            if (createdGoal.getSet() != null)
                set.setText(dateFormat.format(createdGoal.getSet()));
            else set.setText("dd/mm/yyyy");
            
            if (createdGoal.getExpires() != null)
                set.setText(dateFormat.format(createdGoal.getExpires()));
        }
        
        getContentPane().add(panel);
        pack();
    }
    
    class CreateGoal implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Date setD, expiresD;
            
            try
            {
                setD = dateFormat.parse(set.getText());
            }
            catch (ParseException p)
            {
                setD = null;
            }
            
            try
            {
                expiresD = dateFormat.parse(expires.getText());
            }
            catch (ParseException p)
            {
                expiresD = null;
            }
            
            createdGoal = new Goal(name.getText(), desc.getText(), setD, expiresD);
            setVisible(false);
        }
    }
    
    public Goal getCreatedGoal()
    {
        return createdGoal;
    }
}
