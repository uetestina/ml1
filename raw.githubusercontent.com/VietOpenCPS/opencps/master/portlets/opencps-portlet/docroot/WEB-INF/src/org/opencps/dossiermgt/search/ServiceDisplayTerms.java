/**
* OpenCPS is the open source Core Public Services software
* Copyright (C) 2016-present OpenCPS community

* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>
*/

package org.opencps.dossiermgt.search;

import javax.portlet.PortletRequest;

import com.liferay.portal.kernel.dao.search.DisplayTerms;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;

/**
 * @author trungnt
 */
public class ServiceDisplayTerms extends DisplayTerms {

	public static final String COMPANY_ID = "companyId";

	public static final String CREATE_DATE = "createDate";

	public static final String DOMAIN_CODE = "domainCode";

	public static final String GOVAGENCY_INDEX = "govAgencyIndex";

	public static final String GOVAGENCY_CODE = "govAgencyCode";

	public static final String GOVAGENCY_NAME = "govAgencyName";

	public static final String GROUP_ID = "groupId";

	public static final String MODIFIED_DATE = "modifiedDate";

	public static final String SERVICE_CONFIG_ID = "serviceConfigId";

	public static final String SERVICE_DOMAIN_INDEX = "serviceDomainIndex";

	public static final String SERVICE_INFO_ID = "serviceInfoId";

	public static final String SERVICE_NAME = "serviceName";

	public static final String USER_ID = "userId";

	public ServiceDisplayTerms(PortletRequest portletRequest) {
		super(
			portletRequest);

		govAgencyCode = ParamUtil
			.getString(portletRequest, GOVAGENCY_CODE);

		govAgencyName = ParamUtil
			.getString(portletRequest, GOVAGENCY_NAME);

		serviceInfoId = ParamUtil
			.getLong(portletRequest, SERVICE_INFO_ID);

		serviceConfigId = ParamUtil
			.getLong(portletRequest, SERVICE_CONFIG_ID);

		serviceName = ParamUtil
			.getString(portletRequest, SERVICE_NAME);

		domainCode = ParamUtil
			.getString(portletRequest, DOMAIN_CODE);

		serviceDomainIndex = ParamUtil
			.getString(portletRequest, SERVICE_DOMAIN_INDEX);

		govAgencyIndex = ParamUtil
			.getString(portletRequest, GOVAGENCY_INDEX);

		userId = ParamUtil
			.getLong(portletRequest, USER_ID);

		groupId = setGroupId(portletRequest);
	}

	public long getCompanyId() {

		return companyId;
	}

	public String getDomainCode() {

		return domainCode;
	}

	public String getGovAgencyCode() {

		return govAgencyCode;
	}

	public String getGovAgencyIndex() {

		return govAgencyIndex;
	}

	public String getGovAgencyName() {

		return govAgencyName;
	}

	public long getGroupId() {

		return groupId;
	}

	public long getServiceConfigId() {

		return serviceConfigId;
	}

	public String getServiceDomainIndex() {

		return serviceDomainIndex;
	}

	public long getServiceInfoId() {

		return serviceInfoId;
	}

	public String getServiceName() {

		return serviceName;
	}

	public long getUserId() {

		return userId;
	}

	public void setCompanyId(long companyId) {

		this.companyId = companyId;
	}
	public void setDomainCode(String domainCode) {

		this.domainCode = domainCode;
	}
	public void setGovAgencyCode(String govAgencyCode) {

		this.govAgencyCode = govAgencyCode;
	}
	public void setGovAgencyIndex(String govAgencyIndex) {

		this.govAgencyIndex = govAgencyIndex;
	}
	public void setGovAgencyName(String govAgencyName) {

		this.govAgencyName = govAgencyName;
	}
	public long setGroupId(PortletRequest portletRequest) {

		groupId = ParamUtil
			.getLong(portletRequest, GROUP_ID);

		if (groupId != 0) {
			return groupId;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay) portletRequest
			.getAttribute(WebKeys.THEME_DISPLAY);

		return themeDisplay
			.getScopeGroupId();
	}
	public void setServiceConfigId(long serviceConfigId) {

		this.serviceConfigId = serviceConfigId;
	}
	public void setServiceDomainIndex(String serviceDomainIndex) {

		this.serviceDomainIndex = serviceDomainIndex;
	}
	public void setServiceInfoId(long serviceInfoId) {

		this.serviceInfoId = serviceInfoId;
	}
	public void setServiceName(String serviceName) {

		this.serviceName = serviceName;
	}
	public void setUserId(long userId) {

		this.userId = userId;
	}
	
	protected long companyId;

	protected String domainCode;

	protected String govAgencyCode;

	protected String govAgencyIndex;

	protected String govAgencyName;

	protected long groupId;

	protected long serviceConfigId;

	protected String serviceDomainIndex;

	protected long serviceInfoId;

	protected String serviceName;

	protected long userId;

}
