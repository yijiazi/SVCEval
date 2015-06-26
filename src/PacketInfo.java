/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/

/*
 * SVC Packet info class 
 */

public class PacketInfo{
	String startPos;
	int length;
	int LId;
	int TId;
	int QId;
	String packetType;
	boolean discardable;
	boolean truncatable;
	float time;
	
	PacketInfo(String startPos, int length, int LId, int TId, 
			int QId, String packetType, boolean discardable, boolean truncatable){
		this.startPos = startPos;
		this.length = length;
		this.LId = LId;
		this.TId = TId;
		this.QId = QId;
		this.packetType = packetType;
		this.discardable = discardable;
		this.truncatable = truncatable;
		this.time = 0;
		
	}
	
	PacketInfo(){
		
	}
	
	public void printPacketInfo(){
		System.out.println("StartPos:" + startPos);
		System.out.println("Length:" + length);
		System.out.println("LId:" + LId);
		System.out.println("TId:" + TId);
		System.out.println("QId:" + QId);
		System.out.println("packetType:" + packetType);
		System.out.println("discardable:" + discardable);
		System.out.println("truncatable:" + truncatable);
	}

}