package com.kz.pipeCutter.BBB;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.JmDNSImpl;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.kz.pipeCutter.ui.NamedList;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

public class Discoverer {
	private static ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
	private static Discoverer instance;
	private ArrayList<JmDNS> jMdnsS = new ArrayList<JmDNS>();
	private static String bonjourServiceType = "_machinekit._tcp.local.";

	ServiceListener bonjourServiceListener = new ServiceListener() {

		@Override
		public void serviceResolved(ServiceEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("resolved: " + arg0.toString());
		}

		@Override
		public void serviceRemoved(ServiceEvent arg0) {
			System.out.println("- " + arg0.getInfo().getName());
			services.remove(arg0.getInfo());
			Pattern p = Pattern.compile("(.*)service(.*)");
			Matcher m = p.matcher(arg0.getInfo().getName());
			final ServiceEvent arg1 = arg0;
			if (m.find()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (MachinekitSettings.instance != null)
							MachinekitSettings.instance.machinekitServices.removeService(arg1
									.getInfo());
					}
				});
			}
		}

		@Override
		public void serviceAdded(ServiceEvent arg0) {
			System.out.println("+ " + arg0.getInfo().getName());
			services.add(arg0.getInfo());
			Pattern p = Pattern.compile("(.*)service(.*)");
			Matcher m = p.matcher(arg0.getInfo().getName());
			if (m.find()) {
				final ServiceEvent arg1 = arg0;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (MachinekitSettings.instance != null)
							MachinekitSettings.instance.machinekitServices.addService(arg1
									.getInfo());
					}
				});
			}
		}
	};

	public static void main(String[] args) {
		Discoverer discoverer = Discoverer.getInstance();
	}

	public Discoverer() {
		instance = this;
		boolean log = true;
		if (log) {
			Logger logger = Logger.getLogger(JmDNS.class.getName());
			ConsoleHandler handler = new ConsoleHandler();
			logger.addHandler(handler);
			logger.setLevel(Level.FINER);
			handler.setLevel(Level.FINER);
		}

		System.out.println("Initializing discoverer...");

		Enumeration<NetworkInterface> ifc;
		try {
			ifc = NetworkInterface.getNetworkInterfaces();
			while (ifc.hasMoreElements()) {
				NetworkInterface anInterface = ifc.nextElement();
				try {
					if (anInterface.isUp()) {
						Enumeration<InetAddress> addr = anInterface.getInetAddresses();
						while (addr.hasMoreElements()) {
							InetAddress address = addr.nextElement();
							if (!address.equals(InetAddress.getLoopbackAddress())) {
								JmDNS jmdns = JmDNSImpl.create(address, bonjourServiceType);
								jMdnsS.add(jmdns);
								System.out.println("Adding bonjour listener on local IP: "
										+ address.toString());
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// TimerTask myDiscoveryTask = new TimerTask() {
		// @Override
		// public void run() {
		// discover();
		// }
		// };
		// Timer discoveryTimer = new Timer("DiscoveryTimer");
		// discoveryTimer.scheduleAtFixedRate(myDiscoveryTask, 0, 5000);
		// discoveryTimer.schedule(myDiscoveryTask, 0, 5000);
		// discover();
	}

	public static Discoverer getInstance() {
		if (instance == null)
			instance = new Discoverer();
		return instance;
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		System.out.println("destructor discoverer");
		super.finalize();
	}

	public void discover() {
		Thread t = new Thread(new Runnable() {
			public void run() {

				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							if (MachinekitSettings.instance != null)
								MachinekitSettings.instance.machinekitServices.removeAll();

							for (JmDNS jmDNS : Discoverer.this.jMdnsS) {
								System.out.println("Discovering Machinekit services...");
								jmDNS.addServiceListener(bonjourServiceType,
										bonjourServiceListener);
								ServiceInfo[] infos = jmDNS.list(bonjourServiceType);
								jmDNS.addServiceListener(bonjourServiceType,
										bonjourServiceListener);
							}
						}
					});
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		t.start();
	}

	public ArrayList<ServiceInfo> getDiscoveredServices() {
		return this.services;
	}
}
