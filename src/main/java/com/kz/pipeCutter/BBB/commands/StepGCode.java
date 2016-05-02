package com.kz.pipeCutter.BBB.commands;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class StepGCode extends MachineTalkCommand {

	int lineNo;
	
	public StepGCode(int lineNo)
	{
		this.lineNo = lineNo;
	}
	
	public Container prepareContainer() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		
		pb.Message.Container.Builder builder = Container.newBuilder();
		
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setLineNumber(this.lineNo).build();
		builder.setEmcCommandParams(emcCommandParameter);
		
		builder.setType(ContainerType.MT_EMC_TASK_PLAN_STEP);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);
		Container container = builder.build();
		byte[] buff = container.toByteArray();
		getCommandSocket().send(buff, 0);
		parseAndOutput();
		
		return container;
	}

}
