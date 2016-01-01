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
//		/192.168.7.1
//		Added: [ServiceInfoImpl@11393876 name: 'Status service on beaglebone._local pid 5126._machinekit._tcp.local.' address: '(null):64306' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		Added: [ServiceInfoImpl@32678821 name: 'Command service on beaglebone._local pid 5126._machinekit._tcp.local.' address: '(null):64907' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		Added: [ServiceInfoImpl@11714816 name: 'Preview service on beaglebone._local pid 5126._machinekit._tcp.local.' address: '(null):49153' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		Added: [ServiceInfoImpl@28660940 name: 'Log service on beaglebone._local pid 4714._machinekit._tcp.local.' address: '(null):49152' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		Added: [ServiceInfoImpl@21779733 name: 'File service on beaglebone._local pid 5126._machinekit._tcp.local.' address: '(null):58192' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		Added: [ServiceInfoImpl@28650770 name: 'Error service on beaglebone._local pid 5126._machinekit._tcp.local.' address: '(null):59611' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		Added: [ServiceInfoImpl@22366245 name: 'Previewstatus service on beaglebone._local pid 5126._machinekit._tcp.local.' address: '(null):49154' status: 'NO DNS state: probing 1 task: null', has NO data empty]
//		/192.168.1.106
//		/127.0.0.1		
		ServiceInfo command = discoverer.getCommandService();
		String commandUrl = command.getProtocol() + "://" + command.getServer() + ":" + command.getPort() + "/";
		System.out.println("command url: " + commandUrl);
		// tcp://beaglebone.local.:64907/
		Context con = ZMQ.context(1);
		Socket req = con.socket(ZMQ.REQ);
		req.connect(commandUrl);
		req.send("test");
		String result = req.recvStr();
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

}
