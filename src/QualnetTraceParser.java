/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, Ecole Polytech of University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/

 
/* @author Jiazi Yi
 * Ecole Polytech'Nantes, France
 * Dec 2009
 */

import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.dom4j.io.XMLWriter;
import java.io.*;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader; 


public class QualnetTraceParser {
	
	SAXReader saxReader;
	Document document;
	
	ArrayList<QualnetTraceInfo> sendList;
	ArrayList<QualnetTraceInfo> recvList;
	
	TreeMap<Integer, QualnetTraceInfo> sendSequenceBS;
	TreeMap<Integer, QualnetTraceInfo> recvSequenceBS;
	TreeMap<Integer, QualnetTraceInfo> dropSequenceBS;
	
	
	String inputFile;
	String bitstreamInput;
	String outputSender;
	String outputReceiver;
	String bitstreamOutput;
	String dropOutput;
	String logOutput;
	int senderNode;
	int receiverNode;
	int protocolType;
	boolean non_dis;
	double delayThreshhold;
	
	int totalLost;
	int delayLost;
	int recovered;
	int actualLost;

	public void initialize(QualnetTraceParser qualnetTrace){
		
		
		qualnetTrace.saxReader = new SAXReader();
		
		qualnetTrace.document = null;
		
		sendList = new ArrayList<QualnetTraceInfo>();
		recvList = new ArrayList<QualnetTraceInfo>();
		
		sendSequenceBS = new TreeMap<Integer, QualnetTraceInfo>();
		recvSequenceBS = new TreeMap<Integer, QualnetTraceInfo>();
		dropSequenceBS = new TreeMap<Integer, QualnetTraceInfo>();
		
		
		try{
			qualnetTrace.document = qualnetTrace.saxReader.read(qualnetTrace.inputFile);
		}catch(DocumentException e){
			e.printStackTrace();
		}
		
		Element root = qualnetTrace.document.getRootElement();
		
		System.out.println("Get Root Element OK!!");
		
		//List<Element> rec_list = qualnetTrace.document.selectNodes("/trace_file/body/rec");
		
		Element body = root.element("body");
		Element rec = body.element("rec");
		
				
		
	//	System.out.println("Select nodes OK!!");
		
	//	Iterator<Element> listIter = rec_list.iterator();
		Iterator<Element> listIter = body.elementIterator();
		int i = 0;
		
		while(listIter.hasNext()){

			Element recElement = listIter.next();
			
			if(recElement.element("rechdr") == null){
				continue;  //not rec element
			}
			
			//System.out.println("Reading..." + i + recElement.toString());
			i++;
			
			Element rechdrElement = recElement.element("rechdr");
			Element actionElement = rechdrElement.element("action");
			Element bodyElement = recElement.element("recbody");
			Element udpElement = bodyElement.element("udp");
			
			if(udpElement == null)
				continue;
			
			QualnetTraceInfo traceInfo = new QualnetTraceInfo();
			
			//read the record header
			StringTokenizer rechdrST = new StringTokenizer(rechdrElement.getText());
			int tokenNumber = 0;
			String rechdrTokens[] = new String[6];
			while(rechdrST.hasMoreElements()){
				rechdrTokens[tokenNumber] = rechdrST.nextToken();
				tokenNumber ++;
			}
			
			traceInfo.origNode = Integer.valueOf(rechdrTokens[0]);
			traceInfo.messageSeq = Integer.valueOf(rechdrTokens[1]);
			traceInfo.simTime = Double.valueOf(rechdrTokens[2]);
			traceInfo.origProtocol = Integer.valueOf(rechdrTokens[3]);
			traceInfo.tracingNode = Integer.valueOf(rechdrTokens[4]);
			traceInfo.tracingProtocol = Integer.valueOf(rechdrTokens[5]);
			
			//read the action field
			StringTokenizer actionST = new StringTokenizer(actionElement.getText());
			tokenNumber = 0;
			String actionTokens[] = new String[4];
			while(actionST.hasMoreElements()){
				actionTokens[tokenNumber] = actionST.nextToken();
				tokenNumber ++;
			}
			
			traceInfo.actionType = Integer.valueOf(actionTokens[0]);
			traceInfo.commentOfAction = Integer.valueOf(actionTokens[1]);
			
			//read the udp field			
			StringTokenizer udpST = new StringTokenizer(udpElement.getText());
			tokenNumber = 0;
			String udpTokens[] = new String[4];
			while(udpST.hasMoreElements()){
				udpTokens[tokenNumber] = udpST.nextToken();
				tokenNumber ++;				
			}
			
			traceInfo.udpSourcePort = Integer.valueOf(udpTokens[0]);
			traceInfo.udpDestPort = Integer.valueOf(udpTokens[1]);
			traceInfo.udpLength = Integer.valueOf(udpTokens[2]);
			traceInfo.udpChecksum = Integer.valueOf(udpTokens[3]);
			
			//traceInfo.printTraceInfo();
			if(traceInfo.origProtocol == qualnetTrace.protocolType){
				//traceInfo.printTraceInfo();
				if(traceInfo.origNode == qualnetTrace.senderNode && traceInfo.actionType ==1){
					//traceInfo.printTraceInfo();
					sendList.add(traceInfo);
					sendSequenceBS.put(traceInfo.messageSeq, traceInfo);
				}
				
				if(traceInfo.origNode == qualnetTrace.senderNode && traceInfo.actionType == 2){
					//traceInfo.printTraceInfo();
					recvList.add(traceInfo);
					recvSequenceBS.put(traceInfo.messageSeq, traceInfo);
				}
			}
				
		}
	}

	public void writeToFile(){
		
		Iterator<QualnetTraceInfo> it = sendList.iterator();
		
		try{
			//print the sender trace
			File sendFile = new File(outputSender);
			if(!sendFile.exists()){
				sendFile.createNewFile();
			}
			FileOutputStream sendOutputSteam = new FileOutputStream(sendFile); 
			PrintStream p = new PrintStream(sendOutputSteam);
			while(it.hasNext()){
				QualnetTraceInfo info = it.next();
			//	info.printTraceInfo();
				p.println(info.simTime+"\t"+"id "+info.messageSeq+"\t udp "+info.udpLength);
			}
			
			//print the receiver trace
			File receiveFile = new File(outputReceiver);
			if(!receiveFile.exists()){
				receiveFile.createNewFile();
			}
			FileOutputStream receiveOutputStream = new FileOutputStream(receiveFile);
			p = new PrintStream(receiveOutputStream);
			it = recvList.iterator();
			
			while(it.hasNext()){
				QualnetTraceInfo info = it.next();
			//	info.printTraceInfo();
				p.println(info.simTime+"\t"+"id "+info.messageSeq+"\t udp "+info.udpLength);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void readBitStream(){
		
		File inputFile = new File(bitstreamInput);
		
		try{
			if(!inputFile.exists() || inputFile.isDirectory()){
				System.out.println(inputFile.toString());
				throw new FileNotFoundException();
			}
			
			FileInputStream fin = new FileInputStream(bitstreamInput);
			
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
            
            for(int qi : sendSequenceBS.keySet()){
            	thisLine = br.readLine();
            	
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
                if (tokens[6].compareToIgnoreCase("yes") == 0 || tokens[6].compareToIgnoreCase("true") == 0)
                	discardable = true;
                else
                	discardable = false;
                
                if (tokens[7].compareToIgnoreCase("yes") == 0 || tokens[7].compareToIgnoreCase("true") == 0)
                	truncatable = true;
                else
                	truncatable = false;
                
                packetInfo = new PacketInfo(startPos, length, LId, TId, 
                		QId, packetType, discardable, truncatable);
                
         //       System.out.println(qi+ "\t" + length +"\t"+ sendSequenceBS.get(qi).udpLength);
                
                sendSequenceBS.get(qi).bitStreamInfo = packetInfo;
            }
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void writeBitStream(){
		
		File bitstreamOutputFile = new File(bitstreamOutput);
		
		try{
			// the received trace
			if(!bitstreamOutputFile.exists()){
				bitstreamOutputFile.createNewFile();
			}
			FileOutputStream bitStreamOutputSteam = new FileOutputStream(bitstreamOutputFile); 
			PrintStream p = new PrintStream(bitStreamOutputSteam);
			
			p.println("Start-Pos.  Length  LId  TId  QId   Packet-Type  Discardable  Truncatable");
			p.println("==========================================================================");
			
			for(int qi:recvSequenceBS.keySet()){
				p.println(sendSequenceBS.get(qi).bitStreamInfo.startPos + "\t"+sendSequenceBS.get(qi).bitStreamInfo.length+"\t"
						+sendSequenceBS.get(qi).bitStreamInfo.LId + "\t"+ sendSequenceBS.get(qi).bitStreamInfo.TId + "\t"
						+sendSequenceBS.get(qi).bitStreamInfo.QId + "\t"+ sendSequenceBS.get(qi).bitStreamInfo.packetType + "\t" 
						+ sendSequenceBS.get(qi).bitStreamInfo.discardable + "\t"+ sendSequenceBS.get(qi).bitStreamInfo.truncatable);
			}
			
			//the dropped trace
			bitstreamOutputFile = new File(dropOutput);
			if(!bitstreamOutputFile.exists()){
				bitstreamOutputFile.createNewFile();
			}
			bitStreamOutputSteam = new FileOutputStream(bitstreamOutputFile); 
			p = new PrintStream(bitStreamOutputSteam);
			
			p.println("Start-Pos.  Length  LId  TId  QId   Packet-Type  Discardable  Truncatable");
			p.println("==========================================================================");
			
			for(int qi:dropSequenceBS.keySet()){
				p.println(sendSequenceBS.get(qi).bitStreamInfo.startPos + "\t"+sendSequenceBS.get(qi).bitStreamInfo.length+"\t"
						+sendSequenceBS.get(qi).bitStreamInfo.LId + "\t"+ sendSequenceBS.get(qi).bitStreamInfo.TId + "\t"
						+sendSequenceBS.get(qi).bitStreamInfo.QId + "\t"+ sendSequenceBS.get(qi).bitStreamInfo.packetType + "\t" 
						+ sendSequenceBS.get(qi).bitStreamInfo.discardable + "\t"+ sendSequenceBS.get(qi).bitStreamInfo.truncatable);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	/*
	 * recover the nonDiscardable packet if needed
	 * 
	 * discard the packets with delay more than delayThreshold
	 */
	public void recoverNonDiscardable(QualnetTraceParser qualnetTrace){
		
		int i = 0;
		int j = 0;
		int drop_delay = 0;
		
		 for(int qi : sendSequenceBS.keySet()){
			 QualnetTraceInfo qti = sendSequenceBS.get(qi);
		//	 qti.bitStreamInfo.printPacketInfo();
		/*	 if(recvSequenceBS.get(qi) == null){
				 //for the lost packets
				 if(qti.bitStreamInfo.discardable == false){	 
					 //if it's a non-discardable packet
					 if(qualnetTrace.non_dis){
						//if  we need to recover the non-discardable packets
						 recvSequenceBS.put(qi, sendSequenceBS.get(qi)); //recover the non-discardable packets						
					 }
					 i++;
				 }else{
			//		 System.out.println("I got a ture!!!");
				 }
				 j++;
			 }else{
				 //for received packets
				 double delay =  recvSequenceBS.get(qi).simTime - sendSequenceBS.get(qi).simTime;
				 if(delayThreshhold > 0 ){
					 if (delay > delayThreshhold ){//&& qti.bitStreamInfo.discardable == true){
					//	 System.out.println("The delay is " + delay + ", higher than the threshhold " + delayThreshhold);
						 drop_delay ++;
						 recvSequenceBS.remove(qi);
					 }
				 }
			 }*/
			 if(recvSequenceBS.get(qi) != null){
				 double delay =  recvSequenceBS.get(qi).simTime - sendSequenceBS.get(qi).simTime;
				 if(delayThreshhold > 0 ){
					 if (delay > delayThreshhold ){
					//	 System.out.println("The delay is " + delay + ", higher than the threshhold " + delayThreshhold);
						 drop_delay ++;
						 recvSequenceBS.remove(qi);
					 }
				 } 
			 }
			 

		 }
		 
		 for(int qi : sendSequenceBS.keySet()){
			 QualnetTraceInfo qti = sendSequenceBS.get(qi);
			 if(recvSequenceBS.get(qi) == null){
				 //for the lost packets
				 if(qti.bitStreamInfo.discardable == false){	 
					 //if it's a non-discardable packet
					 if(qualnetTrace.non_dis){
						//if  we need to recover the non-discardable packets
						 recvSequenceBS.put(qi, sendSequenceBS.get(qi)); //recover the non-discardable packets						
					 }
					 i++;
				 }
				 j++;
			 }
		 }
		 
		 qualnetTrace.totalLost = j;// + drop_delay;
		 qualnetTrace.delayLost = drop_delay;
		 qualnetTrace.recovered = i;
		 qualnetTrace.actualLost = qualnetTrace.totalLost - qualnetTrace.recovered;
		 
		 //generate the list of dropped packets
		 for(int qi: sendSequenceBS.keySet()){
			 QualnetTraceInfo qti = sendSequenceBS.get(qi);
			 if(recvSequenceBS.get(qi) == null){
				 dropSequenceBS.put(qi, sendSequenceBS.get(qi));
			 }			 
		 }
		 
	}
	
	public void printResults(QualnetTraceParser qualnetTrace, PrintStream ps){
		ps.println("******************************************************************************");
		ps.println("*Created by Jiazi Yi, Ecole Polytech'Nantes, 2010");
		ps.println("*The input Qualnet trace file is "+ qualnetTrace.inputFile);
		ps.println("*The input BitStream trace file is "+ qualnetTrace.bitstreamInput);
		ps.println("*The output sender trace file is " + qualnetTrace.outputSender);
		ps.println("*The output receiver trace file is " + qualnetTrace.outputReceiver);
		ps.println("*The output BitStream trace file is " + qualnetTrace.bitstreamOutput);
		ps.println("*The output dropped bitstream trace file is " + qualnetTrace.dropOutput);
		ps.println("*Totally " + qualnetTrace.totalLost + " packets are lost");
		ps.println("*In which " + qualnetTrace.delayLost + " packets are dropped because of delay threshhold " + qualnetTrace.delayThreshhold);
		 
		if(non_dis){
			 ps.println("*ATTENTION! " + qualnetTrace.recovered + " non-discardable packets are reocvered");
			 ps.println("*That is to say, there are " + qualnetTrace.actualLost + " actually lost.");
		} else if((!non_dis) && (qualnetTrace.recovered>0))
			ps.println("*ATTENTION! " + qualnetTrace.recovered + " non-discardable packets are lost. The bitstream might not be able to decoded");
		ps.println("******************************************************************************");
	}
	
	//print the log to a single file to facilitate the gathering of data
	public void printLog(QualnetTraceParser qualnetTrace){
		try{
			File outputLogFile= new File("log.log");
			if(!outputLogFile.exists()){
				outputLogFile.createNewFile();
			}
			RandomAccessFile  rLogFile = new RandomAccessFile("log.log","rw");
			
			rLogFile.seek(rLogFile.length());//将指针移动到文件末尾 
			rLogFile.writeBytes("\n" +qualnetTrace.logOutput + "\t" + qualnetTrace.totalLost + "\t"
					+ qualnetTrace.delayLost + "\t" + qualnetTrace.recovered + "\t"
					+ qualnetTrace.actualLost);
			 
		
		/*	FileOutputStream outputLogStream = new FileOutputStream(outputLogFile);
			ps = new PrintStream(outputLogStream);
			
			qualnetTrace.printResults(qualnetTrace, ps);*/
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws DocumentException {
		// TODO Auto-generated method stub
		
		QualnetTraceParser qualnetTrace = new QualnetTraceParser();
		
		if(args.length !=7){
			System.err.println("Parameter error");
			System.err.println("Usage: QualnetTraceParser <source_id> <destination_id> <protocol_type> <non-dis> <delay_threshold> <input_QualnetTrace> <input_BitstreamTrace>");
			System.err.println("<source_id> : The source ID in the Qualnet simulation");
			System.err.println("<destination_id> : The destination ID in the Qualnet simulation");
			System.err.println("<protocol_type> : The application layer protocol type. It is set to 58 for TRAFFIC-TRACE");
			System.err.println("<non_dis>:  If we preserve the non-discardable packets or not (true/false)");
			System.err.println("<delay_threshold>: The delay threshold in seconds. The packets with delay more than this are regarded as lost");
			System.err.println("\t\t If it is <=0, the threshold is not considered");
			System.err.println("<input_QualnetTrace>: Qualnet XML trace");
			System.err.println("<input_bitstreamTrace>: The original bitstream trace");
			
			System.exit(0);
		}
		//read the parameters
		qualnetTrace.senderNode = Integer.valueOf(args[0]);
		qualnetTrace.receiverNode = Integer.valueOf(args[1]);
		qualnetTrace.protocolType = Integer.valueOf(args[2]);
		qualnetTrace.non_dis = Boolean.valueOf(args[3]);
		qualnetTrace.delayThreshhold = Double.valueOf(args[4]);
		qualnetTrace.inputFile = args[5];
		qualnetTrace.bitstreamInput = args[6];
		
		String s;
	//	if(args.length == 4){
			int l = qualnetTrace.inputFile.length();		
			s = qualnetTrace.inputFile.substring(0,l-6);
	//	}else{
	//		s = args[4];
	//	}
		qualnetTrace.outputReceiver = s+"_rcv.tr";
		qualnetTrace.outputSender = s+"_snd.tr";
		qualnetTrace.bitstreamOutput = s+"_bs.btr";
		qualnetTrace.dropOutput = s + "_bs.dtr"; //the dropped packets trace
		qualnetTrace.logOutput = s +"_out.log";
		
		System.out.println("Analyzing the Qualnet Trace file... This may take several minutes depending on the file size.");
		
		System.out.println("Initializing...");
		
		qualnetTrace.initialize(qualnetTrace);
		
		System.out.println("Writing the sender trace and recevier trace...");
		
		qualnetTrace.writeToFile(); //write the sender trace file and receiver trace file 
		
		
		System.out.println("Reading the Bitstream trace...");
		
		qualnetTrace.readBitStream();//read the input Bitstream info and set the related field in TreeMap
		
		if(qualnetTrace.non_dis == true){
			//preserve the non-discardable packets, that's to say, recover the non-discardable packets
			System.out.println("ATTENTION! the non_dis option is set to true, so the non-discardable packets will be recovered");		
		}
		qualnetTrace.recoverNonDiscardable(qualnetTrace);  //recover the non-discardable packets if needed
																	//discard the packets with delay more than delayThreshold
		
		//System.out.println("Writing the Bistream strace...");
		
		qualnetTrace.writeBitStream();//write the Bitstream according to the recv trace
		
		//print the result
		PrintStream ps = System.out;
		qualnetTrace.printResults(qualnetTrace, ps);
		
		try{
			File outputLogFile = new File(qualnetTrace.logOutput);
			if(!outputLogFile.exists()){
				outputLogFile.createNewFile();
			}
			
			FileOutputStream outputLogStream = new FileOutputStream(outputLogFile);
			ps = new PrintStream(outputLogStream);
			
			qualnetTrace.printResults(qualnetTrace, ps);
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//print the log into one file log.log
		qualnetTrace.printLog(qualnetTrace);
		

	}

}
