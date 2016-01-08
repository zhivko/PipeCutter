package com.kz.pipeCutter.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JSlider;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class Settings extends JFrame {

	private JPanel contentPane;
	public String iniFullFileName = null;
	public static Settings instance=null;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Settings frame = new Settings();
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

		String iniFileName = "pipeCutter.ini";
		iniFullFileName = null;
		try {
			String path = new File(".").getCanonicalPath();
			iniFullFileName = path + File.separator + iniFileName;
			File f = new File(iniFullFileName);
			if (!f.exists()) {
				System.out.println(iniFullFileName + " does not exist. Creating in path:" + path);
				File fout = new File(iniFullFileName);
				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				bw.write("#pipecutter ini file");
				bw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.setTitle("PipeCutter settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 824, 514);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel textPanel = new JPanel();
		contentPane.add(textPanel);

		JScrollPane scrollPane = new JScrollPane();
		textPanel.add(scrollPane);
		textPanel.setMinimumSize(new Dimension(600, 600));

		JTextArea textArea = new JTextArea();
		textArea.setColumns(1);
		textArea.setRows(10);
		scrollPane.add(textArea);
		textArea.setText("This is textArea...");

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setMinimumSize(new Dimension(600, 200));
		contentPane.add(tabbedPane);

		JPanel tabPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tabbedPane.addTab("Settings", tabPanel1);

		JTextField tab1_field1 = new JTextField();
		tab1_field1.setText("This is text 1");
		tabPanel1.add(tab1_field1);

		JPanel tabPanel2 = new JPanel();
		tabPanel2.setPreferredSize(new Dimension(220, 250));
		FlowLayout flowLayout = (FlowLayout) tabPanel2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		tabbedPane.addTab("Rotators", tabPanel2);

		// ----------ROTATOR 1---------------------------

		JPanel panelRotator1 = new JPanel();
		panelRotator1.setPreferredSize(new Dimension(220, 450));
		tabPanel2.add(panelRotator1);
		panelRotator1.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel1 = new JLabel("Rotator1");
		panelRotator1.add(lblNewLabel1);

		SavableText rotator1_vel = new SavableText();
		rotator1_vel.setParId("rotator1_vel1");
		rotator1_vel.setLabelTxt("velocity:");
		panelRotator1.add(rotator1_vel);

		SavableText rotator1_acel = new SavableText();
		rotator1_acel.setLabelTxt("acceleration:");
		panelRotator1.add(rotator1_acel);
		rotator1_acel.setParId("rotator1_acel1");

		SavableSlider sliderRot1 = new SavableSlider();
		sliderRot1.setValues("1,10,100,1000");
		sliderRot1.setLabelTxt("Move for:");
		sliderRot1.setMinValue(0);
		sliderRot1.setMaxValue(3);
		sliderRot1.setStepValue(1);
		sliderRot1.setParId("rotator1_step");
		panelRotator1.add(sliderRot1);

		// ----------ROTATOR 2---------------------------
		JPanel panelRotator2 = new JPanel();
		panelRotator2.setPreferredSize(new Dimension(220, 450));
		panelRotator2.setMinimumSize(new Dimension(250, 200));
		tabPanel2.add(panelRotator2);
		panelRotator2.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel2 = new JLabel("Rotator2");
		panelRotator2.add(lblNewLabel2);

		SavableText rotator2_vel1 = new SavableText();
		rotator2_vel1.setLabelTxt("velocity");
		panelRotator2.add(rotator2_vel1);
		rotator2_vel1.setParId("rotator2_vel");

		SavableText savableSetting = new SavableText();
		savableSetting.setLabelTxt("acceleration:");
		savableSetting.setParId("rotator2_acc");
		panelRotator2.add(savableSetting);

		SavableSlider sliderRot2 = new SavableSlider();
		sliderRot2.setValues("1,10,100,1000");
		sliderRot2.setLabelTxt("Move for:");
		sliderRot2.setMinValue(0);
		sliderRot2.setMaxValue(3);
		sliderRot2.setStepValue(1);
		sliderRot2.setParId("rotator2_step");
		panelRotator2.add(sliderRot2);

		// ----------ROTATOR 3---------------------------
		JPanel panelRotator3 = new JPanel();
		panelRotator3.setPreferredSize(new Dimension(220, 450));
		panelRotator3.setLayout(new MyVerticalFlowLayout());
		panelRotator3.setMinimumSize(new Dimension(250, 200));
		tabPanel2.add(panelRotator3);
		JLabel lblNewLabel3 = new JLabel("Rotator3");
		panelRotator3.add(lblNewLabel3);

		SavableText rotator3Speed = new SavableText();
		rotator3Speed.setLabelTxt("velocity:");
		rotator3Speed.setParId("rotator3_vel");
		panelRotator3.add(rotator3Speed);

		SavableText savableSetting_1 = new SavableText();
		savableSetting_1.setLabelTxt("acceleration:");
		savableSetting_1.setParId("rotator3_acc");
		panelRotator3.add(savableSetting_1);

		SavableSlider sliderRot3 = new SavableSlider();
		sliderRot3.setValues("1,10,100,1000");
		sliderRot3.setLabelTxt("Move for:");
		sliderRot3.setMinValue(0);
		sliderRot3.setMaxValue(3);
		sliderRot3.setStepValue(1);
		sliderRot3.setParId("rotator3_step");
		panelRotator3.add(sliderRot3);

		tabbedPane.setSelectedIndex(1);
		Settings.instance = this;

	}

}
