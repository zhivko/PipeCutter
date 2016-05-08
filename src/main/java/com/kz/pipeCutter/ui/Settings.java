package com.kz.pipeCutter.ui;

//install bbonjour avahi sevice in windows: https://support.apple.com/kb/DL999?locale=sl_SI
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.kz.pipeCutter.BBB.BBBError;
import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.ui.tab.GcodeViewer;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;
import com.kz.pipeCutter.ui.tab.OtherSettings;
import com.kz.pipeCutter.ui.tab.PlasmaSettings;
import com.kz.pipeCutter.ui.tab.RotatorSettings;
import com.kz.pipeCutter.ui.tab.XYZSettings;

public class Settings extends JFrame {

	private JPanel contentPane;
	public static String iniFullFileName = getIniPath();
	public static Settings instance;
	public static Discoverer discoverer;
	public static BBBError error;
	public static BBBStatus status;

	public JSplitPane splitPane;
	CommandPanel commandPanel;

	boolean repositioned = false;

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
				// discoverer = Discoverer.getInstance();
				Settings.instance.initServices();
			}
		});

	}

	protected void initServices() {
		error = new BBBError();
		status = new BBBStatus();
	}

	/**
	 * Create the frame.
	 */
	public Settings() {

		this.setTitle("PipeCutter settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setBounds(400, 500, 800, 650);
		this.setPreferredSize(new Dimension(900, 700));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		// tabbedPane.setMinimumSize(new Dimension(600, 500));

		tabbedPane.addTab("MachinekitSettings", new MachinekitSettings());
		tabbedPane.addTab("Rotators", new RotatorSettings());
		tabbedPane.addTab("XYZ", new XYZSettings());
		tabbedPane.addTab("Plasma", new PlasmaSettings());
		tabbedPane.addTab("Other", new OtherSettings());
		tabbedPane.addTab("Gcode", new GcodeViewer());

		tabbedPane.setSelectedIndex(1);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(splitPane, BorderLayout.NORTH);
		splitPane.setDividerLocation(490);

		commandPanel = new CommandPanel();
		splitPane.setTopComponent(tabbedPane);
		splitPane.setBottomComponent(commandPanel);

		this.pack();
		Settings.instance = this;

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				Component c = (Component) evt.getSource();
				System.out.println(c.getName() + " resized: " + c.getSize().toString());
				if (c.getName().equals("frame0") && repositioned) {
					try {
						FileInputStream in = new FileInputStream(Settings.iniFullFileName);
						SortedProperties props = new SortedProperties();
						props.load(in);
						in.close();

						FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
						props.setProperty("frame0", c.getSize().getWidth() + "x" + c.getSize().getHeight());
						props.store(out, null);
						out.close();

					} catch (Exception ex) {
						ex.printStackTrace();
					}
					// splitPane.setDividerLocation(1 -
					// (commandPanel.getHeight() /
					// Settings.instance.getHeight()));
				}
			}
		});

		this.addComponentListener(new ComponentAdapter() {
			public void componentMoved(ComponentEvent evt) {
				if (repositioned) {
					Component c = (Component) evt.getSource();
					Point currentLocationOnScreen = c.getLocationOnScreen();
					System.out.println("frame moved.");
					FileInputStream in;
					try {
						in = new FileInputStream(Settings.iniFullFileName);

						SortedProperties props = new SortedProperties();
						props.load(in);
						in.close();

						FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
						props.setProperty("frame0_location",
								String.format("%.0fx%.0f", currentLocationOnScreen.getX(), currentLocationOnScreen.getY()));
						props.store(out, null);
						out.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			public void componentHidden(ComponentEvent e) {
				/* code run when component hidden */
			}

			public void componentShown(ComponentEvent e) {
				FileInputStream in;
				try {
					in = new FileInputStream(Settings.iniFullFileName);
					SortedProperties props = new SortedProperties();
					props.load(in);
					in.close();

					if (props.get("frame0") != null) {
						String size = props.get("frame0").toString();
						try {
							String[] splittedSize = size.split("x");
							System.out.println(size);
							Settings.this.setSize(new Dimension(Double.valueOf(splittedSize[0]).intValue(),
									Double.valueOf(splittedSize[1]).intValue()));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						System.out.println("size: " + size);
					}

					if (props.get("frame0_location") != null) {
						String location_str = props.get("frame0_location").toString();
						try {
							String[] splittedSize = location_str.split("x");
							System.out.println(location_str);
							Settings.this.setLocation(new Point(Integer.valueOf(splittedSize[0]), Integer.valueOf(splittedSize[1])));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						System.out.println("size: " + location_str);
					}
					
					

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally {
					repositioned=true;
				}
			}
		});

		this.setVisible(true);

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
		// TODO Auto-generated method stub
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

	public void setSetting(String parameterId, Double value) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
		// otherSymbols.setDecimalSeparator('.');
		// otherSymbols.setGroupingSeparator(',');

		DecimalFormat df = new DecimalFormat("##,##0.0000", otherSymbols);
		df.setDecimalSeparatorAlwaysShown(true);
		String strValue = df.format(value);
		setSetting(parameterId, strValue);
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

	public static <T extends Component> List<T> harvestMatches(Container root, Class<T> clazz) {
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

	public void log(String txt) {
		System.out.println(txt);
		txt = txt.replaceAll("reply_ticket: (.*)\n", "");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		commandPanel.log.append(sdf.format(new Date()) + " " + txt);
		commandPanel.log.setCaretPosition(commandPanel.log.getText().length());
	}
}
