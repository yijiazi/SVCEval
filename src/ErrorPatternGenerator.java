/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, Ecole Polytech of University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * 
 */

/**
 * @author Jiazi Yi
 * 
 * Ecole Polytech, Nantes, Jan 2010
 *
 */
public class ErrorPatternGenerator {
	
	ArrayList<PacketInfo> packetList;
	ArrayList<PacketInfo> outputPacketList;
	ArrayList<PacketInfo> droppedPacketList;
	File inputFile;
	File outputFile;
	
	ErrorType errorType;
	float errorValue;
	
	long seed;
	String inputFileName;
	HashMap<String, Float> parameterMap;
	
	String nameStub;
	String outputFileName;
	String outputDroppedFileName;
	String outputLogFileName;
	
	//statistic
	int totalPackets;
	int droppedPackets;
	int totalBytes;
	int droppedBytes;
	HashMap<String, Integer> lostPacketsByType;
	HashMap<String, Integer> packetsByType;
	HashMap<String, Integer> totalBytesByType;
	HashMap<String, Integer> droppedBytesByType;
	
	public enum ErrorType{
		byRate,byByte,byPacket;
	}
	
	
	public void readParameter(ErrorPatternGenerator ep, String[] args){
		int length = args.length/2;
		
		if(args[0].equalsIgnoreCase("r")){
			errorType = ErrorType.byRate;
		}else if(args[0].equalsIgnoreCase("p")){
			errorType = ErrorType.byPacket;
		}else if(args[0].equalsIgnoreCase("b")){
			errorType = ErrorType.byByte;
		}else {
			System.err.println("Error_Type Error!");
			System.exit(0);
		}
//		ep.errorValue = Float.valueOf(args[1]);
		
		ep.seed = Integer.valueOf(args[1]);;
		ep.inputFileName = args[2];
		
		for(int i = 1; i < length ; i++){
			String Id = args[i*2 + 1]; //get layer information
			if(Id.length()!=2){
				System.err.println("Parameter Error!");
				System.err.println("Usage: ErrorPatternGenerator <randmon_seed> <inputFile> <ErrorPattern>");
				System.err.println("Example: ErrorPatternGenerator 1 input.trace L1 0.2 T2 0.3");
				System.exit(0);
			}
			Float value = Float.valueOf(args[i*2 + 2]);
			ep.parameterMap.put(Id, value);
			ep.lostPacketsByType.put(Id, 0);
			ep.packetsByType.put(Id, 0);
			ep.totalBytesByType.put(Id, 0);
			ep.droppedBytesByType.put(Id, 0);
					
		}
	}
	
	public void readBitstream(ErrorPatternGenerator ep){
		inputFile = new File(inputFileName);
		
		try{
			if(!inputFile.exists() || inputFile.isDirectory()){
				System.out.println(inputFile.toString());
				throw new FileNotFoundException();
			}
			
			FileInputStream fin = new FileInputStream(inputFileName);
			
			BufferedReader	br = new BufferedReader(new InputStreamReader(fin));
			
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
                if ((tokens[6].compareTo("Yes") == 0) || (tokens[6].compareTo("true") == 0))
                	discardable = true;
                else
                	discardable = false;
                if ((tokens[7].compareTo("Yes") == 0) || (tokens[7].compareTo("true") == 0))
                	truncatable = true;
                else
                	truncatable = false;
                
                packetInfo = new PacketInfo(startPos, length, LId, TId, 
                		QId, packetType, discardable, truncatable);
                
                //packetInfo.printPacketInfo();
                
                ep.packetList.add(packetInfo);
                
    			//get the related statistic info
    			ep.totalPackets ++;
    			ep.totalBytes += packetInfo.length;
                for (String key:ep.parameterMap.keySet()){
    				String type = key.substring(0,1);
    				int layer = Integer.valueOf(key.substring(1,2));
    				
    				if(type.compareToIgnoreCase("l") == 0){
    					if (layer == packetInfo.LId){
    						ep.packetsByType.put(key, ep.packetsByType.get(key)+1);
    	    				ep.totalBytesByType.put(key, ep.totalBytesByType.get(key) + packetInfo.length);
    					}
    				}
    				
    				if(type.compareToIgnoreCase("t") == 0){
    					if (layer == packetInfo.TId){
    						ep.packetsByType.put(key, ep.packetsByType.get(key)+1);
    	    				ep.totalBytesByType.put(key, ep.totalBytesByType.get(key) + packetInfo.length);
    					}
    				}
    				
    			}
                
            }

            br.close();
            fin.close();

		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void insertErrors(ErrorPatternGenerator ep){
		
		Random random = new Random(ep.seed);
		
		int numberOfParameter = ep.parameterMap.size();
		
	//	boolean loopFlag = true;
		
	//	while(loopFlag == true){
		
		for (PacketInfo pi:ep.packetList){
			
			boolean insertFlag = true;	//decide the packet should be reserved or not
			
			for (String key:ep.parameterMap.keySet()){
				
				String type = key.substring(0,1);
				int layer = Integer.valueOf(key.substring(1,2));
				float value = parameterMap.get(key);
				float randomLossRate = 0;;
				
				switch(ep.errorType){
				case byRate:
					randomLossRate = (float) (value*1.5); //the actual loss rate is set a little higher to make sure enough packets a dropped
					break;
				case byByte:
					randomLossRate = (float) (value/ep.totalBytesByType.get(key) +0.05);
					break;
				case byPacket:
					randomLossRate = (float) (value/ep.packetsByType.get(key) + 0.1);
					break;
				}

				
				float randomValue = random.nextFloat();
							
				if(type.compareTo("l")==0 || type.compareTo("L") == 0){
					//for LID
					if (layer == pi.LId){
												
						if (randomValue > randomLossRate){
							//do something if the packet should be reserved
						}else if(pi.TId == 0){
							//if it's TID == 0...
						}else{
							//the packet should be dropped?
							switch(ep.errorType){
							case byRate:
								float actualLossRate = (float)(ep.lostPacketsByType.get(key))/(float)(ep.packetsByType.get(key));
								if (actualLossRate <= value){
									insertFlag = false;
								//	System.out.println(actualLossRate);
								}else{
									numberOfParameter --;
								}
								break;
							case byPacket:
								if(ep.lostPacketsByType.get(key)<=value){
									insertFlag = false;
								}else{
									numberOfParameter --;
								}
								break;
							case byByte:
								if(ep.droppedBytesByType.get(key)<=value){
									insertFlag = false;
								}else{
									numberOfParameter --;
								}									
							}
							
							if(insertFlag == false){
								ep.lostPacketsByType.put(key, ep.lostPacketsByType.get(key) + 1);
								ep.droppedBytesByType.put(key, ep.droppedBytesByType.get(key) + pi.length);
							}
						//	if(numberOfParameter == 0)
						}
					}
					
				}else if(type.compareTo("t")==0 || type.compareTo("T") == 0){
					//for TID
					if (layer == pi.TId){

						if (randomValue > randomLossRate){

						}else{

							switch(ep.errorType){
							case byRate:
								float actualLossRate = (float)(ep.lostPacketsByType.get(key))/(float)(ep.packetsByType.get(key));
								if (actualLossRate < value){
									insertFlag = false;
									System.out.println(actualLossRate);
								}else{
									numberOfParameter --;
								}
							case byPacket:
								if(ep.lostPacketsByType.get(key)<value){
									insertFlag = false;
								}else{
									numberOfParameter --;
								}
								break;
							case byByte:
								if(ep.droppedBytesByType.get(key)<value){
									insertFlag = false;
								}else{
									numberOfParameter --;
								}									
							}
							
							if(insertFlag == false){
								ep.lostPacketsByType.put(key, ep.lostPacketsByType.get(key) + 1);
								ep.droppedBytesByType.put(key, ep.droppedBytesByType.get(key) + pi.length);
							}
						}
					}else{
						//if layer != pi.Tid

					}
				}else{
					System.err.println("Type Error");
					System.exit(0);
				}
				
				//System.out.println(type + layer + value);			
			}
			
			//temp code, to remove 9 bytes packets
	/*		if(!pi.packetType.equalsIgnoreCase("parameterset")&& pi.length == 9)
				insertFlag = false;
			*/
			if(insertFlag == true){
				ep.outputPacketList.add(pi);
			}else{
				ep.droppedPacketList.add(pi);
				//ep.lostPacketsByType.put(key, ep.lostPacketsByType.get(key) + 1);
				ep.droppedPackets ++;
				ep.droppedBytes += pi.length;
			}
		}
	//	}
	}
	
	public void writeTrace(ErrorPatternGenerator ep){
		
		
		int l = ep.inputFileName.length();
		String s = ep.inputFileName.substring(0,l-6);
		
		s += "_seed" + String.valueOf(ep.seed);
		
		s += "_"+ ep.errorType.toString();
		
		for (String key:ep.parameterMap.keySet()){
			s += "_" + key + "_"+ parameterMap.get(key);
		}
		
		ep.nameStub = s;
		ep.outputDroppedFileName = s + ".drop";
		ep.outputFileName = s + ".trace";
		ep.outputLogFileName = s + ".log";
		
		//print the trace
		try{
			File outputFile = new File(ep.outputFileName);
			if(!outputFile.exists()){
				outputFile.createNewFile();
			}
			FileOutputStream outputStream = new FileOutputStream(outputFile); 
			PrintStream ps = new PrintStream(outputStream);
			
			ps.println("Start-Pos.  Length  LId  TId  QId   Packet-Type  Discardable  Truncatable");
			ps.println("==========================================================================");
			
			for(PacketInfo pi:ep.outputPacketList){
				ps.println(pi.startPos + "\t" + pi.length + "\t" +pi.LId + "\t"+ pi.TId + "\t"
						+pi.QId + "\t"+ pi.packetType + "\t" + pi.discardable + "\t"+ pi.truncatable);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//print the dropped trace
		try{
			File outputDropFile = new File(ep.outputDroppedFileName);
			if(!outputDropFile.exists()){
				outputDropFile.createNewFile();
			}
			FileOutputStream outputDropStream = new FileOutputStream(outputDropFile);
			PrintStream ps = new PrintStream(outputDropStream);
			
			ps.println("Start-Pos.  Length  LId  TId  QId   Packet-Type  Discardable  Truncatable");
			ps.println("==========================================================================");
			
			for(PacketInfo pi:ep.droppedPacketList){
				ps.println(pi.startPos + "\t" + pi.length + "\t" +pi.LId + "\t"+ pi.TId + "\t"
						+pi.QId + "\t"+ pi.packetType + "\t" + pi.discardable + "\t"+ pi.truncatable);
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//print the log
		try{
			File outputLogFile = new File(ep.outputLogFileName);
			if(!outputLogFile.exists()){
				outputLogFile.createNewFile();
			}
			
			FileOutputStream outputLogStream = new FileOutputStream(outputLogFile);
			PrintStream ps = new PrintStream(outputLogStream);
			
			ep.printResults(ep, ps);
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	public void printResults(ErrorPatternGenerator ep, PrintStream ps){
		ps.println("Input File: \t" + ep.inputFileName);
		ps.println("Random Seed: \t" + ep.seed);
		ps.println("Loss Pattern:");
		for (String key:ep.parameterMap.keySet()){
			ps.println("\t" + key + "\t"+ ep.parameterMap.get(key));
		}
		
		ps.println("Output Trace File: \t" + ep.outputFileName);
		ps.println("Output Dropped File: \t" + ep.outputDroppedFileName);
		ps.println("Output Log File: \t" + ep.outputLogFileName);
		ps.println();
		ps.println("Dropped Packets Statistcis");
		ps.println("Type \tDrp_No\tDrp_Bs \tTtl_Pkt\t" +
				"TtlBs\tB/Pkt\tActual_Lost_Ratio\t Set_Ratio");
		for(String key:ep.lostPacketsByType.keySet()){
			ps.println(key + "\t" + ep.lostPacketsByType.get(key) + "\t" +ep.droppedBytesByType.get(key) + "\t"+ 
					ep.packetsByType.get(key) + "\t" + ep.totalBytesByType.get(key) +"\t" + 
					ep.totalBytesByType.get(key)/ep.packetsByType.get(key) + "\t"+ 
					(float)ep.lostPacketsByType.get(key)/(float)ep.packetsByType.get(key) + "\t" + ep.parameterMap.get(key));
		}
		ps.println();
		ps.println("Total Bytes: \t" + ep.totalBytes);
		ps.println("Dropped Bytes: \t" + ep.droppedBytes);
		ps.println("Total Lost Rate (by bytes):\t" + (float)ep.droppedBytes/(float)ep.totalBytes);
		ps.println("Total Packets: \t" + ep.totalPackets);
		ps.println("Dropped Packets: \t" + ep.droppedPackets);
		float rate = (float)ep.droppedPackets/ (float)ep.totalPackets;
		ps.println("Total Lost Rate (by packets): \t " + rate);
		
		ps.println();
		ps.println("//ErrorPatternGenerator v0.1 for JSVM BitstreamExtractor, By Jiazi Yi, Polytech'Nantes, 2010");
		
		//just related command
/*		ps.println();
		String bitStreamFileName = ep.inputFileName.substring(0, ep.inputFileName.length() - 6) + ".264";
		ps.println(bitStreamFileName);
		String bitStreamExtracotrCommandString = "BitStreamExtractorStaticd " + bitStreamFileName + " " + ep.nameStub + ".264 -et " + ep.outputFileName;
		ps.println(bitStreamExtracotrCommandString);
		
		String yuvFileName = ep.nameStub + ".yuv";
		String decoderCommandString = "H264AVCDecoderLibTestStaticd " + ep.nameStub + ".264 " + yuvFileName + " -ec 2";
		ps.println(decoderCommandString);
		
		String pnsrFileName = ep.nameStub + ".txt";
		String pnsrCommandString = "PSNRStaticd 352 288 football_420_352x288_60.yuv " + 
			yuvFileName + " > " + pnsrFileName;
		ps.println(pnsrCommandString);*/
	}
	
	public void runCommand(ErrorPatternGenerator ep){
		try{
			
			//run the BitStreamExtactor
			String bitStreamFileName = ep.inputFileName.substring(0, ep.inputFileName.length() - 6) + ".264";
			System.out.println(bitStreamFileName);
			String bitStreamExtracotrCommandString = "BitStreamExtractorStaticd " + "football_y00_v98_160frms_1000_rf1.264" + " " + ep.nameStub + ".264 -et " + ep.outputFileName;
			System.out.println(bitStreamExtracotrCommandString);
			
			Process process = Runtime.getRuntime().exec(bitStreamExtracotrCommandString);
			
			try{
				  //do what you want to do before sleeping
			/*	  process.waitFor();
				  process.getInputStream().close();
				  process.getOutputStream().close();
				  process.getErrorStream().close();*/
				Thread.currentThread().sleep(3000);//
				  //do what you want to do after sleeptig
			}
				catch(Exception ie){
				//If this thread was intrrupted by nother thread 
			}
				
			//run the decoder
			String yuvFileName = ep.nameStub + ".yuv";
			String decoderCommandString = "H264AVCDecoderLibTestStaticd " + ep.nameStub + ".264 " + yuvFileName + " -ec 2";

			System.out.println(decoderCommandString);
			process = Runtime.getRuntime().exec(decoderCommandString);
			
			try{
				  //do what you want to do before sleeping
			/*	  process.waitFor();
				  process.getInputStream().close();
				  process.getOutputStream().close();
				  process.getErrorStream().close();*/
			//	Thread.currentThread().sleep(3000000);//
				  //do what you want to do after sleeptig
			}
				catch(Exception ie){
				//If this thread was intrrupted by nother thread 
			}
			//run the PNSR
			String pnsrFileName = ep.nameStub + ".txt";
			String pnsrCommandString = "PSNRStaticd.exe 352 288 football_420_352x288_60.yuv " + 
				yuvFileName + " > " + pnsrFileName;
			System.out.println(pnsrCommandString);
	//		process = Runtime.getRuntime().exec(pnsrCommandString);
			
		}catch(IOException e ){
			e.printStackTrace();
		}
	}
	public ErrorPatternGenerator(){
		
		packetList = new ArrayList<PacketInfo>();
		outputPacketList = new ArrayList<PacketInfo>();
		droppedPacketList = new ArrayList<PacketInfo>();
		parameterMap = new HashMap<String, Float>();
		lostPacketsByType = new HashMap<String, Integer>();
		packetsByType = new HashMap<String, Integer>();
		totalBytesByType = new HashMap<String, Integer>();
		droppedBytesByType = new HashMap<String, Integer>();
		totalPackets = 0;
		droppedPackets = 0;
		totalBytes = 0;
		droppedBytes = 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ErrorPatternGenerator ep = new ErrorPatternGenerator();

		ep.readParameter(ep, args); //read the input parameter 
		
		ep.readBitstream(ep); //read the input bitstream trace file
		
		ep.insertErrors(ep);  //insert error according to the error patterns
		
		ep.writeTrace(ep); //write the related file
		
		ep.printResults(ep, System.out); //print the results to the standard output
		
	//	ep.runCommand(ep);
		
	}

}
