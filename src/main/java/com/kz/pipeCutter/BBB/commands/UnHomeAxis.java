package com.kz.pipeCutter.BBB.commands;

import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;

import pb.Message.Container;
import pb.Types.ContainerType;

public class UnHomeAxis extends BBBMachineTalkCommand {
	int axisNo;
	
	public UnHomeAxis(int i) {
		this.axisNo = i;
	}

	@Override
	public Container prepareContainer() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setIndex(axisNo)
				.build();
		builder.setType(ContainerType.MT_EMC_AXIS_UNHOME);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();
		return container;
	}

}
