package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.concurrent.Semaphore;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.kz.pipeCutter.SurfaceDemo;

@SuppressWarnings("serial")
public class SavableText extends SavableControl {
	public JTextField jValue;
	String value;

	Semaphore semaphore = new Semaphore(1);

	public boolean preventResize = false;

	public SavableText() {
		super();

		jValue = new JTextField();
		jValue.setMinimumSize(new Dimension(50, 50));
		// jValue.setMinimumSize();
		jValue.setHorizontalAlignment(SwingConstants.LEFT);
		jValue.setText("This is value");
		// jValue.setColumns(1);

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
					if (!SavableText.this.isLoadingValue()) {
						value = jValue.getText();
						SavableText.this.save();
						valueChangedFromUI();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		add(jValue);
		// jValue.setColumns(10);

	}

	@Override
	public void setParValue(String val) {

		semaphore.acquireUninterruptibly();

		try {
			// synchronized (this.value) {
			if (!this.isLoadingValue && SurfaceDemo.getInstance().isInitialized() && val.equals(""))
			{
				System.out.println(this.getParId() + " empty value: " + Thread.currentThread().getName());
				if(this.getParId().startsWith("position_") || this.getParId().equals("mymotion.laserHeight1mm") )
					System.out.println("");
			}

			if (!val.trim().equals(""))
				this.value = val.trim();
			else
				this.value = "0";
			// resizeBox();
			// String myValue = val;
			// SwingUtilities.invokeLater(new Runnable() {
			//
			// @Override
			// public void run() {

			// }
			// });
			// }
		} finally {
			semaphore.release();
		}
		SavableText.this.jValue.setText(this.value);
		resizeBox();
	}

	@Override
	public String getParValue() {
		String ret;
		semaphore.acquireUninterruptibly();
		try {
			if (this.getParId().equals("mymotion.laserHeight1") && value.trim().equals(""))
			{
				System.out.println(this.getParId() + " empty value: " + Thread.currentThread().getName());
			}
			ret = this.value;
		} finally {
			semaphore.release();
		}
		return ret;
	}

	@Override
	public void valueChangedFromUI() {
		if (!preventResize) {
			// resizeBox();
		}
		updateHal();
	}

	private void resizeBox() {
		String val = jValue.getText();
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
		Font font = new Font("Tahoma", Font.PLAIN, 12);
		this.setFont(font);
		int textwidth = (int) (font.getStringBounds(val, frc).getWidth() * 1.2);
		int textheight = (int) (font.getStringBounds(val, frc).getHeight());

		if (textwidth < 30)
			textwidth = 30;

		this.jValue.setPreferredSize(new Dimension(textwidth, textheight + 3));

	}

	@Override
	public PinDef getPin() {
		// TODO Auto-generated method stub
		return pinDef;
	}

}
