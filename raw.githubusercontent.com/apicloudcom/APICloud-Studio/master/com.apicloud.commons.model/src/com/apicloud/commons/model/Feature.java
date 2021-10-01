/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.commons.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jface.viewers.TreeNode;

import com.apicloud.commons.util.XMLUtil;

@SuppressWarnings("unchecked")
public class Feature {
	private String name;
	private String desc;
	private boolean isAndroid;
	private boolean isIos;
	private String type;
	
	private List<Param> params = new ArrayList<Param>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Param> getParams() {
		return params;
	}

	public void addParams(Param param) {
		this.params.add(param);
	}
	
	public void removeParams(Param param) {
		this.params.remove(param);
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isAndroid() {
		return isAndroid;
	}

	public void setAndroid(boolean isAndroid) {
		this.isAndroid = isAndroid;
	}

	public boolean isIos() {
		return isIos;
	}

	public void setIos(boolean isIos) {
		this.isIos = isIos;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Feature other = (Feature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		return true;
	}
	public TreeNode[] createTreeNode(TreeNode parent) {
		int size = getParams().size();
		TreeNode params[] = new TreeNode[size];
		for (int i = 0; i < size; i++) {
			TreeNode param = new TreeNode(getParams().get(i));
			param.setParent(parent);
			params[i] = param;
		}
		return params;
	}
	
	public static List<Feature> loadXml(File file) {
        if (!file.exists()) {
            return new ArrayList<Feature>();
        }
        Document document = null;
        try {
            document = XMLUtil.loadXmlFile(file);
        } catch (DocumentException e) {
        	e.printStackTrace();
        	return null;
        }
        Element rootElement = document.getRootElement();
        List<Element> featureElementList = rootElement.elements("feature");
        return parseFeature(featureElementList);
    }
	
	public static List<Feature> loadXml2(File file) {
        if (!file.exists()) {
            return new ArrayList<Feature>();
        }
        Document document = null;
        try {
            document = XMLUtil.loadXmlFile(file);
        } catch (DocumentException e) {
        	e.printStackTrace();
        	return null;
        }
        Element rootElement = document.getRootElement();
        List<Element> featureElementList = rootElement.elements("feature");
        return parseFeature2(featureElementList);
    }
	public static List<Feature> loadXml3(File file) {
        if (!file.exists()) {
            return new ArrayList<Feature>();
        }
        Document document = null;
        try {
            document = XMLUtil.loadXmlFile(file);
        } catch (DocumentException e) {
        	e.printStackTrace();
        	return null;
        }
        Element rootElement = document.getRootElement();
        List<Element> featureElementList = rootElement.elements("feature");
        return parseFeature3(featureElementList);
    }
	
	private static List<Feature> parseFeature(List<Element> featureElementList) {
		List<Feature> features = new ArrayList<Feature>();
		 for (Element pref : featureElementList) {
			 Feature feature = new Feature();
	            String name = pref.attributeValue("name");
	            String desc = pref.attributeValue("desc");
	            String isAndroid = pref.attributeValue("isAndroid");
	            String isIos = pref.attributeValue("isIOS");
	            String type = pref.attributeValue("type");
	            feature.setName(name);
	            feature.setDesc(desc);
	            feature.setAndroid(Boolean.parseBoolean(isAndroid));
	            feature.setIos(Boolean.parseBoolean(isIos));
	            feature.setType(type);
	            features.add(feature);
		  }
		return features;
	}
	
	private static List<Feature> parseFeature2(List<Element> featureElementList) {
		List<Feature> features = new ArrayList<Feature>();
		 for (Element pref : featureElementList) {
			 Feature feature = new Feature();
	            String name = pref.attributeValue("name");
	            String desc = pref.attributeValue("desc");
	            String isAndroid = pref.attributeValue("isAndroid");
	            String isIos = pref.attributeValue("isIOS");
	            String type = pref.attributeValue("type");
	            feature.setName(name);
	            feature.setDesc(desc);
	            feature.setAndroid(Boolean.parseBoolean(isAndroid));
	            feature.setIos(Boolean.parseBoolean(isIos));
	            feature.setType(type);
	            List<Element>paramsLists= pref.elements("param");
	            for(Element pelement:paramsLists){
	            	Param param=new Param();
	            	String pname=pelement.attributeValue("name");
	            	String pvalue=pelement.attributeValue("value");
	            	param.setName(pname);
	            	param.setValue(pvalue);
	            	feature.addParams(param);
	            }
	           
	            features.add(feature);
	         
		  }
		return features;
	}
	private static List<Feature> parseFeature3(List<Element> featureElementList) {
		List<Feature> features = new ArrayList<Feature>();
		for (Element pref : featureElementList) {
			Feature feature = new Feature();
	    	String name = pref.attributeValue("name");
	        feature.setName(name);
	        List<Element>paramsLists= pref.elements("param");
	        for(Element pelement:paramsLists){
	            Param param=new Param();
	            String pname=pelement.attributeValue("name");
	            String pvalue=pelement.attributeValue("value");
	            param.setName(pname);
	            param.setValue(pvalue);
	            feature.addParams(param);
	        }
	            
	        features.add(feature);
		  }
		return features;
	}

	public static File saveXml(List<Feature> features, File file) {
		 if (!file.exists()) {
			 	try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        Document document = XMLUtil.createDocument();
	        Element rootElement = document.addElement("Features");
	        createFeatureElement(rootElement, features);
	        try {
				XMLUtil.saveXml(file, document);
			} catch (IOException e) {
				e.printStackTrace();
			}
		 return file;
	}
	
	public static File saveXml2(List<Feature> features, File file) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    Document document = XMLUtil.createDocument();
	    Element rootElement = document.addElement("Features");
	    createFeatureElement2(rootElement, features);
	    try {
			XMLUtil.saveXml(file, document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static Element saveXml3(List<Feature> features) {
	    Document document = XMLUtil.createDocument();
	    Element rootElement = document.addElement("Features");
	    createFeatureElement3(rootElement, features);
	     
		return rootElement;
	}
	
	private static void createFeatureElement(Element rootElement,
			List<Feature> features) {
		for(Feature feature : features) {
			Element FeatureElement = rootElement.addElement("feature");
			FeatureElement.addAttribute("name", feature.getName());
			FeatureElement.addAttribute("desc", feature.getDesc());
			FeatureElement.addAttribute("isAndroid", feature.isAndroid() + "");
			FeatureElement.addAttribute("isIOS", feature.isIos() + "");
			FeatureElement.addAttribute("type", feature.getType());
		}
		
	}
	private static void createFeatureElement2(Element rootElement,
			List<Feature> features) {
		for(Feature feature : features) {
			Element featureElement = rootElement.addElement("feature");
			featureElement.addAttribute("name", feature.getName());
			featureElement.addAttribute("desc", feature.getDesc());
			featureElement.addAttribute("isAndroid", feature.isAndroid() + "");
			featureElement.addAttribute("isIOS", feature.isIos() + "");
			featureElement.addAttribute("type", feature.getType());
			for(Param param:feature.getParams()){
				Element paramElement = featureElement.addElement("param");
				paramElement.addAttribute("name", param.getName());
				paramElement.addAttribute("value",param.getValue());
			}
		}
		
	}
	public static void createFeatureElement3(Element rootElement,
			List<Feature> features) {
		for(Feature feature : features) {
			Element featureElement = rootElement.addElement("feature");
			featureElement.addAttribute("name", feature.getName());
			if(feature.getParams().size()>0){
				Element paramElement = featureElement.addElement("param");
				paramElement.addAttribute(feature.getParams().get(0).getValue(),feature.getParams().get(1).getValue());
			}
		}
	}
	public static String ConvertXMLtoJSON(String xml)  {  
		XMLSerializer xmlSerializer = new XMLSerializer();  
		JSON json =null;
		  
        json = xmlSerializer.read(xml);  
		return json.toString(1);  
	}

	public static String XMLTOString2(File file){
		String documentStr ="";
		List<Feature> features=Feature.loadXml3(file);
		Document document = DocumentHelper.createDocument(); 
			
		Element rootElement=document.addElement("Features");
		for(Feature feature : features) {
			Element featureElement = rootElement.addElement("feature");
			featureElement.setName(feature.getName());
			if(feature.getParams().size()==1) {
				Element paramElement = featureElement.addElement("param");
				paramElement.addAttribute(feature.getName(),feature.getParams().get(0).getName()+":"+feature.getParams().get(0).getValue());
			}else if(feature.getParams().size()==2){
				Element paramElement = featureElement.addElement("param"); 
				paramElement.addAttribute(feature.getParams().get(0).getName(), feature.getParams().get(0).getValue());
				paramElement.addAttribute(feature.getParams().get(1).getName(), feature.getParams().get(1).getValue());
			}
		}
		documentStr = document.asXML();
		return documentStr;
	}

	@Override
	public String toString() {
		return name;
	}
}
