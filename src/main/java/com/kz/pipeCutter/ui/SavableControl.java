package com.kz.pipeCutter.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public abstract class SavableControl extends JPanel {

	private String parId;
	private String parValue;
	public String iniFullFileName;
	private String labelTxt;
	public JTextField jValue;
	public JLabel jLabel;

	boolean isLoadingValue;

	public JPanel panel;

	public SavableControl() {
		super();
		jLabel = new JLabel("This is label:");
		add(jLabel);

		jValue = new JTextField();
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
				SavableControl.this.parValue = jValue.getText();
				try {
					SavableControl.this.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		add(jValue);
		jValue.setColumns(10);

	}

	public void save() throws IOException {
		if (!this.isLoadingValue && Settings.instance != null && Settings.instance.isVisible())
			if (this.getParValue() != null) {
				FileInputStream in = new FileInputStream(Settings.instance.iniFullFileName);
				Properties props = new Properties();
				props.load(in);
				in.close();

				FileOutputStream out = new FileOutputStream(Settings.instance.iniFullFileName);
				props.setProperty(this.getParId(), this.getParValue());
				props.store(out, null);
				out.close();
			} else {
				System.out.println("Value for: " + this.getParId() + " is NULL!");
			}

	}

	public void load() throws IOException {
		// TODO Auto-generated method stub
		FileInputStream in = new FileInputStream(Settings.instance.iniFullFileName);
		Properties props = new Properties();
		props.load(in);
		in.close();
		if (props.getProperty(this.getParId()) != null) {
			this.isLoadingValue = true;
			this.setParValue(props.getProperty(this.getParId()));
			this.isLoadingValue = false;
		}
	}

	public String getParId() {
		return this.parId;
	}

	public void setParId(String parId) {
		this.parId = parId;
	}

	public String getParValue() {
		return this.parValue;
	}

	public String getIniFullFileName() {
		// TODO Auto-generated method stub
		return iniFullFileName;
	}

	public void setParValue(String value) {
		this.parValue = value;
		this.jValue.setText(value);
	}

	public String getLabelTxt() {
		return labelTxt;
	}

	public void setLabelTxt(String labelTxt) {
		this.labelTxt = labelTxt;
		this.jLabel.setText(labelTxt);
	}

}
