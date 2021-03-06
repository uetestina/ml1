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
// This code was generated by Rory Fitzpatrick using IMS Development Environment (version 1.54 build 2705.14694)
// Copyright (C) 1995-2007 IMS MAXIMS plc. All rights reserved.

package ims.assessment.forms.assessmentfavourites;

import ims.assessment.forms.assessmentfavourites.GenForm.lyr1Layer.tabSearchContainer.Group1Enumeration;
import ims.assessment.forms.assessmentfavourites.GenForm.lyr1Layer.tabSearchContainer.grdAssessmentRow;
import ims.assessment.vo.GraphicAssessmentShortVo;
import ims.assessment.vo.GraphicAssessmentShortVoCollection;
import ims.assessment.vo.PatientAssessmentFolderVo;
import ims.assessment.vo.PatientAssessmentFolderVoCollection;
import ims.assessment.vo.UserAssessmentShortVo;
import ims.assessment.vo.UserAssessmentShortVoCollection;
import ims.assessment.vo.UserPatientAssessmentFavouritesVo;
import ims.core.resource.people.vo.HcpRefVo;
import ims.core.vo.lookups.PreActiveActiveInactiveStatus;
import ims.domain.exceptions.StaleObjectException;
import ims.framework.LayerBridge;
import ims.framework.controls.TreeNode;
import ims.framework.enumerations.DialogResult;
import ims.framework.enumerations.FormMode;
import ims.framework.exceptions.PresentationLogicException;
import ims.vo.ValueObject;

public class Logic extends BaseLogic
{
	private static final String	ROOT_FOLDER			= "Root";

	private static final long	serialVersionUID	= 1L;

	@Override
	protected void onFormOpen(Object[] args) throws ims.framework.exceptions.PresentationLogicException
	{
		initialize();
		open();
	}

	private void enableFavouritesContextMenu(boolean isChecked, boolean isTabFavourite)
	{
		if (getCurrentHcpId() == null)
		{
			form.getContextMenus().hideAllFavouritesMenuItems();
		}
		else
		{
			form.getContextMenus().getFavouritesAddNewFolderItem().setVisible(isTabFavourite);
			form.getContextMenus().getFavouritesAddItem().setVisible(isChecked && !isTabFavourite);

			TreeNode node = form.lyr1().tabFavourites().treFavourites().getSelectedNode();

			boolean isNodeRemovable = node != null && node.getNodes().size() == 0;
			boolean isFolderNodeEditable = node != null && node.getValue() == null;
			form.getContextMenus().getFavouritesRemoveFolderItem().setVisible(isTabFavourite && isNodeRemovable);
			form.getContextMenus().getFavouritesEditFolderItem().setVisible(isTabFavourite && isFolderNodeEditable);
		}
	}

	private void initialize()
	{
		if (engine.getPreviousNonDialogFormName().equals(form.getForms().ICP.ICPConfig.getName()))
		{
			form.lyr1().tabFavourites().setHeaderVisible(false);
			form.lyr1().tabFavourites().setVisible(false);
		}
		else
		{
			form.lyr1().tabFavourites().setHeaderVisible(false);
			form.lyr1().tabFavourites().setVisible(false);
		}
		form.lyr1().tabSearch().setHeaderVisible(true);
		form.lyr1().tabSearch().setVisible(true);
		
		if (populateFavorites())
		{
			form.lyr1().showtabFavourites();
			form.lyr1().tabFavourites().setHeaderVisible(true);
			form.lyr1().tabFavourites().setVisible(true);
		}
		else
			form.lyr1().showtabSearch();

		enableFavouritesContextMenu(false, form.lyr1().tabFavourites().isHeaderVisible());

		if (form.getGlobalContext().Core.getGraphicAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getGraphicAssessmentsSelectable().booleanValue() && form.getGlobalContext().Core.getStructuredAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getStructuredAssessmentsSelectable().booleanValue())
			form.lyr1().tabSearch().Group1().setVisible(true);
		else
			form.lyr1().tabSearch().Group1().setVisible(false);

		form.btnSelect().setEnabled(false);

		form.setMode(FormMode.VIEW);
	}

	private HcpRefVo getCurrentHcpId()
	{
		Object hcp = domain.getHcpUser();
		return hcp instanceof HcpRefVo ? ((HcpRefVo) hcp) : null;
	}

	private boolean populateFavorites()
	{
		
		form.btnSelect().setEnabled(false);
		boolean hasFavourites = false;
		HcpRefVo hcpRefvo = getCurrentHcpId();
		if (hcpRefvo != null)
		{
			UserPatientAssessmentFavouritesVo voFavourites = domain.getFavourite(hcpRefvo);
			form.getLocalContext().setFavouriteAssessments(voFavourites);
			form.lyr1().tabFavourites().treFavourites().clear();
			if (voFavourites != null && voFavourites.getFolderIsNotNull())
			{
				voFavourites.getFolder().sort();

				hasFavourites = voFavourites.getFolder().size() > 0;
				// Create the map with the folders
				for (int i = 0; i < voFavourites.getFolder().size(); i++)
				{
					if (voFavourites.getFolder().get(i).getFolderNameIsNotNull())
					{
						String folder = voFavourites.getFolder().get(i).getFolderName();

						if (!folder.equals(ROOT_FOLDER))
						{
							form.lyr1().tabFavourites().treFavourites().getNodes().add(null, folder, 2);
						}
					}
				}

				// Add Nodes
				for (int i = 0; i < voFavourites.getFolder().size(); i++)
				{
					PatientAssessmentFolderVo patientassessmentFolderVo = voFavourites.getFolder().get(i);
					if (patientassessmentFolderVo.getFolderNameIsNotNull())
					{
						TreeNode node = getFolderRootRow(patientassessmentFolderVo.getFolderName());
						if (node == null)
						{
							if (form.getGlobalContext().Core.getGraphicAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getGraphicAssessmentsSelectable().booleanValue())
							{
								for (int j = 0; patientassessmentFolderVo.getGraphicAssessmentsIsNotNull() && j < patientassessmentFolderVo.getGraphicAssessments().size(); j++)
								{
									String nodeText = patientassessmentFolderVo.getGraphicAssessments().get(j).getName();
									form.lyr1().tabFavourites().treFavourites().getNodes().add(patientassessmentFolderVo.getGraphicAssessments().get(j), nodeText, 1, new int[]{0, 2});
								}
							}
							if (form.getGlobalContext().Core.getStructuredAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getStructuredAssessmentsSelectable().booleanValue())
							{
								for (int k = 0; patientassessmentFolderVo.getPatientAssessmentsIsNotNull() && k < patientassessmentFolderVo.getPatientAssessments().size(); k++)
								{
									String nodeText = patientassessmentFolderVo.getPatientAssessments().get(k).getName();
									form.lyr1().tabFavourites().treFavourites().getNodes().add(patientassessmentFolderVo.getPatientAssessments().get(k), nodeText, 1, new int[]{0, 2});
								}
							}
						}
					}
				}

				for (int i = 0; i < voFavourites.getFolder().size(); i++)
				{
					PatientAssessmentFolderVo patientassessmentFolderVo = voFavourites.getFolder().get(i);
					if (patientassessmentFolderVo.getFolderNameIsNotNull())
					{
						TreeNode node = getFolderRootRow(patientassessmentFolderVo.getFolderName());
						if (node != null)
						{
							node.setExpanded(true);
							node.setCheckBoxVisible(false);
							node.setCollapsedImage(form.getImages().Core.CollapseAll);
							node.setExpandedImage(form.getImages().Core.ExpandAll);

							patientassessmentFolderVo.getGraphicAssessments().sort();
							patientassessmentFolderVo.getPatientAssessments().sort();

							if (form.getGlobalContext().Core.getGraphicAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getGraphicAssessmentsSelectable().booleanValue())
							{
								for (int j = 0; patientassessmentFolderVo.getGraphicAssessmentsIsNotNull() && j < patientassessmentFolderVo.getGraphicAssessments().size(); j++)
								{
									String nodeText = patientassessmentFolderVo.getGraphicAssessments().get(j).getName();
									node.getNodes().add(patientassessmentFolderVo.getGraphicAssessments().get(j), nodeText, 3, new int[]{0, 2});
								}
							}
							if (form.getGlobalContext().Core.getStructuredAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getStructuredAssessmentsSelectable().booleanValue())
							{
								for (int k = 0; patientassessmentFolderVo.getPatientAssessmentsIsNotNull() && k < patientassessmentFolderVo.getPatientAssessments().size(); k++)
								{
									String nodeText = patientassessmentFolderVo.getPatientAssessments().get(k).getName();
									node.getNodes().add(patientassessmentFolderVo.getPatientAssessments().get(k), nodeText, 3, new int[]{0, 2});
								}
							}
						}
					}
				}
			}
		}

		return hasFavourites;
	}

	private PatientAssessmentFolderVo getAssessmentFolder(UserPatientAssessmentFavouritesVo favoritesVo, String rootFolder)
	{
		if (favoritesVo == null || favoritesVo.getFolder() == null)
			return null;

		for (int i = 0; i < favoritesVo.getFolder().size(); i++)
		{
			if (favoritesVo.getFolder().get(i).equals(rootFolder))
				return favoritesVo.getFolder().get(i);
		}

		PatientAssessmentFolderVo voPatAssessFolder = new PatientAssessmentFolderVo();
		voPatAssessFolder.setFolderName(rootFolder);
		voPatAssessFolder.setPatientAssessments(new UserAssessmentShortVoCollection());
		voPatAssessFolder.setGraphicAssessments(new GraphicAssessmentShortVoCollection());

		return voPatAssessFolder;
	}

	private TreeNode getFolderRootRow(String folder)
	{
		for (int i = 0; i < form.lyr1().tabFavourites().treFavourites().getNodes().size(); i++)
		{
			TreeNode node = form.lyr1().tabFavourites().treFavourites().getNodes().get(i);
			String nodeText = node.getText();
			if (nodeText != null && nodeText.startsWith(folder))
				return node;
		}
		return null;
	}

	private void open()
	{
		// form.lyr1().tabSearch().grdAssessment().getRows().clear();

		// setDefaultScreenData();
		// enableContextMenu();
	}

	protected void onBtnCancelClick() throws ims.framework.exceptions.PresentationLogicException
	{
		engine.close(DialogResult.CANCEL);
	}

	protected void onBtnSelectClick() throws ims.framework.exceptions.PresentationLogicException
	{
		form.getGlobalContext().Core.setSelectedGraphicAssessmentVo(null);
		form.getGlobalContext().Core.setSelectedUserAssessmentVo(null);

		if (form.lyr1().tabSearch().isVisible())
		{

			for (int i = 0; i < form.lyr1().tabSearch().grdAssessment().getRows().size(); i++)
			{
				if (form.lyr1().tabSearch().grdAssessment().getRows().get(i).getcolSelect())
				{
					grdAssessmentRow row = form.lyr1().tabSearch().grdAssessment().getRows().get(i);

					if (row.getValue() instanceof ims.assessment.vo.GraphicAssessmentShortVo)
						form.getGlobalContext().Core.setSelectedGraphicAssessmentVo(domain.getGraphicAssessment((ims.assessment.vo.GraphicAssessmentShortVo) row.getValue()));

					if (row.getValue() instanceof UserAssessmentShortVo)
						form.getGlobalContext().Core.setSelectedUserAssessmentVo(domain.getUserAssessment((UserAssessmentShortVo) row.getValue()));

					break;
				}
			}
		}
		else
		{
			if (form.lyr1().tabFavourites().treFavourites().getSelectedNode() != null)
			{
				if (form.lyr1().tabFavourites().treFavourites().getSelectedNode().getValue() instanceof ims.assessment.vo.GraphicAssessmentShortVo)
					form.getGlobalContext().Core.setSelectedGraphicAssessmentVo(domain.getGraphicAssessment((ims.assessment.vo.GraphicAssessmentShortVo) form.lyr1().tabFavourites().treFavourites().getSelectedNode().getValue()));

				if (form.lyr1().tabFavourites().treFavourites().getSelectedNode().getValue() instanceof UserAssessmentShortVo)
					form.getGlobalContext().Core.setSelectedUserAssessmentVo(domain.getUserAssessment((UserAssessmentShortVo) form.lyr1().tabFavourites().treFavourites().getSelectedNode().getValue()));
			}
		}
		engine.close(DialogResult.OK);
	}

	protected void onRadioButtonGroup1ValueChanged() throws ims.framework.exceptions.PresentationLogicException
	{
		// TODO Add your code here.
	}

	@Override
	protected void onImbClearClick() throws ims.framework.exceptions.PresentationLogicException
	{
		doClear();
		enableFavouritesContextMenu(false, false);

	}

	private void doClear()
	{
		clearSearchControls();
		// clearSelectedUserDefinedObjectShort();
		// enableContextMenu();
	}

	private GraphicAssessmentShortVoCollection listGraphical()
	{
		GraphicAssessmentShortVoCollection voColl = domain.listGraphicAssessment(getSearchString());

		if (voColl == null || voColl.size() == 0)
		{
			engine.showMessage("No Graphical Assessments found for the Search Criteria Provided");
			return null;
		}

		return voColl;
	}

	private String getSearchString()
	{
		String name = form.lyr1().tabSearch().txtNameSearch().getValue();
		if (name != null)
		{
			name = name.trim();
			name = '%' + name + '%';
		}
		return name;
	}

	private UserAssessmentShortVoCollection listStructured()
	{
		UserAssessmentShortVoCollection voColl = domain.listUserAssessments(getSearchString());

		if (voColl == null || voColl.size() == 0)
		{
			engine.showMessage("No Structured Assessments found for the Search Criteria Provided");
			return null;
		}

		return voColl;
	}

	protected void onImbSearchClick() throws ims.framework.exceptions.PresentationLogicException
	{
		doSearch();
	}

	private void doSearch()
	{
		// clearSearchControls();
		form.lyr1().tabSearch().grdAssessment().getRows().clear();

		if (form.getGlobalContext().Core.getGraphicAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getGraphicAssessmentsSelectable().booleanValue() && form.getGlobalContext().Core.getStructuredAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getStructuredAssessmentsSelectable().booleanValue())
		{
			Group1Enumeration groupEnum = form.lyr1().tabSearch().Group1().getValue();
			if (groupEnum.equals(Group1Enumeration.rdoGraphical))
				searchGraphical();
			else
				searchStructured();
		}
		else if (form.getGlobalContext().Core.getGraphicAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getGraphicAssessmentsSelectable().booleanValue())
			searchGraphical();
		else if (form.getGlobalContext().Core.getStructuredAssessmentsSelectableIsNotNull() && form.getGlobalContext().Core.getStructuredAssessmentsSelectable().booleanValue())
			searchStructured();
		else
		{
			listStructured();
		}

	}

	private void searchStructured()
	{
		UserAssessmentShortVoCollection voColl = listStructured();
		populateGridStructured(voColl);
	}

	private void populateGridStructured(UserAssessmentShortVoCollection voColl)
	{
		UserAssessmentShortVo voStruct = null;

		for (int i = 0; voColl != null && i < voColl.size(); i++)
		{
			voStruct = voColl.get(i);
			if (voStruct.getActiveStatusIsNotNull() && voStruct.getActiveStatus().equals(PreActiveActiveInactiveStatus.ACTIVE))
			{
				grdAssessmentRow rowSelect = form.lyr1().tabSearch().grdAssessment().getRows().newRow();
				rowSelect.setcolName(voStruct.getName());
				rowSelect.setValue(voStruct);
			}
		}
	}

	private void searchGraphical()
	{
		GraphicAssessmentShortVoCollection voColl = listGraphical();
		populateGridGraphical(voColl);
	}

	private void populateGridGraphical(GraphicAssessmentShortVoCollection voColl)
	{
		GraphicAssessmentShortVo voGraphical = null;

		for (int i = 0; voColl != null && i < voColl.size(); i++)
		{
			voGraphical = voColl.get(i);
			if (voGraphical.getActiveStatusIsNotNull() && voGraphical.getActiveStatus().equals(PreActiveActiveInactiveStatus.ACTIVE))
			{
				grdAssessmentRow rowSelect = form.lyr1().tabSearch().grdAssessment().getRows().newRow();
				rowSelect.setcolName(voGraphical.getName());
				rowSelect.setValue(voGraphical);
			}
		}
	}

	private void clearSearchControls()
	{
		form.lyr1().tabSearch().grdAssessment().getRows().clear();
		form.lyr1().tabSearch().txtNameSearch().setValue("");
		form.btnSelect().setEnabled(false);
	}

	private String getAlreadyInFavorites(UserPatientAssessmentFavouritesVo voFavourite, ValueObject voObject)
	{
		if (voFavourite != null && voObject != null && voFavourite.getFolderIsNotNull())
		{
			for (int i = 0; i < voFavourite.getFolder().size(); i++)
			{
				PatientAssessmentFolderVo voFolder = voFavourite.getFolder().get(i);

				if (voObject instanceof ims.assessment.vo.GraphicAssessmentShortVo)
				{
					if (voFolder.getGraphicAssessmentsIsNotNull() && voFolder.getGraphicAssessments().indexOf((GraphicAssessmentShortVo) voObject) >= 0)
						return voFolder.getFolderName();
				}

				if (voObject instanceof UserAssessmentShortVo)
				{
					if (voFolder.getPatientAssessmentsIsNotNull() && voFolder.getPatientAssessments().indexOf((UserAssessmentShortVo) voObject) >= 0)
						return voFolder.getFolderName();
				}

			}
		}
		return null;
	}

	private UserPatientAssessmentFavouritesVo getCurrentUserAssessmentFavourite()
	{
		UserPatientAssessmentFavouritesVo voFavourites = form.getLocalContext().getFavouriteAssessments();
		if (voFavourites == null)
			voFavourites = new UserPatientAssessmentFavouritesVo();

		Object hcp = domain.getHcpUser();
		if (hcp instanceof HcpRefVo)
			voFavourites.setHCP((HcpRefVo) hcp);

		return voFavourites;
	}

	private ValueObject getSelectedAssessment()
	{
		ValueObject voAssessmentObject = null;
		for (int i = 0; i < form.lyr1().tabSearch().grdAssessment().getRows().size(); i++)
		{
			if (form.lyr1().tabSearch().grdAssessment().getRows().get(i).getcolSelect())
			{
				voAssessmentObject = form.lyr1().tabSearch().grdAssessment().getRows().get(i).getValue();
				break;
			}
		}
		return voAssessmentObject;
	}

	private boolean addToFavorites()
	{
		ValueObject voAssessmentObject = getSelectedAssessment();
		GraphicAssessmentShortVo voGraphicAssessment = null;
		UserAssessmentShortVo voUserAssessment = null;

		if (voAssessmentObject instanceof ims.assessment.vo.GraphicAssessmentShortVo)
			voGraphicAssessment = (GraphicAssessmentShortVo) voAssessmentObject;
		else if (voAssessmentObject instanceof UserAssessmentShortVo)
			voUserAssessment = (UserAssessmentShortVo) voAssessmentObject;

		// Check if not exits to favourites (list context)
		UserPatientAssessmentFavouritesVo favoritesVo = getCurrentUserAssessmentFavourite();
		String existingFolder = getAlreadyInFavorites(favoritesVo, voAssessmentObject);
		if (existingFolder != null)
		{
			if (existingFolder.equals(ROOT_FOLDER))
				existingFolder = " ";
			else
				existingFolder = "' " + existingFolder + " '";
			String szName = voGraphicAssessment != null ? voGraphicAssessment.getName() : voUserAssessment != null ? voUserAssessment.getName() : "";
			engine.showMessage("'" + szName + "'" + " is already added to your" + existingFolder + "favourites folder");
			return false;
		}

		PatientAssessmentFolderVo voFolder = getAssessmentFolder(favoritesVo, ROOT_FOLDER);
		if (voFolder == null)
		{
			voFolder = new PatientAssessmentFolderVo();

			voFolder.setFolderName(ROOT_FOLDER);
			voFolder.setGraphicAssessments(new GraphicAssessmentShortVoCollection());
			voFolder.setPatientAssessments(new UserAssessmentShortVoCollection());
		}

		if (voGraphicAssessment != null)
			voFolder.getGraphicAssessments().add(voGraphicAssessment);
		if (voUserAssessment != null)
			voFolder.getPatientAssessments().add(voUserAssessment);

		if (favoritesVo.getFolder() == null)
			favoritesVo.setFolder(new PatientAssessmentFolderVoCollection());

		int index = favoritesVo.getFolder().indexOf(voFolder);
		if (index >= 0)
			favoritesVo.getFolder().set(index, voFolder);
		else
			favoritesVo.getFolder().add(voFolder);

		if (saveFavorites(favoritesVo) == false)
			return false;

		form.lyr1().tabSearch().setHeaderEnabled(true);
		form.lyr1().tabSearch().setHeaderVisible(true);
		form.lyr1().tabFavourites().setHeaderEnabled(true);
		form.lyr1().tabFavourites().setHeaderVisible(true);

		return true;
	}

	private boolean saveFavorites(UserPatientAssessmentFavouritesVo favoriteVo)
	{
		String[] errors = favoriteVo.validate();
		if (errors != null)
		{
			engine.showErrors(errors);
			return false;
		}
		try
		{
			favoriteVo = domain.saveFavourite(favoriteVo);
		}
		catch (StaleObjectException e)
		{
			engine.showMessage(ims.configuration.gen.ConfigFlag.UI.STALE_OBJECT_MESSAGE.getValue());
			return false;
		}

		form.getLocalContext().setFavouriteAssessments(favoriteVo);

		return true;
	}



	@Override
	protected void onContextMenuItemClick(int menuItemID, ims.framework.Control sender) throws ims.framework.exceptions.PresentationLogicException
	{
		switch (menuItemID)
		{
			case GenForm.ContextMenus.Favourites.Add :
				addToFavorites();
			break;

			case GenForm.ContextMenus.Favourites.AddNewFolder :
				addNewFolder();
			break;

			case GenForm.ContextMenus.Favourites.EditFolder :
				editFolder();
			break;

			case GenForm.ContextMenus.Favourites.RemoveFolder :
				removeFolder();
			break;
		}

	}

	@Override
	protected void onGrdAssessmentSelectionChanged() throws PresentationLogicException
	{
		form.btnSelect().setEnabled(true);

	}

	private String getFavouriteFolderName()
	{
		String name = "Favourite";
		int newIndex = 0;
		for (int i = 0; i < form.lyr1().tabFavourites().treFavourites().getNodes().size(); i++)
		{
			TreeNode node = form.lyr1().tabFavourites().treFavourites().getNodes().get(i);
			if (node.getValue() == null && node.getText() != null)
			{
				int textLength = node.getText().length();
				if (textLength > 0)
				{
					char chIndex = node.getText().charAt(textLength - 1);
					if (Character.isDigit(chIndex))
					{
						int currentIndex = Integer.valueOf(Character.toString(chIndex)).intValue();
						if (currentIndex > newIndex)
							newIndex = currentIndex;
					}
				}
			}
		}
		name = name + (newIndex + 1);
		return name;
	}

	private void addNewFolder()
	{
		String folderName = getFavouriteFolderName();
		if(form.btnSelect().isEnabled())
		{
			form.btnSelect().setEnabled(false);
			
		}
		if (folderName != null && folderName.length() > 0)
		{
			TreeNode node = null;
			node = form.lyr1().tabFavourites().treFavourites().getNodes().add(null, folderName, true, 2);
			node.setCollapsedImage(form.getImages().Core.CollapseAll);
			node.setExpandedImage(form.getImages().Core.ExpandAll);
			form.lyr1().tabFavourites().treFavourites().beginEditSelectedNode();
			enableFavouritesContextMenu(false, form.lyr1().tabFavourites().isHeaderVisible());

			if (node != null)
				node.setCheckBoxVisible(false);

			if (form.getMode().equals(FormMode.VIEW))
				form.setMode(FormMode.EDIT);

			if (form.lyr1().tabFavourites().isHeaderVisible() == false)
				form.lyr1().tabFavourites().setHeaderVisible(true);

			form.lyr1().showtabFavourites();
		}
	}

	private void editFolder()
	{
		TreeNode folderNode = form.lyr1().tabFavourites().treFavourites().getSelectedNode();
		if (folderNode == null && folderNode.getParent() != null)
		{
			engine.showMessage("Please select a Folder");
			return;
		}

		form.lyr1().tabFavourites().treFavourites().beginEditSelectedNode();

		if (form.getMode().equals(FormMode.VIEW))
			form.setMode(FormMode.EDIT);
	}

	private void removeFolder()
	{
		form.lyr1().tabFavourites().treFavourites().getNodes().remove(form.lyr1().tabFavourites().treFavourites().getSelectedNode());
		enableFavouritesContextMenu(false, form.lyr1().tabFavourites().isHeaderVisible());
		
		form.btnSelect().setEnabled(false);
		
		if (form.getMode().equals(FormMode.VIEW))
			form.setMode(FormMode.EDIT);
	}

	protected void onlyrTabsTabChanged(LayerBridge tab)
	{
		boolean isFavouriteTab = tab.equals(form.lyr1().tabFavourites());
		if (isFavouriteTab)
			populateFavorites();

		enableFavouritesContextMenu(isRecordChecked(), isFavouriteTab);

	}

	private boolean isRecordChecked()
	{
		for (int i = 0; i < form.lyr1().tabSearch().grdAssessment().getRows().size(); i++)
		{
			if (form.lyr1().tabSearch().grdAssessment().getRows().get(i).getcolSelect())
			{
				return true;
			}
		}

		return false;
	}

	protected void onTreFavouritesTreeViewCheck(TreeNode node) throws PresentationLogicException
	{
		if (node.isChecked())
			clearCheckedNodes(node);

		if (node.isChecked())
			form.btnSelect().setEnabled(true);
		else
			form.btnSelect().setEnabled(false);

		form.getContextMenus().getFavouritesAddNewFolderItem().setVisible(node.isChecked());
	}

	private boolean isNodeChecked(TreeNode nodeParent, TreeNode currenNode)
	{
		if (currenNode == null || currenNode == null)
			return false;

		if (nodeParent.isChecked())
		{
			Object value = nodeParent.getValue();
			if (value != null && !value.equals(currenNode.getValue()))
				return true;
		}

		return false;
	}

	private void clearCheckedNodes(TreeNode node)
	{
		for (int i = 0; i < form.lyr1().tabFavourites().treFavourites().getNodes().size(); i++)
		{
			TreeNode nodeParent = form.lyr1().tabFavourites().treFavourites().getNodes().get(i);
			if (isNodeChecked(nodeParent, node))
			{
				nodeParent.setChecked(false);
				break;
			}

			for (int j = 0; j < nodeParent.getNodes().size(); j++)
			{
				TreeNode childNode = nodeParent.getNodes().get(j);
				if (isNodeChecked(childNode, node))
				{
					childNode.setChecked(false);
					break;
				}
			}
		}
	}

	@Override
	protected void onGrdAssessmentGridCheckBoxClicked(int column, grdAssessmentRow row, boolean isChecked) throws PresentationLogicException
	{
		if (isChecked)
		{
			form.btnSelect().setEnabled(true);

			// clear the rest of the checks
			for (int i = 0; i < form.lyr1().tabSearch().grdAssessment().getRows().size(); i++)
			{
				ValueObject voObject = form.lyr1().tabSearch().grdAssessment().getRows().get(i).getValue();
				if (voObject != null && voObject.equals(row.getValue()))
					continue;
				form.lyr1().tabSearch().grdAssessment().getRows().get(i).setcolSelect(false);

			}
		}
		else
		{
			boolean bCheckFound = false;

			for (int i = 0; i < form.lyr1().tabSearch().grdAssessment().getRows().size(); i++)
			{
				if (form.lyr1().tabSearch().grdAssessment().getRows().get(i).getcolSelect())
					bCheckFound = true;
			}
			if (!bCheckFound)
				form.btnSelect().setEnabled(false);

		}
		enableFavouritesContextMenu(isChecked, false);
	}

	@Override
	protected void onTreFavouritesTreeViewNodeDropped(TreeNode node, TreeNode previousParentNode) throws PresentationLogicException
	{
		if (form.getMode().equals(FormMode.VIEW))
			form.setMode(FormMode.EDIT);

		TreeNode parent = node.getParent();
		if (parent != null)
		{
			parent.setCollapsedImage(form.getImages().Core.CollapseAll);
			parent.setExpandedImage(form.getImages().Core.ExpandAll);
		}
	}

	@Override
	protected void onTreFavouritesTreeViewSelectionChanged(TreeNode node) throws PresentationLogicException
	{
		enableFavouritesContextMenu(false, form.lyr1().tabFavourites().isHeaderVisible());

		if (form.lyr1().tabFavourites().treFavourites().getSelectedNode().getValue() != null)
			form.btnSelect().setEnabled(true);
		else
			form.btnSelect().setEnabled(false);
	}

	@Override
	protected void onBtnSaveFavouriteClick() throws PresentationLogicException
	{
		if (save())
		{
			populateFavorites();
			if(form.lyr1().tabFavourites().treFavourites().getSelectedNode()!=null)
			{
			form.getContextMenus().getFavouritesEditFolderItem().setVisible(true);
			form.getContextMenus().getFavouritesRemoveFolderItem().setVisible(true);
			}
			else
			{
				form.getContextMenus().getFavouritesEditFolderItem().setVisible(false);
				form.getContextMenus().getFavouritesRemoveFolderItem().setVisible(false);
			}
			
			form.setMode(FormMode.VIEW);
		}
	}

	private TreeNode[] getParentTreeNodes()
	{
		int size = form.lyr1().tabFavourites().treFavourites().getNodes().size();
		TreeNode[] nodes = new TreeNode[size];
		for (int i = 0; i < size; i++)
		{
			nodes[i] = form.lyr1().tabFavourites().treFavourites().getNodes().get(i);
		}

		return nodes;
	}

	private boolean save()
	{
		UserPatientAssessmentFavouritesVo voFavourite = form.getLocalContext().getFavouriteAssessments();
		if (voFavourite == null)
		{
			voFavourite = new UserPatientAssessmentFavouritesVo();
			Object hcp = domain.getHcpUser();
			if (hcp instanceof HcpRefVo)
				voFavourite.setHCP((HcpRefVo) hcp);
		}

		TreeNode[] parentNodes = getParentTreeNodes();
		voFavourite.setFolder(new PatientAssessmentFolderVoCollection());
		// add nodes
		for (int i = 0; i < parentNodes.length; i++)
		{
			TreeNode parentNode = parentNodes[i];
			int nodesCount = parentNode.getNodes().size();
			String folderName = nodesCount == 0 && parentNode.getValue() != null ? ROOT_FOLDER : parentNode.getText();
			PatientAssessmentFolderVo assessmentFolder = getAssessmentFolder(voFavourite, folderName);
			if (nodesCount == 0)
			{
				Object value = parentNode.getValue();
				if (value instanceof GraphicAssessmentShortVo)
					assessmentFolder.getGraphicAssessments().add((GraphicAssessmentShortVo) value);
				if (value instanceof UserAssessmentShortVo)
					assessmentFolder.getPatientAssessments().add((UserAssessmentShortVo) value);

			}
			else
			{
				for (int j = 0; j < nodesCount; j++)
				{
					TreeNode childNode = parentNode.getNodes().get(j);
					Object value = childNode.getValue();
					if (value instanceof GraphicAssessmentShortVo)
						assessmentFolder.getGraphicAssessments().add((GraphicAssessmentShortVo) value);
					if (value instanceof UserAssessmentShortVo)
						assessmentFolder.getPatientAssessments().add((UserAssessmentShortVo) value);
				}
			}

			int indexFolder = voFavourite.getFolder().indexOf(assessmentFolder);
			if (indexFolder >= 0)
				voFavourite.getFolder().set(indexFolder, assessmentFolder);
			else
				voFavourite.getFolder().add(assessmentFolder);
		}

		if (saveFavorites(voFavourite) == false)
			return false;

		return true;
	}

	@Override
	protected void onlyr1TabChanged(LayerBridge tab)
	{
		boolean isFavouriteTab = tab.equals(form.lyr1().tabFavourites());
		if (isFavouriteTab)
			populateFavorites();

		enableFavouritesContextMenu(isRecordChecked(), isFavouriteTab);
	}

}
