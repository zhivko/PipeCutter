package com.kz.pipeCutter.BBB.commands;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.BBB.BBBError;
import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.BBBHalRComp;
import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;
import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Types.ContainerType;

public class MachineKitShutDown extends BBBMachineTalkCommand {
	public MachineKitShutDown() {
	}

	@Override
	public Container prepareContainer() throws Exception {
		// TODO Auto-generated method stub
		if (BBBHalRComp.instance != null)
			BBBHalRComp.instance.stop();
		if (BBBStatus.instance != null)
			BBBStatus.instance.stop();
		if (BBBError.instance != null)
			BBBError.instance.stop();
		if (BBBHalCommand.instance != null)
			BBBHalCommand.instance.stop();
		
		
		pb.Message.Container.Builder builder = Container.newBuilder();
		builder.setType(ContainerType.MT_SHUTDOWN);
		builder.setTicket(getNextTicket());
		Container container = builder.build();
		socket.send(container.toByteArray(), 0);
		try {
			parseAndOutput(1);
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Settings.getInstance().log("MK instance shutted DOWN.");
		return null;
	}

}
