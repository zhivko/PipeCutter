package com.kz.pipeCutter.BBB.commands;

import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Status.EmcTaskModeType;
import pb.Types.ContainerType;

public class Jog extends BBBMachineTalkCommand {

	int axisNo;
	double velocity;
	double distance;
	
	public Jog(int axisNo, double velocity, double distance)
	{
		this.axisNo = axisNo;
		this.velocity = velocity;
		this.distance = distance;
	}
	
	@Override
	public Container prepareContainer() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setIndex(axisNo)
				.setVelocity(velocity).setDistance(distance).build();

		builder.setType(ContainerType.MT_EMC_AXIS_INCR_JOG);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();

		return container;
	}

}
