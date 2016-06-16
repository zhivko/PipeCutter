package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

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
		//jValue.setColumns(1);

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
		//jValue.setColumns(10);

	}

	@Override
	public void setParValue(String val) {
		this.jValue.setText(val);
		//jValue.setColumns(val.length());

		AffineTransform affinetransform = new AffineTransform();     
		FontRenderContext frc = new FontRenderContext(affinetransform,true,true);     
		Font font = new Font("Tahoma", Font.PLAIN, 12);
		int textwidth = (int)(font.getStringBounds(val, frc).getWidth());
		int textheight = (int)(font.getStringBounds(val, frc).getHeight());
		this.jValue.setPreferredSize(new Dimension(textwidth+15, textheight+3));
		
		
//		int width = jValue.getFontMetrics(jValue.getFont().getbou ).stringWidth(val);
//		int height = jValue.getFontMetrics(jValue.getFont()).getHeight();
//		this.jValue.setPreferredSize(new Dimension(width, height));
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
