package com.kz.pipeCutter.BBB;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshUtils {

	public static void main(String[] args) {
		try {
			String hostName = "beaglebone.local";
			String userName = "machinekit";
			String password = "machinekit";
			String localPath = "/mnt/Extra10Gb/git/PipeCutter/";
			String remotePath = "/home/machinekit/machinekit/nc_files/";
			String fileName = "prog.gcode";
			String userAndPass = userName + ":" + password;
			
			
			
			sshCopy("file://" + localPath + fileName, "ssh://" + userAndPass + "@"
					+ hostName + remotePath + fileName);
			sshCopy(
					"ssh://" + userAndPass + "@" + hostName + "/var/log/linuxcnc.log",
					"file:///home/kz/temp/linuxcnc.log");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static final Logger log = Logger.getLogger(SshUtils.class);

	/**
	 * Beware, session must be disconnected!
	 * 
	 * @param uri
	 * @param props
	 * @return
	 * @throws JSchException
	 */
	private static Session newSession(URI uri, Map<String, String> props)
			throws JSchException {
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(getUser(uri), uri.getHost(),
					getPort(uri));
			session.setPassword(getPass(uri));
			Properties config = new Properties();
			config.putAll(props);
			session.setConfig(config);
			session.connect();
			return session;
		} catch (JSchException e) {
			throw new JSchException("Can not create ssh session " + uri, e);
		}
	}

	/**
	 * @param uri
	 * @return
	 * @throws JSchException
	 */
	private static Session newSession(URI uri) throws JSchException {
		Map<String, String> props = new HashMap<>();
		props.put("StrictHostKeyChecking", "no");
		return newSession(uri, props);
	}

	/**
	 * @param session
	 * @param type
	 * @return
	 * @throws JSchException
	 */
	@SuppressWarnings("unchecked")
	private static <C extends Channel> C newChannel(Session session, String type)
			throws JSchException {
		try {
			Channel channel = session.openChannel(type);
			channel.connect();
			return (C) channel;
		} catch (JSchException e) {
			throw new JSchException("Can not create " + type + " channel for "
					+ session.getHost(), e);
		}
	}

	/**
	 * <pre>
	 * sshCopy(
	 * 		&quot;file:/C:/UBS/work/downloads/error.log&quot;,
	 * 		&quot;ssh://aceadmin:ace01admin@ubs00000df2.ashcloud.ubsdev.net/home/aceadmin/temp/log&quot;);
	 * sshCopy("ssh://aceadmin:ace01admin@ubs00000df2.ashcloud.ubsdev.net/home/aceadmin/temp/log/error.log", "file:/C:/UBS/work/downloads");
	 * 
	 * <pre>
	 * @param fromUri file
	 * @param toUri directory
	 * @throws Exception
	 */
	public static void sshCopy(String fromUri, String toUri) throws Exception {
		try {
			URI from = new URI(fromUri);
			URI to = new URI(toUri);

			if ("ssh".equals(to.getScheme()) && !"ssh".equals(from.getScheme())) {
				upload(from, to);
			} else if ("ssh".equals(from.getScheme())
					&& !"ssh".equals(to.getScheme())) {
				download(from, to);
			} else {
				throw new IllegalArgumentException(fromUri + " --> " + toUri);
			}
		} catch (URISyntaxException e) {
			throw new Exception(e);
		}
	}

	/**
	 * @param from
	 *          file
	 * @param to
	 *          directory
	 * @throws Exception
	 */
	private static void upload(URI from, URI to) throws Exception {
		Session session = newSession(to);
		File fromFile = new File(from);
		try (FileInputStream fis = new FileInputStream(fromFile)) {
			log.info(from + " --> " + to);
			ChannelSftp channelSftp = newChannel(session, "sftp");
			channelSftp.cd(getFolderFromSsh(to));
			channelSftp.put(fis, getNameFromSsh(to));
		} catch (Exception e) {
			throw new Exception("Can not upload file", e);
		} finally {
			session.disconnect();
		}
	}

	private static String getNameFromSsh(URI to) {
		int last = to.getPath().lastIndexOf("/");
		return to.getPath().substring(last+1, to.getPath().length());
	}

	private static String getFolderFromSsh(URI to) {
		int last = to.getPath().lastIndexOf("/");
		return to.getPath().substring(0, last);
	}

	/**
	 * @param from
	 *          file
	 * @param to
	 *          directory
	 * @throws Exception
	 */
	private static void download(URI from, URI to) throws Exception {
		Session session = newSession(from);
		File out = new File(new File(to).getPath());
		try (OutputStream os = new FileOutputStream(out);
				BufferedOutputStream bos = new BufferedOutputStream(os)) {
			log.info(from + " --> " + to);
			ChannelSftp ChannelSftp = newChannel(session, "sftp");
			ChannelSftp.cd(getFolderFromSsh(from));
			ChannelSftp.get(getNameFromSsh(from), bos);
		} catch (Exception e) {
			throw new Exception("Can not upload file", e);
		} finally {
			session.disconnect();
		}
	}

	private static int getPort(URI uri) {
		return uri.getPort() < 0 ? 22 : uri.getPort();
	}

	private static String getUser(URI uri) {
		return uri.getUserInfo().split(":")[0];
	}

	private static String getPass(URI uri) {
		return uri.getUserInfo().split(":")[1];
	}

	private SshUtils() {
	}
}