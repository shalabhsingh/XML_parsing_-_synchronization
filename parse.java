/* code to read an XML file and print the XML elements, which have only text information inside them  and no tags */

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class parse{
	public static void getNodes(Node n, Hashtable<String, String> hash){		//function to traverse the hierarchical tree
		if(n.hasChildNodes() == false){
			System.out.println(n.getParentNode().getNodeName() + " -> " + n.getNodeValue());
			hash.put(n.getParentNode().getNodeName(), n.getNodeValue());
			return;
		}
		else{
			//System.out.println("NOT LEAF");
			NodeList nlist = n.getChildNodes();

			for(int i=0;i<nlist.getLength(); i++){				//depth first search traversal of the tree
				getNodes(nlist.item(i), hash);
			} 
		}
	}

	public static void main(String[] args){
		try{	
			System.out.println("Tag     ->      Text\n-------------------------");
			File data1 = new File("data1.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(data1);
			doc.getDocumentElement().normalize();
			//System.out.println(doc.getDocumentElement().getNodeName());
			
			Node r = doc.getDocumentElement();	//Extracting the root of the tree made by DOM
			Element root = (Element) r;

			Hashtable<String, String> hash = new Hashtable<String, String>();
			
			getNodes(root, hash);			//function call to traverse the tree and update the hash table for the same.
			System.out.println("\n\nHash Table-\n" + hash);		
		}
		catch (Exception e){
			System.out.println("ERROR DETECTED");
			e.printStackTrace();
		}

	}
}