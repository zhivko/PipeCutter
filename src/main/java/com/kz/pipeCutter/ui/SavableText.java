package com.kz.pipeCutter.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class SavableText extends SavableControl {
	public JTextField jValue;
	public SavableText() {
		super();

		jValue = new JTextField();
		jValue.setHorizontalAlignment(SwingConstants.LEFT);
		jValue.setText("This is value");
		jValue.setColumns(1);

		jValue.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				warn();
			}

			public void removeUpdate(DocumentEvent e) {
				warn();
			}

			public void insertUpdate(DocumentEvent e) {
				warn();
			}

			public void warn() {
				try {
					if(!SavableText.this.isLoadingValue())
					{
						SavableText.this.save();
						valueChangedFromUI();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		add(jValue);
		jValue.setColumns(10);
		
	}
	
	@Override
	public void setParValue(String val) {
			this.jValue.setText(val);
	}
	
	@Override
	public String getParValue() {
		return this.jValue.getText();
	}

	@Override
	public void valueChangedFromUI() {
		// TODO Auto-generated method stub
		
	}

}
