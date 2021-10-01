/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Class to represent NamedTemplateList.
 *
 * @author Kristof Szabados
 */
public final class NamedTemplates extends ASTNode implements IIncrementallyUpdateable {
	public static final String DUPLICATEFIELDNAMEFIRST = "Duplicate field name `{0}'' was first declared here";
	public static final String DUPLICATEFIELDNAMEREPEATED = "Duplicate field name `{0}'' was declared here again";

	private final ArrayList<NamedTemplate> named_templates;

	private HashMap<String, NamedTemplate> namedTemplateMap;
	private List<NamedTemplate> duplicatedNames;
	private CompilationTimeStamp lastUniquenessCheck;

	public NamedTemplates() {
		super();
		named_templates = new ArrayList<NamedTemplate>();
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		named_templates.trimToSize();
		for (final NamedTemplate template : named_templates) {
			template.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		for (final NamedTemplate template : named_templates) {
			if (template == child) {
				return builder.append(INamedNode.DOT).append(template.getName().getDisplayName());
			}
		}

		return builder;
	}

	/**
	 * Adds a new template to the list.
	 *
	 * @param template
	 *                the template to be added.
	 * */
	public void addTemplate(final NamedTemplate template) {
		if (template != null && template.getName() != null) {
			named_templates.add(template);
			template.setFullNameParent(this);
		}
	}

	/**
	 * Remove all named values that were not parsed, but generated during
	 * previous semantic checks.
	 * */
	public void removeGeneratedValues() {
		if (named_templates != null) {
			NamedTemplate temp;
			for (final Iterator<NamedTemplate> iterator = named_templates.iterator(); iterator.hasNext();) {
				temp = iterator.next();
				if (!temp.isParsed()) {
					iterator.remove();
				}
			}
		}
	}

	/** @return the number of templates in the list */
	public int getNofTemplates() {
		return named_templates.size();
	}

	/**
	 * @param index
	 *                the index of the element to return.
	 *
	 * @return the template on the indexed position.
	 * */
	public NamedTemplate getTemplateByIndex(final int index) {
		return named_templates.get(index);
	}

	/**
	 * Checks if there is a named template in the list, with a given name.
	 *
	 * @param id
	 *                the name to search for.
	 * @return true if the list has a template with the provided name, false
	 *         otherwise.
	 */
	public boolean hasNamedTemplateWithName(final Identifier id) {
		if (lastUniquenessCheck == null) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		return namedTemplateMap.containsKey(id.getName());
	}

	/**
	 * Checks if there is a template with the provided name, and if found
	 * returns it.
	 *
	 * @param id
	 *                the name to search for.
	 * @return the template with the provided name position if such exists,
	 *         otherwise null.
	 */
	public NamedTemplate getNamedTemplateByName(final Identifier id) {
		if (lastUniquenessCheck == null) {
			checkUniqueness(CompilationTimeStamp.getBaseTimestamp());
		}

		if (namedTemplateMap.containsKey(id.getName())) {
			return namedTemplateMap.get(id.getName());
		}

		return null;
	}

	/**
	 * Checks the uniqueness of the named templates.
	 *
	 * @param timestamp
	 *                the timestamp of the actual build cycle
	 * */
	public void checkUniqueness(final CompilationTimeStamp timestamp) {
		if (lastUniquenessCheck != null && !lastUniquenessCheck.isLess(timestamp)) {
			return;
		}

		Identifier identifier;
		String name;

		if (lastUniquenessCheck == null) {
			namedTemplateMap = new HashMap<String, NamedTemplate>(named_templates.size());
			duplicatedNames = new ArrayList<NamedTemplate>();

			for (final NamedTemplate template : named_templates) {
				identifier = template.getName();
				name = identifier.getName();
				if (namedTemplateMap.containsKey(name)) {
					if (duplicatedNames == null) {
						duplicatedNames = new ArrayList<NamedTemplate>();
					}
					duplicatedNames.add(template);
				} else {
					namedTemplateMap.put(name, template);
				}
			}

			if (duplicatedNames != null) {
				for (final NamedTemplate template : duplicatedNames) {
					named_templates.remove(template);
				}
			}
		}

		if (duplicatedNames != null) {
			for (final NamedTemplate template : duplicatedNames) {
				identifier = template.getName();
				name = identifier.getName();
				final Location namedLocation = namedTemplateMap.get(name).getName().getLocation();
				namedLocation.reportSingularSemanticError(MessageFormat.format(DUPLICATEFIELDNAMEFIRST, identifier.getDisplayName()));
				template.getLocation().reportSemanticError(
						MessageFormat.format(DUPLICATEFIELDNAMEREPEATED, identifier.getDisplayName()));
			}
		}

		lastUniquenessCheck = timestamp;
	}

	/**
	 * Handles the incremental parsing of this list of named templates.
	 *
	 * @param reparser
	 *                the parser doing the incremental parsing.
	 * @param isDamaged
	 *                true if the location contains the damaged area, false
	 *                if only its' location needs to be updated.
	 * */
	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (final NamedTemplate template : named_templates) {
			template.updateSyntax(reparser, false);
			reparser.updateLocation(template.getLocation());
		}

		if (duplicatedNames != null) {
			for (final NamedTemplate template : duplicatedNames) {
				template.updateSyntax(reparser, false);
				reparser.updateLocation(template.getLocation());
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (named_templates == null) {
			return;
		}

		for (final NamedTemplate namedTemp : named_templates) {
			namedTemp.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (named_templates != null) {
			for (final NamedTemplate nt : named_templates) {
				if (!nt.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
