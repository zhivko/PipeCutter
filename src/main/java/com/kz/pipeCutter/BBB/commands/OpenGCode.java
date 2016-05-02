package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class OpenGCode extends MachineTalkCommand {

	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		
		
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskMode(EmcTaskModeType.EMC_TASK_MODE_AUTO).build();
		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();
		byte[] buff = container.toByteArray();
		getCommandSocket().send(buff, 0);
		Thread.sleep(1000);
		parseAndOutput();
		

		builder = Container.newBuilder();
		builder.setType(ContainerType.MT_EMC_TASK_PLAN_INIT);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		container = builder.build();
		buff = container.toByteArray();
		getCommandSocket().send(buff, 0);
		Thread.sleep(1000);
		parseAndOutput();

		builder = Container.newBuilder();
		emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setPath("/home/machinekit/machinekit/nc_files/prog.gcode").build();
		builder.setType(ContainerType.MT_EMC_TASK_PLAN_OPEN);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Thread.sleep(1000);
		container = builder.build();
		
		return container;
	}

}
