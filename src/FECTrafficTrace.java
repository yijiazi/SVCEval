/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/

import java.util.ArrayList;


/**
 * @author Jiazi Yi
 *
 */
public class FECTrafficTrace {
	
	int packetId;  //increase from 0,1,2...
	int packetSize;
	
	int bufferedPacket;
	int generatedPacket;
	int neededPacket;
	boolean systematic;
	
	ArrayList<String> packetList;    //hold the information of the related packets
	
	public FECTrafficTrace(){
		this.packetList = new ArrayList<String>();
	}
	
	public String toString(){
		String toReturn =  packetId + "\t" + packetSize + "\t" + bufferedPacket + "\t" + generatedPacket + "\t" + neededPacket + "\t" + systematic ;
		
		for(String s : packetList){
			toReturn += ("\t" + s);
		}
		
		return toReturn;
	}
}
