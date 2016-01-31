package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class ChangeMode extends MachineTalkCommand {

	
	EmcTaskModeType emcTaskMode;
	
	public ChangeMode(EmcTaskModeType emcTaskModeMdi) {
		this.emcTaskMode = emcTaskModeMdi;
	}

	@Override
	public Container prepareContainer() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskMode(emcTaskMode).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		return container;
	}

}
