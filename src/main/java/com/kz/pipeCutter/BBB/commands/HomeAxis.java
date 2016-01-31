package com.kz.pipeCutter.BBB.commands;

import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class HomeAxis extends MachineTalkCommand {
	int axisNo;
	
	public HomeAxis(int i) {
		this.axisNo = i;
	}

	@Override
	public Container prepareContainer() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setIndex(axisNo)
				.build();
		builder.setType(ContainerType.MT_EMC_AXIS_HOME);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();
		return container;
	}

}
