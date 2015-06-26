/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * 
 */

/**
 * @author Jiazi Yi
 *
 */
public class QualnetFECTrafficGen {
	
	String fecConfigInput;
	String bitStreamInput;
	
	String fecTraceOutput;
	String qualnetTrafficOutput;
	
	int packetRate;
	
	ArrayList<PacketInfo> packetList;
	
	ArrayList<FECConfigSet> fecConfigSetList;
	
	ArrayList<FECTrafficTrace> fecTraffic;
	
	public void readBitStream(QualnetFECTrafficGen tg){
		
		File inputFile = new File(bitStreamInput);
		if(!inputFile.exists() || inputFile.isDirectory()){
			System.out.println(inputFile.toString());
	//		throw new FileNotFoundException();
		}
		
		//read the file info
		try{
			FileInputStream fin = new FileInputStream(inputFile);
			
			BufferedReader	br = new BufferedReader(new InputStreamReader(fin));

            if( br==null ) {
                System.err.println("/!\\ Null buffer");
                System.exit(0);
            }
            
            StringTokenizer st;     
            int     tokens_number;
            String	thisLine;
        	String	tokens[] = new String[8];
            PacketInfo  packetInfo;
            
            br.readLine();br.readLine(); //jump the first two rows
            
            while ((thisLine = br.readLine()) != null){
            	//System.out.println(thisLine);
            	
            	//read the info of current line
            	//trace format: 
            	//======================================================================
            	//Start-Pos.  Length  LId  TId  QId   Packet-Type  Discardable  Truncatable
            	//==========  ======  ===  ===  ===  ============  ===========  ===========
            	
            	if(thisLine.trim().substring(0, 2).equalsIgnoreCase("//")){
            //		System.out.println(thisLine.substring(0, 1));
            		continue;
            	}
            	st = new StringTokenizer(thisLine);
                tokens_number = 0 ;
                while(st.hasMoreElements() ) {
                    tokens[tokens_number]= st.nextToken();
                    ++ tokens_number;
                }
                
                String startPos = tokens[0];
                int length = Integer.valueOf(tokens[1]);
                int LId = Integer.valueOf(tokens[2]);
                int TId = Integer.valueOf(tokens[3]);
                int QId = Integer.valueOf(tokens[4]);
                String packetType = tokens[5];
                boolean discardable;
                boolean truncatable;
                if ((tokens[6].compareToIgnoreCase("Yes") == 0) || (tokens[6].compareToIgnoreCase("true") == 0))
                	discardable = true;
                else
                	discardable = false;
                if ((tokens[7].compareToIgnoreCase("Yes") == 0) || (tokens[7].compareToIgnoreCase("true") == 0))
                	truncatable = true;
                else
                	truncatable = false;
                
                packetInfo = new PacketInfo(startPos, length, LId, TId, 
                		QId, packetType, discardable, truncatable);
                
  //              System.out.println(packetInfo.time+"MS\t"+packetInfo.length);
                
                packetList.add(packetInfo);
              //  System.out.println(packetInfo.toString());
             //   packetInfo.printPacketInfo();
                
            }

            br.close();
            fin.close();
			
		} catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("/!\\ Array Index : " + e.getMessage());
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.err.println("/!\\ No files with name " + bitStreamInput);
            System.exit(0);
        } catch (IOException e) {
            System.err.println("/!\\ I/O exception reading a line");
            System.exit(0);
        }
        
	}
		
	/**
	 * read the FEC config file
	 * related information is saved in fecConfigSetList
	 */
	public void readFECConfig(QualnetFECTrafficGen tg){
		File inputFile = new File(fecConfigInput);
		if(!inputFile.exists() || inputFile.isDirectory()){
			System.out.println(inputFile.toString());
	//		throw new FileNotFoundException();
		}
		
		//read the file info
		try{
			FileInputStream fin = new FileInputStream(inputFile);
			
			BufferedReader	br = new BufferedReader(new InputStreamReader(fin));

            if( br==null ) {
                System.err.println("/!\\ Null buffer");
                System.exit(0);
            }
            
            StringTokenizer st;     
            int     tokens_number;
            String	thisLine;
        	String	tokens[] = new String[5];
            FECConfigSet fecConfigSet;
                       
            while ((thisLine = br.readLine()) != null){
      //      	System.out.println(thisLine);
            	st = new StringTokenizer(thisLine);
                tokens_number = 0 ;
                while(st.hasMoreElements() ) {
                    tokens[tokens_number]= st.nextToken();
                    ++ tokens_number;
                }
                fecConfigSet = new FECConfigSet();
                
                fecConfigSet.layer = tokens[0];
                fecConfigSet.bufferedPacket = Integer.valueOf(tokens[1]);
                fecConfigSet.generatedPacket = Integer.valueOf(tokens[2]);
                fecConfigSet.neededPacket = Integer.valueOf(tokens[3]);
                
                if(tokens[4].equalsIgnoreCase("s")){
                	fecConfigSet.systematic = true;
                }else{
                	fecConfigSet.systematic = false;
                }
                
                System.out.println(fecConfigSet.toString());
                
                tg.fecConfigSetList.add(fecConfigSet);
                                            
            }
		}catch(Exception e){
			System.err.println("FEC configuration file reading error");
			e.printStackTrace();
		}
	}
	
	public void generateFECTrace(QualnetFECTrafficGen tg){
		Iterator<PacketInfo> pIt = packetList.iterator();
		int fecPacketId = 0;
		
		while(pIt.hasNext()){
		/*	PacketInfo tempPacket = pIt.next();
			//	tempPacket.printPacketInfo();
			
			//get the FEC pattern
			FECConfigSet fecConfig = getFECPattern(tg, tempPacket);
			
			for(int i = 0; i < fecConfig.generatedPacket; i++){
				FECTrafficTrace fecTrace = new FECTrafficTrace();
				fecTrace.packetId = fecPacketId;
				fecTrace.generatedPacket = fecConfig.generatedPacket;
				fecTrace.bufferedPacket = fecConfig.bufferedPacket;
				fecTrace.neededPacket = fecConfig.neededPacket;
				fecTrace.systematic = fecConfig.systematic;
				fecTrace.packetSize = tempPacket.length * (fecConfig.generatedPacket - fecConfig.neededPacket + 1 ) / fecConfig.generatedPacket;
				fecTrace.packetList.add(tempPacket.startPos);
				fecPacketId ++;
				
		//		System.out.println(fecTrace.toString());
				tg.fecTraffic.add(fecTrace);*/
			
			//PacketInfo tempPacket = pIt.next();
			//	tempPacket.printPacketInfo();
			ArrayList<PacketInfo> tempPacketList = new ArrayList<PacketInfo>();
			
			tempPacketList.add(pIt.next());
			
			//get the FEC pattern
			FECConfigSet fecConfig = getFECPattern(tg, tempPacketList.get(0));
			
			for(int i = 1; i < fecConfig.bufferedPacket; i++){
				if(pIt.hasNext()){
					tempPacketList.add(pIt.next());
				}else{
					continue;
				}
			}
			
			for(int i = 0; i < fecConfig.generatedPacket; i++){
				FECTrafficTrace fecTrace = new FECTrafficTrace();
				fecTrace.packetId = fecPacketId;
				fecTrace.generatedPacket = fecConfig.generatedPacket;
				fecTrace.bufferedPacket = fecConfig.bufferedPacket;
				fecTrace.neededPacket = fecConfig.neededPacket;
				fecTrace.systematic = fecConfig.systematic;
				
				int bufferSize = 0;
				for(PacketInfo pi:tempPacketList){
					fecTrace.packetList.add(pi.startPos);
					bufferSize += pi.length;
				}
				fecTrace.packetSize = bufferSize * (fecConfig.generatedPacket - fecConfig.neededPacket + 1 ) / fecConfig.generatedPacket;
				//fecTrace.packetList.add(tempPacket.startPos);
				fecPacketId ++;
				
		//		System.out.println(fecTrace.toString());
				tg.fecTraffic.add(fecTrace);
			}
			
		}
		
	}
	
	/*
	 * get the FEC pattern by T_Id
	 */
	public FECConfigSet getFECPattern(QualnetFECTrafficGen tg,PacketInfo tempPacket){
		FECConfigSet fecConfig = new FECConfigSet();
		boolean flag = false;
		
		for(FECConfigSet tempConfig : tg.fecConfigSetList){
			if (tempPacket.TId == Integer.valueOf(tempConfig.layer.substring(1,2))){
				fecConfig = tempConfig;
				flag = true;
			}
		}
		
		//if there is no match, set the default value
		if(flag == false){
				fecConfig.bufferedPacket = 1;
				fecConfig.generatedPacket = 1;
				fecConfig.neededPacket = 1;
				fecConfig.layer = "T" + tempPacket.TId;
				fecConfig.systematic = false;
		}
		
		return fecConfig;
	}
	
	public void printFECTrace(QualnetFECTrafficGen tg){
		File fecOutput = new File(tg.fecTraceOutput);
		
		try{
			if(!fecOutput.exists()){
				fecOutput.createNewFile();
			}
			
			FileOutputStream out = new FileOutputStream(fecOutput);
			PrintStream p = new PrintStream(out);
			
			for(FECTrafficTrace tr:tg.fecTraffic){
				p.println(tr.toString());
			}			
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void printQualnetTrafficTrace(QualnetFECTrafficGen tg){
		File outputFile = new File(tg.qualnetTrafficOutput);
		
		try{
			if( !outputFile.exists()) {
				outputFile.createNewFile();
			}
			
			FileOutputStream s = new FileOutputStream(outputFile);
			PrintStream ps = new PrintStream(s);
			
			float interval = 1000 * 1 / (float)tg.packetRate;
			
			for(FECTrafficTrace fecTrace : tg.fecTraffic){
				ps.println(interval+ "MS\t" + fecTrace.packetSize);
			}
		}catch(IOException e){
			
		}
	}
	
	public QualnetFECTrafficGen(){
		this.packetList = new ArrayList<PacketInfo>();
		this.fecConfigSetList = new ArrayList<FECConfigSet>();
		this.fecTraffic = new ArrayList<FECTrafficTrace>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QualnetFECTrafficGen tg = new QualnetFECTrafficGen();
		
		
		//check parameters
	/*	if(args.length != 3){
			System.err.println("Parameter error");
			System.err.println("Usage: QualnetFECTrafficGen <packet_rate/s> <bitstream_trace> <FEC_config>");
			
			System.exit(0);
		
		}*/
		
		//for test
		//tg.bitStreamInput = "football_y00_v98_160frms_1500_rf1_without_9bytes.trace";
		tg.packetRate = 10;
		tg.bitStreamInput = "foreman_160frm_1000_wo9.trace";
		tg.fecConfigInput = "fecConfig.cfg";
		
		tg.fecTraceOutput = "foreman_160frm_1000_wo9.fec";
		tg.qualnetTrafficOutput = "foreman_160frm_1000_wo9_fec.trc";
		
		
		
		
		//read the bitstream file, related info is saved in packetList
		tg.readBitStream(tg);
		
		//read the fec config file
		tg.readFECConfig(tg);
		
		//generate FEC trace and qualnet traffic trace
		tg.generateFECTrace(tg);
		
		//print FEC traffic trace to file
		tg.printFECTrace(tg);
		
		//print Qualnet traffic trace to file
		tg.printQualnetTrafficTrace(tg);

	}

}
