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

package ims.icp.vo.beans;

public class ICPPhaseConfiguration_CreateVoBean extends ims.vo.ValueObjectBean
{
	public ICPPhaseConfiguration_CreateVoBean()
	{
	}
	public ICPPhaseConfiguration_CreateVoBean(ims.icp.vo.ICPPhaseConfiguration_CreateVo vo)
	{
		this.id = vo.getBoId();
		this.version = vo.getBoVersion();
		this.name = vo.getName();
		this.actions = vo.getActions() == null ? null : vo.getActions().getBeanCollection();
		this.status = vo.getStatus() == null ? null : (ims.vo.LookupInstanceBean)vo.getStatus().getBean();
		this.isactivatedoninitiation = vo.getIsActivatedOnInitiation();
	}

	public void populate(ims.vo.ValueObjectBeanMap map, ims.icp.vo.ICPPhaseConfiguration_CreateVo vo)
	{
		this.id = vo.getBoId();
		this.version = vo.getBoVersion();
		this.name = vo.getName();
		this.actions = vo.getActions() == null ? null : vo.getActions().getBeanCollection();
		this.status = vo.getStatus() == null ? null : (ims.vo.LookupInstanceBean)vo.getStatus().getBean();
		this.isactivatedoninitiation = vo.getIsActivatedOnInitiation();
	}

	public ims.icp.vo.ICPPhaseConfiguration_CreateVo buildVo()
	{
		return this.buildVo(new ims.vo.ValueObjectBeanMap());
	}

	public ims.icp.vo.ICPPhaseConfiguration_CreateVo buildVo(ims.vo.ValueObjectBeanMap map)
	{
		ims.icp.vo.ICPPhaseConfiguration_CreateVo vo = null;
		if(map != null)
			vo = (ims.icp.vo.ICPPhaseConfiguration_CreateVo)map.getValueObject(this);
		if(vo == null)
		{
			vo = new ims.icp.vo.ICPPhaseConfiguration_CreateVo();
			map.addValueObject(this, vo);
			vo.populate(map, this);
		}
		return vo;
	}

	public Integer getId()
	{
		return this.id;
	}
	public void setId(Integer value)
	{
		this.id = value;
	}
	public int getVersion()
	{
		return this.version;
	}
	public void setVersion(int value)
	{
		this.version = value;
	}
	public String getName()
	{
		return this.name;
	}
	public void setName(String value)
	{
		this.name = value;
	}
	public ims.icp.vo.beans.ICPActionConfiguration_CreateVoBean[] getActions()
	{
		return this.actions;
	}
	public void setActions(ims.icp.vo.beans.ICPActionConfiguration_CreateVoBean[] value)
	{
		this.actions = value;
	}
	public ims.vo.LookupInstanceBean getStatus()
	{
		return this.status;
	}
	public void setStatus(ims.vo.LookupInstanceBean value)
	{
		this.status = value;
	}
	public Boolean getIsActivatedOnInitiation()
	{
		return this.isactivatedoninitiation;
	}
	public void setIsActivatedOnInitiation(Boolean value)
	{
		this.isactivatedoninitiation = value;
	}

	private Integer id;
	private int version;
	private String name;
	private ims.icp.vo.beans.ICPActionConfiguration_CreateVoBean[] actions;
	private ims.vo.LookupInstanceBean status;
	private Boolean isactivatedoninitiation;
}
