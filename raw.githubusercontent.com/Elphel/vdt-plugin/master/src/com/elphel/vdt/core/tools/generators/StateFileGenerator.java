/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse or Eclipse plugins (or a modified version of those libraries),
 * containing parts covered by the terms of EPL/CPL, the licensors of this
 * Program grant you additional permission to convey the resulting work.
 * {Corresponding Source for a non-source form of such a combination shall
 * include the source code for the parts of Eclipse or Eclipse plugins used
 * as well as that of the covered work.}
 *******************************************************************************/
package com.elphel.vdt.core.tools.generators;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

public class StateFileGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_STATE_FILE;
    public StateFileGenerator(FormatProcessor topProcessor)
    {
    	super(topProcessor);
    }
    
    public String getName() {
        return NAME;
    }
    
    protected String[] getStringValues() {
//    	return new String[] {(tool0!=null)?tool0.getStateFile(): ""};
    	return new String[] {(getCurrentTool()!=null)?getCurrentTool().getStateFile(): ""};
    }
}
