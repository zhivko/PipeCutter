package com.kz.pipeCutter.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Settings extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Settings frame = new Settings();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Settings() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel textPanel = new JPanel();
		contentPane.add(textPanel, BorderLayout.SOUTH);
		
		JScrollPane scrollPane = new JScrollPane();
		textPanel.add(scrollPane);
		textPanel.setMinimumSize(new Dimension(600,600));
		
		JTextArea textArea = new JTextArea();
		scrollPane.add(textArea);
		
		
		JPanel tabPanel = new JPanel();
		contentPane.add(tabPanel, BorderLayout.CENTER);
		tabPanel.setMinimumSize(new Dimension(600,200));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabPanel.add(tabbedPane);
	}

}
