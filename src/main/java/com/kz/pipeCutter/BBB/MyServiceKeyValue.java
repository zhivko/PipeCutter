package com.kz.pipeCutter.BBB;

import javax.jmdns.ServiceInfo;

public class MyServiceKeyValue {
	private String key;
	private String value;
	private ServiceInfo serviceInfo;

	public MyServiceKeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return this.key + " - " + this.value;
	}

}
