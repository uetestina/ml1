// Generated by xsd compiler for android/java
// DO NOT CHANGE!
package com.ebay.trading.api;

import java.io.Serializable;
import com.leansoft.nano.annotation.*;
import java.util.List;

public class StoreCustomPageType implements Serializable {

    private static final long serialVersionUID = -1L;

	@Element(name = "Name")
	@Order(value=0)
	public String name;	
	
	@Element(name = "PageID")
	@Order(value=1)
	public Long pageID;	
	
	@Element(name = "URLPath")
	@Order(value=2)
	public String urlPath;	
	
	@Element(name = "URL")
	@Order(value=3)
	public String url;	
	
	@Element(name = "Status")
	@Order(value=4)
	public StoreCustomPageStatusCodeType status;	
	
	@Element(name = "Content")
	@Order(value=5)
	public String content;	
	
	@Element(name = "LeftNav")
	@Order(value=6)
	public Boolean leftNav;	
	
	@Element(name = "PreviewEnabled")
	@Order(value=7)
	public Boolean previewEnabled;	
	
	@Element(name = "Order")
	@Order(value=8)
	public Integer order;	
	
	@AnyElement
	@Order(value=9)
	public List<Object> any;	
	
    
}