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

package ims.assessment.vo.beans;

public class UserAssessmentShortVoBean extends ims.vo.ValueObjectBean
{
	public UserAssessmentShortVoBean()
	{
	}
	public UserAssessmentShortVoBean(ims.assessment.vo.UserAssessmentShortVo vo)
	{
		this.id = vo.getBoId();
		this.version = vo.getBoVersion();
		this.name = vo.getName();
		this.description = vo.getDescription();
		this.assessmenttype = vo.getAssessmentType() == null ? null : (ims.vo.LookupInstanceBean)vo.getAssessmentType().getBean();
		this.activestatus = vo.getActiveStatus() == null ? null : (ims.vo.LookupInstanceBean)vo.getActiveStatus().getBean();
		this.helpurl = vo.getHelpURL();
		this.systeminfo = vo.getSystemInfo() == null ? null : (ims.vo.SysInfoBean)vo.getSystemInfo().getBean();
		this.scoringmethod = vo.getScoringMethod() == null ? null : (ims.vo.LookupInstanceBean)vo.getScoringMethod().getBean();
		this.isflatview = vo.getIsFlatView();
		this.category = vo.getCategory() == null ? null : (ims.vo.LookupInstanceBean)vo.getCategory().getBean();
		this.ismultiplegroup = vo.getIsMultipleGroup();
		this.groupquestionwidth = vo.getGroupQuestionWidth();
		this.storeprintedassessment = vo.getStorePrintedAssessment();
	}

	public void populate(ims.vo.ValueObjectBeanMap map, ims.assessment.vo.UserAssessmentShortVo vo)
	{
		this.id = vo.getBoId();
		this.version = vo.getBoVersion();
		this.name = vo.getName();
		this.description = vo.getDescription();
		this.assessmenttype = vo.getAssessmentType() == null ? null : (ims.vo.LookupInstanceBean)vo.getAssessmentType().getBean();
		this.activestatus = vo.getActiveStatus() == null ? null : (ims.vo.LookupInstanceBean)vo.getActiveStatus().getBean();
		this.helpurl = vo.getHelpURL();
		this.systeminfo = vo.getSystemInfo() == null ? null : (ims.vo.SysInfoBean)vo.getSystemInfo().getBean();
		this.scoringmethod = vo.getScoringMethod() == null ? null : (ims.vo.LookupInstanceBean)vo.getScoringMethod().getBean();
		this.isflatview = vo.getIsFlatView();
		this.category = vo.getCategory() == null ? null : (ims.vo.LookupInstanceBean)vo.getCategory().getBean();
		this.ismultiplegroup = vo.getIsMultipleGroup();
		this.groupquestionwidth = vo.getGroupQuestionWidth();
		this.storeprintedassessment = vo.getStorePrintedAssessment();
	}

	public ims.assessment.vo.UserAssessmentShortVo buildVo()
	{
		return this.buildVo(new ims.vo.ValueObjectBeanMap());
	}

	public ims.assessment.vo.UserAssessmentShortVo buildVo(ims.vo.ValueObjectBeanMap map)
	{
		ims.assessment.vo.UserAssessmentShortVo vo = null;
		if(map != null)
			vo = (ims.assessment.vo.UserAssessmentShortVo)map.getValueObject(this);
		if(vo == null)
		{
			vo = new ims.assessment.vo.UserAssessmentShortVo();
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
	public String getDescription()
	{
		return this.description;
	}
	public void setDescription(String value)
	{
		this.description = value;
	}
	public ims.vo.LookupInstanceBean getAssessmentType()
	{
		return this.assessmenttype;
	}
	public void setAssessmentType(ims.vo.LookupInstanceBean value)
	{
		this.assessmenttype = value;
	}
	public ims.vo.LookupInstanceBean getActiveStatus()
	{
		return this.activestatus;
	}
	public void setActiveStatus(ims.vo.LookupInstanceBean value)
	{
		this.activestatus = value;
	}
	public String getHelpURL()
	{
		return this.helpurl;
	}
	public void setHelpURL(String value)
	{
		this.helpurl = value;
	}
	public ims.vo.SysInfoBean getSystemInfo()
	{
		return this.systeminfo;
	}
	public void setSystemInfo(ims.vo.SysInfoBean value)
	{
		this.systeminfo = value;
	}
	public ims.vo.LookupInstanceBean getScoringMethod()
	{
		return this.scoringmethod;
	}
	public void setScoringMethod(ims.vo.LookupInstanceBean value)
	{
		this.scoringmethod = value;
	}
	public Boolean getIsFlatView()
	{
		return this.isflatview;
	}
	public void setIsFlatView(Boolean value)
	{
		this.isflatview = value;
	}
	public ims.vo.LookupInstanceBean getCategory()
	{
		return this.category;
	}
	public void setCategory(ims.vo.LookupInstanceBean value)
	{
		this.category = value;
	}
	public Boolean getIsMultipleGroup()
	{
		return this.ismultiplegroup;
	}
	public void setIsMultipleGroup(Boolean value)
	{
		this.ismultiplegroup = value;
	}
	public Integer getGroupQuestionWidth()
	{
		return this.groupquestionwidth;
	}
	public void setGroupQuestionWidth(Integer value)
	{
		this.groupquestionwidth = value;
	}
	public Boolean getStorePrintedAssessment()
	{
		return this.storeprintedassessment;
	}
	public void setStorePrintedAssessment(Boolean value)
	{
		this.storeprintedassessment = value;
	}

	private Integer id;
	private int version;
	private String name;
	private String description;
	private ims.vo.LookupInstanceBean assessmenttype;
	private ims.vo.LookupInstanceBean activestatus;
	private String helpurl;
	private ims.vo.SysInfoBean systeminfo;
	private ims.vo.LookupInstanceBean scoringmethod;
	private Boolean isflatview;
	private ims.vo.LookupInstanceBean category;
	private Boolean ismultiplegroup;
	private Integer groupquestionwidth;
	private Boolean storeprintedassessment;
}
