package com.kz.pipeCutter.BBB.commands;

import org.zeromq.ZMQ;

import com.google.protobuf.ByteString;
import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Types.ContainerType;

public class ExecuteMdi extends BBBMachineTalkCommand {

	String mdiCommand;

	public ExecuteMdi(String mdiCommand) {
		this.mdiCommand = mdiCommand;
	}

	@Override
	public Container prepareContainer() {
		Container container = null;
		try {
			String[] splittedCmds = mdiCommand.split("\\r?\\n");

			int i = 0;
			for (String cmd : splittedCmds) {
				pb.Message.Container.Builder builder = Container.newBuilder();
				Settings.getInstance().log(cmd);
				ByteString comm = ByteString.copyFrom(cmd.getBytes("US-ASCII"));
				pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setCommandBytes(comm).build();
				builder.setType(ContainerType.MT_EMC_TASK_PLAN_EXECUTE);
				builder.setEmcCommandParams(emcCommandParameter);
				builder.setInterpName("execute");
				builder.setTicket(getNextTicket());
				container = builder.build();
				
				
				byte[] buff = container.toByteArray();
				String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
				System.out.println("Message: " + hexOutput);
				socket.send(buff, ZMQ.DONTWAIT);
				
				if (i < splittedCmds.length)
					parseAndOutput(2);
				i++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return container;
	}

}
