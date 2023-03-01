package com.onecashye.web.security.helper;

import java.util.Map;

import java.io.StringReader;
import java.util.HashMap;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlToMapConverter {

	public static Map<String, Object> convertXmlToMap(String xml) throws Exception {

		XmlMapper xmlMapper = new XmlMapper();
		Map<String, Object> map = xmlMapper.readValue(xml, new TypeReference<Map<String, Object>>() {
	    });
		
		return map;
	}
	public static Map<String, Object> convertXmlToFlattenedMap(String xml) throws Exception {

		InputSource is = new InputSource(new StringReader(xml));
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document document = db.parse(is);
	    return XmlToMapConverter.createMapObj(document.getDocumentElement());
	}
	private static Map<String, Object> createMapObj(Node node) {
	    Map<String, Object> map = new HashMap<>();
	    
	    if(node.hasChildNodes()) {
	    	
	    
	    	NodeList nodeList = node.getChildNodes();
	    	
	    	for (int i = 0; i < nodeList.getLength(); i++) {
	    		
	    		Node currentNode = nodeList.item(i);
	    		
	    		if(currentNode.hasChildNodes()) {
	    			map.putAll(createMapObj(currentNode));
	    		}
	    		
	    		if(currentNode.getNodeType()==1) {
	    			
		    		map.put(currentNode.getLocalName(), currentNode.getTextContent());
	    		}
	    		
	    	}
	    }
	       
	    return map;
	}
	public static Map<String, String> convertXmlDocumentToMap(String xml) throws Exception {

		InputSource is = new InputSource(new StringReader(xml));
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    dbf.setNamespaceAware(true);
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document document = db.parse(is);
	    return XmlToMapConverter.createMap(document.getDocumentElement());
	}	
	private static Map<String, String> createMap(Node node) {
	    Map<String, String> map = new HashMap<String, String>();
	    
	    if(node.hasChildNodes()) {
	    	
	    
	    	NodeList nodeList = node.getChildNodes();
	    	
	    	for (int i = 0; i < nodeList.getLength(); i++) {
	    		
	    		Node currentNode = nodeList.item(i);
	    		
	    		if(currentNode.hasChildNodes()) {
	    			map.putAll(createMap(currentNode));
	    		}
	    		
	    		if(currentNode.getNodeType()==1) {
	    			
		    		map.put(currentNode.getLocalName(), currentNode.getTextContent());
	    		}
	    		
	    	}
	    }
	       
	    return map;
	}
	
	
}
