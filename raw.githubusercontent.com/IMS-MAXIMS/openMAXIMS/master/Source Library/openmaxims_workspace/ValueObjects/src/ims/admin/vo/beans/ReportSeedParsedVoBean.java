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

package ims.admin.vo.beans;

public class ReportSeedParsedVoBean extends ims.vo.ValueObjectBean
{
	public ReportSeedParsedVoBean()
	{
	}
	public ReportSeedParsedVoBean(ims.admin.vo.ReportSeedParsedVo vo)
	{
		this.name = vo.getName();
		this.type = vo.getType();
		this.value = vo.getValue();
		this.boname = vo.getBOName();
		this.bofield = vo.getBOField();
		this.gp = vo.getGP() == null ? null : (ims.core.vo.beans.GpShortVoBean)vo.getGP().getBean();
		this.hcp = vo.getHCP() == null ? null : (ims.core.vo.beans.HcpLiteVoBean)vo.getHCP().getBean();
		this.mos = vo.getMOS() == null ? null : (ims.core.vo.beans.MemberOfStaffShortVoBean)vo.getMOS().getBean();
		this.organisation = vo.getOrganisation() == null ? null : (ims.core.vo.beans.OrgShortVoBean)vo.getOrganisation().getBean();
		this.locsite = vo.getLocSite() == null ? null : (ims.core.vo.beans.LocSiteLiteVoBean)vo.getLocSite().getBean();
		this.location = vo.getLocation() == null ? null : (ims.core.vo.beans.LocationLiteVoBean)vo.getLocation().getBean();
		this.canbenull = vo.getCanBeNull();
		this.sex = vo.getSex() == null ? null : (ims.vo.LookupInstanceBean)vo.getSex().getBean();
		this.outcome = vo.getOutcome() == null ? null : (ims.vo.LookupInstanceBean)vo.getOutcome().getBean();
		this.searchtype = vo.getSearchType();
		this.searchby = vo.getSearchBy();
		this.displayfields = vo.getDisplayFields();
		this.referralby = vo.getReferralBy() == null ? null : (ims.vo.LookupInstanceBean)vo.getReferralBy().getBean();
		this.displaytext = vo.getDisplayText();
	}

	public void populate(ims.vo.ValueObjectBeanMap map, ims.admin.vo.ReportSeedParsedVo vo)
	{
		this.name = vo.getName();
		this.type = vo.getType();
		this.value = vo.getValue();
		this.boname = vo.getBOName();
		this.bofield = vo.getBOField();
		this.gp = vo.getGP() == null ? null : (ims.core.vo.beans.GpShortVoBean)vo.getGP().getBean(map);
		this.hcp = vo.getHCP() == null ? null : (ims.core.vo.beans.HcpLiteVoBean)vo.getHCP().getBean(map);
		this.mos = vo.getMOS() == null ? null : (ims.core.vo.beans.MemberOfStaffShortVoBean)vo.getMOS().getBean(map);
		this.organisation = vo.getOrganisation() == null ? null : (ims.core.vo.beans.OrgShortVoBean)vo.getOrganisation().getBean(map);
		this.locsite = vo.getLocSite() == null ? null : (ims.core.vo.beans.LocSiteLiteVoBean)vo.getLocSite().getBean(map);
		this.location = vo.getLocation() == null ? null : (ims.core.vo.beans.LocationLiteVoBean)vo.getLocation().getBean(map);
		this.canbenull = vo.getCanBeNull();
		this.sex = vo.getSex() == null ? null : (ims.vo.LookupInstanceBean)vo.getSex().getBean();
		this.outcome = vo.getOutcome() == null ? null : (ims.vo.LookupInstanceBean)vo.getOutcome().getBean();
		this.searchtype = vo.getSearchType();
		this.searchby = vo.getSearchBy();
		this.displayfields = vo.getDisplayFields();
		this.referralby = vo.getReferralBy() == null ? null : (ims.vo.LookupInstanceBean)vo.getReferralBy().getBean();
		this.displaytext = vo.getDisplayText();
	}

	public ims.admin.vo.ReportSeedParsedVo buildVo()
	{
		return this.buildVo(new ims.vo.ValueObjectBeanMap());
	}

	public ims.admin.vo.ReportSeedParsedVo buildVo(ims.vo.ValueObjectBeanMap map)
	{
		ims.admin.vo.ReportSeedParsedVo vo = null;
		if(map != null)
			vo = (ims.admin.vo.ReportSeedParsedVo)map.getValueObject(this);
		if(vo == null)
		{
			vo = new ims.admin.vo.ReportSeedParsedVo();
			map.addValueObject(this, vo);
			vo.populate(map, this);
		}
		return vo;
	}

	public String getName()
	{
		return this.name;
	}
	public void setName(String value)
	{
		this.name = value;
	}
	public String getType()
	{
		return this.type;
	}
	public void setType(String value)
	{
		this.type = value;
	}
	public String getValue()
	{
		return this.value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	public String getBOName()
	{
		return this.boname;
	}
	public void setBOName(String value)
	{
		this.boname = value;
	}
	public String getBOField()
	{
		return this.bofield;
	}
	public void setBOField(String value)
	{
		this.bofield = value;
	}
	public ims.core.vo.beans.GpShortVoBean getGP()
	{
		return this.gp;
	}
	public void setGP(ims.core.vo.beans.GpShortVoBean value)
	{
		this.gp = value;
	}
	public ims.core.vo.beans.HcpLiteVoBean getHCP()
	{
		return this.hcp;
	}
	public void setHCP(ims.core.vo.beans.HcpLiteVoBean value)
	{
		this.hcp = value;
	}
	public ims.core.vo.beans.MemberOfStaffShortVoBean getMOS()
	{
		return this.mos;
	}
	public void setMOS(ims.core.vo.beans.MemberOfStaffShortVoBean value)
	{
		this.mos = value;
	}
	public ims.core.vo.beans.OrgShortVoBean getOrganisation()
	{
		return this.organisation;
	}
	public void setOrganisation(ims.core.vo.beans.OrgShortVoBean value)
	{
		this.organisation = value;
	}
	public ims.core.vo.beans.LocSiteLiteVoBean getLocSite()
	{
		return this.locsite;
	}
	public void setLocSite(ims.core.vo.beans.LocSiteLiteVoBean value)
	{
		this.locsite = value;
	}
	public ims.core.vo.beans.LocationLiteVoBean getLocation()
	{
		return this.location;
	}
	public void setLocation(ims.core.vo.beans.LocationLiteVoBean value)
	{
		this.location = value;
	}
	public Boolean getCanBeNull()
	{
		return this.canbenull;
	}
	public void setCanBeNull(Boolean value)
	{
		this.canbenull = value;
	}
	public ims.vo.LookupInstanceBean getSex()
	{
		return this.sex;
	}
	public void setSex(ims.vo.LookupInstanceBean value)
	{
		this.sex = value;
	}
	public ims.vo.LookupInstanceBean getOutcome()
	{
		return this.outcome;
	}
	public void setOutcome(ims.vo.LookupInstanceBean value)
	{
		this.outcome = value;
	}
	public String getSearchType()
	{
		return this.searchtype;
	}
	public void setSearchType(String value)
	{
		this.searchtype = value;
	}
	public String[] getSearchBy()
	{
		return this.searchby;
	}
	public void setSearchBy(String[] value)
	{
		this.searchby = value;
	}
	public String[] getDisplayFields()
	{
		return this.displayfields;
	}
	public void setDisplayFields(String[] value)
	{
		this.displayfields = value;
	}
	public ims.vo.LookupInstanceBean getReferralBy()
	{
		return this.referralby;
	}
	public void setReferralBy(ims.vo.LookupInstanceBean value)
	{
		this.referralby = value;
	}
	public String getDisplayText()
	{
		return this.displaytext;
	}
	public void setDisplayText(String value)
	{
		this.displaytext = value;
	}

	private String name;
	private String type;
	private String value;
	private String boname;
	private String bofield;
	private ims.core.vo.beans.GpShortVoBean gp;
	private ims.core.vo.beans.HcpLiteVoBean hcp;
	private ims.core.vo.beans.MemberOfStaffShortVoBean mos;
	private ims.core.vo.beans.OrgShortVoBean organisation;
	private ims.core.vo.beans.LocSiteLiteVoBean locsite;
	private ims.core.vo.beans.LocationLiteVoBean location;
	private Boolean canbenull;
	private ims.vo.LookupInstanceBean sex;
	private ims.vo.LookupInstanceBean outcome;
	private String searchtype;
	private String[] searchby;
	private String[] displayfields;
	private ims.vo.LookupInstanceBean referralby;
	private String displaytext;
}
