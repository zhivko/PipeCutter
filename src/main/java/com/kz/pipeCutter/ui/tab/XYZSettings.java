package com.kz.pipeCutter.ui.tab;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.kz.pipeCutter.BBB.commands.CapSenseCalibrate;
import com.kz.pipeCutter.BBB.commands.CenterXOnPipe;
import com.kz.pipeCutter.BBB.commands.CenterXOnPipeProcedure;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.BBB.commands.Jog;
import com.kz.pipeCutter.BBB.commands.MakeXHorizontal;
import com.kz.pipeCutter.BBB.commands.PlasmaTouch;
import com.kz.pipeCutter.ui.MyLaserWebsocketClient;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.PinDef;
import com.kz.pipeCutter.ui.SavableSlider;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

import pb.Types.HalPinDirection;
import pb.Types.ValueType;

@SuppressWarnings("serial")
public class XYZSettings extends JPanel {

	public XYSeries seriesXZ = new XYSeries("XYGraph");
	public JFreeChart chart;

	WebSocketContainer wsContainer;
	MyLaserWebsocketClient myWebsocketClient;
	public Session wsSession;

	private CenterXOnPipe centerXOnPipe;
	private CapSenseCalibrate capSenseCalibrate;

	private static XYZSettings instance = null;

	public static XYZSettings getInstance() {
		return instance;
	}

	public XYZSettings() {
		super();

		Dimension panelPreferedDimension = new Dimension(300, 600);

		this.setPreferredSize(new Dimension(900, 650));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		String moveToText = "-10³,-10²,-10,-1,1,10,10²,10³";

		// ---------- X AXIS ---------------------------

		JPanel panelXAxis = new JPanel();
		panelXAxis.setPreferredSize(panelPreferedDimension);
		this.add(panelXAxis);
		panelXAxis.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel1 = new JLabel("X Axis");
		panelXAxis.add(lblNewLabel1);

		SavableText x_vel = new SavableText();
		x_vel.setParId("myini.maxvel_0");
		x_vel.setLabelTxt("max velocity [mm/s]:");
		x_vel.setPin(new PinDef("myini.maxvel_0", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		x_vel.requiresHalRCompSet = true;
		panelXAxis.add(x_vel);

		SavableText x_acel = new SavableText();
		x_acel.setLabelTxt("max acceleration:");
		panelXAxis.add(x_acel);
		x_acel.setPin(new PinDef("myini.maxacc_0", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		x_acel.requiresHalRCompSet = true;
		x_acel.setParId("myini.maxacc_0");

		SavableText x_stepgvel = new SavableText();
		x_stepgvel.setPin(new PinDef("myini.stepgen_maxvel_0", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		x_stepgvel.requiresHalRCompSet = true;
		x_stepgvel.setLabelTxt("max stepgen velocity [step/sec]:");
		x_stepgvel.setParId("myini.stepgen_maxvel_0");
		panelXAxis.add(x_stepgvel);

		SavableText x_stepgacc = new SavableText();
		x_stepgacc.setPin(new PinDef("myini.stepgen_maxacc_0", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		x_stepgacc.requiresHalRCompSet = true;
		x_stepgacc.setLabelTxt("max stepgen acc [step/sec^2]:");
		x_stepgacc.setParId("myini.stepgen_maxacc_0");
		panelXAxis.add(x_stepgacc);

		SavableSlider sliderX = new SavableSlider();
		sliderX.setValues(moveToText);
		sliderX.setLabelTxt("Move for:");
		sliderX.setParId("x_step");
		panelXAxis.add(sliderX);

		SavableText x_jog_speed = new SavableText();
		x_jog_speed.setLabelTxt("jog velocity [mm/min]:");
		x_jog_speed.setParId("x_jog_vel");
		panelXAxis.add(x_jog_speed);

		JButton jog1 = new JButton("JOG");
		jog1.setPreferredSize(new Dimension(100, 50));
		panelXAxis.add(jog1);
		jog1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance().getSetting("x_jog_vel"));
				Double distance = Double.valueOf(Settings.getInstance().getSetting("x_step"));
				new Jog(0, velocity / 60, distance).start();
			}
		});
		SavableText positionX = new SavableText();
		positionX.setLabelTxt("Position:");
		panelXAxis.add(positionX);
		positionX.setParId("position_x");
		positionX.setNeedsSave(false);

		final JToggleButton btnC = new JToggleButton("Center X on pipe");
		btnC.setBounds(75, 32, 54, 31);
		panelXAxis.add(btnC);
		btnC.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e1) {
				AbstractButton abstractButton = (AbstractButton) e1.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (selected) {
					centerXOnPipe = new CenterXOnPipe();
					new Thread(centerXOnPipe).start();
				} else {
					centerXOnPipe.stop();
				}
			}
		});

		JButton btnCenterProc = new JButton("Center X procedure");
		btnCenterProc.setBounds(75, 32, 54, 31);
		panelXAxis.add(btnCenterProc);
		btnCenterProc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e1) {
				new Thread(new CenterXOnPipeProcedure()).start();
			}
		});

		final SavableText x_cut_centProc = new SavableText();
		x_cut_centProc.setLabelTxt("X cut cent proc [mm]:");
		x_cut_centProc.setParId("x_cut_centProc");
		x_cut_centProc.preventResize = true;

		// x_cut_centProc.jValue.setText("0.01");

		// Settings.getInstance().setSetting("x_cut_centProc", "0.00012");
		panelXAxis.add(x_cut_centProc);
		x_cut_centProc.jValue.addKeyListener(new KeyListener() {

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
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					Settings.getInstance().log("Setting x offset...");
					Float dimX = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_x"));

					ExecuteMdi command = new ExecuteMdi("G92 X" + (dimX.floatValue() / 2.0f - Float.valueOf(x_cut_centProc.jValue.getText())));
					command.start();
					Settings.getInstance().log("Setting x offset...Done.");
				}
			}
		});

		// ---------- Y AXIS ---------------------------
		JPanel panelYAxis = new JPanel();
		panelYAxis.setPreferredSize(panelPreferedDimension);
		panelYAxis.setMinimumSize(new Dimension(250, 200));
		this.add(panelYAxis);
		panelYAxis.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel2 = new JLabel("Y Axis");
		panelYAxis.add(lblNewLabel2);

		SavableText y_vel = new SavableText();
		y_vel.setParId("myini.maxvel_1");
		y_vel.setLabelTxt("max velocity [mm/s]:");
		y_vel.setPin(new PinDef("myini.maxvel_1", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		y_vel.requiresHalRCompSet = true;
		panelYAxis.add(y_vel);

		SavableText y_acc = new SavableText();
		y_acc.setLabelTxt("max acceleration:");
		y_acc.setParId("myini.maxacc_1");
		y_acc.setPin(new PinDef("myini.maxacc_1", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		y_acc.requiresHalRCompSet = true;
		panelYAxis.add(y_acc);

		SavableText y_stepgvel = new SavableText();
		y_stepgvel.setPin(new PinDef("myini.stepgen_maxvel_1", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		y_stepgvel.requiresHalRCompSet = true;
		y_stepgvel.setLabelTxt("max stepgen velocity [step/sec]:");
		y_stepgvel.setParId("myini.stepgen_maxvel_1");
		panelYAxis.add(y_stepgvel);

		SavableText y_stepgacc = new SavableText();
		y_stepgacc.setPin(new PinDef("myini.stepgen_maxacc_1", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		y_stepgacc.requiresHalRCompSet = true;
		y_stepgacc.setLabelTxt("max stepgen acc [step/sec^2]:");
		y_stepgacc.setParId("myini.stepgen_maxacc_1");
		panelYAxis.add(y_stepgacc);

		SavableSlider sliderY = new SavableSlider();
		sliderY.setValues(moveToText);
		sliderY.setLabelTxt("Move for:");
		sliderY.setParId("y_step");
		panelYAxis.add(sliderY);

		SavableText y_jog_speed = new SavableText();
		y_jog_speed.setLabelTxt("jog velocity [mm/min]:");
		y_jog_speed.setParId("y_jog_vel");
		panelYAxis.add(y_jog_speed);

		JButton jog2 = new JButton("JOG");
		jog2.setPreferredSize(new Dimension(100, 50));
		panelYAxis.add(jog2);
		jog2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance().getSetting("y_jog_vel"));
				Double distance = Double.valueOf(Settings.getInstance().getSetting("y_step"));
				new Jog(1, velocity / 60, distance).start();
			}
		});
		SavableText positionY = new SavableText();
		positionY.setLabelTxt("Position:");
		panelYAxis.add(positionY);
		positionY.setParId("position_y");
		positionY.setNeedsSave(false);

		// ---------- Z AXIS ---------------------------
		JPanel panelZAxis = new JPanel();
		panelZAxis.setPreferredSize(panelPreferedDimension);
		panelZAxis.setLayout(new MyVerticalFlowLayout());
		panelZAxis.setMinimumSize(new Dimension(250, 500));
		this.add(panelZAxis);
		JLabel lblNewLabel3 = new JLabel("Z Axis");
		panelZAxis.add(lblNewLabel3);

		SavableText z_vel = new SavableText();
		z_vel.setLabelTxt("max velocity [mm/s]:");
		z_vel.setParId("myini.maxvel_2");
		z_vel.setPin(new PinDef("myini.maxvel_2", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		z_vel.requiresHalRCompSet = true;
		panelZAxis.add(z_vel);

		SavableText z_acc = new SavableText();
		z_acc.setLabelTxt("max acceleration:");
		z_acc.setParId("myini.maxacc_2");
		z_acc.setPin(new PinDef("myini.maxacc_2", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		z_acc.requiresHalRCompSet = true;
		panelZAxis.add(z_acc);

		SavableText z_stepgvel = new SavableText();
		z_stepgvel.setPin(new PinDef("myini.stepgen_maxvel_2", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		z_stepgvel.requiresHalRCompSet = true;
		z_stepgvel.setLabelTxt("max stepgen velocity [step/sec]:");
		z_stepgvel.setParId("myini.stepgen_maxvel_2");
		panelZAxis.add(z_stepgvel);

		SavableText z_stepgacc = new SavableText();
		z_stepgacc.setPin(new PinDef("myini.stepgen_maxacc_2", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		z_stepgacc.requiresHalRCompSet = true;
		z_stepgacc.setLabelTxt("max stepgen acc [step/sec^2]:");
		z_stepgacc.setParId("myini.stepgen_maxacc_2");
		panelZAxis.add(z_stepgacc);

		SavableSlider sliderZ = new SavableSlider();
		sliderZ.setValues(moveToText);
		sliderZ.setLabelTxt("Move for:");
		sliderZ.setParId("z_step");
		panelZAxis.add(sliderZ);

		SavableText z_jog_speed = new SavableText();
		z_jog_speed.setLabelTxt("jog velocity [mm/min]:");
		z_jog_speed.setParId("z_jog_vel");
		panelZAxis.add(z_jog_speed);

		JButton jog3 = new JButton("JOG");
		jog3.setPreferredSize(new Dimension(100, 50));
		panelZAxis.add(jog3);
		jog3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance().getSetting("z_jog_vel"));
				Double distance = Double.valueOf(Settings.getInstance().getSetting("z_step"));
				new Jog(2, velocity / 60, distance).start();
			}
		});
		SavableText positionZ = new SavableText();
		positionZ.setLabelTxt("Position:");
		panelZAxis.add(positionZ);
		positionZ.setParId("position_z");
		positionZ.setNeedsSave(false);

		JButton btnZRot = new JButton("Make X axis horizontal");
		btnZRot.setBounds(75, 32, 54, 31);
		panelZAxis.add(btnZRot);
		btnZRot.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e1) {
				new Thread(new MakeXHorizontal()).start();
			}
		});

		JButton btnPlasmaTouch = new JButton("Plasma touch");
		btnPlasmaTouch.setBounds(75, 32, 54, 31);
		panelZAxis.add(btnPlasmaTouch);
		btnPlasmaTouch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e1) {
				new Thread(new PlasmaTouch()).start();
			}
		});

		SavableText laserDistance1 = new SavableText();
		laserDistance1.setLabelTxt("Cap. sense distance: ");
		laserDistance1.setParId("mymotion.laserHeight1");
		laserDistance1.setNeedsSave(false);
		// laserDistance1.setPin(new PinDef("mymotion.laserHeight1",
		// HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		laserDistance1.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void focusGained(FocusEvent e) {

			}
		});
		panelZAxis.add(laserDistance1);

		SavableText laserDistance2 = new SavableText();
		laserDistance2.setLabelTxt("Cap. sense distance [mm]: ");
		laserDistance2.setParId("mymotion.laserHeight1mm");
		laserDistance2.setNeedsSave(false);
		// laserDistance1.setPin(new PinDef("mymotion.laserHeight1",
		// HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		panelZAxis.add(laserDistance2);

		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(seriesXZ);
		chart = ChartFactory.createXYLineChart("", // Title
				"x[mm]", // x-axis Label
				"z[mm]", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				false, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
		);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300, 200));
		panelZAxis.add(chartPanel);

		final JToggleButton btnCalibrate = new JToggleButton("CalibrateCapSense");
		btnCalibrate.setBounds(75, 32, 54, 31);
		panelZAxis.add(btnCalibrate);
		btnCalibrate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e1) {
				AbstractButton abstractButton = (AbstractButton) e1.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				if (selected) {
					capSenseCalibrate = new CapSenseCalibrate();
					new Thread(capSenseCalibrate).start();
				} else {
					capSenseCalibrate.stop();
				}
			}
		});

		if (instance == null)
			instance = this;

	}

	public void updateChartRange() {
		ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
		ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
		// set max time window as 1 sec

		Float pipeDimX = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_x"));

		// Float posX =
		// Float.valueOf(Settings.getInstance().getSetting("position_x"));
		// Float pipeDimZ =
		// Float.valueOf(Settings.getInstance().getSetting("pipe_dim_z"));

		// current las measurement
		Float lasHeight = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));

		domainAxis.setRange(-pipeDimX - CenterXOnPipe.offsetX, pipeDimX + CenterXOnPipe.offsetX);
		rangeAxis.setRange(lasHeight - 0.001, lasHeight + 0.001);
		// rangeAxis.setTickUnit(new NumberTickUnit(0.05));
	}

	public void makeWebsocketConnection() {
		URI uri = null;
		try {
			if (wsSession != null && wsSession.isOpen())
				wsSession.close();

			ClientManager cm = ClientManager.createClient();

			String positionerUrlStr = Settings.getInstance().getSetting("laser_1_websocketurl");

			if (positionerUrlStr != null && !positionerUrlStr.equals("")) {
				uri = new URI(positionerUrlStr);
				myWebsocketClient = MyLaserWebsocketClient.getInstance();
				if (Settings.getInstance() != null)
					Settings.getInstance().log("Connecting to: " + uri.toString());
				wsSession = cm.asyncConnectToServer(myWebsocketClient, uri).get(2000, TimeUnit.MILLISECONDS);
			}
		} catch (Exception e) {
			if (Settings.getInstance() != null)
				Settings.getInstance().log("\t" + uri.toString() + " " + e.toString());
		}

	}

}
