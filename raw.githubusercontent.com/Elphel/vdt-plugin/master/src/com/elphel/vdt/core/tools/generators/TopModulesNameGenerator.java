/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

//import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Generate the top module name from last selected verilog source file. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class TopModulesNameGenerator extends AbstractGenerator {
    public static final String NAME = "TopModules";
    
    public TopModulesNameGenerator(String prefix, 
                             String suffix, 
                             String separator) 
    {
        super(prefix, suffix, separator,null); // null for topFormatProcessor - this generator can not reference other parameters
    }
    
    public String getName() {
        return NAME;
    }
    protected String[] getStringValues() {
        IResource resource = SelectedResourceManager.getDefault().getChosenVerilogFile();
    	// Use tool top file if available, otherwise use getChosenVerilogFile() as before
    	String topFile = null;
    	if (topProcessor!=null){
    		Tool tool=topProcessor.getCurrentTool();
    		if (tool != null) {
    			topFile=tool.getTopFile();
    		} else {
    			System.out.println("ToolNameGenerator():  topProcessor.getCurrentTool() is null");
    		}
            if ((topFile != null) && (!topFile.equals("")) && (resource !=null)) {
            	IResource resource1 = resource.getProject().getFile(topFile);
            	if ((resource1 != null) && (resource1.getType() == IResource.FILE)){
            		resource = resource1;
            	}
            }
    	}
        
        if ((resource != null) && (resource.getType() == IResource.FILE)) {
        	String[] outlineElementsNames= VerilogUtils.getTopModuleNames((IFile)resource);
        	if ((outlineElementsNames!=null) && (outlineElementsNames.length>0)) return outlineElementsNames;
        } else {
            fault("There are no selected verilog files");
        }
        return null;
    }
} // class TopModulesNameGenerator
