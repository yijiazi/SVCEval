/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/
/**
 * @author Jiazi Yi
 * 
 * Qualnet trace info class
 *
 */
public class QualnetTraceInfo {
	int origNode;
	int messageSeq;
	double simTime;
	int origProtocol;
	int tracingNode;
	int tracingProtocol;
	
	int actionType; //1 send, 2 recv, 3 drop, 4 enqueue, 5 dequeue
	int commentOfAction;
	
	int udpSourcePort;
	int udpDestPort;
	int udpLength;
	int udpChecksum;
	
	String startPos;
	
	PacketInfo bitStreamInfo;  //this part is just for bitstream
	
	public void printTraceInfo(){
		System.out.println("************************************************");
		System.out.println("Originating Node: " + origNode);
		System.out.println("Message Seq Number: " + messageSeq);
		System.out.println("Simulation time: " + simTime);
		System.out.println("Originating Protocol: " + origProtocol);
		System.out.println("Tracing Node: " + tracingNode);
		System.out.println("Tracing Protocol: " + tracingProtocol);
		System.out.println("Action Type: "+ actionType);
		System.out.println("Comment of Action: " + commentOfAction);
		System.out.println("UDP Source Port:" + udpSourcePort);
		System.out.println("UDP Destination Port: " + udpDestPort);
		System.out.println("UDP Length: " + udpLength);		
	}

}
