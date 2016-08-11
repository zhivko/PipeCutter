package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class SavableText extends SavableControl {
	public JTextField jValue;
	public boolean preventResize = false;

	public SavableText() {
		super();

		jValue = new JTextField();
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
		SavableText.this.jValue.setText(val);
		resizeBox();
//		final String myValue = val;
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				SavableText.this.jValue.setText(myValue);
//				//resizeBox();
//			}
//		});
	}

	@Override
	public String getParValue() {
		return this.jValue.getText();
	}

	@Override
	public void valueChangedFromUI() {
		if (!preventResize) {
			resizeBox();
			this.getParent().revalidate();
		}
	}

	private void resizeBox() {
		String val = jValue.getText();
		AffineTransform affinetransform = new AffineTransform();
		FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
		Font font = new Font("Tahoma", Font.PLAIN, 12);
		this.setFont(font);
		int textwidth = (int) (font.getStringBounds(val, frc).getWidth() * 1.2);
		int textheight = (int) (font.getStringBounds(val, frc).getHeight());
		
		if(textwidth<30)
			textwidth = 30;
		
		this.jValue
				.setPreferredSize(new Dimension(textwidth, textheight + 3));
	}

	@Override
	public PinDef getPin() {
		// TODO Auto-generated method stub
		return pinDef;
	}

}
