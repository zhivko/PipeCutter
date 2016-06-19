package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class PauseGCode extends MachineTalkCommand {

	public PauseGCode() {
	}

	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();

		builder.setType(ContainerType.MT_EMC_TASK_PLAN_PAUSE);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();
		byte[] buff = container.toByteArray();
		getCommandSocket().send(buff);
		parseAndOutput();

		return container;
	}

}
