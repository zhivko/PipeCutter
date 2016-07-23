package com.kz.pipeCutter.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

public class MyButton extends JButton implements IHasPinDef{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2616660361104648156L;
	private String value;
	private Color oldColor;
	public PinDef pinDef;


	public MyButton() {
		super();
		this.oldColor = this.getBackground();
	}

	public MyButton(String string) {
		super(string);

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				go();
			}
		});
	}

	public void doIt() {
	}

	public void go() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							MyButton.this.doIt();
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void setParValue(String value) {
		this.value = value;
		if (Boolean.valueOf(value) != false) {
			this.setBackground(Color.GREEN);
		} else {
			this.setBackground(oldColor);
		}
	}

	public void setPin(PinDef pinDef) {
		this.pinDef = pinDef;
	}
	
	public PinDef getPin() {
		return this.pinDef;
	}
	
	
}
