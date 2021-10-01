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

package org.opencps.accountmgt.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencps.accountmgt.NoSuchBusinessException;
import org.opencps.accountmgt.model.Business;
import org.opencps.accountmgt.model.BusinessDomain;
import org.opencps.accountmgt.service.base.BusinessLocalServiceBaseImpl;
import org.opencps.util.DLFolderUtil;
import org.opencps.util.DateTimeUtil;
import org.opencps.util.PortletConstants;
import org.opencps.util.PortletPropsValues;
import org.opencps.util.PortletUtil;
import org.opencps.util.WebKeys;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.Address;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.EmailAddress;
import com.liferay.portal.model.ListTypeConstants;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.OrganizationConstants;
import com.liferay.portal.model.Phone;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.model.Website;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.ContactLocalServiceUtil;
import com.liferay.portal.service.OrganizationLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserGroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.announcements.model.AnnouncementsDelivery;
import com.liferay.portlet.documentlibrary.model.DLFolder;
import com.liferay.portlet.documentlibrary.service.DLAppServiceUtil;
import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;
import com.liferay.util.PwdGenerator;

/**
 * The implementation of the business local service.
 * <p>
 * All custom service methods should be put in this class. Whenever methods are
 * added, rerun ServiceBuilder to copy their definitions into the
 * {@link org.opencps.accountmgt.service.BusinessLocalService} interface.
 * <p>
 * This is a local service. Methods of this service will not have security
 * checks based on the propagated JAAS credentials because this service can only
 * be accessed from within the same VM.
 * </p>
 *
 * @author khoavd
 * @author trungnt
 * @see org.opencps.accountmgt.service.base.BusinessLocalServiceBaseImpl
 * @see org.opencps.accountmgt.service.BusinessLocalServiceUtil
 */
public class BusinessLocalServiceImpl extends BusinessLocalServiceBaseImpl {

	public Business addBusiness(String fullName, String enName,
			String shortName, String businessType, String idNumber,
			String address, String cityCode, String districtCode,
			String wardCode, String cityName, String districtName,
			String wardName, String telNo, String email,
			String representativeName, String representativeRole,
			String[] businessDomainCodes, int birthDateDay, int birthDateMonth,
			int birthDateYear, long repositoryId, String sourceFileName,
			String contentType, String title, InputStream inputStream,
			long size, ServiceContext serviceContext) throws SystemException,
			PortalException {

		Role roleDefault = RoleLocalServiceUtil.getRole(
				serviceContext.getCompanyId(),
				WebKeys.CITIZEN_BUSINESS_ROLE_NAME);

		long businessId = counterLocalService.increment(Business.class
				.getName());

		Business business = businessPersistence.create(businessId);

		Date now = new Date();

		PortletUtil.SplitName spn = PortletUtil.splitName(fullName);

		boolean autoPassword = true;
		boolean autoScreenName = true;
		boolean sendEmail = false;

		long[] groupIds = null;
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;

		String password1 = null;
		String password2 = null;
		String screenName = null;

		// add default role
		if (Validator.isNotNull(roleDefault)) {

			roleIds = new long[] { roleDefault.getRoleId() };
		}

		UserGroup userGroup = null;
		try {
			userGroup = UserGroupLocalServiceUtil.getUserGroup(
					serviceContext.getCompanyId(),
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS);
		} catch (Exception e) {
			_log.error(e);
		}

		if (userGroup == null) {
			userGroup = UserGroupLocalServiceUtil.addUserGroup(
					serviceContext.getUserId(), serviceContext.getCompanyId(),
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS,
					StringPool.BLANK, serviceContext);

		}

		if (userGroup != null) {
			userGroupIds = new long[] { userGroup.getUserGroupId() };
		}

		password1 = PwdGenerator.getPassword();
		password2 = password1;

		Role adminRole = RoleLocalServiceUtil.getRole(
				serviceContext.getCompanyId(), "Administrator");
		List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole
				.getRoleId());

		PrincipalThreadLocal.setName(adminUsers.get(0).getUserId());
		PermissionChecker permissionChecker;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(adminUsers
					.get(0));
			PermissionThreadLocal.setPermissionChecker(permissionChecker);

			serviceContext.setUserId(adminUsers.get(0).getUserId());
		} catch (Exception e) {
			_log.error(e);
		}

		User mappingUser = userService.addUserWithWorkflow(
				serviceContext.getCompanyId(), autoPassword, password1,
				password2, autoScreenName, screenName, email, 0L,
				StringPool.BLANK, LocaleUtil.getDefault(), spn.getFirstName(),
				spn.getMidName(), spn.getLastName(), 0, 0, true,
				birthDateMonth, birthDateDay, birthDateYear, "Business",
				groupIds, organizationIds, roleIds, userGroupIds,
				new ArrayList<Address>(), new ArrayList<EmailAddress>(),
				new ArrayList<Phone>(), new ArrayList<Website>(),
				new ArrayList<AnnouncementsDelivery>(), sendEmail,
				serviceContext);

		int status = WorkflowConstants.STATUS_INACTIVE;

		Organization groupOrgBusiness = null;

		try {
			groupOrgBusiness = organizationPersistence.findByC_N(
					serviceContext.getCompanyId(),
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS);
		} catch (Exception e) {
			_log.error(e);
		}

		if (groupOrgBusiness == null) {
			groupOrgBusiness = OrganizationLocalServiceUtil.addOrganization(
					mappingUser.getUserId(),
					OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID,
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS,
					OrganizationConstants.TYPE_REGULAR_ORGANIZATION, 0, 0,
					ListTypeConstants.ORGANIZATION_STATUS_DEFAULT,
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS, true,
					serviceContext);
		}

		Organization org = OrganizationLocalServiceUtil.addOrganization(
				mappingUser.getUserId(), groupOrgBusiness.getOrganizationId(),
				fullName +

				StringPool.OPEN_PARENTHESIS + idNumber
						+ StringPool.CLOSE_PARENTHESIS,
				OrganizationConstants.TYPE_REGULAR_ORGANIZATION, 0, 0,
				ListTypeConstants.ORGANIZATION_STATUS_DEFAULT, enName, true,
				serviceContext);
		userService.addOrganizationUsers(org.getOrganizationId(),
				new long[] { mappingUser.getUserId() });

		mappingUser = userService.updateStatus(mappingUser.getUserId(), status);

		String[] folderNames = new String[] {
				PortletConstants.DestinationRoot.BUSINESS.toString(), cityName,
				districtName, wardName, String.valueOf(mappingUser.getUserId()) };

		String destination = PortletUtil.getDestinationFolder(folderNames);

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		FileEntry fileEntry = null;

		if (size > 0 && inputStream != null) {

			DLFolder dlFolder = DLFolderUtil.getTargetFolder(
					mappingUser.getUserId(), serviceContext.getScopeGroupId(),
					repositoryId, false, 0, destination, StringPool.BLANK,
					false, serviceContext);
			fileEntry = DLAppServiceUtil.addFileEntry(repositoryId,
					dlFolder.getFolderId(), sourceFileName, contentType, title,
					StringPool.BLANK, StringPool.BLANK, inputStream, size,
					serviceContext);
		}

		business.setAccountStatus(PortletConstants.ACCOUNT_STATUS_REGISTERED);
		business.setAddress(address);

		business.setAttachFile(fileEntry != null ? fileEntry.getFileEntryId()
				: 0);
		business.setBusinessType(businessType);
		business.setCityCode(cityCode);
		business.setCompanyId(serviceContext.getCompanyId());
		business.setCreateDate(now);
		business.setDistrictCode(districtCode);
		business.setEmail(email);
		business.setEnName(enName);
		business.setGroupId(serviceContext.getScopeGroupId());
		business.setIdNumber(idNumber);

		business.setMappingOrganizationId(org != null ? org.getOrganizationId()
				: 0L);

		business.setMappingUserId(mappingUser.getUserId());
		business.setModifiedDate(now);
		business.setName(fullName);
		business.setRepresentativeName(representativeName);
		business.setRepresentativeRole(representativeRole);
		business.setShortName(shortName);
		business.setTelNo(telNo);
		business.setUserId(mappingUser.getUserId());

		business.setUuid(PortalUUIDUtil.generate());
		business.setWardCode(wardCode);

		business = businessPersistence.update(business);

		if (businessDomainCodes != null && businessDomainCodes.length > 0) {
			businessDomainLocalService.addBusinessDomains(businessId,
					businessDomainCodes);
		}

		return business;
	}

	public void deleteBusinessByBusinessId(long businessId)

	throws SystemException, PortalException {

		Business business = businessPersistence.findByPrimaryKey(businessId);

		long fileEntryId = business.getAttachFile();

		long mappingUserId = business.getMappingUserId();

		long mappingOrgId = business.getMappingOrganizationId();

		if (mappingUserId > 0) {
			User mappingUser = null;
			try {
				mappingUser = userLocalService.getUser(mappingUserId);
			} catch (Exception e) {
				_log.error(e);
			}

			if (mappingUser != null) {
				userLocalService.deleteUser(mappingUserId);
			}
		}

		if (fileEntryId > 0) {

			try {
				FileEntry fileEntry = DLAppServiceUtil
						.getFileEntry(fileEntryId);
				DLFolderLocalServiceUtil.deleteFolder(fileEntry.getFolderId());

			} catch (Exception e) {
				_log.error(e);
			}

		}

		if (mappingOrgId > 0) {
			Organization organization = null;
			try {
				organization = organizationLocalService
						.getOrganization(mappingOrgId);
			} catch (Exception e) {

				_log.error(e);
			}

			if (organization != null) {
				organizationLocalService.deleteOrganization(mappingOrgId);
			}

		}

		List<BusinessDomain> businessDomains = new ArrayList<BusinessDomain>();

		businessDomains = businessDomainPersistence
				.findByBusinessId(businessId);

		for (BusinessDomain businessDomain : businessDomains) {
			businessDomainPersistence.remove(businessDomain);
		}

		businessPersistence.remove(business);

	}

	public Business getBusiness(long mappingUserId) throws SystemException,
			NoSuchBusinessException {

		return businessPersistence.findByMappingUserId(mappingUserId);
	}

	public Business getBusiness(String email) throws NoSuchBusinessException,
			SystemException {

		return businessPersistence.findByEmail(email);
	}
	
	public Business getBusinessByIdNumber(String idNumber)
			throws NoSuchBusinessException, SystemException {

			return businessPersistence.findByIdNumber(idNumber);
		}

	public Business getBusinessByUUID(String uuid) throws SystemException,
			NoSuchBusinessException {

		return businessPersistence.findByUUID(uuid);
	}
	
	public Business updateBusiness(
			long businessId, String fullName, String enName, String shortName,
			String businessType, String idNumber, String address, String cityCode,
			String districtCode, String wardCode, String cityName,
			String districtName, String wardName, String telNo,
			String representativeName, String representativeRole,
			String[] businessDomainCodes, boolean isChangePassword,
			String password, String rePassword, long repositoryId,
			ServiceContext serviceContext, Date dateOfIdNumber)
			throws SystemException, PortalException {

			Business business = businessPersistence.findByPrimaryKey(businessId);

			User mappingUser = userLocalService
					.getUser(business.getMappingUserId());

			Date now = new Date();

			if (mappingUser != null) {
				// Reset password
				if (isChangePassword) {
					userLocalService.updateModifiedDate(mappingUser.getUserId(), now);
					
					mappingUser = userLocalService.updatePassword(
							mappingUser.getUserId(), rePassword, rePassword, false);
				}

				if ((cityCode != business.getCityCode()
						|| districtCode != business.getDistrictCode() || wardCode != business
						.getWardCode()) && business.getAttachFile() > 0) {
					// Move image folder

					String[] newFolderNames = new String[] {
							PortletConstants.DestinationRoot.BUSINESS.toString(),
							cityName, districtName, wardName };

					String destination = PortletUtil
							.getDestinationFolder(newFolderNames);

					DLFolder parentFolder = DLFolderUtil
							.getTargetFolder(mappingUser.getUserId(),
									serviceContext.getScopeGroupId(), repositoryId,
									false, 0, destination, StringPool.BLANK, false,
									serviceContext);

					FileEntry fileEntry = DLAppServiceUtil.getFileEntry(business
							.getAttachFile());

					DLFolderLocalServiceUtil.moveFolder(mappingUser.getUserId(),
							fileEntry.getFolderId(), parentFolder.getFolderId(),
							serviceContext);
				}
			}

			Organization organization = organizationPersistence
					.findByPrimaryKey(business.getMappingOrganizationId());
			organization.setName(fullName + StringPool.OPEN_PARENTHESIS + idNumber
					+ StringPool.CLOSE_PARENTHESIS);
			organizationPersistence.update(organization);

			business.setAddress(address);

			business.setBusinessType(businessType);
			business.setCityCode(cityCode);
			business.setCompanyId(serviceContext.getCompanyId());
			business.setCreateDate(now);
			business.setDistrictCode(districtCode);
			business.setName(fullName);
			business.setEnName(enName);
			business.setGroupId(serviceContext.getScopeGroupId());
			business.setIdNumber(idNumber);

			business.setMappingUserId(mappingUser.getUserId());
			business.setModifiedDate(now);

			business.setRepresentativeName(representativeName);
			business.setRepresentativeRole(representativeRole);
			business.setShortName(shortName);
			business.setTelNo(telNo);
			business.setUserId(mappingUser.getUserId());
			business.setUuid(serviceContext.getUuid());
			business.setWardCode(wardCode);
			
			business.setDateOfIdNumber(dateOfIdNumber);
			
			business = businessPersistence.update(business);

			if (businessDomainCodes != null && businessDomainCodes.length > 0) {

				businessDomainLocalService.addBusinessDomains(businessId,
						businessDomainCodes);
			} else if (businessDomainCodes != null
					&& businessDomainCodes.length <= 0) {
				List<BusinessDomain> currentBusinessDomains = new ArrayList<BusinessDomain>();
				currentBusinessDomains = businessDomainPersistence
						.findByBusinessId(businessId);

				for (BusinessDomain bdm : currentBusinessDomains) {
					businessDomainPersistence.remove(bdm);
				}
			}

			return business;

		}

	public Business updateBusiness(
		long businessId, String fullName, String enName, String shortName,
		String businessType, String idNumber, String address, String cityCode,
		String districtCode, String wardCode, String cityName,
		String districtName, String wardName, String telNo,
		String representativeName, String representativeRole,
		String[] businessDomainCodes, boolean isChangePassword,
		String password, String rePassword, long repositoryId, String sourceFileName,
        String contentType,String title, InputStream inputStream,long size,
		ServiceContext serviceContext, Date dateOfIdNumber)
		throws SystemException, PortalException {

		Business business = businessPersistence.findByPrimaryKey(businessId);

		User mappingUser = userLocalService
				.getUser(business.getMappingUserId());

		Date now = new Date();

		if (mappingUser != null) {
			// Reset password
			if (isChangePassword) {
				userLocalService.updateModifiedDate(mappingUser.getUserId(), now);
				
				mappingUser = userLocalService.updatePassword(
						mappingUser.getUserId(), rePassword, rePassword, false);
			}

			if ((cityCode != business.getCityCode()
					|| districtCode != business.getDistrictCode() || wardCode != business
					.getWardCode()) && business.getAttachFile() > 0) {
				// Move image folder

				String[] newFolderNames = new String[] {
						PortletConstants.DestinationRoot.BUSINESS.toString(),
						cityName, districtName, wardName };

				String destination = PortletUtil
						.getDestinationFolder(newFolderNames);

				DLFolder parentFolder = DLFolderUtil
						.getTargetFolder(mappingUser.getUserId(),
								serviceContext.getScopeGroupId(), repositoryId,
								false, 0, destination, StringPool.BLANK, false,
								serviceContext);

				FileEntry fileEntry = DLAppServiceUtil.getFileEntry(business
						.getAttachFile());

				DLFolderLocalServiceUtil.moveFolder(mappingUser.getUserId(),
						fileEntry.getFolderId(), parentFolder.getFolderId(),
						serviceContext);
			}
		}

		Organization organization = organizationPersistence
				.findByPrimaryKey(business.getMappingOrganizationId());
		organization.setName(fullName + StringPool.OPEN_PARENTHESIS + idNumber
				+ StringPool.CLOSE_PARENTHESIS);
		organizationPersistence.update(organization);
		
		String[] folderNames = new String[] {
				PortletConstants.DestinationRoot.BUSINESS.toString(), cityName,
				districtName, wardName, String.valueOf(mappingUser.getUserId()) };

		String destination = PortletUtil.getDestinationFolder(folderNames);

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		FileEntry fileEntry = null;

		if (size > 0 && inputStream != null) {
			
			if(business.getSignImageId()>0) {
				DLAppServiceUtil.deleteFileEntry(business.getSignImageId());
			}

			DLFolder dlFolder = DLFolderUtil.getTargetFolder(
					mappingUser.getUserId(), serviceContext.getScopeGroupId(),
					repositoryId, false, 0, destination, StringPool.BLANK,
					false, serviceContext);
			fileEntry = DLAppServiceUtil.addFileEntry(repositoryId,
					dlFolder.getFolderId(), sourceFileName, contentType, title,
					StringPool.BLANK, StringPool.BLANK, inputStream, size,
					serviceContext);
		}

		business.setAddress(address);

		business.setBusinessType(businessType);
		business.setCityCode(cityCode);
		business.setCompanyId(serviceContext.getCompanyId());
		business.setCreateDate(now);
		business.setDistrictCode(districtCode);
		business.setName(fullName);
		business.setEnName(enName);
		business.setGroupId(serviceContext.getScopeGroupId());
		business.setIdNumber(idNumber);

		business.setMappingUserId(mappingUser.getUserId());
		business.setModifiedDate(now);

		business.setRepresentativeName(representativeName);
		business.setRepresentativeRole(representativeRole);
		business.setShortName(shortName);
		business.setTelNo(telNo);
		business.setUserId(mappingUser.getUserId());
		business.setUuid(serviceContext.getUuid());
		business.setWardCode(wardCode);
		
		business.setDateOfIdNumber(dateOfIdNumber);
		
		business.setSignImageId(fileEntry != null ? fileEntry.getFileEntryId()
				: 0);
		
		business = businessPersistence.update(business);

		if (businessDomainCodes != null && businessDomainCodes.length > 0) {

			businessDomainLocalService.addBusinessDomains(businessId,
					businessDomainCodes);
		} else if (businessDomainCodes != null
				&& businessDomainCodes.length <= 0) {
			List<BusinessDomain> currentBusinessDomains = new ArrayList<BusinessDomain>();
			currentBusinessDomains = businessDomainPersistence
					.findByBusinessId(businessId);

			for (BusinessDomain bdm : currentBusinessDomains) {
				businessDomainPersistence.remove(bdm);
			}
		}

		return business;

	}
	
	public Business updateBusiness(long businessId, String fullName,
			String enName, String shortName, String businessType,
			String idNumber, String address, String cityCode,
			String districtCode, String wardCode, String cityName,
			String districtName, String wardName, String telNo,
			String representativeName, String representativeRole,
			String[] businessDomainCodes, int birthDateDay, int birthDateMonth,
			int birthDateYear, long repositoryId, ServiceContext serviceContext)
			throws SystemException, PortalException {

		Business business = businessPersistence.findByPrimaryKey(businessId);

		User mappingUser = userLocalService
				.getUser(business.getMappingUserId());

		Date now = new Date();

		Date birthDate = DateTimeUtil.getDate(birthDateDay, birthDateMonth,
				birthDateYear);

		if (mappingUser != null) {

			if ((cityCode != business.getCityCode()
					|| districtCode != business.getDistrictCode() || wardCode != business
					.getWardCode()) && business.getAttachFile() > 0) {
				// Move image folder

				String[] newFolderNames = new String[] {
						PortletConstants.DestinationRoot.BUSINESS.toString(),
						cityName, districtName, wardName };

				String destination = PortletUtil
						.getDestinationFolder(newFolderNames);

				DLFolder parentFolder = DLFolderUtil
						.getTargetFolder(mappingUser.getUserId(),
								serviceContext.getScopeGroupId(), repositoryId,
								false, 0, destination, StringPool.BLANK, false,
								serviceContext);

				FileEntry fileEntry = DLAppServiceUtil.getFileEntry(business
						.getAttachFile());

				DLFolderLocalServiceUtil.moveFolder(mappingUser.getUserId(),
						fileEntry.getFolderId(), parentFolder.getFolderId(),
						serviceContext);

			}

			// Change user name
			if (!fullName.equals(business.getName())) {

				PortletUtil.SplitName spn = PortletUtil.splitName(fullName);

				mappingUser.setFirstName(spn.getFirstName());
				mappingUser.setLastName(spn.getLastName());
				mappingUser.setMiddleName(spn.getMidName());
			}

			mappingUser = userLocalService.updateUser(mappingUser);

			// update birth date

			Contact contact = ContactLocalServiceUtil.getContact(mappingUser
					.getContactId());

			if (contact != null) {
				contact.setBirthday(birthDate);
				contact = ContactLocalServiceUtil.updateContact(contact);
			}
		}

		Organization organization = organizationPersistence
				.findByPrimaryKey(business.getMappingOrganizationId());
		organization.setName(fullName + StringPool.OPEN_PARENTHESIS + idNumber
				+ StringPool.CLOSE_PARENTHESIS);

		organizationPersistence.update(organization);

		business.setAddress(address);

		business.setBusinessType(businessType);
		business.setCityCode(cityCode);

		business.setDistrictCode(districtCode);

		business.setEnName(enName);

		business.setIdNumber(idNumber);

		business.setMappingUserId(mappingUser.getUserId());
		business.setModifiedDate(now);

		business.setRepresentativeName(representativeName);
		business.setRepresentativeRole(representativeRole);
		business.setShortName(shortName);
		business.setTelNo(telNo);

		business.setWardCode(wardCode);

		business = businessPersistence.update(business);

		if (businessDomainCodes != null && businessDomainCodes.length > 0) {

			businessDomainLocalService.addBusinessDomains(businessId,
					businessDomainCodes);
		} else if (businessDomainCodes != null
				&& businessDomainCodes.length <= 0) {
			List<BusinessDomain> currentBusinessDomains = new ArrayList<BusinessDomain>();
			currentBusinessDomains = businessDomainPersistence
					.findByBusinessId(businessId);

			for (BusinessDomain bdm : currentBusinessDomains) {

				businessDomainPersistence.remove(bdm);
			}
		}
		return business;
	}

	public Business updateStatus(long businessId, long userId, int accountStatus)

	throws SystemException, PortalException {

		Business business = businessPersistence.findByPrimaryKey(businessId);

		int userStatus = WorkflowConstants.STATUS_INACTIVE;

		if (accountStatus == PortletConstants.ACCOUNT_STATUS_APPROVED) {
			userStatus = WorkflowConstants.STATUS_APPROVED;
		}

		if (business.getMappingUserId() > 0) {

			userLocalService.updateStatus(business.getMappingUserId(),
					userStatus);

		}

		business.setUserId(userId);
		business.setModifiedDate(new Date());
		business.setAccountStatus(accountStatus);

		return businessPersistence.update(business);
	}

	public List<Business> getBusinesses(int start, int end,
			OrderByComparator odc) throws SystemException {

		return businessPersistence.findAll(start, end, odc);
	}

	public List<Business> getBusinesses(long groupId, int accountStatus)

	throws SystemException {

		return businessPersistence.findByG_S(groupId, accountStatus);
	}

	public List<Business> getBusinesses(long groupId, String name)

	throws SystemException {

		return businessPersistence.findByG_N(groupId, name);
	}

	public List<Business> getBusinesses(long groupId, String name,
			int accountStatus) throws SystemException {

		return businessPersistence.findByG_N_S(groupId, name, accountStatus);
	}

	public int countAll() throws SystemException {

		return businessPersistence.countAll();
	}

	public int countByG_S(long groupId, int accountStatus)

	throws SystemException {

		return businessPersistence.countByG_S(groupId, accountStatus);
	}

	public Business getBymappingOrganizationId(long mappingOrganizationId)

	throws SystemException {

		return businessPersistence
				.fetchBymappingOrganizationId(mappingOrganizationId);
	}

	public Business getByMappingOrganizationId(long mappingOrganizationId)
			throws SystemException, NoSuchBusinessException {

		return businessPersistence
				.findBymappingOrganizationId(mappingOrganizationId);
	}

	public List<Business> searchBusiness(long groupId, String keywords,
			int accountStatus, String businessDomain, int start, int end)
			throws SystemException {

		return businessFinder.searchBusiness(groupId, keywords, accountStatus,
				businessDomain, start, end);
	}

	public int countBusiness(long groupId, String keywords, int accountStatus,
			String businessDomain) throws SystemException {

		return businessFinder.countBussiness(groupId, keywords, accountStatus,
				businessDomain);
	}
	
	public Business addBusiness(String fullName, String enName,
			String shortName, String businessType, String idNumber,
			String address, String cityCode, String districtCode,
			String wardCode, String cityName, String districtName,
			String wardName, String telNo, String email,
			String representativeName, String representativeRole,
			int birthDateDay, int birthDateMonth, int birthDateYear,
			long repositoryId, String sourceFileName, String contentType,
			String title, InputStream inputStream, long size, String password,
			Date dateOfIdNumber, ServiceContext serviceContext)
			throws SystemException, PortalException {

		Role roleDefault = RoleLocalServiceUtil.getRole(
				serviceContext.getCompanyId(),
				WebKeys.CITIZEN_BUSINESS_ROLE_NAME);

		long businessId = counterLocalService.increment(Business.class
				.getName());

		Business business = businessPersistence.create(businessId);

		Date now = new Date();

		PortletUtil.SplitName spn = PortletUtil.splitName(fullName);

		boolean autoPassword = true;
		boolean autoScreenName = true;
		boolean sendEmail = false;

		long[] groupIds = null;
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;

		String password1 = password;
		String password2 = password;
		String screenName = null;

		// add default role
		if (Validator.isNotNull(roleDefault)) {
			roleIds = new long[] { roleDefault.getRoleId() };
		}

		UserGroup userGroup = null;
		try {
			userGroup = UserGroupLocalServiceUtil.getUserGroup(
					serviceContext.getCompanyId(),
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS);
		} catch (Exception e) {
			_log.error(e);
		}

		if (userGroup == null) {
			userGroup = UserGroupLocalServiceUtil.addUserGroup(
					serviceContext.getUserId(), serviceContext.getCompanyId(),
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS,
					StringPool.BLANK, serviceContext);

		}

		if (userGroup != null) {
			userGroupIds = new long[] { userGroup.getUserGroupId() };
		}

		Role adminRole = RoleLocalServiceUtil.getRole(
				serviceContext.getCompanyId(), "Administrator");
		List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole
				.getRoleId());

		PrincipalThreadLocal.setName(adminUsers.get(0).getUserId());
		PermissionChecker permissionChecker;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(adminUsers
					.get(0));
			PermissionThreadLocal.setPermissionChecker(permissionChecker);

			serviceContext.setUserId(adminUsers.get(0).getUserId());
		} catch (Exception e) {
			_log.error(e);
		}

		User mappingUser = userService.addUserWithWorkflow(
				serviceContext.getCompanyId(), autoPassword, password1,
				password2, autoScreenName, screenName, email, 0L,
				StringPool.BLANK, LocaleUtil.getDefault(), spn.getFirstName(),
				spn.getMidName(), spn.getLastName(), 0, 0, true,
				birthDateMonth, birthDateDay, birthDateYear, "Business",
				groupIds, organizationIds, roleIds, userGroupIds,
				new ArrayList<Address>(), new ArrayList<EmailAddress>(),
				new ArrayList<Phone>(), new ArrayList<Website>(),
				new ArrayList<AnnouncementsDelivery>(), sendEmail,
				serviceContext);
		
		userService.updatePassword(mappingUser.getUserId(), password1, 
				password2, false);
		
		int status = WorkflowConstants.STATUS_INACTIVE;

		Organization groupOrgBusiness = null;

		try {
			groupOrgBusiness = organizationPersistence.findByC_N(
					serviceContext.getCompanyId(),
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS);
		} catch (Exception e) {
			_log.error(e);
		}

		if (groupOrgBusiness == null) {
			groupOrgBusiness = OrganizationLocalServiceUtil.addOrganization(
					mappingUser.getUserId(),
					OrganizationConstants.DEFAULT_PARENT_ORGANIZATION_ID,
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS,
					OrganizationConstants.TYPE_REGULAR_ORGANIZATION, 0, 0,
					ListTypeConstants.ORGANIZATION_STATUS_DEFAULT,
					PortletPropsValues.USERMGT_USERGROUP_NAME_BUSINESS, true,
					serviceContext);
		}

		Organization org = OrganizationLocalServiceUtil.addOrganization(
				mappingUser.getUserId(), groupOrgBusiness.getOrganizationId(),
				fullName +

				StringPool.OPEN_PARENTHESIS + idNumber
						+ StringPool.CLOSE_PARENTHESIS,
				OrganizationConstants.TYPE_REGULAR_ORGANIZATION, 0, 0,
				ListTypeConstants.ORGANIZATION_STATUS_DEFAULT, enName, true,
				serviceContext);
		userService.addOrganizationUsers(org.getOrganizationId(),
				new long[] { mappingUser.getUserId() });

		mappingUser = userService.updateStatus(mappingUser.getUserId(), 0);

		String[] folderNames = new String[] {
				PortletConstants.DestinationRoot.BUSINESS.toString(), cityName,
				districtName, wardName, String.valueOf(mappingUser.getUserId()) };

		String destination = PortletUtil.getDestinationFolder(folderNames);

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);

		FileEntry fileEntry = null;

		if (size > 0 && inputStream != null) {

			DLFolder dlFolder = DLFolderUtil.getTargetFolder(
					mappingUser.getUserId(), serviceContext.getScopeGroupId(),
					repositoryId, false, 0, destination, StringPool.BLANK,
					false, serviceContext);
			fileEntry = DLAppServiceUtil.addFileEntry(repositoryId,
					dlFolder.getFolderId(), sourceFileName, contentType, title,
					StringPool.BLANK, StringPool.BLANK, inputStream, size,
					serviceContext);
		}

		business.setAccountStatus(PortletConstants.ACCOUNT_STATUS_REGISTERED);
		business.setAddress(address);

		business.setAttachFile(fileEntry != null ? fileEntry.getFileEntryId()
				: 0);
		business.setBusinessType(businessType);
		business.setCityCode(cityCode);
		business.setCompanyId(serviceContext.getCompanyId());
		business.setCreateDate(now);
		business.setDistrictCode(districtCode);
		business.setEmail(email);
		business.setEnName(enName);
		business.setGroupId(serviceContext.getScopeGroupId());
		business.setIdNumber(idNumber);

		business.setMappingOrganizationId(org != null ? org.getOrganizationId()
				: 0L);

		business.setMappingUserId(mappingUser.getUserId());
		business.setModifiedDate(now);
		business.setName(fullName);
		business.setRepresentativeName(representativeName);
		business.setRepresentativeRole(representativeRole);
		business.setShortName(shortName);
		business.setTelNo(telNo);
		business.setUserId(mappingUser.getUserId());

		business.setUuid(PortalUUIDUtil.generate());
		business.setWardCode(wardCode);
		
		business.setDateOfIdNumber(dateOfIdNumber);

		business = businessPersistence.update(business);

		return business;
	}
	
	public Business updateBusiness(long businessId, String fullName,
			String enName, String shortName, String businessType,
			String idNumber, String address, String cityCode,
			String districtCode, String wardCode, String cityName,
			String districtName, String wardName, String telNo, String email,
			String representativeName, String representativeRole,
			long repositoryId, String sourceFileName, String contentType,
			String title, InputStream inputStream, long size, String password,
			Date dateOfIdNumber, ServiceContext serviceContext)
			throws SystemException, PortalException {

		Role adminRole = RoleLocalServiceUtil.getRole(
				serviceContext.getCompanyId(), "Administrator");
		List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole
				.getRoleId());

		PrincipalThreadLocal.setName(adminUsers.get(0).getUserId());
		PermissionChecker permissionChecker;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(adminUsers
					.get(0));
			PermissionThreadLocal.setPermissionChecker(permissionChecker);

			serviceContext.setUserId(adminUsers.get(0).getUserId());
		} catch (Exception e) {
			_log.error(e);
		}
		
		Business business = businessPersistence.findByPrimaryKey(businessId);

		User mappingUser = userLocalService
				.getUser(business.getMappingUserId());

		Date now = new Date();
		
		if (mappingUser != null) {
			
			userLocalService.updateEmailAddress(mappingUser.getUserId(),
					password, email, email);
			
			// Reset password
			/*if (isChangePassword) {
				userLocalService.updateModifiedDate(mappingUser.getUserId(), now);
				
				mappingUser = userLocalService.updatePassword(
						mappingUser.getUserId(), password, password, false);
			}*/

			if ((cityCode != business.getCityCode()
					|| districtCode != business.getDistrictCode() || wardCode != business
					.getWardCode()) && business.getAttachFile() > 0) {
				// Move image folder

				String[] newFolderNames = new String[] {
						PortletConstants.DestinationRoot.BUSINESS.toString(),
						cityName, districtName, wardName };

				String destination = PortletUtil
						.getDestinationFolder(newFolderNames);

				DLFolder parentFolder = DLFolderUtil
						.getTargetFolder(mappingUser.getUserId(),
								serviceContext.getScopeGroupId(), repositoryId,
								false, 0, destination, StringPool.BLANK, false,
								serviceContext);

				FileEntry fileEntry = DLAppServiceUtil.getFileEntry(business
						.getAttachFile());

				DLFolderLocalServiceUtil.moveFolder(mappingUser.getUserId(),
						fileEntry.getFolderId(), parentFolder.getFolderId(),
						serviceContext);
			}
		}

		Organization organization = organizationPersistence
				.findByPrimaryKey(business.getMappingOrganizationId());
		organization.setName(fullName + StringPool.OPEN_PARENTHESIS + idNumber
				+ StringPool.CLOSE_PARENTHESIS);
		organizationPersistence.update(organization);

		business.setAddress(address);

		business.setBusinessType(businessType);
		business.setCityCode(cityCode);
		business.setCompanyId(serviceContext.getCompanyId());
		business.setCreateDate(now);
		business.setDistrictCode(districtCode);
		business.setName(fullName);
		business.setEmail(email);
		business.setEnName(enName);
		business.setGroupId(serviceContext.getScopeGroupId());
		business.setIdNumber(idNumber);

		business.setMappingUserId(mappingUser.getUserId());
		business.setModifiedDate(now);

		business.setRepresentativeName(representativeName);
		business.setRepresentativeRole(representativeRole);
		business.setShortName(shortName);
		business.setTelNo(telNo);
		business.setUserId(mappingUser.getUserId());
		business.setUuid(serviceContext.getUuid());
		business.setWardCode(wardCode);
		
		business.setDateOfIdNumber(dateOfIdNumber);
		
		business = businessPersistence.update(business);

		return business;

	}

	private Log _log = LogFactoryUtil.getLog(BusinessLocalServiceImpl.class
			.getName());

}