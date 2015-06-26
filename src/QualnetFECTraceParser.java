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


import org.dom4j.*;

import java.util.*;


import org.dom4j.io.*;
import java.io.*;


public class QualnetFECTraceParser {
	
	QualnetTraceParser qualnetTrace;
	ArrayList<FECTrafficTrace> fecTraffic;
	TreeSet<String> bitStreamList;
	
	HashMap<String,PacketInfo> bitStreamMap;//to save the info from the original bitstream
	
	String fecTraceInput;
	
	int numFECRecord = 0;
	int bytesFECRecord = 0;
	int numReceivedFECRecord = 0;
	int numReconstructedBySys = 0;
	int numReconstructedByNonSys = 0;
	int numBitstreamRecord = 0;
	
	int numRecoveredNonDiscardableBitstreamRecord = 0;
	int numActualReceivedBitstreamRecord = 0;
	int numTotalReceivedBitstreamRecord = 0;
	
	//read the FEC Trace 
	public void readFECTrace(QualnetFECTraceParser qp){
		File file = new File(qp.fecTraceInput);
		try{
			if(!file.exists()){
				System.err.println("FEC input file no found!");
				System.exit(0);
			}
			
			FileInputStream fin = new FileInputStream(file);
			BufferedReader	br = new BufferedReader(new InputStreamReader(fin));

            if( br==null ) {
                System.err.println("/!\\ Null buffer");
                System.exit(0);
            }
            
            StringTokenizer st;     
            int     tokens_number;
            String	thisLine;
            
            while ((thisLine = br.readLine()) != null){
          //  	System.out.println(thisLine);
            	
            	st = new StringTokenizer(thisLine);
            	
            	String[] tokens = new String[6];
            	ArrayList<String> packetList = new ArrayList<String>(); 
            	FECTrafficTrace trace = new FECTrafficTrace();
            	int i = 0;
                       	
            	while(st.hasMoreElements()){
            		//System.out.println(st.nextToken());
            		if(i<6){
            			tokens[i] = st.nextToken();
            		}else{
            			packetList.add(st.nextToken());
            		}
            		i++;
            	}
            	
            	trace.packetId = Integer.valueOf(tokens[0]);
            	trace.packetSize = Integer.valueOf(tokens[1]);
            	trace.bufferedPacket = Integer.valueOf(tokens[2]);
            	trace.generatedPacket = Integer.valueOf(tokens[3]);
            	trace.neededPacket = Integer.valueOf(tokens[4]);
            	trace.systematic = Boolean.valueOf(tokens[5]);
            	
            	trace.packetList = packetList;
            	
            	qp.fecTraffic.add(trace);
            	
            	qp.numFECRecord ++;
            	qp.bytesFECRecord += trace.packetSize;
            	
            }
				
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void fecDecode(QualnetFECTraceParser qp){
		//this array is to identify if the FEC packet is received or not
		boolean[] flag = new boolean[fecTraffic.size()];
		
		//initialize the flag array
		for(int i = 0; i<qp.fecTraffic.size();i++)
			flag[i] = false;
		
/*		for(int key: qp.qualnetTrace.recvSequenceBS.keySet()){
			qp.qualnetTrace.recvSequenceBS.get(key).
		}*/
		
		//check the FEC packet is received or not
		for(int i = 0; i<qp.fecTraffic.size();i++){
			int key = qp.qualnetTrace.sendList.get(i).messageSeq;
			
			if(qp.qualnetTrace.recvSequenceBS.containsKey(key)){
				flag[i] = true;		
				qp.numReceivedFECRecord ++ ;
			}
	//		System.out.println(fecTraffic.get(i).toString() + "\t" +flag[i]);
		}
		
		//generate the bitstream array that can be reconstructed.
		for(int i = 0; i<qp.fecTraffic.size(); ){
			
			int bufferedPacket = qp.fecTraffic.get(i).bufferedPacket;
			int generatedPacket = qp.fecTraffic.get(i).generatedPacket;
			int neededPacket = qp.fecTraffic.get(i).neededPacket;
			boolean systematic = qp.fecTraffic.get(i).systematic;
			int recvedPacket = 0;
			
			for(int j = 0; j<generatedPacket; j++){
				if(flag[i] == true)
					recvedPacket++;
				i++;
			}
			
			if(recvedPacket >= neededPacket){
				bitStreamList.addAll(fecTraffic.get(i - generatedPacket).packetList);
				if(systematic == true){
					qp.numReconstructedBySys += fecTraffic.get(i - generatedPacket).packetList.size();
				}else{
					qp.numReconstructedByNonSys += fecTraffic.get(i - generatedPacket).packetList.size();
				}
			}else{
				if(systematic == true){
					for(int j = 0; j < recvedPacket; j++){
						FECTrafficTrace fecTemp = fecTraffic.get(i-generatedPacket);
						bitStreamList.add(fecTemp.packetList.get(j));
						qp.numReconstructedByNonSys ++;
					}
				}
			}
		}
		
	/*	for(String s: qp.bitStreamList){
			System.out.println(s);
		}*/
		
	}
	
	public void readBitStream(QualnetFECTraceParser qp){
		
		File inputFile = new File(qp.qualnetTrace.bitstreamInput);
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
                
  //              System.out.println(packetInfo.time+"MS\t"+packetInfo.length);
                
                bitStreamMap.put(packetInfo.startPos, packetInfo);
                qp.numBitstreamRecord ++;
              //  System.out.println(packetInfo.toString());
             //   packetInfo.printPacketInfo();
             //   qp.qualnetTrace.sendSequenceBS.get(qi).bitStreamInfo = packetInfo;
                
            }

            br.close();
            fin.close();
			
		} catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("/!\\ Array Index : " + e.getMessage());
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.err.println("/!\\ No files with name " + qp.qualnetTrace.bitstreamInput);
            System.exit(0);
        } catch (IOException e) {
            System.err.println("/!\\ I/O exception reading a line");
            System.exit(0);
        }
		
	}
	
	public void wirteBitStream(QualnetFECTraceParser qp){
		
		File bitstreamOutputFile = new File(qp.qualnetTrace.bitstreamOutput);
	/*	for(String s : bitStreamList){
			//System.out.println(qp.packetList.get(s).toString());
			qp.packetList.get(s).printPacketInfo();
		}*/
		
		try{
			if(!bitstreamOutputFile.exists()){
				bitstreamOutputFile.createNewFile();
			}
			FileOutputStream bitStreamOutputSteam = new FileOutputStream(bitstreamOutputFile); 
			PrintStream p = new PrintStream(bitStreamOutputSteam);
			
			p.println("Start-Pos.  Length  LId  TId  QId   Packet-Type  Discardable  Truncatable");
			p.println("==========================================================================");
			
			for(String s : bitStreamList){
				p.println(qp.bitStreamMap.get(s).startPos + "\t"+qp.bitStreamMap.get(s).length+"\t"
						+qp.bitStreamMap.get(s).LId + "\t"+ qp.bitStreamMap.get(s).TId + "\t"
						+qp.bitStreamMap.get(s).QId + "\t"+ qp.bitStreamMap.get(s).packetType + "\t" 
						+ qp.bitStreamMap.get(s).discardable + "\t"+ qp.bitStreamMap.get(s).truncatable);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//cut the timeout packets 
	public void dropTimeout(QualnetFECTraceParser qp){
		int i = 0;
		int j = 0;
		int drop_delay = 0;
		
		 for(int qi : qp.qualnetTrace.sendSequenceBS.keySet()){
			 QualnetTraceInfo qti = qp.qualnetTrace.sendSequenceBS.get(qi);
			 
			 if(qp.qualnetTrace.recvSequenceBS.get(qi) != null){
				 double delay =  qp.qualnetTrace.recvSequenceBS.get(qi).simTime - qp.qualnetTrace.sendSequenceBS.get(qi).simTime;
				 if(qp.qualnetTrace.delayThreshhold > 0 ){
					 if (delay > qp.qualnetTrace.delayThreshhold ){
					//	 System.out.println("The delay is " + delay + ", higher than the threshhold " + delayThreshhold);
						 drop_delay ++;
						 qp.qualnetTrace.recvSequenceBS.remove(qi);
					 }
				 } 
			 }
		 }
		 
		 qp.qualnetTrace.delayLost = drop_delay;
	}
	
	public void recoverNondiscardable(QualnetFECTraceParser qp){
		
		int received = 0;
		int totalLost = 0;
		int recovered = 0;
		
		for(String key:qp.bitStreamMap.keySet()){
			
			if(qp.bitStreamList.contains(key)){
				//bitstream received
				received ++;
			}else{
				//bitstream lost
				totalLost ++;
				if(qp.bitStreamMap.get(key).discardable == false){
					//it's a non-discardable packet
					if(qp.qualnetTrace.non_dis == true){
						//recover the packet
						qp.bitStreamList.add(key);
						recovered ++;
					}
				}
					
			}
		}
		
		qp.numRecoveredNonDiscardableBitstreamRecord = recovered;
		qp.numActualReceivedBitstreamRecord = received;
		qp.numTotalReceivedBitstreamRecord = recovered + received;
		
	}
	
	public void printResults(QualnetFECTraceParser qp, PrintStream ps){
		ps.println("*****************************************************************************");
		ps.println("*            Created by Jiazi YI, Ecole Polytech'Nantes, 2010               *");
		ps.println("*****************************************************************************");
		ps.println("*The sender node is " + qp.qualnetTrace.senderNode + ", the reciever node is " + qp.qualnetTrace.receiverNode);
		ps.println("*The delay threshold is " + qp.qualnetTrace.delayThreshhold + ", the recover non-discardable option is " + qp.qualnetTrace.non_dis);
		ps.println("*The Qualnet trace input is " + qp.qualnetTrace.inputFile);
		ps.println("*The FEC trace input is " + qp.fecTraceInput);
		ps.println("*The original bitstream input is "+ qp.qualnetTrace.bitstreamInput);
		ps.println("*The distorted bitsteam output is " + qp.qualnetTrace.bitstreamOutput);
		ps.println("*The log output is " + qp.qualnetTrace.logOutput);
		ps.println("*");
		ps.println("* Total num of bitstream record is " + qp.numBitstreamRecord);
		ps.println("* Total num recieved bitstream record is " + qp.numTotalReceivedBitstreamRecord);
		ps.println("* Num of recovered bitstream record because of the non_dis option is " + qp.numRecoveredNonDiscardableBitstreamRecord);
		ps.println("* Num of acutal received bitstream record is " + qp.numActualReceivedBitstreamRecord);
		ps.println("* Num of bitstream record reconstructed by systematic code: " + qp.numReconstructedBySys);
		ps.println("* Num of bitsrream record reconstructed by non-systematic code: " + qp.numReconstructedByNonSys);
		ps.println();
		ps.println("* Num of FEC record: " + qp.numFECRecord);
		ps.println("* Bytes of FEC record: " + qp.bytesFECRecord);
		ps.println("* Num of received FEC record: " + qp.numReceivedFECRecord);
		ps.println("**************************************************************************");
		
	}
	
	//print the log to a single file to facilitate the gathering of data
	public void printLog(QualnetFECTraceParser qp){
		try{
			File outputLogFile= new File("log.log");
			if(!outputLogFile.exists()){
				outputLogFile.createNewFile();
			}
			RandomAccessFile  rLogFile = new RandomAccessFile("log.log","rw");
			
			rLogFile.seek(rLogFile.length());//将指针移动到文件末尾 
			rLogFile.writeBytes("\n" + qp.qualnetTrace.logOutput + "\t" + qp.numBitstreamRecord + "\t" + 
					qp.numTotalReceivedBitstreamRecord + "\t" +	qp.numRecoveredNonDiscardableBitstreamRecord + "\t" + 
					qp.numActualReceivedBitstreamRecord + "\t" + qp.numReconstructedBySys + "\t" + 
					qp.numReconstructedByNonSys + "\t" + qp.numFECRecord + "\t" + qp.bytesFECRecord + "\t" + qp.numReceivedFECRecord);
			 
		
		/*	FileOutputStream outputLogStream = new FileOutputStream(outputLogFile);
			ps = new PrintStream(outputLogStream);
			
			qualnetTrace.printResults(qualnetTrace, ps);*/
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public QualnetFECTraceParser(){
		fecTraffic = new ArrayList<FECTrafficTrace>();
		qualnetTrace = new QualnetTraceParser();
		bitStreamList = new TreeSet<String>();
		bitStreamMap = new HashMap<String,PacketInfo>();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		QualnetFECTraceParser qp = new QualnetFECTraceParser();
		
		qp.qualnetTrace.protocolType = 58;
		
		if(args.length != 7){
			System.err.println("Parameter error");
			System.exit(0);
		}
		
		qp.qualnetTrace.senderNode = Integer.valueOf(args[0]);
		qp.qualnetTrace.receiverNode = Integer.valueOf(args[1]);
		qp.qualnetTrace.delayThreshhold = Float.valueOf(args[2]);
		qp.qualnetTrace.non_dis = Boolean.valueOf(args[3]);
		
		qp.qualnetTrace.inputFile = args[4]; //qualnet packet trace
		qp.fecTraceInput = args[5]; 	//fec packet trace, input
		
		qp.qualnetTrace.bitstreamInput = args[6]; //original bitstream trace, input
		
		String s = args[4].substring(0,args[4].length() - 6);
		qp.qualnetTrace.bitstreamOutput = s + ".btr";
		qp.qualnetTrace.logOutput = s + ".log";
//		qp.qualnetTrace.bitstreamOutput = args[7];
		
		//for test
	/*	qp.qualnetTrace.senderNode = 1;
		qp.qualnetTrace.receiverNode = 6;
		qp.qualnetTrace.delayThreshhold = 0.1;
		qp.qualnetTrace.non_dis = true;
		
		qp.qualnetTrace.inputFile = "fecTestQualnet.trace";
		qp.fecTraceInput = "fecTest.fec";
		
		qp.qualnetTrace.bitstreamInput = "fecTest.trace";
		qp.qualnetTrace.bitstreamOutput = "fecTest_out.trace";
		*/
		
		//read the qualnetTrace by using QualnetTraceParser
		qp.qualnetTrace.initialize(qp.qualnetTrace);
	/*	for(QualnetTraceInfo info:qp.qualnetTrace.sendList){
			info.printTraceInfo();
		}*/
		
		//drop the timeout packets 
		qp.dropTimeout(qp);
		
		//the received packets info is saved in recvSequenceBS
	//	qp.qualnetTrace.recoverNonDiscardable(qp.qualnetTrace);
		
		//read the FEC trace file
		qp.readFECTrace(qp);
		
	/*	for(FECTrafficTrace trace :qp.fecTraffic){
			System.out.println(trace.toString());
		}*/
		
		//FEC decode simulation		
		qp.fecDecode(qp);
		
		//read the original bitstream file
		qp.readBitStream(qp);
		
		//recover the nonDiscardable packets
		//the received bitstream packets are saved in bitStreamList
		//the whole bitstream packets are save in packetList
		qp.recoverNondiscardable(qp);
		
		//reconstruct the distorted bitstream file

		qp.wirteBitStream(qp);
		
		//log file
		PrintStream ps = System.out;
		qp.printResults(qp, ps);
		
		try{
			File outputLogFile = new File(qp.qualnetTrace.logOutput);
			if(!outputLogFile.exists()){
				outputLogFile.createNewFile();
			}
			
			FileOutputStream outputLogStream = new FileOutputStream(outputLogFile);
			ps = new PrintStream(outputLogStream);
			
			qp.printResults(qp, ps);
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		qp.printLog(qp);
	
		
	}

}
