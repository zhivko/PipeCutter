package com.kz.pipeCutter.BBB.commands;

import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;

import pb.Message.Container;
import pb.Types.ContainerType;

public class PlayGCode extends BBBMachineTalkCommand {

	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		
		pb.Message.Container.Builder builder = Container.newBuilder();
		
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setLineNumber(0).build();
		builder.setEmcCommandParams(emcCommandParameter);
		
		builder.setType(ContainerType.MT_EMC_TASK_PLAN_RUN);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();
		
		return container;
	}

}
