package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class SavableSlider extends SavableControl {
	private JSlider slider;
	private int minValue;
	private int maxValue;

	private JTextField jValue;

	private String values;
	Hashtable<Integer, JLabel> table;
	private JPanel panel1;
	private JPanel panel2;

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.isLoadingValue = true;
		this.values = values;

		String[] splittedValues = values.split(",");
		table = new Hashtable<Integer, JLabel>();
		int i = 0;
		int maxcol = 1;
		for (String strValue : splittedValues) {
			table.put(i, new JLabel(strValue));
			i++;
			if (strValue.length() > maxcol)
				maxcol = strValue.length();
		}
		this.jValue.setColumns(maxcol);
		this.slider.setLabelTable(table);
		this.slider.setMaximum(splittedValues.length - 1);
		this.slider.setMinimum(0);
		this.setPreferredSize(new Dimension(300, 100));
		this.isLoadingValue = false;
	}

	public SavableSlider() {
		super();
		this.setLayout(new MyVerticalFlowLayout());

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
				try {
					if (!SavableSlider.this.isLoadingValue()) {
						SavableSlider.this.save();
						valueChangedFromUI();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		panel1 = new JPanel();
		this.add(panel1);
		panel1.setLayout(new FlowLayout());
		panel1.add(this.jLabel);
		panel1.add(this.jValue);

		slider = new JSlider();
		slider.setSnapToTicks(true);
		panel2 = new JPanel();
		panel1.setLayout(new FlowLayout());
		this.add(panel2);
		panel2.add(slider);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		GridBagConstraints gbc_panel2 = new GridBagConstraints();
		gbc_panel2.fill = GridBagConstraints.BOTH;
		gbc_panel2.gridx = 0;
		gbc_panel2.gridy = 1;
		add(panel2, gbc_panel2);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (!isLoadingValue)
					if (Settings.getInstance() != null && !SavableSlider.this.slider.getValueIsAdjusting()) {
						String txt = SavableSlider.this.table.get(SavableSlider.this.slider.getValue()).getText();
						txt = txt.replaceAll("²", "0");
						txt = txt.replaceAll("³", "00");
						SavableSlider.this.setParValue(txt);
					}
			}
		});
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
		this.slider.setMinimum(minValue);
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		this.slider.setMaximum(maxValue);
	}

	public void setStepValue(int stepValue) {
		while (SavableSlider.this.table.keys().hasMoreElements()) {
			Integer i = SavableSlider.this.table.keys().nextElement();
			if (Integer.valueOf(SavableSlider.this.table.get(i).getText()).intValue() == stepValue) {
				SavableSlider.this.slider.setValue(i);
				break;
			}
		}
	}

	@Override
	public void setParValue(String val) {
		this.jValue.setText(val);
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
