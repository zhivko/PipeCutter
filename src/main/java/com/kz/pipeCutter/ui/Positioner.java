package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.kz.pipeCutter.ui.tab.RotatorSettings;

public class Positioner extends JPanel {
	WebSocketClient ws = null;
	private boolean m;
	private Double x;
	private Double y;
	private Double z;
	private Double e;
	double step = 0;

	public boolean isConnected = false;

	int prevSliderVerValue;
	int prevSliderHorValue;

	int id;

	public Positioner() {
		this(0);
	}

	public Positioner(int id) {
		this.id = id;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 60, 0, 51, 0, 46, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 32, 19, 5 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0 };
		setLayout(gridBagLayout);

		JSlider sliderVer = new JSlider();
		sliderVer.setSnapToTicks(true);
		sliderVer.setPaintTicks(true);
		sliderVer.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_slider_1 = new GridBagConstraints();
		gbc_slider_1.fill = GridBagConstraints.BOTH;
		gbc_slider_1.gridheight = 4;
		gbc_slider_1.insets = new Insets(0, 0, 5, 5);
		gbc_slider_1.gridx = 0;
		gbc_slider_1.gridy = 0;
		add(sliderVer, gbc_slider_1);
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

					int dz = (int) (difference * step);
					int de = (int) (-1 * difference * step);

					String signZ = ((dz >= 0) ? "+" : "-");
					String signE = ((de >= 0) ? "+" : "-");
					String commToSend = "Z" + signZ + Math.abs(dz) + " E" + signE
							+ Math.abs(de);
					ws.send(commToSend);
					prevSliderHorValue = sliderVer.getValue();
				}
			}
		});

		JButton btnGor = new JButton("⇑");
		btnGor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_btnGor = new GridBagConstraints();
		gbc_btnGor.gridwidth = 2;
		gbc_btnGor.insets = new Insets(0, 0, 5, 5);
		gbc_btnGor.fill = GridBagConstraints.BOTH;
		gbc_btnGor.gridx = 2;
		gbc_btnGor.gridy = 0;
		add(btnGor, gbc_btnGor);

		JButton button_2 = new JButton("⇐");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_button_2 = new GridBagConstraints();
		gbc_button_2.insets = new Insets(0, 0, 5, 5);
		gbc_button_2.gridx = 1;
		gbc_button_2.gridy = 1;
		add(button_2, gbc_button_2);

		JButton button = new JButton("⇓");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});

		JButton button_1 = new JButton("⇒");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_1.insets = new Insets(0, 0, 5, 0);
		gbc_button_1.gridx = 4;
		gbc_button_1.gridy = 1;
		add(button_1, gbc_button_1);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_button.gridwidth = 2;
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 2;
		gbc_button.gridy = 2;
		add(button, gbc_button);

		JSlider sliderHor = new JSlider();
		sliderHor.setPaintLabels(true);
		sliderHor.setPaintTicks(true);
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.insets = new Insets(0, 0, 5, 0);
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.anchor = GridBagConstraints.SOUTH;
		gbc_slider.gridwidth = 4;
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 3;
		add(sliderHor, gbc_slider);
		sliderHor.setMinimum(0);
		sliderHor.setMaximum(100);
		sliderHor.setValue(50);
		prevSliderHorValue = sliderHor.getValue();

		this.setPreferredSize(new Dimension(247, 206));
		sliderHor.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				System.out.println("Hor slider value: " + sliderHor.getValue());
				if (isConnected && !sliderHor.getValueIsAdjusting()) {
					int difference = sliderHor.getValue() - prevSliderHorValue;

					int dx = (int) (difference * step);
					int dy = (int) (-1 * difference * step);

					String signX = ((dx >= 0) ? "+" : "-");
					String signY = ((dy >= 0) ? "+" : "-");
					String commToSend = "X" + signX + Math.abs(dx) + " Y" + signY
							+ Math.abs(dy);
					ws.send(commToSend);
					prevSliderHorValue = sliderHor.getValue();
				}
			}
		});

		SavableText positionerStep = new SavableText();
		positionerStep.setLabelTxt("Positioner step:");
		positionerStep.setParId("rotator" + id + "_positioner_step");
		positionerStep.jValue.setColumns(5);
		GridBagConstraints gbc_positioner1Step = new GridBagConstraints();
		gbc_positioner1Step.insets = new Insets(0, 0, 5, 0);
		gbc_positioner1Step.fill = GridBagConstraints.HORIZONTAL;
		gbc_positioner1Step.anchor = GridBagConstraints.NORTH;
		gbc_positioner1Step.gridwidth = 5;
		gbc_positioner1Step.gridx = 0;
		gbc_positioner1Step.gridy = 4;
		add(positionerStep, gbc_positioner1Step);
		positionerStep.jValue.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						updateHappened();
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						updateHappened();
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						updateHappened();
					}

					public void updateHappened() {
						setStep(Double.valueOf(positionerStep.jValue.getText()));
					}
				});

		SavableText positionerUrl = new SavableText();
		positionerUrl.setLabelTxt("Url:");
		positionerUrl.setParId("rotator" + id + "_positioner_url");
		positionerUrl.jValue.setColumns(12);
		GridBagConstraints gbc_positionerUrl = new GridBagConstraints();
		gbc_positionerUrl.fill = GridBagConstraints.HORIZONTAL;
		gbc_positionerUrl.anchor = GridBagConstraints.SOUTH;
		gbc_positionerUrl.gridwidth = 5;
		gbc_positionerUrl.gridx = 0;
		gbc_positionerUrl.gridy = 5;
		add(positionerUrl, gbc_positionerUrl);
		positionerUrl.jValue.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void removeUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						updateHappened();
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						updateHappened();
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						updateHappened();
					}

					public void updateHappened() {
						try {
							setUri(positionerUrl.jValue.getText());
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
				});

	}

	public void setUri(String uri) throws URISyntaxException {
		ws = new WebSocketClient(new URI(uri)) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				// TODO Auto-generated method stub
				isConnected = true;
			}

			@Override
			public void onMessage(String message) {
				// TODO Auto-generated method stub
				String res[] = message.split(" ");
				if (message.substring(0, 1).equals("X") && res.length == 5) {
					Positioner.this.x = Double.valueOf(res[0]);
					Positioner.this.y = Double.valueOf(res[1]);
					Positioner.this.z = Double.valueOf(res[2]);
					Positioner.this.e = Double.valueOf(res[3]);
					if (res[4].endsWith("1"))
						Positioner.this.m = true;
					else
						Positioner.this.m = false;
				}
			}

			@Override
			public void onError(Exception ex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				// TODO Auto-generated method stub
				isConnected = false;
			}
		};

		SavableText positioner1SocketUrl = new SavableText();
		positioner1SocketUrl.setLabelTxt("Positioner url:");
		positioner1SocketUrl.setParId("positioner1_socket_url");
		this.add(positioner1SocketUrl);
	}

	public void setStep(Double step) {
		this.step = step;
	}
}
