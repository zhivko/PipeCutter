package com.kz.pipeCutter.BBB.commands;

import org.apache.log4j.Logger;

import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.RotatorSettings;

public class CenterPipe implements Runnable {

	@Override
	public void run() {
		String speed = " F4000";

		int angle = 0;
		new ExecuteMdi("G01 A" + angle + " B" + angle + speed).start();

		int i = 0;
		while (i < 2) {
			try {
				Thread.sleep(1000);
				float angleMK = Float.valueOf(Settings.getInstance().getSetting("position_a"));
				if (angleMK == 0.000f)
					i++;
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		float z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		angle = 90;
		new ExecuteMdi("G01 A" + angle + " B" + angle + speed).start();
		i = 0;
		while (i < 2) {
			try {
				Thread.sleep(1000);
				float angleMK = Float.valueOf(Settings.getInstance().getSetting("position_a"));
				if (angleMK == 90.000f)
					i++;
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		float y = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		angle = 180;
		new ExecuteMdi("G01 A" + angle + " B" + angle + speed).start();
		i = 0;
		while (i < 2) {
			try {
				Thread.sleep(1000);
				float angleMK = Float.valueOf(Settings.getInstance().getSetting("position_a"));
				if (angleMK == 180.000f)
					i++;
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		float e = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		angle = 270;
		new ExecuteMdi("G01 A" + angle + " B" + angle + speed).start();
		i = 0;
		while (i < 2) {
			try {
				Thread.sleep(1000);
				float angleMK = Float.valueOf(Settings.getInstance().getSetting("position_a"));
				if (angleMK == 270.000f)
					i++;
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		float x = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		angle = 0;
		new ExecuteMdi("G01 A" + angle + " B" + angle + speed).start();
		i = 0;
		while (i < 2) {
			try {
				Thread.sleep(1000);
				float angleMK = Float.valueOf(Settings.getInstance().getSetting("position_a"));
				if (angleMK == 0.000f)
					i++;
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

		
		
		// vertical
		double middleVert = (z - e) / 2.0;
		int stepsVert = (int) Math.round(middleVert * 100.0);

		String signZ = ((stepsVert >= 0) ? "-" : "+");
		String signE = ((stepsVert >= 0) ? "+" : "-");
		String commToSendVer = "Z" + signE + Math.abs(stepsVert) + " E" + signZ + Math.abs(stepsVert);
		
		double middleHort = (x - y) / 2.000;
		int stepsHort = (int) Math.round(middleHort * 100.0);

		String signX = ((stepsHort >= 0) ? "+" : "-");
		String signY = ((stepsHort >= 0) ? "-" : "+");
		String commToSendHor = "X" + signX + Math.abs(stepsHort) + " Y" + signY + Math.abs(stepsHort);
		
		Settings.instance.log(String.format("x:%4.2f y:%4.2f z:%4.2f e:%4.2f", x,y,z,e));
		Logger.getLogger(this.getClass()).info(commToSendVer + " " + commToSendHor);
		String centerComm = commToSendVer + " " + commToSendHor;
		Settings.instance.log(centerComm);
		RotatorSettings.instance.pos2.socketSend(centerComm);
	}

}
