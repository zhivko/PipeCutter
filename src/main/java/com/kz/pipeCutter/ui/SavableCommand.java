package com.kz.pipeCutter.ui;

import javax.swing.Action;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class SavableCommand extends SavableControl {
	JButton button ;
	public SavableCommand() {
		super();
		JButton button = new JButton();
		this.add(button);
	}
	
	public void setActionListener(Action a)
	{
		button.setAction(a);
	}

}
