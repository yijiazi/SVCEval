/******************************************************************
* Copyright (C) 2010                                                     
*     by   Jiazi Yi (IRCCyN, Ecole Polytech of University of Nantes, France) jiazi.yi@univ-nantes.fr 
*      
                                                                                                                      
*     This program is distributed in the hope that it will be useful,                                                          
*     but WITHOUT ANY WARRANTY; without even the implied warranty of
*     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
****************************************************************
*******************************************************************/

import java.util.*;
import java.lang.*;
import java.io.*;

import javax.naming.NameNotFoundException;


import java.lang.ArrayIndexOutOfBoundsException;
import java.util.TreeMap;
import java.lang.Math;

/*
 * @author Jiazi Yi
 * Ecole Polytech, Nantes, Sept, 2009
 * 
 * This program is to generate the Qualnet packet trace based on the output of JSVM Bitstream Extractor
 */



public class QualnetTrafficGen {
	
	static ArrayList<PacketInfo> packetList; 

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		packetList = new ArrayList<PacketInfo>();
		int rate; 
		String inputFileName;
		String outputFileName;
		
		long totalSize = 0;
		int packetNum = 0;
		
		if (args.length != 3){
			System.err.println("Syntax: QualnetTrafficGen <rate_in_packets/s> <input_file> <output_file>");
			System.err.println("eg. QaulnetTrafficGen 10 input.txt output.trc");
			System.err.println("Create by Jiazi Yi, Ecole Polytech'Nantes, 2009");
			System.err.println("The input file is from JSVM Bitstream Extractor. The output file is for Qualnet TrafficTrace");
			System.exit(0);
		}
		
		/*  File   file=new   File(".");   
		  System.out.println(file.getAbsolutePath());*/
		
		//get the file
		rate = Integer.valueOf(args[0]);
		inputFileName = args[1];
		outputFileName = args[2];
		
		File inputFile = new File(inputFileName);
		if(!inputFile.exists() || inputFile.isDirectory()){
			System.out.println(inputFile.toString());
			throw new FileNotFoundException();
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
                
                packetList.add(packetInfo);
                
            }

            br.close();
            fin.close();
			
		} catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("/!\\ Array Index : " + e.getMessage());
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.err.println("/!\\ No files with name " + inputFileName);
            System.exit(0);
        } catch (IOException e) {
            System.err.println("/!\\ I/O exception reading a line");
            System.exit(0);
        }
        
        //packet regrouping
        
        //set the packet time
        setTimestamp(rate); //rate is packets/second
        
        //out put
        File outputFile = new File(outputFileName);
        if(!outputFile.exists())
        	outputFile.createNewFile();  //create new file
        
        try{
        	FileOutputStream out = new FileOutputStream(outputFile);
        	PrintStream p= new PrintStream(out);
        	
        //	p.println("#time length  startPos \t LId \t TId \t QId \t discardable trunctable");
        //	p.println("#======================================================================");
        	
        	Iterator<PacketInfo> it = packetList.iterator();
        	
        	while (it.hasNext()){
        		PacketInfo tempPacket = it.next();
        	//	 System.out.println(tempPacket.time+"MS\t"+tempPacket.length);
        		totalSize += tempPacket.length;
        		packetNum ++;
        		p.println(tempPacket.time*1000+"MS\t"+tempPacket.length);/*+"\t"+tempPacket.startPos+"\t" + 
        				tempPacket.LId + "\t" + tempPacket.TId + "\t" + tempPacket.QId + "\t" + 
        				tempPacket.discardable + "\t" + tempPacket.truncatable);   	*/	
        	}
        	
        //	p.println("#Trace total size:  " + totalSize +" bytes. Packet Number:  " + packetNum);
			System.out.println("******************************************************************************");
        	System.out.println("*Created by Jiazi Yi, Ecole Polytech'Nantes, 2009");
			System.out.println("*The input file "+ inputFile.toString() + " is from JSVM Bitstream Extractor.");
			System.out.println("*The output file " + outputFile.toString()+ " is for Qualnet TrafficTrace");
			System.out.println("******************************************************************************");
        	System.out.println("Trace total size:  " + totalSize +" bytes. Packet Number:  " + packetNum);
        	System.out.println("Transmission finished in about " + packetNum/rate + 
        			" seconds. Average Rate: " + totalSize/(packetNum/rate)+ " Bytes/seconds, " + 8*totalSize/(packetNum/rate) + " bps" );
        	
        }catch(FileNotFoundException e){
        	e.printStackTrace();
        }
	}
	
	public static void setTimestamp(int rate){
		Iterator<PacketInfo> it = packetList.iterator();
		float interval = (float)1/rate;
			
		while(it.hasNext()){
			PacketInfo tempPacket = it.next();
			tempPacket.time = interval;
		}
		
	}
}

