package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class CloseGCode extends MachineTalkCommand {

	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder  builder = Container.newBuilder();
		builder.setType(ContainerType.MT_EMC_TASK_PLAN_CLOSE);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();

		return container;
	}

}
