package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class PlayGCode extends MachineTalkCommand {

	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder().setTaskMode(EmcTaskModeType.EMC_TASK_MODE_AUTO).build();
		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();
		byte[] buff = container.toByteArray();
		getCommandSocket().send(buff, 0);
		
		
		builder = Container.newBuilder();
		builder.setType(ContainerType.MT_EMC_OPEN);
		
		
		parseAndOutput();
		
		return container;
	}

}
