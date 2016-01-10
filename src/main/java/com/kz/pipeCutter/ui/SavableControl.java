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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

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
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 91, 74, 0 };
		gridBagLayout.rowHeights = new int[] { 20, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

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
				SavableControl.this.parValue = jValue.getText();
				try {
					SavableControl.this.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		jLabel = new JLabel("This is label:");
		GridBagConstraints gbc_jLabel = new GridBagConstraints();
		gbc_jLabel.anchor = GridBagConstraints.WEST;
		gbc_jLabel.insets = new Insets(0, 0, 0, 5);
		gbc_jLabel.gridx = 0;
		gbc_jLabel.gridy = 0;
		add(jLabel, gbc_jLabel);
		GridBagConstraints gbc_jValue = new GridBagConstraints();
		gbc_jValue.anchor = GridBagConstraints.NORTHEAST;
		gbc_jValue.gridx = 1;
		gbc_jValue.gridy = 0;
		add(jValue, gbc_jValue);
		jValue.setColumns(10);

	}

	public void save() throws IOException {
		if (!this.isLoadingValue && Settings.instance != null && Settings.instance.isVisible())
			if (this.getParValue() != null) {
				FileInputStream in = new FileInputStream(Settings.iniFullFileName);
				SortedProperties props = new SortedProperties();
				props.load(in);
				in.close();

				FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
				props.setProperty(this.getParId(), this.getParValue());
				props.store(out, null);
				out.close();
			} else {
				System.out.println("Value for: " + this.getParId() + " is NULL!");
			}

	}

	public void load() throws IOException {
		// TODO Auto-generated method stub
		FileInputStream in = new FileInputStream(Settings.iniFullFileName);
		Properties props = new Properties();
		props.load(in);
		in.close();
		if (props.getProperty(this.getParId()) != null) {
			this.isLoadingValue = true;
			this.setParValue(props.getProperty(this.getParId()));
			this.isLoadingValue = false;
		}
		else
			this.jValue.setText("");
	}

	public String getParId() {
		return this.parId;
	}

	public void setParId(String parId) {
		this.parId = parId;
		try {
			load();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

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
