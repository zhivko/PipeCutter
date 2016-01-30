package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SavableCheckBox extends SavableControl {
	public JCheckBox jCheckBox;
	public SavableCheckBox() {
		super();

		jCheckBox = new JCheckBox();
		jCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		jCheckBox.setText("This is value");

		jCheckBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				try {
					SavableCheckBox.this.save();
					valueChangedFromUI();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		add(jCheckBox);
	}
	
	@Override
	public void setParValue(String val) {
			this.jCheckBox.setSelected(Boolean.valueOf(val));
	}
	
	@Override
	public String getParValue() {
		if(this.jCheckBox.isSelected())
			return "1";
		else
			return "0";
	}

	@Override
	public void setLabelTxt(String txt)
	{
		super.jLabel.setPreferredSize(new Dimension(0,0));
		super.setLabelTxt("");
		jCheckBox.setText(txt);
	}

	@Override
	public void valueChangedFromUI() {
		// TODO Auto-generated method stub
		
	}
	
}
