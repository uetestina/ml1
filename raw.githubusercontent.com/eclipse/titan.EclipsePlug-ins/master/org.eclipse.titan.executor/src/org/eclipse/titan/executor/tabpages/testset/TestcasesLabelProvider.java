/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.tabpages.testset;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeLeaf;
import org.eclipse.titan.executor.graphics.ImageCache;

/**
 * @author Kristof Szabados
 * */
public final class TestcasesLabelProvider extends LabelProvider {
	private final Set<String> availableTestcases;
	private final Set<String> availableControlparts;

	public TestcasesLabelProvider() {
		availableTestcases = new HashSet<String>();
		availableControlparts = new HashSet<String>();
	}

	public void addTestcases(final List<String> testcases) {
		availableTestcases.addAll(testcases);
	}

	public void addControlParts(final List<String> controlparts) {
		availableControlparts.addAll(controlparts);
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof TestsetTreeElement) {
			final List<ITreeLeaf> temp = ((TestsetTreeElement) element).children();
			for (final ITreeLeaf aTemp : temp) {
				final String name = aTemp.name();
				if (availableTestcases.contains(name)
						|| availableControlparts.contains(name)) {
					return ImageCache.getImage("testset.gif");
				}
			}
			return ImageCache.getImage("erroneous.gif");
		} else if (element instanceof TestCaseTreeElement) {
			final String name = ((TestCaseTreeElement) element).name();
			if (availableTestcases.contains(name)
					|| availableControlparts.contains(name)) {
				return ImageCache.getImage("testcase.gif");
			}
			return ImageCache.getImage("erroneous.gif");
		}
		return super.getImage(element);
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof TreeLeaf) {
			return ((TreeLeaf) element).name();
		}
		return super.getText(element);
	}

}
