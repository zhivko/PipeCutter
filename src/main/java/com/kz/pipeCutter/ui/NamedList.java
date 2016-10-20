package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;
import com.kz.pipeCutter.BBB.MyServiceInfo;

@SuppressWarnings("serial")
public class NamedList extends JPanel implements IParameter, IHasLabel {
	private JLabel jLabel;
	private final JList myList;

	private String parId;
	private String parValue;

	private DefaultListModel listModel;
	private String labelTxt;

	public NamedList() {
		super();
		this.setLayout(new FlowLayout());

		jLabel = new JLabel("this is label");
		this.add(jLabel);
		myList = new JList();
		myList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (!e.getValueIsAdjusting() && myList.getSelectedValue() != null)
					System.out.println(myList.getSelectedValue().toString());
			}

		});
		final JScrollPane pane = new JScrollPane(myList);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		listModel = new DefaultListModel();
		// Border border = BorderFactory.createLineBorder(Color.black);
		// this.setBorder(border);
		myList.setModel(listModel);

		// pane.setPreferredSize(new Dimension(400, 206));
		// this.setPreferredSize(new Dimension(394, 216));
		// myList.setPreferredSize(new Dimension(394-10, 216-10));

		// pane.setPreferredSize(new Dimension(450,430));
		this.add(pane);
		Dimension d = new Dimension(440, 230);
		// Dimension d1 = new Dimension(d.width-20, d.height-20);
		// myList.setPreferredSize(d);
		pane.setPreferredSize(d);
		this.setPreferredSize(new Dimension(new Double(d.getWidth()).intValue(), new Double(d.getHeight()).intValue() + 25));
		// this.setPreferredSize(d1);
	}

	@Override
	public String getParId() {
		// TODO Auto-generated method stub
		return this.parId;
	}

	@Override
	public void setParId(String parId) {
		// TODO Auto-generated method stub
		this.parId = parId;
	}

	@Override
	public String getParValue() {
		// TODO Auto-generated method stub
		return this.parValue;
	}

	@Override
	public void setParValue(String value) {
		this.parValue = value;
		String splited[] = value.split(",");
		myList.setListData(splited);
	}

	public void addValue(String value) {
		String[] splited = value.split(" ");
		boolean exist = false;
		for (int i = 0; i < this.listModel.size(); i++) {
			String listItemStr = this.listModel.getElementAt(i).toString();
			String[] listItemStrSplit = listItemStr.split(" ");
			if (listItemStrSplit[0].equals(splited[0])) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			this.listModel.addElement(value);
			this.repaint();
		}

	}

	public void removeValue(String value) {
		this.listModel.removeElement(value);
		this.repaint();
	}

	public String getLabelTxt() {
		return labelTxt;
	}

	public void setLabelTxt(String labelTxt) {
		this.labelTxt = labelTxt;
		this.jLabel.setText(labelTxt);
	}

	public void setListPrefferedSize(int width, int height) {
		this.myList.setPreferredSize(new Dimension(width, height));
		this.setPreferredSize(new Dimension(width + 3, height + 20));
	}

	public boolean containsValue(Object value) {
		return this.listModel.contains(value);
	}

	public void addServices(ArrayList<ServiceInfo> services) {
		this.listModel.removeAllElements();
		for (ServiceInfo serviceInfo : services) {
			MyServiceInfo mi = new MyServiceInfo(serviceInfo);
			this.listModel.addElement(mi.toString());
		}
	}

	public void addService(final ServiceInfo serviceInfo) {

		final MyServiceInfo mi = new MyServiceInfo(serviceInfo);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				getCommandServiceUrl(serviceInfo);
				getErrorServiceUrl(serviceInfo);
				getStatusServiceUrl(serviceInfo);
				getPreviewStatusServiceUrl(serviceInfo);
				getHalCmdServiceUrl(serviceInfo);
				
				for (int i = 0; i < listModel.getSize(); i++) {
					String row = listModel.getElementAt(i).toString();
					if (row.startsWith(mi.name)) {
						listModel.removeElementAt(i);
						break;
					}
				}
				listModel.addElement(mi.name + " (" + mi.url + ")");
				myList.repaint();
			}
		});

	}

	public static void getCommandServiceUrl(ServiceInfo serviceInfo) {
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("Command.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			serviceInfo.getQualifiedName();
			serviceInfo.getPort();
			final String commandUrl = "tcp://" + getServer(serviceInfo.getServer()) + ":" + ret.getPort();
			if (!commandUrl.equals(Settings.getInstance().getSetting("machinekit_commandService_url"))) {
				Settings.getInstance().setSetting("machinekit_commandService_url", commandUrl);
				Settings.instance.initCommandService();
			}
		}
	}

	private static String getServer(String server) {
		if (server.endsWith("."))
			return server.substring(0, server.length() - 1);
		return server;
	}

	public static void getErrorServiceUrl(ServiceInfo serviceInfo) {
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("Error.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			final String errorUrl = "tcp://" + getServer(serviceInfo.getServer()) + ":" + ret.getPort();
			if (!errorUrl.equals(Settings.getInstance().getSetting("machinekit_errorService_url"))) {
				Settings.getInstance().setSetting("machinekit_errorService_url", errorUrl);
				Settings.instance.initErrorService();
			}
		}
	}

	public static void getStatusServiceUrl(ServiceInfo serviceInfo) {
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("Status.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			final String statusUrl = "tcp://" + getServer(serviceInfo.getServer()) + ":" + ret.getPort();
			if (!statusUrl.equals(Settings.getInstance().getSetting("machinekit_statusService_url"))) {
				Settings.getInstance().setSetting("machinekit_statusService_url", statusUrl);
				Settings.instance.initStatusService();
			}
		}
	}

	public static void getPreviewStatusServiceUrl(ServiceInfo serviceInfo) {
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("Previewstatus.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			final String previewStatusUrl = "tcp://" + getServer(serviceInfo.getServer()) + ":" + ret.getPort();
			if (!previewStatusUrl.equals(Settings.getInstance().getSetting("machinekit_previewstatusService_url"))) {
				Settings.getInstance().setSetting("machinekit_previewstatusService_url", previewStatusUrl);
				Settings.getInstance().initPreviewStatusService();
			}
		}
	}

//	public static void getHalRCompServiceUrl(ServiceInfo serviceInfo) {
//		ServiceInfo ret = null;
//		if (serviceInfo.getName().matches("HAL Rcomp.*")) {
//			ret = serviceInfo;
//		}
//		if (ret != null) {
//			String ip = Settings.getInstance().getSetting("machinekit_ip");
//			String host = Settings.getInstance().getSetting("machinekit_host");
//			final String hallRCompUri = "tcp://" + getServer(serviceInfo.getServer()) + ":" + ret.getPort();
//			Settings.getInstance().setSetting("machinekit_halRCompService_url", hallRCompUri);
//			Settings.getInstance().initHalRcompService();
//		}
//	}

	public static void getHalCmdServiceUrl(ServiceInfo serviceInfo) {
		String hallGroupUrl = null;
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("HAL Rcommand.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			hallGroupUrl = "tcp://" + getServer(serviceInfo.getServer()) + ":" + ret.getPort();
			if (!hallGroupUrl.equals(Settings.getInstance().getSetting("machinekit_halCmdService_url"))) {
				Settings.getInstance().setSetting("machinekit_halCmdService_url", hallGroupUrl);
				Settings.instance.initHalCmdService();
			}
		}
	}

	public void removeService(ServiceInfo serviceInfo) {
		MyServiceInfo mi = new MyServiceInfo(serviceInfo);
		for (int i = 0; i < listModel.getSize(); i++) {
			String row = listModel.getElementAt(i).toString();
			if (row.startsWith(mi.name)) {
				listModel.removeElementAt(i);
			}
		}
	}

	public void removeAll() {
		this.myList.clearSelection();
		listModel.clear();
	}

	public String properUri(String uri) {
		if (uri.endsWith("/"))
			uri = uri.substring(0, uri.length() - 1);
		return uri;
	}
}
