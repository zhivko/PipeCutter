package com.kz.pipeCutter.BBB.commands;

import com.google.protobuf.ByteString;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Types.ContainerType;

public class ExecuteMdi extends MachineTalkCommand {

	@Override
	public Container prepareContainer() {
		Container container = null;
		try {
			pb.Message.Container.Builder builder = Container.newBuilder();
			ByteString comm = ByteString.copyFrom(Settings.parMdiCommand.getBytes("US-ASCII"));
			pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
					.setCommandBytes(comm).build();
			builder.setType(ContainerType.MT_EMC_TASK_PLAN_EXECUTE);
			builder.setEmcCommandParams(emcCommandParameter);
			builder.setInterpName("execute");
			builder.setTicket(ticket++);
			container = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return container;
	}

}
