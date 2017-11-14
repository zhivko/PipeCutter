package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.BBBHalRComp;

import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

import java.awt.FlowLayout;

@SuppressWarnings("serial")
public abstract class SavableControl extends JPanel implements IParameter, ISaveableAndLoadable, IHasPinDef {

	private String parId;
	public String iniFullFileName;
	private String labelTxt;
	public boolean requiresHalRCompSet = false;

	PinDef pinDef = null;

	public JLabel jLabel;
	boolean needsSave = true;

	public boolean isNeedsSave() {
		return needsSave;
	}

	public void setNeedsSave(boolean needsSave) {
		this.needsSave = needsSave;
	}

	boolean isLoadingValue;

	public JPanel panel;

	public SavableControl() {
		super();

		setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		jLabel = new JLabel("This is label:");
		add(jLabel);

	}

	public synchronized void save() throws IOException {
		if (this.isNeedsSave() && !this.isLoadingValue && Settings.getInstance() != null)
			if (this.getParValue() != null) {
				FileInputStream in = new FileInputStream(Settings.iniFullFileName);
				SortedProperties props = new SortedProperties();
				props.load(in);
				in.close();

				FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
				props.setProperty(this.getParId(), this.getParValue());
				props.store(out, null);
				out.close();
			} else {
				System.out.println("Value for: " + this.getParId() + " is NULL!");
			}

	}

	public void load() throws IOException {
		// TODO Auto-generated method stub
		Logger.getLogger(this.getClass()).info("Loading value for: " + this.getParId());
		this.isLoadingValue = true;
		FileInputStream in = new FileInputStream(Settings.iniFullFileName);
		Properties props = new Properties();
		props.load(in);
		in.close();
		if (props.getProperty(this.getParId()) != null) {
			this.isLoadingValue = true;
			this.setParValue(props.getProperty(this.getParId()));
			this.isLoadingValue = false;
		} else {
			if (this.getParId().equals("position_z"))
				System.out.println("");
			this.setParValue("");
		}
		this.isLoadingValue = false;
	}

	public String getParId() {
		return this.parId;
	}

	public void setParId(String parId) {
		this.parId = parId;
		try {
			load();
			Settings.controls.put(this.parId, this);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public String getIniFullFileName() {
		// TODO Auto-generated method stub
		return iniFullFileName;
	}

	public String getLabelTxt() {
		return labelTxt;
	}

	public void setLabelTxt(String labelTxt) {
		this.labelTxt = labelTxt;
		this.jLabel.setText(labelTxt);
		int length = this.jLabel.getFontMetrics(this.jLabel.getFont()).stringWidth(labelTxt);
		this.jLabel.setPreferredSize(new Dimension(length, 12));
	}

	public boolean isLoadingValue() {
		return this.isLoadingValue;
	}

	public abstract void setParValue(String val);

	public abstract String getParValue();

	public abstract void valueChangedFromUI();

	public void setPin(PinDef pinDef) {
		this.pinDef = pinDef;
	}

	public PinDef getPin() {
		return this.pinDef;
	}

	public void updateHal() {
		if (this.getPin() != null && this.requiresHalRCompSet && !this.getParValue().equals("")) {
			pb.Message.Container.Builder builder = Container.newBuilder();
			builder.setType(ContainerType.MT_HALRCOMP_SET);

			builder.setReplyRequired(true);

			pb.Object.Pin.Builder pin = pb.Object.Pin.newBuilder().setName(this.getPin().getPinName());
			pin.setDir(this.getPin().getPinDir());
			pin.setType(this.getPin().getPinType());
			pin.setName(this.getPin().getPinName());
			pin.setHandle(BBBHalRComp.getInstance().getPinHandle(this.getPin().getPinName()));

			try {

				if (this.getPin().getPinType() == ValueType.HAL_FLOAT) {
					pin.setHalfloat(Double.valueOf(this.getParValue()).doubleValue());
				} else if (this.getPin().getPinType() == ValueType.HAL_S32) {
					pin.setHals32(Integer.valueOf(this.getParValue()).intValue());
				} else if (this.getPin().getPinType() == ValueType.HAL_U32) {
					pin.setHalu32(Integer.valueOf(this.getParValue()).intValue());
				} else if (this.getPin().getPinType() == ValueType.HAL_BIT) {
					pin.setHalbit(Boolean.valueOf(this.getParValue()).booleanValue());
				}
				builder.addPin(pin);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			byte[] buff = builder.build().toByteArray();
			String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
			System.out.println("Message:  " + hexOutput);
			BBBHalCommand.getInstance().socket.send(buff, 0);

		}
		// this.getParent().revalidate();
	}
}
