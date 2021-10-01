/*******************************************************************************
 * Debrief - the Open Source Maritime Analysis Application
 * http://debrief.info
 *
 * (C) 2000-2020, Deep Blue C Technology Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *******************************************************************************/

package org.mwc.cmap.core.wizards;

import java.beans.PropertyDescriptor;

import org.eclipse.jface.viewers.ISelection;

import MWC.GUI.Editable;

public class EnterBooleanPage extends CoreEditableWizardPage {

	public static class DataItem implements Editable {
		Boolean newVal;

		@Override
		public EditorType getInfo() {
			return null;
		}

		@Override
		public String getName() {
			return "Boolean editor";
		}

		public Boolean getValue() {
			return newVal;
		}

		@Override
		public boolean hasEditor() {
			return false;
		}

		public void setValue(final Boolean val) {
			newVal = val;
		}

	}

	public static String NAME = "Get Name";
	DataItem _myWrapper;
	private final String _fieldExplanation;
	private final Boolean _startValue;

	public EnterBooleanPage(final ISelection selection, final Boolean startVal, final String pageTitle,
			final String pageExplanation, final String fieldExplanation, final String imagePath,
			final String helpContext, final String trailingMessage) {
		super(selection, NAME, pageTitle, pageExplanation, imagePath, helpContext, false, trailingMessage);
		_startValue = startVal;
		_fieldExplanation = fieldExplanation;
	}

	@Override
	protected Editable createMe() {
		if (_myWrapper == null) {
			_myWrapper = new DataItem();
			_myWrapper.setValue(_startValue);
		}

		return _myWrapper;
	}

	public Boolean getBoolean() {
		return _myWrapper.getValue();
	}

	@Override
	protected PropertyDescriptor[] getPropertyDescriptors() {
		final PropertyDescriptor[] descriptors = { prop("Value", _fieldExplanation, getEditable()) };
		return descriptors;
	}

}
