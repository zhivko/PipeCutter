package com.kz.pipeCutter.BBB;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MyOutputStreamReader extends OutputStream {
	private ArrayList<String> lines = null;
	private String line = null;

	public MyOutputStreamReader() {
		super();
		lines = new ArrayList<String>();
		line = "";
	}

	@Override
	public void write(int b) throws IOException {
		char c = (char) b;
		if (c == '\n') {
			System.out.println(line);
			lines.add(line);
			line = "";
		} else {
			line += c;
		}
	}

	public ArrayList<String> getLines() {
		return this.lines;
	}

}
