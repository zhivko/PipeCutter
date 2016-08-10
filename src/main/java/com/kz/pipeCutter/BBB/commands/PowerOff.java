package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;

import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;

import pb.Status;
import pb.Types.ContainerType;

public class PowerOff extends BBBMachineTalkCommand {

	@Override
	public Container prepareContainer() {
		// TODO Auto-generated method stub
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_OFF).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();
		return container;
	}

}
