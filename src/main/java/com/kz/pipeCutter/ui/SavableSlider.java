package com.kz.pipeCutter.ui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class SavableSlider extends SavableControl {
	private JSlider slider;
	private int minValue;
	private int maxValue;
	private int stepValue;

	private String values;
	Hashtable<Integer, JLabel> table;
	private JPanel panel1;
	private JPanel panel2;

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
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

	}

	public SavableSlider() {
		super();
		this.setLayout(new MyVerticalFlowLayout());

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
				//if (Settings.instance != null) {
					String txt = SavableSlider.this.table.get(SavableSlider.this.slider.getValue()).getText();
					SavableSlider.this.setParValue(txt);
				//}
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

	public int getStepValue() {
		return stepValue;
	}

	public void setStepValue(int stepValue) {
		this.stepValue = stepValue;
	}

}
