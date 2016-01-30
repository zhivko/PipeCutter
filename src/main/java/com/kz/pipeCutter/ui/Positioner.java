package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URI;
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
	public Session wsSession;
	boolean m;
	Double x;
	Double y;
	Double z;
	Double e;

	public boolean isConnected = false;

	int prevSliderVerValue;
	int prevSliderHorValue;

	int id;

	public Positioner() {
		this(0);
	}

	SavableText positionerStep;

	public Positioner(int id) {
		wsContainer = ContainerProvider.getWebSocketContainer();
		wsContainer.getDefaultAsyncSendTimeout();
		this.id = id;
		this.setPreferredSize(new Dimension(184, 219));
		setLayout(null);

		JSlider sliderVer = new JSlider();
		sliderVer.setBounds(0, 0, 31, 97);
		sliderVer.setSnapToTicks(true);
		sliderVer.setPaintTicks(true);
		sliderVer.setOrientation(SwingConstants.VERTICAL);
		add(sliderVer);
		sliderVer.setMinimum(0);
		sliderVer.setMaximum(5000);
		sliderVer.setValue(2500);
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

		JButton btnGor = new JButton("⇑");
		btnGor.setBounds(75, 0, 54, 33);
		btnGor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Z+" + positionerStep.getParValue() + " E+" + positionerStep.getParValue();
				socketSend(message);
			}
		});
		add(btnGor);

		JButton button_2 = new JButton("⇐");
		button_2.setBounds(30, 32, 48, 31);
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "X-" + positionerStep.getParValue() + " Y-" + positionerStep.getParValue();
				socketSend(message);
			}
		});
		add(button_2);

		JButton button = new JButton("⇓");
		button.setBounds(75, 61, 54, 31);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "Z-" + positionerStep.getParValue() + " E-" + positionerStep.getParValue();
				socketSend(message);
			}
		});

		JButton button_1 = new JButton("⇒");
		button_1.setBounds(127, 32, 48, 31);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = "X+" + positionerStep.getParValue() + " Y+" + positionerStep.getParValue();
				socketSend(message);
			}
		});
		add(button_1);
		add(button);

		JSlider sliderHor = new JSlider();
		sliderHor.setBounds(27, 96, 155, 31);
		sliderHor.setPaintLabels(true);
		sliderHor.setPaintTicks(true);
		add(sliderHor);
		sliderHor.setMinimum(0);
		sliderHor.setMaximum(5000);
		sliderHor.setValue(2500);
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

		SavableText positionerUrl = new SavableText() {
			@Override
			public void valueChangedFromUI() {
				// TODO Auto-generated method stub
				System.out.println(this.getParId());
				if (Settings.instance != null)
					Positioner.this.setUri(this.getParValue());
			}
		};
		positionerUrl.setBounds(0, 118, 177, 30);
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
					setUri(positionerUrl.jValue.getText());
				} else {
					System.out.println(e.getKeyCode());
				}

			}
		});

		positionerStep = new SavableText();
		positionerStep.setBounds(0, 178, 176, 30);
		positionerStep.setLabelTxt("Positioner step:");
		positionerStep.setParId("rotator" + id + "_positioner_step");
		positionerStep.jValue.setColumns(5);
		add(positionerStep);

		SavableCheckBox motorEnableCheckBox = new SavableCheckBox() {
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
		motorEnableCheckBox.setBounds(0, 147, 177, 33);
		motorEnableCheckBox.setLabelTxt("Motors enable");
		motorEnableCheckBox.setParId("rotator" + id + "_positioner_motors_enable");
		add(motorEnableCheckBox);
	}

	public void setUri(String uriStr) {

		try {
			URI uri = new URI(uriStr);
			ClientManager cm = ClientManager.createClient();
			MyWebsocketClient myWebsocketClient = new MyWebsocketClient(this);
			if (Settings.instance != null)
				Settings.instance.log("Connecting to: " + uriStr + "\n");
			wsSession = cm.asyncConnectToServer(myWebsocketClient, uri).get(2000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
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

}
