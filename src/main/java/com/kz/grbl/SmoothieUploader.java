package com.kz.grbl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.net.telnet.TelnetClient;

public class SmoothieUploader extends Thread {

	TelnetClient tc = null;
	String returnedString = "";
	private File file;
	public static String smoothieIP = "192.168.1.15"; 
	public static int smoothieRemotePort = 23; 

	public SmoothieUploader(File file)
	{
		this.file = file;
	}
	
	public static void main(String[] args) {

		// PySystemState state = new PySystemState();
		// state.argv.append (new PyString ("prog.gcode"));
		// state.argv.append (new PyString ("192.168.1.4"));
		// PythonInterpreter interp = new PythonInterpreter(null, state);
		// interp.execfile("smoothie-upload.py");
		SmoothieUploader uploader = new SmoothieUploader(new File("prog.gcode"));
		uploader.sendFile();
	}

	public void sendFile() {

		try {

			tc = new TelnetClient();
			tc.setConnectTimeout(2000);
			tc.connect(smoothieIP, 115);
			read();

			String fileName = "prog.gcode";
			String command = "\nSTOR OLD /sd/" + fileName + "\n";
			write(command);
			read();
			write("SIZE " + this.file.length() + "\n");
			read();

			InputStream    fis;
			BufferedReader br;
			String         line;
			fis = new FileInputStream(this.file);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				tc.getOutputStream().write((line + "\n").getBytes());
				tc.getOutputStream().flush();				
			}

			// Done with the file
			br.close();
			br = null;
			fis = null;			
			
			read();
			
			write("DONE\n");
			read();
			tc.disconnect();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
	}

	private void write(String command) throws IOException {
		System.out.println(command);
		tc.getOutputStream().write(command.getBytes("US-ASCII"));
		tc.getOutputStream().flush();
	}

	private void read() throws IOException {
		byte[] buff = new byte[1024];
		int ret_read = 0;

		ret_read = tc.getInputStream().read(buff);
		if (ret_read > 0) {
			this.returnedString = new String(buff, 0, ret_read);
			System.out.println("\t" + returnedString);
		}
	}
	
	
	@Override
	public void run()
	{
		sendFile();
	}
	
}