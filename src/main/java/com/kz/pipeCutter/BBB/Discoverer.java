package com.kz.pipeCutter.BBB;

import java.io.IOException;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class Discoverer {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Discoverer discoverer = new Discoverer();
		try {
			Thread.currentThread().sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		//String bonjourServiceType = "_http._tcp.local.";
		String bonjourServiceType = "_machinekit._tcp.local.";
		//String bonjourServiceType = "machinekit";
		
		JmDNS bonjourService;
		try {
			bonjourService = JmDNS.create();
			bonjourService.addServiceListener(bonjourServiceType,
					bonjourServiceListener);
			ServiceInfo[] serviceInfos = bonjourService
					.list(bonjourServiceType);
			for (ServiceInfo info : serviceInfos) {
				System.out.println("## resolve service " + info.getName()
						+ " : " + info.getURL());
			}
			bonjourService.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void discover() {

	}

}
