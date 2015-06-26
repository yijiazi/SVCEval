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
 */
public class FECConfigSet {
	String layer;
	int bufferedPacket;
	int generatedPacket;
	int neededPacket;
	boolean systematic;
	
	public String toString(){
		String output = layer + "\t"+ bufferedPacket+ "\t"+ generatedPacket+ "\t"+neededPacket + "\t" + systematic;
		return output;
	}
}
