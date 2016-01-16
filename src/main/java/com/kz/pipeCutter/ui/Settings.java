package com.kz.pipeCutter.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;
import com.kz.pipeCutter.ui.tab.RotatorSettings;

import pb.Status.EmcTaskModeType;

public class Settings extends JFrame {

	private JPanel contentPane;
	public static double parDistance;
	public static EmcTaskModeType parMode;
	public static String parMdiCommand; 
	
	public static String iniFullFileName = getIniPath();
	public static Settings instance;
	public static Discoverer discoverer;
	
	public static int parAxisNo = 0; 
	public static double parVelocity = 0;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		System.setProperty("java.net.preferIPv4Stack", "true");
		Settings frame = Settings.getInstance();
		frame.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				/* code run when component hidden */
			}

			public void componentShown(ComponentEvent e) {
				discoverer = Discoverer.getInstance();
			}
		});
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public Settings() {

		this.setTitle("PipeCutter settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(400, 500, 782, 516);
		this.setPreferredSize(new Dimension(800, 450));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		// tabbedPane.setMinimumSize(new Dimension(600, 500));
		contentPane.add(tabbedPane);

		tabbedPane.addTab("MachinekitSettings", new MachinekitSettings());
		tabbedPane.addTab("Rotators", new RotatorSettings());

		tabbedPane.setSelectedIndex(1);

		this.pack();
		Settings.instance = this;
	}

	private static String getIniPath() {
		String ret = null;
		String iniFileName = "pipeCutter.ini";
		iniFullFileName = null;
		try {
			String path = new File(".").getCanonicalPath();
			ret = path + File.separator + iniFileName;
			File f = new File(ret);
			if (!f.exists()) {
				System.out.println(ret + " does not exist. Creating in path:" + path);
				File fout = new File(ret);
				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				bw.write("#pipecutter ini file");
				bw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public String getSetting(String parameterId) {
		String ret = null;
		try {
			FileInputStream in = new FileInputStream(Settings.iniFullFileName);
			Properties props = new Properties();
			props.load(in);
			in.close();
			if (props.getProperty(parameterId) != null) {
				ret = props.getProperty(parameterId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public void setSetting(String parameterId, String value) {
		try {
			List<SavableControl> savableControls = harvestMatches(this.getContentPane(), SavableControl.class);
			for (SavableControl savableControl : savableControls) {
				if (savableControl.getParId().equals(parameterId)) {
					savableControl.setParValue(value);
					savableControl.save();
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public IParameter getParameter(String parameterId) {
		IParameter ret = null;
		List<SavableControl> savableControls = harvestMatches(this.getContentPane(), SavableControl.class);
		for (SavableControl savableControl : savableControls) {
			System.out.println("control  id:" + savableControl.getParId());
			if (savableControl.getParId().equals(parameterId)) {
				ret = savableControl;
				break;
			}
		}
		return ret;
	}

	public String getHostOrIp() {
		String host = getSetting("machine_host");
		String ip = getSetting("machine_ip");
		if (host != null)
			return host;
		else if (ip != null)
			return ip;

		return null;
	}

	public <T extends Component> List<T> harvestMatches(Container root, Class<T> clazz) {
		List<Container> containers = new LinkedList<>();
		List<T> harvested = new ArrayList<>();

		containers.add(root);
		while (!containers.isEmpty()) {
			Container container = containers.remove(0);
			for (Component component : container.getComponents()) {
				if (clazz.isAssignableFrom(component.getClass())) {
					harvested.add((T) component);
				} else if (component instanceof Container) {
					containers.add((Container) component);
				}
			}
		}
		return Collections.unmodifiableList(harvested);
	}

	public static Settings getInstance() {
		if (instance == null)
			instance = new Settings();
		return instance;
	}
}
