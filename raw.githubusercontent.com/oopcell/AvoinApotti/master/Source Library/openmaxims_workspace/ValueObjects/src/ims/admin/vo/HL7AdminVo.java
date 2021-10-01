//#############################################################################
//#                                                                           #
//#  Copyright (C) <2014>  <IMS MAXIMS>                                       #
//#                                                                           #
//#  This program is free software: you can redistribute it and/or modify     #
//#  it under the terms of the GNU Affero General Public License as           #
//#  published by the Free Software Foundation, either version 3 of the       #
//#  License, or (at your option) any later version.                          # 
//#                                                                           #
//#  This program is distributed in the hope that it will be useful,          #
//#  but WITHOUT ANY WARRANTY; without even the implied warranty of           #
//#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
//#  GNU Affero General Public License for more details.                      #
//#                                                                           #
//#  You should have received a copy of the GNU Affero General Public License #
//#  along with this program.  If not, see <http://www.gnu.org/licenses/>.    #
//#                                                                           #
//#############################################################################
//#EOH
// This code was generated by Barbara Worwood using IMS Development Environment (version 1.80 build 5007.25751)
// Copyright (C) 1995-2014 IMS MAXIMS. All rights reserved.
// WARNING: DO NOT MODIFY the content of this file

package ims.admin.vo;


public class HL7AdminVo extends ims.vo.ValueObject implements ims.vo.ImsCloneable, Comparable
{
	private static final long serialVersionUID = 1L;

	public HL7AdminVo()
	{
	}
	public HL7AdminVo(ims.admin.vo.beans.HL7AdminVoBean bean)
	{
		this.started = bean.getStarted();
		this.messages = ims.admin.vo.HL7MessageInfoVoCollection.buildFromBeanCollection(bean.getMessages());
		this.isrunning = bean.getIsRunning();
		this.cfgstartinterface = bean.getCfgStartInterface();
		this.ipaddress = bean.getIpAddress();
	}
	public void populate(ims.vo.ValueObjectBeanMap map, ims.admin.vo.beans.HL7AdminVoBean bean)
	{
		this.started = bean.getStarted();
		this.messages = ims.admin.vo.HL7MessageInfoVoCollection.buildFromBeanCollection(bean.getMessages());
		this.isrunning = bean.getIsRunning();
		this.cfgstartinterface = bean.getCfgStartInterface();
		this.ipaddress = bean.getIpAddress();
	}
	public ims.vo.ValueObjectBean getBean()
	{
		return this.getBean(new ims.vo.ValueObjectBeanMap());
	}
	public ims.vo.ValueObjectBean getBean(ims.vo.ValueObjectBeanMap map)
	{
		ims.admin.vo.beans.HL7AdminVoBean bean = null;
		if(map != null)
			bean = (ims.admin.vo.beans.HL7AdminVoBean)map.getValueObjectBean(this);
		if (bean == null)
		{
			bean = new ims.admin.vo.beans.HL7AdminVoBean();
			map.addValueObjectBean(this, bean);
			bean.populate(map, this);
		}
		return bean;
	}
	public boolean getStartedIsNotNull()
	{
		return this.started != null;
	}
	public String getStarted()
	{
		return this.started;
	}
	public static int getStartedMaxLength()
	{
		return 255;
	}
	public void setStarted(String value)
	{
		this.isValidated = false;
		this.started = value;
	}
	public boolean getMessagesIsNotNull()
	{
		return this.messages != null;
	}
	public ims.admin.vo.HL7MessageInfoVoCollection getMessages()
	{
		return this.messages;
	}
	public void setMessages(ims.admin.vo.HL7MessageInfoVoCollection value)
	{
		this.isValidated = false;
		this.messages = value;
	}
	public boolean getIsRunningIsNotNull()
	{
		return this.isrunning != null;
	}
	public Boolean getIsRunning()
	{
		return this.isrunning;
	}
	public void setIsRunning(Boolean value)
	{
		this.isValidated = false;
		this.isrunning = value;
	}
	public boolean getCfgStartInterfaceIsNotNull()
	{
		return this.cfgstartinterface != null;
	}
	public Boolean getCfgStartInterface()
	{
		return this.cfgstartinterface;
	}
	public void setCfgStartInterface(Boolean value)
	{
		this.isValidated = false;
		this.cfgstartinterface = value;
	}
	public boolean getIpAddressIsNotNull()
	{
		return this.ipaddress != null;
	}
	public String getIpAddress()
	{
		return this.ipaddress;
	}
	public static int getIpAddressMaxLength()
	{
		return 255;
	}
	public void setIpAddress(String value)
	{
		this.isValidated = false;
		this.ipaddress = value;
	}
	public final String getIItemText()
	{
		return toString();
	}
	public final Integer getBoId() 
	{
		return null;
	}
	public final String getBoClassName()
	{
		return null;
	}
	public boolean equals(Object obj)
	{
		if(obj == null)
			return false;
		if(!(obj instanceof HL7AdminVo))
			return false;
		HL7AdminVo compareObj = (HL7AdminVo)obj;
		if(this.getStarted() == null && compareObj.getStarted() != null)
			return false;
		if(this.getStarted() != null && compareObj.getStarted() == null)
			return false;
		if(this.getStarted() != null && compareObj.getStarted() != null)
			return this.getStarted().equals(compareObj.getStarted());
		return super.equals(obj);
	}
	public boolean isValidated()
	{
		if(this.isBusy)
			return true;
		this.isBusy = true;
	
		if(!this.isValidated)
		{
			this.isBusy = false;
			return false;
		}
		if(this.messages != null)
		{
			if(!this.messages.isValidated())
			{
				this.isBusy = false;
				return false;
			}
		}
		this.isBusy = false;
		return true;
	}
	public String[] validate()
	{
		return validate(null);
	}
	public String[] validate(String[] existingErrors)
	{
		if(this.isBusy)
			return null;
		this.isBusy = true;
	
		java.util.ArrayList<String> listOfErrors = new java.util.ArrayList<String>();
		if(existingErrors != null)
		{
			for(int x = 0; x < existingErrors.length; x++)
			{
				listOfErrors.add(existingErrors[x]);
			}
		}
		if(this.messages != null)
		{
			String[] listOfOtherErrors = this.messages.validate();
			if(listOfOtherErrors != null)
			{
				for(int x = 0; x < listOfOtherErrors.length; x++)
				{
					listOfErrors.add(listOfOtherErrors[x]);
				}
			}
		}
		int errorCount = listOfErrors.size();
		if(errorCount == 0)
		{
			this.isBusy = false;
			this.isValidated = true;
			return null;
		}
		String[] result = new String[errorCount];
		for(int x = 0; x < errorCount; x++)
			result[x] = (String)listOfErrors.get(x);
		this.isBusy = false;
		this.isValidated = false;
		return result;
	}
	public Object clone()
	{
		if(this.isBusy)
			return this;
		this.isBusy = true;
	
		HL7AdminVo clone = new HL7AdminVo();
		
		clone.started = this.started;
		if(this.messages == null)
			clone.messages = null;
		else
			clone.messages = (ims.admin.vo.HL7MessageInfoVoCollection)this.messages.clone();
		clone.isrunning = this.isrunning;
		clone.cfgstartinterface = this.cfgstartinterface;
		clone.ipaddress = this.ipaddress;
		clone.isValidated = this.isValidated;
		
		this.isBusy = false;
		return clone;
	}
	public int compareTo(Object obj)
	{
		return compareTo(obj, true);
	}
	public int compareTo(Object obj, boolean caseInsensitive)
	{
		if (obj == null)
		{
			return -1;
		}
		if(caseInsensitive); // this is to avoid eclipse warning only.
		if (!(HL7AdminVo.class.isAssignableFrom(obj.getClass())))
		{
			throw new ClassCastException("A HL7AdminVo object cannot be compared an Object of type " + obj.getClass().getName());
		}
		HL7AdminVo compareObj = (HL7AdminVo)obj;
		int retVal = 0;
		if (retVal == 0)
		{
			if(this.getStarted() == null && compareObj.getStarted() != null)
				return -1;
			if(this.getStarted() != null && compareObj.getStarted() == null)
				return 1;
			if(this.getStarted() != null && compareObj.getStarted() != null)
			{
				if(caseInsensitive)
					retVal = this.getStarted().toLowerCase().compareTo(compareObj.getStarted().toLowerCase());
				else
					retVal = this.getStarted().compareTo(compareObj.getStarted());
			}
		}
		return retVal;
	}
	public synchronized static int generateValueObjectUniqueID()
	{
		return ims.vo.ValueObject.generateUniqueID();
	}
	public int countFieldsWithValue()
	{
		int count = 0;
		if(this.started != null)
			count++;
		if(this.messages != null)
			count++;
		if(this.isrunning != null)
			count++;
		if(this.cfgstartinterface != null)
			count++;
		if(this.ipaddress != null)
			count++;
		return count;
	}
	public int countValueObjectFields()
	{
		return 5;
	}
	protected String started;
	protected ims.admin.vo.HL7MessageInfoVoCollection messages;
	protected Boolean isrunning;
	protected Boolean cfgstartinterface;
	protected String ipaddress;
	private boolean isValidated = false;
	private boolean isBusy = false;
}
