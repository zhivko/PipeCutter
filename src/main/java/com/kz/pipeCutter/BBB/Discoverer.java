package com.kz.pipeCutter.BBB;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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

import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

public class Discoverer {
	private static ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
	private static Discoverer instance;
	private ArrayList<JmDNS> jMdnsS = new ArrayList<JmDNS>();
	private static String bonjourServiceType = "_machinekit._tcp.local.";

	ServiceListener bonjourServiceListener;

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

		Settings.instance.log("Initializing discoverer...");

		Enumeration<NetworkInterface> ifc;
		try {
			ifc = NetworkInterface.getNetworkInterfaces();
			bonjourServiceListener = new ServiceListener() {

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
					if (m.find() && MachinekitSettings.instance != null) {
							MachinekitSettings.instance.machinekitServices.removeService(arg0.getInfo());
					}
				}

				@Override
				public void serviceAdded(ServiceEvent arg0) {
					System.out.println("+ " + arg0.getInfo().getName() + " (" + arg0.getInfo().getServer()+ " :" + arg0.getInfo().getPort() + ")");
					services.add(arg0.getInfo());
					Pattern p = Pattern.compile("(.*)service(.*)");
					Matcher m = p.matcher(arg0.getInfo().getName());
					if (m.find() && MachinekitSettings.instance != null) {
						MachinekitSettings.instance.machinekitServices.addService(arg0.getInfo());
					}
				}
			};

			while (ifc.hasMoreElements()) {
				NetworkInterface anInterface = ifc.nextElement();
				try {
					if (anInterface.isUp()) {
						Enumeration<InetAddress> addr = anInterface.getInetAddresses();
						while (addr.hasMoreElements()) {
							InetAddress address = addr.nextElement();
							// work only on inetv4 adresses
							if (address instanceof Inet4Address && !address.equals(InetAddress.getLoopbackAddress())) {
								Settings.instance.log("Adding bonjour listener on local IP: " + address.toString());
								JmDNS jmdns = JmDNSImpl.create(address, bonjourServiceType);
								jMdnsS.add(jmdns);
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
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					if (MachinekitSettings.instance != null)
						MachinekitSettings.instance.machinekitServices.removeAll();

					for (JmDNS jmDNS : Discoverer.this.jMdnsS) {
						System.out.println("Discovering Machinekit services...");
						jmDNS.addServiceListener(bonjourServiceType, bonjourServiceListener);
						ServiceInfo[] infos = jmDNS.list(bonjourServiceType);
						jmDNS.addServiceListener(bonjourServiceType, bonjourServiceListener);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		};
		sw.execute();
	}

	public ArrayList<ServiceInfo> getDiscoveredServices() {
		return this.services;
	}
}
