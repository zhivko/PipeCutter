package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.FlowLayout;

@SuppressWarnings("serial")
public abstract class SavableControl extends JPanel implements IParameter, ISaveableAndLoadable {

	private String parId;
	private String parValue;
	public String iniFullFileName;
	private String labelTxt;
	public JTextField jValue;
	public JLabel jLabel;
	boolean needsSave=true;

	public boolean isNeedsSave() {
		return needsSave;
	}

	public void setNeedsSave(boolean needsSave) {
		this.needsSave = needsSave;
	}

	private boolean isLoadingValue;

	public JPanel panel;

	public SavableControl() {
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
				SavableControl.this.parValue = jValue.getText();
				try {
					if(!SavableControl.this.isLoadingValue)
						SavableControl.this.save();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		jLabel = new JLabel("This is label:");
		add(jLabel);
		add(jValue);
		jValue.setColumns(10);
	}

	public synchronized void save() throws IOException {
		if (this.isNeedsSave() && !this.isLoadingValue && Settings.getInstance() != null)
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
		this.isLoadingValue = true;
		FileInputStream in = new FileInputStream(Settings.iniFullFileName);
		Properties props = new Properties();
		props.load(in);
		in.close();
		if (props.getProperty(this.getParId()) != null) {
			this.isLoadingValue = true;
			this.setParValue(props.getProperty(this.getParId()));
			this.isLoadingValue = false;
		} else
			this.jValue.setText("");
		this.isLoadingValue = false;
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
		//SwingUtilities.invokeLater(new Runnable() {
		//	
		//	@Override
		//	public void run() {
		this.jValue.setText(value);
		//	}
		//});
		
	}

	public String getLabelTxt() {
		return labelTxt;
	}

	public void setLabelTxt(String labelTxt) {
		this.labelTxt = labelTxt;
		this.jLabel.setText(labelTxt);
		int length = this.jLabel.getFontMetrics(this.jLabel.getFont()).stringWidth(labelTxt);
		this.jLabel.setPreferredSize(new Dimension(length,12));
	}

	public boolean isLoadingValue()
	{
		return this.isLoadingValue;
	}
}
