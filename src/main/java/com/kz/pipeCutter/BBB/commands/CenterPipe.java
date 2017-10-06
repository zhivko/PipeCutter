package com.kz.pipeCutter.BBB.commands;

import org.apache.log4j.Logger;

import com.kz.pipeCutter.ui.MyLaserWebsocketClient;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.RotatorSettings;

public class CenterPipe implements Runnable {

	@Override
	public void run() {
		Thread.currentThread().setName("Centering THREAD");
		if (!MyLaserWebsocketClient.getInstance().isOn) {
			Settings.getInstance().log("Connect capacitive sensor!");
			return;
		}

		String speed = " F4000";

		Float dimX = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_x"));
		Float dimZ = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_z"));

		float diagonal = (float) Math.sqrt(Math.pow(dimX / 2, 2) + Math.pow(dimZ / 2, 2));
		float highZPos = (diagonal + 20);

		float angle = 0;
		executeMdiAndWaitFor("G00 X0 Z" + highZPos, "position_z", highZPos);
		executeMdiAndWaitFor("G01 A" + angle + " B" + angle + speed, "position_a", angle);
		moveProbeTo1mmOffset();
		float z = Float.valueOf(Settings.getInstance().getSetting("position_z"))
				+ getCapSenseHeight();

		angle = 90;
		executeMdiAndWaitFor("G00 X0 Z" + highZPos, "position_z", highZPos);
		executeMdiAndWaitFor("G01 A" + angle + " B" + angle + speed, "position_a", angle);
		moveProbeTo1mmOffset();
		float y = Float.valueOf(Settings.getInstance().getSetting("position_z"))
				+ getCapSenseHeight();

		angle = 180;
		executeMdiAndWaitFor("G00 X0 Z" + highZPos, "position_z", highZPos);
		executeMdiAndWaitFor("G01 A" + angle + " B" + angle + speed, "position_a", angle);
		moveProbeTo1mmOffset();
		float e = Float.valueOf(Settings.getInstance().getSetting("position_z"))
				+ getCapSenseHeight();

		angle = 270;
		executeMdiAndWaitFor("G00 X0 Z" + highZPos, "position_z", highZPos);
		executeMdiAndWaitFor("G01 A" + angle + " B" + angle + speed, "position_a", angle);
		moveProbeTo1mmOffset();
		float x = Float.valueOf(Settings.getInstance().getSetting("position_z"))
				+ getCapSenseHeight();

		angle = 0;
		executeMdiAndWaitFor("G00 X0 Z" + highZPos, "position_z", highZPos);
		executeMdiAndWaitFor("G01 A" + angle + " B" + angle + speed, "position_a", angle);

		// vertical
		double middleVert = (z - e) / 2.0;
		int stepsVert = (int) Math.round(middleVert * 100.0);

		String signZ = ((stepsVert >= 0) ? "+" : "-");
		String signE = ((stepsVert >= 0) ? "-" : "+");
		String commToSendVer = "Z" + signE + Math.abs(stepsVert) + " E" + signZ + Math.abs(stepsVert);

		double middleHort = (x - y) / 2.000;
		int stepsHort = (int) Math.round(middleHort * 100.0);

		String signX = ((stepsHort >= 0) ? "-" : "+");
		String signY = ((stepsHort >= 0) ? "+" : "-");
		String commToSendHor = "X" + signX + Math.abs(stepsHort) + " Y" + signY + Math.abs(stepsHort);

		Settings.getInstance().log(String.format("x:%4.2f y:%4.2f z:%4.2f e:%4.2f", x, y, z, e));
		Logger.getLogger(this.getClass()).info(commToSendVer + " " + commToSendHor);
		String centerComm = commToSendVer + " " + commToSendHor;
		Settings.getInstance().log(centerComm);
		RotatorSettings.getInstance().pos2.socketSend(centerComm);
	}

	private float getCapSenseHeight() {
		// TODO Auto-generated method stub
		long ret = 0;
		float sum = 0;

		int samples = 100;

		for (int i = 0; i < samples; i++) {
			
			String val = Settings.getInstance().getSetting("mymotion.laserHeight1mm");
			while(val.equals(""))
				val = Settings.getInstance().getSetting("mymotion.laserHeight1mm");
			
			float distanceMM = Float.valueOf(val);
			sum = sum + distanceMM;
			try {
				Thread.currentThread().sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return (sum / samples);
	}

	public static void moveProbeTo1mmOffset() {

		float laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		while (laserOffset >= 10) {
			float z = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			float newZ = z - 5;
			executeMdiAndWaitFor("G00 X0 Z" + newZ, "position_z", newZ);
			laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		}

		while (laserOffset >= 3.0f) {
			float z = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			float newZ = z - 1;
			executeMdiAndWaitFor("G00 X0 Z" + newZ, "position_z", newZ);
			laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		}
		
		while (laserOffset >= 1.5f) {
			float z = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			float newZ = z - 0.2f;
			executeMdiAndWaitFor("G00 X0 Z" + newZ, "position_z", newZ);
			laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		}		
	}
	
	public static void moveProbeTo5mmOffset() {

		float laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		while (laserOffset >= 10) {
			float z = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			float newZ = z - 4;
			executeMdiAndWaitFor("G00 X0 Z" + newZ, "position_z", newZ);
			laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		}

		while (laserOffset >= 5.0f) {
			float z = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			float newZ = z - 1;
			executeMdiAndWaitFor("G00 X0 Z" + newZ, "position_z", newZ);
			laserOffset = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1mm"));
		}
		
	}	
	

	public static void executeMdiAndWaitFor(String mdiCommand, String setting, float value) {
		long sleepMs = 100;
		long timeOfLastSent = System.currentTimeMillis();
		new ExecuteMdi(mdiCommand).start();

		while (true) {
			try {
				Thread.sleep(sleepMs);

				if (System.currentTimeMillis() - timeOfLastSent > 2000) {
					new ExecuteMdi(mdiCommand).start();
					timeOfLastSent = System.currentTimeMillis();
				}

				String val = Settings.getInstance().getSetting(setting);

				float tempValue = Float.valueOf(val);

				if (Math.round(tempValue * 10.0) / 10.0 == Math.round(value * 10.0) / 10.0)
					break;
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
	}
}
