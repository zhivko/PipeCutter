package com.kz.pipeCutter.BBB;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Discoverer {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Discoverer discoverer = new Discoverer();
		ServiceInfo command = discoverer.getErrorService();
		String commandUrl = "tcp://beaglebone.local:" + command.getPort() + "/";
		System.out.println("command url: " + commandUrl);
		// tcp://beaglebone.local.:64907/
		Context con = ZMQ.context(1);
		//Socket req = con.socket(ZMQ.REQ);
		//req.connect(commandUrl);
		//req.send("test");
		//String result = req.recvStr();
		
		Socket socket = con.socket(ZMQ.SUB);
		socket.connect(commandUrl);
		socket.subscribe("task".getBytes());
		socket.subscribe("motion".getBytes());
		socket.subscribe("io".getBytes());
		socket.subscribe("interp".getBytes());
		socket.subscribe("config".getBytes());
		String content = socket.recvStr();		
		System.out.println(content);
		
	}

	public Discoverer() {
		services = new ArrayList<ServiceInfo>();
		bonjourServiceListener = new ServiceListener() {

			@Override
			public void serviceResolved(ServiceEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void serviceRemoved(ServiceEvent arg0) {
				services.remove(arg0.getInfo());
				System.out.println("Removed: " + arg0.getInfo());
			}

			@Override
			public void serviceAdded(ServiceEvent arg0) {
				services.add(arg0.getInfo());
				System.out.println("Added: " + arg0.getInfo());
			}
		};
		// String bonjourServiceType = "_http._tcp.local.";
		// String bonjourServiceType = "_machinekit._tcp.local.";
		String bonjourServiceType = "_machinekit._tcp.local.";

		Enumeration<NetworkInterface> ifc;
		try {
			ifc = NetworkInterface.getNetworkInterfaces();
			while (ifc.hasMoreElements()) {
				NetworkInterface anInterface = ifc.nextElement();
				try {
					if (anInterface.isUp()) {
						Enumeration<InetAddress> addr = anInterface
								.getInetAddresses();
						while (addr.hasMoreElements()) {
							InetAddress address = addr.nextElement();
							System.out.println(address);
						
							
							JmDNS jmdns = JmDNS.create(address,
									bonjourServiceType);
							ServiceInfo[] infos = jmdns.list(bonjourServiceType);
							jmdns.addServiceListener(bonjourServiceType,
									bonjourServiceListener);

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

	public void discover() {

	}
	
	public ServiceInfo getCommandService()
	{
		ServiceInfo ret=null;
		for (ServiceInfo serviceInfo : services) {
			if(serviceInfo.getName().matches("Command.*"))
			{
				ret = serviceInfo;
				break;
			}
		}
		return ret;
	}

	public ServiceInfo getErrorService()
	{
		ServiceInfo ret=null;
		for (ServiceInfo serviceInfo : services) {
			if(serviceInfo.getName().matches("Error.*"))
			{
				ret = serviceInfo;
				break;
			}
		}
		return ret;
	}
	
	
}
