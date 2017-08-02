/* code to read a pair of XML files and print whether the two XML databases contain the same information
 * or not, where the tags in one XML file is mapped to tags in the other XML file using given hardcoded 
 * mapping rule. 
 */

/* apache poi location-
C:\poi-3.16\lib\log4j-1.2.17.jar;C:\poi-3.16\lib\junit-4.12.jar;C:\poi-3.16\lib\commons-logging-1.2.jar;C:\poi-3.16\lib\commons-collections4-4.1.jar;C:\poi-3.16\lib\commons-codec-1.10.jar;C:\poi-3.16\ooxml-lib\curvesapi-1.04.jar;C:\poi-3.16\ooxml-lib\xmlbeans-2.6.0.jar;C:\poi-3.16\poi-3.16.jar;C:\poi-3.16\poi-ooxml-3.16.jar;C:\poi-3.16\poi-ooxml-schemas-3.16.jar;C:\poi-3.16\poi-examples-3.16.jar;*/

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class parse{
	static int countTextNodes = 0;
	static int countUniqueTextNodes = 0;

	public static String processNumbers(String s1){ // function to process strings containing numbers, so as
		//to bring them to same format before comparing  
		int i=0, count_decimal = 0, count_number = 0;		
		
		if(s1.equals("null")) return "0";
		if(s1.length() == 0) return s1;
 
		while(i<s1.length()){						//to check if given string is a number
			if((s1.charAt(i) >= '0' && s1.charAt(i) <= '9') || s1.charAt(i) == '.'){
				if(s1.charAt(i) == '.') count_decimal++;
				else count_number++;
			}
			i++;
		}
		
		boolean isNumber = false;
		if(count_number == s1.length()){	//to check if given string is a number without decimal point
			while(s1.charAt(0) == '0')   //to remove any 0s before start of number
			{
				s1 = s1.substring(1, s1.length());
				if(s1.length()==0) break;
			}
			isNumber = true;
		}
		else{
			if(count_number == s1.length()-1 && count_decimal == 1 && s1.charAt(0) != '.' && s1.charAt(s1.length()-1) != '.'){
												//to check if it given string is a number with decimal point
				while(s1.charAt(0) == '0')   //to remove any 0s before start of number
				{
					s1 = s1.substring(1, s1.length());
					if(s1.length()==0) break;
				}			
				while(s1.charAt(s1.length()-1) == '0')  //to remove any insignificant 0s after decimal point
				{
					s1 = s1.substring(0, s1.length()-1);
					if(s1.length()==0) break;
				}
				isNumber = true;
			}
		}		
		
		//removing decimal point if not required
		if(isNumber == true && s1.length() > 0) if(s1.charAt(s1.length()-1) == '.') s1 = s1.substring(0, s1.length()-1);
		if(isNumber == true && s1.length() == 0) s1 = "0";
		
		return s1;		
	}

	public static boolean compare(String s1, String s2){ //function to compare two strings after formatting
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		
		if(s1.equals(s2)) return true; //comparing strings in lowercase
		if(processNumbers(s1).equals(processNumbers(s2))) return true; //comparing strings containing numbers
		
		return false;		
	}

	public static String updateKey(String key, int counter){
		//function to generate subsequent keys from initial key
		if(counter == 2){
			key = key + Integer.toString(counter);
		}
		else{
			int index = key.length()-1;
			
			while(key.charAt(index) >= '0' && key.charAt(index) <= '9') index--;
			key = key.substring(0, index+1) + Integer.toString(counter);
		}	
		return key;
	}

	public static int[] traverseNodes(Node n, Hashtable<String, String> hash, Boolean flag, int countTextNodes, int countUniqueTextNodes){
		//function to traverse the hierarchical tree made by DOM parser and update the hash table
		//storing (tag, tag_content) pairs
		int[] array = {countTextNodes, countUniqueTextNodes};
		if(n.hasChildNodes() == false){ //if node is a leaf node
			if (n.getNodeValue().trim().length() != 0){				
				String key = n.getParentNode().getNodeName();
				int counter = 2;
				
				countTextNodes++;
								
				if(!hash.containsKey(key)){
					countUniqueTextNodes++;
				}
				
				while(hash.containsKey(key)){
					key = updateKey(key, counter);
					counter += 1;				
				}
				
				hash.put(key, n.getNodeValue().trim());
				array[0] = countTextNodes;
				array[1] = countUniqueTextNodes;
				return array;
			}
		}
		else{	//if not leaf node, recurse to search deeper within the tree
			NodeList nlist = n.getChildNodes();

			for(int i=0;i<nlist.getLength(); i++){				
				//depth first search traversal of the tree
				array = traverseNodes(nlist.item(i), hash, false, countTextNodes, countUniqueTextNodes);
				countTextNodes = array[0];
				countUniqueTextNodes = array[1];
			}
		}
		
		if(flag == true){
			System.out.println("Total number of text nodes = " + countTextNodes);
			System.out.println("Total number of unique text nodes = " + countUniqueTextNodes);
		}
		return array;
	}
	
	public static Hashtable<String, String> getHashTable(String filename){
		//function to read a XML file, read it's content and retuern the hash table consisting of 
		//(tag, tag_content) pairs
		try{		
			File data = new File(filename);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(data);
			doc.getDocumentElement().normalize();
			
			Node root = doc.getDocumentElement();	//Extracting the root of the tree made by DOM
			
			System.out.println("\n"+filename);
			Hashtable<String, String> hash = new Hashtable<String, String>();
			
			traverseNodes(root, hash, true, 0, 0);
			//function call to traverse the tree and update the hash table for the same.
			return hash;
		}
		catch (Exception e){
			System.out.println("ERROR DETECTED IN PROCESSING FILE " + filename);
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args){
		try{
			//function calls to get the respective hash tables for both XML files
			String fileName1 = "data3.xml", fileName2 = "data4.xml";
			
			Hashtable<String, String> hash_data1 = getHashTable(fileName1);
			Hashtable<String, String> hash_data2 = getHashTable(fileName2);

			//reading excel file in java containing mapping scheme
			File excel = new File("mapping.xlsx");
			FileInputStream file = new FileInputStream(excel);
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);

			Iterator<Row> rowI = sheet.iterator();
			Row r = rowI.next();
			r = rowI.next();
			
			System.out.println("");
			
			boolean comparisonStatus = true;			
			int ind = 1;
			
			while(rowI.hasNext()){
				Row row = rowI.next();
				Iterator<Cell> cellI = row.cellIterator();
				
				int column = 1;
				String tag_data1 = "", tag_data2 = "";
			
				while(cellI.hasNext()){
					Cell cell = cellI.next();	
					
					if(column == 1) tag_data1 = cell.getStringCellValue().trim();
					else tag_data2 = cell.getStringCellValue().trim();

					column++;
				}
				
				//search begins now
				String key1 = tag_data1, key2 = tag_data2, key1_original = key1, key2_original = key2;
				int counter1 = 2, counter2 = 2;
				boolean flag = false;
				
				//loop construct to check unique tags and their content for both XML files and output
				//if they contain same data or not
				
				if( hash_data1.containsKey(key1) && hash_data2.containsKey(key2) ){
					key1 = updateKey(key1, counter1);
					counter1++;
					key2 = updateKey(key2, counter2);
					counter2++;

					if( !(hash_data1.containsKey(key1) || hash_data2.containsKey(key2)) ){						
						if(compare(hash_data1.get(key1_original), hash_data2.get(key2_original)) == false){
							if(comparisonStatus == true){
								System.out.println("Both XML files don't match. Tags whose content don't match are as follows-");
							}
							System.out.println("\nError " + ind + ":");
							System.out.println("Tag1 = " + key1_original);
							System.out.println("Content1 = " + hash_data1.get(key1_original));
							System.out.println("Tag2 = " + key2_original);
							System.out.println("Content2 = " + hash_data1.get(key2_original));
							ind++;
							comparisonStatus = false;
						}
					}
						
				}				
				
				/*
				while(hash_data1.containsKey(key1)){					
					if( compare(hash_data1.get(key1), hash_data2.get(key2)) == true){
						System.out.println(key1 + "\t" + key2 + "\tTrue");
						flag = true;
					}
					else{
						System.out.println(key1 + "\t" + key2 + "\t"+ "\tFalse");
					}
					key2 = updateKey(key2, counter2);
					counter2++;
					key1 = updateKey(key1, counter1);
					counter1++;				
				}			
				
				while(hash_data1.containsKey(key1)){			
					key2 = tag_data2;
					counter2 = 2;

					while(hash_data2.containsKey(key2)){
						//System.out.println(key1 + "\t" + key2 + "\tTrue");
						//System.out.println(hash_data1.get(key1)+ "\t" +  hash_data2.get(key2));
						if( compare(hash_data1.get(key1), hash_data2.get(key2)) == true){
							System.out.println(key1 + "\t" + key2 + "\t"+ "\tTrue");
							flag = true;
						}
						else{
							System.out.println(key1 + "\t" + key2 + "\t"+ "\tFalse");
						}

						//updating key values		
						key2 = updateKey(key2, counter2);
						counter2++;
					}
					key1 = updateKey(key1, counter1);
					counter1++;
				}				
				
				if(flag == false){
					System.out.println(key1_original + "\t" + key2_original + "\tFalse");
				}
				*/
				workbook.close();
				file.close();
			}

			if(comparisonStatus == true){
				System.out.println("Both XML files contain the same records.");
			}

			
		}
		catch (Exception e){
			System.out.println("ERROR DETECTED");
			e.printStackTrace();
		}
	}
	
	//End of program
}
