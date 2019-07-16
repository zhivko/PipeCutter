package com.kz.pipeCutter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.SortedProperties;

public class MyContinuousEdge extends MyEdge {

	private float kerfOffset;

	public enum EdgeType {
		START, ONPIPE, END
	}

	EdgeType edgeType;

	public MyContinuousEdge(Integer edgeNo, Integer surfaceNo) {
		super(edgeNo, surfaceNo);
		this.edgeType = EdgeType.ONPIPE;
	}

	public MyContinuousEdge() {
		super();
		this.edgeType = EdgeType.ONPIPE;
	}

	public String toString() {
		String type = "";
		if (edgeType == EdgeType.START)
			type = "START";
		else if (edgeType == EdgeType.START)
			type = "ONPIPE";
		else if (edgeType == EdgeType.END)
			type = "END";
		return (this.edgeNo + " " + type);
	}

	public void setKerfOffset(Float valueOf) {
		// TODO Auto-generated method stub
		this.kerfOffset = valueOf;
		FileOutputStream out;
		try {
			FileInputStream in = new FileInputStream(Settings.iniContinuousEdgeProperties);
			SortedProperties props = new SortedProperties();
			props.load(in);
			in.close();

			out = new FileOutputStream(Settings.iniContinuousEdgeProperties);
			props.setProperty(this.edgeNo + ".kerfOffset", String.valueOf(valueOf));

			props.store(out, null);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public Object getKerfOffset() {
		// TODO Auto-generated method stub
		FileInputStream in;
		try {
			in = new FileInputStream(Settings.iniContinuousEdgeProperties);
			SortedProperties props = new SortedProperties();
			props.load(in);
			
			String val = props.getProperty(this.edgeNo + ".kerfOffset");
			if(val==null)
				val = "0.0";
			this.kerfOffset = Float.valueOf(val);
			in.close();					
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.kerfOffset;
	}
}
