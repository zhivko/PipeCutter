package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import com.kz.pipeCutter.BBB.BBBHalCommand;

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
					if (BBBHalCommand.instance != null)
						SavableCheckBox.this.updateHal();
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
		if (val.equals("1"))
			val = "True";
		else
			val = "False";

		this.jCheckBox.setSelected(Boolean.valueOf(val));
	}

	@Override
	public String getParValue() {
		if (this.jCheckBox.isSelected())
			return "True";
		else
			return "False";
	}

	@Override
	public void setLabelTxt(String txt) {
		super.jLabel.setPreferredSize(new Dimension(0, 0));
		super.setLabelTxt("");
		jCheckBox.setText(txt);
	}

	@Override
	public void valueChangedFromUI() {
		// TODO Auto-generated method stub

	}

}
