package com.kz.pipeCutter.BBB.commands;

import java.util.concurrent.Callable;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;

public abstract class SSH_Command {
	private static JSch jsch;
	protected static ChannelExec channelExec = null;
	protected static Session session;

	public abstract void runSshCmd() throws Exception;	
	
	public void start() {
		// TODO Auto-generated method stub
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					SSH_Login();
					runSshCmd();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		t.run();
	}



	public static void SSH_Login() {
		try {
			if (jsch == null || session == null || !session.isConnected()) {
				jsch = new JSch();
				String host = Settings.getInstance().getSetting("machinekit_host");
				String ip = Settings.getInstance().getSetting("machinekit_ip");
				String user = Settings.getInstance().getSetting("machinekit_user");
				String pass = Settings.getInstance().getSetting("machinekit_password");

				session = jsch.getSession(user, host, 22);
				session.setPassword(pass);

				session.setConfig("StrictHostKeyChecking", "no");

				session.setServerAliveInterval(2000);
				session.setServerAliveCountMax(Integer.MAX_VALUE);

				session.setOutputStream(System.out);
				session.connect(3000); // making a connection with timeout.
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}



}
