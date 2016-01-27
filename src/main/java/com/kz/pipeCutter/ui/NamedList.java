package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.kz.pipeCutter.BBB.MyServiceInfo;
import com.kz.pipeCutter.BBB.commands.MachineTalkCommand;

@SuppressWarnings("serial")
public class NamedList extends JPanel implements IParameter, IHasLabel {
	private JLabel jLabel;
	private JList myList;

	private String parId;
	private String parValue;

	private DefaultListModel listModel;
	private String labelTxt;

	public NamedList() {
		super();
		this.setLayout(new MyVerticalFlowLayout());

		jLabel = new JLabel("this is label");
		this.add(jLabel);
		myList = new JList();
		myList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if (!e.getValueIsAdjusting())
					System.out.println(myList.getSelectedValue().toString());
			}

		});
		JScrollPane pane = new JScrollPane(myList);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		listModel = new DefaultListModel();
		// Border border = BorderFactory.createLineBorder(Color.black);
		// this.setBorder(border);
		myList.setModel(listModel);
		this.add(pane);
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

	public void addService(ServiceInfo serviceInfo) {
		MyServiceInfo mi = new MyServiceInfo(serviceInfo);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < listModel.getSize(); i++) {
					String row = listModel.getElementAt(i).toString();
					if (row.startsWith(mi.name)) {
						listModel.removeElementAt(i);
						break;
					}
				}
				listModel.addElement(mi.toString());
			}
		});

		getCommandServiceUrl(serviceInfo);
		getErrorServiceUrl(serviceInfo);
		getStatusServiceUrl(serviceInfo);
		getPreviewStatusServiceUrl(serviceInfo);
	}

	public static void getCommandServiceUrl(ServiceInfo serviceInfo) {
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("Command.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			String commandUrl = "tcp://" + serviceInfo.getServer() + ":" + ret.getPort() + "/";
			Settings.getInstance().setSetting("machinekit_commandService_url", commandUrl);
			MachineTalkCommand.commandSocket=null;
		}
	}

	public static void getErrorServiceUrl(ServiceInfo serviceInfo) {
		ServiceInfo ret = null;
		if (serviceInfo.getName().matches("Error.*")) {
			ret = serviceInfo;
		}
		if (ret != null) {
			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String host = Settings.getInstance().getSetting("machinekit_host");
			String errorUrl = "tcp://" + serviceInfo.getServer() + ":" + ret.getPort() + "/";
			Settings.getInstance().setSetting("machinekit_errorService_url", errorUrl);
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
			String errorUrl = "tcp://" + serviceInfo.getServer() + ":" + ret.getPort() + "/";
			if(!errorUrl.equals(Settings.getInstance().getSetting("machinekit_statusService_url")))
			{
				Settings.getInstance().setSetting("machinekit_statusService_url", errorUrl);
				Settings.instance.initServices();
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
			String previewStatusUrl = "tcp://" + serviceInfo.getServer() + ":" + ret.getPort() + "/";
			if(!previewStatusUrl.equals(Settings.getInstance().getSetting("machinekit_previewstatusService_url")))
			{
				Settings.getInstance().setSetting("machinekit_previewstatusService_url", previewStatusUrl);
				Settings.instance.initServices();				
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

}
