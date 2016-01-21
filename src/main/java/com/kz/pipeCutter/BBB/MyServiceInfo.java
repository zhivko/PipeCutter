package com.kz.pipeCutter.BBB;

import javax.jmdns.ServiceInfo;

public class MyServiceInfo {
	public String name;
	public String url;
	public ServiceInfo si;

	public MyServiceInfo(ServiceInfo si) {
		this.name = si.getName();
		this.url = "tcp://" + si.getServer() + ":" + si.getPort() + "/";
		this.si = si;
	}

	public String toString() {
		return this.name + " " + this.url;
	}

}
