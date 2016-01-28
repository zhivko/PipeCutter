package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class Positioner extends JPanel {
	public Positioner() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{35, 0, 51, 46, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 32, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JButton button_1 = new JButton("R");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JSlider slider_1 = new JSlider();
		slider_1.setSnapToTicks(true);
		slider_1.setPaintTicks(true);
		slider_1.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_slider_1 = new GridBagConstraints();
		gbc_slider_1.fill = GridBagConstraints.BOTH;
		gbc_slider_1.gridheight = 4;
		gbc_slider_1.insets = new Insets(0, 0, 0, 5);
		gbc_slider_1.gridx = 0;
		gbc_slider_1.gridy = 1;
		add(slider_1, gbc_slider_1);
		
		JButton btnGor = new JButton("U");
		btnGor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_btnGor = new GridBagConstraints();
		gbc_btnGor.insets = new Insets(0, 0, 5, 5);
		gbc_btnGor.fill = GridBagConstraints.BOTH;
		gbc_btnGor.gridx = 2;
		gbc_btnGor.gridy = 1;
		add(btnGor, gbc_btnGor);
		
		JButton button_2 = new JButton("L");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_button_2 = new GridBagConstraints();
		gbc_button_2.insets = new Insets(0, 0, 5, 5);
		gbc_button_2.gridx = 1;
		gbc_button_2.gridy = 2;
		add(button_2, gbc_button_2);
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_1.insets = new Insets(0, 0, 5, 0);
		gbc_button_1.gridx = 3;
		gbc_button_1.gridy = 2;
		add(button_1, gbc_button_1);
		
		JButton button = new JButton("D");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 2;
		gbc_button.gridy = 3;
		add(button, gbc_button);
		
		JSlider slider = new JSlider();
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.anchor = GridBagConstraints.SOUTH;
		gbc_slider.gridwidth = 3;
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 4;
		add(slider, gbc_slider);
		
		this.setPreferredSize(new Dimension(180,150));
	}

}
