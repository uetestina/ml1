//#############################################################################
//#                                                                           #
//#  Copyright (C) <2015>  <IMS MAXIMS>                                       #
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
//#  IMS MAXIMS provides absolutely NO GUARANTEE OF THE CLINICAL SAFTEY of    #
//#  this program.  Users of this software do so entirely at their own risk.  #
//#  IMS MAXIMS only ensures the Clinical Safety of unaltered run-time        #
//#  software that it builds, deploys and maintains.                          #
//#                                                                           #
//#############################################################################
//#EOH
// This code was generated by Barbara Worwood using IMS Development Environment (version 1.80 build 5589.25814)
// Copyright (C) 1995-2015 IMS MAXIMS. All rights reserved.
// WARNING: DO NOT MODIFY the content of this file

package ims.core.vo;

/**
 * Linked to core.resource.place.Location business object (ID: 1007100007).
 */
public class LocationLiteVo extends ims.core.resource.place.vo.LocationRefVo implements ims.vo.ImsCloneable, Comparable, ims.framework.interfaces.ILocation, ims.vo.interfaces.IGenericItemType
{
	private static final long serialVersionUID = 1L;

	public LocationLiteVo()
	{
	}
	public LocationLiteVo(Integer id, int version)
	{
		super(id, version);
	}
	public LocationLiteVo(ims.core.vo.beans.LocationLiteVoBean bean)
	{
		this.id = bean.getId();
		this.version = bean.getVersion();
		this.name = bean.getName();
		this.isactive = bean.getIsActive();
		this.isvirtual = bean.getIsVirtual();
		this.type = bean.getType() == null ? null : ims.core.vo.lookups.LocationType.buildLookup(bean.getType());
		this.displayinedtracking = bean.getDisplayInEDTracking();
		this.casenotefolderlocation = bean.getCaseNoteFolderLocation();
	}
	public void populate(ims.vo.ValueObjectBeanMap map, ims.core.vo.beans.LocationLiteVoBean bean)
	{
		this.id = bean.getId();
		this.version = bean.getVersion();
		this.name = bean.getName();
		this.isactive = bean.getIsActive();
		this.isvirtual = bean.getIsVirtual();
		this.type = bean.getType() == null ? null : ims.core.vo.lookups.LocationType.buildLookup(bean.getType());
		this.displayinedtracking = bean.getDisplayInEDTracking();
		this.casenotefolderlocation = bean.getCaseNoteFolderLocation();
	}
	public ims.vo.ValueObjectBean getBean()
	{
		return this.getBean(new ims.vo.ValueObjectBeanMap());
	}
	public ims.vo.ValueObjectBean getBean(ims.vo.ValueObjectBeanMap map)
	{
		ims.core.vo.beans.LocationLiteVoBean bean = null;
		if(map != null)
			bean = (ims.core.vo.beans.LocationLiteVoBean)map.getValueObjectBean(this);
		if (bean == null)
		{
			bean = new ims.core.vo.beans.LocationLiteVoBean();
			map.addValueObjectBean(this, bean);
			bean.populate(map, this);
		}
		return bean;
	}
	public Object getFieldValueByFieldName(String fieldName)
	{
		if(fieldName == null)
			throw new ims.framework.exceptions.CodingRuntimeException("Invalid field name");
		fieldName = fieldName.toUpperCase();
		if(fieldName.equals("NAME"))
			return getName();
		if(fieldName.equals("ISACTIVE"))
			return getIsActive();
		if(fieldName.equals("ISVIRTUAL"))
			return getIsVirtual();
		if(fieldName.equals("TYPE"))
			return getType();
		if(fieldName.equals("DISPLAYINEDTRACKING"))
			return getDisplayInEDTracking();
		if(fieldName.equals("CASENOTEFOLDERLOCATION"))
			return getCaseNoteFolderLocation();
		return super.getFieldValueByFieldName(fieldName);
	}
	public boolean getNameIsNotNull()
	{
		return this.name != null;
	}
	public String getName()
	{
		return this.name;
	}
	public static int getNameMaxLength()
	{
		return 120;
	}
	public void setName(String value)
	{
		this.isValidated = false;
		this.name = value;
	}
	public boolean getIsActiveIsNotNull()
	{
		return this.isactive != null;
	}
	public Boolean getIsActive()
	{
		return this.isactive;
	}
	public void setIsActive(Boolean value)
	{
		this.isValidated = false;
		this.isactive = value;
	}
	public boolean getIsVirtualIsNotNull()
	{
		return this.isvirtual != null;
	}
	public Boolean getIsVirtual()
	{
		return this.isvirtual;
	}
	public void setIsVirtual(Boolean value)
	{
		this.isValidated = false;
		this.isvirtual = value;
	}
	public boolean getTypeIsNotNull()
	{
		return this.type != null;
	}
	public ims.core.vo.lookups.LocationType getType()
	{
		return this.type;
	}
	public void setType(ims.core.vo.lookups.LocationType value)
	{
		this.isValidated = false;
		this.type = value;
	}
	public boolean getDisplayInEDTrackingIsNotNull()
	{
		return this.displayinedtracking != null;
	}
	public Boolean getDisplayInEDTracking()
	{
		return this.displayinedtracking;
	}
	public void setDisplayInEDTracking(Boolean value)
	{
		this.isValidated = false;
		this.displayinedtracking = value;
	}
	public boolean getCaseNoteFolderLocationIsNotNull()
	{
		return this.casenotefolderlocation != null;
	}
	public Boolean getCaseNoteFolderLocation()
	{
		return this.casenotefolderlocation;
	}
	public void setCaseNoteFolderLocation(Boolean value)
	{
		this.isValidated = false;
		this.casenotefolderlocation = value;
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if(this.name != null)
			sb.append(this.name);
		return sb.toString();
	}
	/**
	* Mixed Sort
	*/
	public static LocationLiteVoCollection mixedSort(LocationLiteVoCollection coll)
	{
		return mixedSort(coll, ims.framework.enumerations.SortOrder.ASCENDING);
	}
	public static LocationLiteVoCollection mixedSort(LocationLiteVoCollection coll, ims.framework.enumerations.SortOrder order)
	{		
		return coll.sort(new LocationLiteMixedVoComparator(order));
	}
	private static class LocationLiteMixedVoComparator implements java.util.Comparator
	{
		private int direction = 1;
		public LocationLiteMixedVoComparator()
		{
			this(ims.framework.enumerations.SortOrder.ASCENDING);
		}
		public LocationLiteMixedVoComparator(ims.framework.enumerations.SortOrder order)
		{
			if (order == ims.framework.enumerations.SortOrder.DESCENDING)
			{
				direction = -1;
			}
		}
		public int compare(Object obj1, Object obj2)
		{
			LocationLiteVo voObj1 = (LocationLiteVo)obj1;
			LocationLiteVo voObj2 = (LocationLiteVo)obj2;
			return direction*(voObj1.getName().compareTo(voObj2.getName()));
		}
		public boolean equals(Object obj)
		{
			return false;
		}
	}
	/**
	* ILocation implementation
	*/
	public int getID()
	{
		return getID_Location() == null ? 0 : getID_Location().intValue();
	}
	/**
	* IGenericItemType methods
	*/
	public Integer getIGenericItemInfoID()
	{
		return this.getID_Location();
	}
	public String getIGenericItemInfoName()
	{
		return this.getName();
	}
	public Boolean getIGenericItemInfoIsActive()
	{
		return this.isactive;
	}
	public ims.core.vo.enums.SelectItemType getIGenericItemTypeSelectItemType()
	{
		return ims.core.vo.enums.SelectItemType.LOCATION_LITE;
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
		if(this.name == null || this.name.length() == 0)
			listOfErrors.add("Name is mandatory");
		else if(this.name.length() > 120)
			listOfErrors.add("The length of the field [name] in the value object [ims.core.vo.LocationLiteVo] is too big. It should be less or equal to 120");
		if(this.isvirtual == null)
			listOfErrors.add("IsVirtual is mandatory");
		if(this.type == null)
			listOfErrors.add("Type is mandatory");
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
	public void clearIDAndVersion()
	{
		this.id = null;
		this.version = 0;
	}
	public Object clone()
	{
		if(this.isBusy)
			return this;
		this.isBusy = true;
	
		LocationLiteVo clone = new LocationLiteVo(this.id, this.version);
		
		clone.name = this.name;
		clone.isactive = this.isactive;
		clone.isvirtual = this.isvirtual;
		if(this.type == null)
			clone.type = null;
		else
			clone.type = (ims.core.vo.lookups.LocationType)this.type.clone();
		clone.displayinedtracking = this.displayinedtracking;
		clone.casenotefolderlocation = this.casenotefolderlocation;
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
		if (!(LocationLiteVo.class.isAssignableFrom(obj.getClass())))
		{
			throw new ClassCastException("A LocationLiteVo object cannot be compared an Object of type " + obj.getClass().getName());
		}
		LocationLiteVo compareObj = (LocationLiteVo)obj;
		int retVal = 0;
		if (retVal == 0)
		{
			if(this.getName() == null && compareObj.getName() != null)
				return -1;
			if(this.getName() != null && compareObj.getName() == null)
				return 1;
			if(this.getName() != null && compareObj.getName() != null)
			{
				if(caseInsensitive)
					retVal = this.getName().toLowerCase().compareTo(compareObj.getName().toLowerCase());
				else
					retVal = this.getName().compareTo(compareObj.getName());
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
		if(this.name != null)
			count++;
		if(this.type != null)
			count++;
		if(this.displayinedtracking != null)
			count++;
		if(this.casenotefolderlocation != null)
			count++;
		return count;
	}
	public int countValueObjectFields()
	{
		return 4;
	}
	protected String name;
	protected Boolean isactive;
	protected Boolean isvirtual;
	protected ims.core.vo.lookups.LocationType type;
	protected Boolean displayinedtracking;
	protected Boolean casenotefolderlocation;
	private boolean isValidated = false;
	private boolean isBusy = false;
}
