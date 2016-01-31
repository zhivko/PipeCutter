package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;

@SuppressWarnings("serial")
public class Positioner extends JPanel {
	WebSocketContainer wsContainer;
	MyWebsocketClient myWebsocketClient;
	public Session wsSession;
	boolean m;
	long x;
	long y;
	long z;
	long e;

	boolean isConnecting = false;
	public boolean isConnected = false;

	int prevSliderVerValue;
	int prevSliderHorValue;

	int id;

	public Positioner() {
		this(0);
	}

	SavableText positionerStep;
	SavableText positionerUrl;
	SavableCheckBox motorEnableCheckBox;

	JButton btnUp, btnDown, btnLeft, btnRight;

	public Positioner(int id) {
		wsContainer = ContainerProvider.getWebSocketContainer();
		wsContainer.getDefaultAsyncSendTimeout();
		this.id = id;
		this.setPreferredSize(new Dimension(184, 214));
		setLayout(null);

		JSlider sliderVer = new JSlider();
		sliderVer.setBounds(0, 0, 31, 97);
		sliderVer.setSnapToTicks(true);
		sliderVer.setPaintTicks(true);
		sliderVer.setOrientation(SwingConstants.VERTICAL);
		add(sliderVer);
		sliderVer.setMinimum(0);
		sliderVer.setMaximum(100);
		sliderVer.setValue(50);
		prevSliderVerValue = sliderVer.getValue();
		sliderVer.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Ver slider value: " + sliderVer.getValue());
				if (isConnected && !sliderVer.getValueIsAdjusting()) {
					int difference = sliderVer.getValue() - prevSliderHorValue;
					int step = Integer.valueOf(positionerStep.getParValue());
					int dz = (int) (difference * step);
					int de = (int) (-1 * difference * step);

					String signZ = ((dz >= 0) ? "+" : "-");
					String signE = ((de >= 0) ? "+" : "-");
					String commToSend = "Z" + signZ + Math.abs(dz) + " E" + signE + Math.abs(de);
					socketSend(commToSend);
					prevSliderHorValue = sliderVer.getValue();
				}
			}
		});

		btnUp = new JButton();
		try {
			ShrinkIcon si = new ShrinkIcon(
					new File("C:\\Users\\klemen\\git\\PipeCutter\\src\\main\\resources\\ArrowUp.png").toURI().toURL());
			btnUp.setIcon(si);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		btnUp.setBounds(75, 0, 54, 33);
		btnUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Z+" + positionerStep.getParValue() + " E+" + positionerStep.getParValue();
				socketSend(message);
			}
		});
		add(btnUp);

		btnLeft = new JButton();
		try {
			ShrinkIcon si = new ShrinkIcon(
					new File("C:\\Users\\klemen\\git\\PipeCutter\\src\\main\\resources\\ArrowLeft.png").toURI().toURL());
			btnLeft.setIcon(si);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		btnLeft.setBounds(30, 32, 48, 31);
		btnLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "X-" + positionerStep.getParValue() + " Y-" + positionerStep.getParValue();
				socketSend(message);
			}
		});
		add(btnLeft);

		btnDown = new JButton();
		try {
			ShrinkIcon si = new ShrinkIcon(
					new File("C:\\Users\\klemen\\git\\PipeCutter\\src\\main\\resources\\ArrowDown.png").toURI().toURL());
			btnDown.setIcon(si);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		btnDown.setBounds(75, 61, 54, 31);
		btnDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Z-" + positionerStep.getParValue() + " E-" + positionerStep.getParValue();
				socketSend(message);
			}
		});

		btnRight = new JButton();
		try {
			ShrinkIcon si = new ShrinkIcon(
					new File("C:\\Users\\klemen\\git\\PipeCutter\\src\\main\\resources\\ArrowRight.png").toURI().toURL());
			btnRight.setIcon(si);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		btnRight.setBounds(127, 32, 48, 31);
		btnRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "X+" + positionerStep.getParValue() + " Y+" + positionerStep.getParValue();
				socketSend(message);
			}
		});
		add(btnRight);
		add(btnDown);

		JSlider sliderHor = new JSlider();
		sliderHor.setBounds(27, 96, 155, 31);
		sliderHor.setPaintLabels(true);
		sliderHor.setPaintTicks(true);
		add(sliderHor);
		sliderHor.setMinimum(0);
		sliderHor.setMaximum(100);
		sliderHor.setValue(50);
		prevSliderHorValue = sliderHor.getValue();

		sliderHor.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Hor slider value: " + sliderHor.getValue());
				if (isConnected && !sliderHor.getValueIsAdjusting()) {
					int difference = sliderHor.getValue() - prevSliderHorValue;
					int step = Integer.valueOf(positionerStep.getParValue());
					int dx = (int) (difference * step);
					int dy = (int) (-1 * difference * step);

					String signX = ((dx >= 0) ? "+" : "-");
					String signY = ((dy >= 0) ? "+" : "-");
					String commToSend = "X" + signX + Math.abs(dx) + " Y" + signY + Math.abs(dy);
					socketSend(commToSend);
					prevSliderHorValue = sliderHor.getValue();
				}
			}
		});

		positionerUrl = new SavableText();
		positionerUrl.setBounds(0, 126, 177, 22);
		positionerUrl.setLabelTxt("Url:");
		positionerUrl.setParId("rotator" + id + "_positioner_url");
		positionerUrl.jValue.setColumns(12);
		add(positionerUrl);

		positionerUrl.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 10) {
					makeWebsocketConnection();
				} else {
					System.out.println(e.getKeyCode());
				}

			}
		});

		positionerStep = new SavableText();
		positionerStep.setBounds(0, 169, 176, 22);
		positionerStep.setLabelTxt("Positioner step:");
		positionerStep.setParId("rotator" + id + "_positioner_step");
		positionerStep.jValue.setColumns(5);
		add(positionerStep);

		motorEnableCheckBox = new SavableCheckBox() {
			@Override
			public void valueChangedFromUI() {
				String messageToSend;
				if (getParValue().equals("1"))
					messageToSend = "enable";
				else
					messageToSend = "disable";
				socketSend(messageToSend);
			}
		};
		motorEnableCheckBox.setBounds(0, 146, 177, 22);
		motorEnableCheckBox.setLabelTxt("Motors enable");
		motorEnableCheckBox.setParId("rotator" + id + "_positioner_motors_enable");
		add(motorEnableCheckBox);

		JButton stopBtn = new JButton("Stop");
		stopBtn.setBounds(0, 191, 78, 20);
		stopBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				socketSend("stop");
			}
		});
		add(stopBtn);
		
		initPositioners();
	}

	public void makeWebsocketConnection() {
		URI uri=null;
		try {
			if (wsSession != null && wsSession.isOpen())
				wsSession.close();

			ClientManager cm = ClientManager.createClient();
			uri = new URI(positionerUrl.getParValue());
			myWebsocketClient = new MyWebsocketClient(this);
			if (Settings.instance != null)
				Settings.instance.log("Connecting to: " + uri.toString() + "\n");
			wsSession = cm.asyncConnectToServer(myWebsocketClient, uri).get(2000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			if (Settings.instance != null)
				Settings.instance.log("\t" + uri.toString() + " " + e.toString() + "\n");
		}

	}

	public void socketSend(String message) {
		if (isConnected)
			try {
				wsSession.getBasicRemote().sendText(message);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

	}

	public void initPositioners() {
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		Runnable connectPositioner = new Runnable() {
			@Override
			public void run() {
				if (!isConnecting && !Positioner.this.isConnected && Positioner.this.positionerUrl.getParValue() != null) {
					isConnecting = true;
					Positioner.this.makeWebsocketConnection();
					scheduler.shutdown();
					isConnecting = false;
				}
			}
		};
		scheduler.scheduleAtFixedRate(connectPositioner, 1000, 2000, TimeUnit.MILLISECONDS);
	}

	public void initToolTips()
	{
		btnUp.setToolTipText("E " + String.valueOf(this.e));
		btnDown.setToolTipText("Z " + String.valueOf(this.z));

		btnLeft.setToolTipText("X " + String.valueOf(this.x));
		btnRight.setToolTipText("Y " + String.valueOf(this.y));
	}
}
