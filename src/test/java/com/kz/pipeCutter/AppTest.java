package com.kz.pipeCutter;

import org.zeromq.ZMQ;

import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.BBBMachineTalkCommand;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pb.Status;
import pb.Message.Container;
import pb.Types.ContainerType;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
        
        // EStop test
        /*
        int ticket =0;
        
  			String halCmdUri = "tcp://beaglebone.local:6202";
    		// // String halCmdUri = "tcp://beaglebone.local.:49155/";
    		System.out.println("Expecting halcmd uri at: " + halCmdUri);
    		BBBMachineTalkCommand halCmd = new BBBMachineTalkCommand(halCmdUri);
    		halCmd.initSocket();

    		pb.Message.Container.Builder builder = Container.newBuilder();
    		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
    				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ESTOP_RESET).build();

    		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
    		builder.setEmcCommandParams(emcCommandParameter);
    		builder.setInterpName("execute");
    		builder.setTicket(ticket++);

    		Container container = builder.build();
    		
    		byte[] buff = container.toByteArray();
				String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
				System.out.println("Message: " + hexOutput);
				halCmd.getSocket().send(buff, ZMQ.DONTWAIT);
*/
    		System.exit(0);        
        
    }
}
