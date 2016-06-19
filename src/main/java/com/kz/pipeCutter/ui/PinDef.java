package com.kz.pipeCutter.ui;

import pb.Types.HalPinDirection;
import pb.Types.ValueType;

public class PinDef {
	public HalPinDirection getPinDir() {
		return pinDir;
	}

	public ValueType getPinType() {
		return pinType;
	}

	String pinName;
	HalPinDirection pinDir;
	ValueType pinType;
	
	public PinDef(String name, HalPinDirection dir, ValueType type)
	{
		this.pinName = name;
		this.pinDir = dir;
		this.pinType = type;
	}
	
	public String getPinName()
	{
		return this.pinName;
	}
	
	
	
}
