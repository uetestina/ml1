/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.jgeppert.struts2.jquery.grid.components;

import com.jgeppert.struts2.jquery.components.AbstractRemoteBean;
import com.opensymphony.xwork2.util.ValueStack;
import org.apache.struts2.components.Component;
import org.apache.struts2.views.annotations.StrutsTag;
import org.apache.struts2.views.annotations.StrutsTagAttribute;
import org.apache.struts2.views.annotations.StrutsTagSkipInheritance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <!-- START SNIPPET: javadoc -->
 * <p>
 * Renders a column for the grid
 * </p>
 * <!-- END SNIPPET: javadoc -->
 * <p>
 * <p>
 * Examples
 * </p>
 *
 * @author <a href="http://www.jgeppert.com">Johannes Geppert</a>
 */
@StrutsTag(name = "gridColumn", tldTagClass = "com.jgeppert.struts2.jquery.grid.views.jsp.ui.GridColumnTag", description = "Renders a column for the grid")
public class GridColumn extends AbstractRemoteBean {

    public static final String TEMPLATE = "gridcolumn";
    public static final String TEMPLATE_CLOSE = "gridcolumn-close";
    public static final String COMPONENT_NAME = GridColumn.class.getName();

    private static final String PARAM_NAME = "name";
    private static final String PARAM_JSONMAP = "jsonmap";
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_INDEX = "index";
    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_EDITABLE = "editable";
    private static final String PARAM_EDITOPTIONS = "editoptions";
    private static final String PARAM_EDITTYPE = "edittype";
    private static final String PARAM_EDITRULES = "editrules";
    private static final String PARAM_FIXED = "fixed";
    private static final String PARAM_FORMATTER = "formatter";
    private static final String PARAM_FORMATOPTIONS = "formatoptions";
    private static final String PARAM_FROZEN = "frozen";
    private static final String PARAM_SORTABLE = "sortable";
    private static final String PARAM_SORTTYPE = "sorttype";
    private static final String PARAM_RESIZABLE = "resizable";
    private static final String PARAM_KEY = "key";
    private static final String PARAM_SEARCH = "search";
    private static final String PARAM_SEARCHOPTIONS = "searchoptions";
    private static final String PARAM_SEARCHTYPE = "searchtype";
    private static final String PARAM_HIDDEN = "hidden";
    private static final String PARAM_HIDEDLG = "hidedlg";
    private static final String PARAM_ALIGN = "align";
    private static final String PARAM_FORMOPTIONS = "formoptions";
    private static final String PARAM_DEFVAL = "defval";
    private static final String PARAM_SURL = "surl";
    private static final String PARAM_DISPLAY_TITLE = "displayTitle";
    private static final String PARAM_GRID = "grid";

    protected String name;
    protected String jsonmap;
    protected String title;
    protected String index;
    protected String width;
    protected String editable;
    protected String editoptions;
    protected String edittype;
    protected String editrules;
    protected String fixed;
    protected String formatter;
    protected String formatoptions;
    protected String frozen;
    protected String sortable;
    protected String sorttype;
    protected String resizable;
    protected String key;
    protected String search;
    protected String searchtype;
    protected String searchoptions;
    protected String hidden;
    protected String hidedlg;
    protected String align;
    protected String formoptions;
    protected String defval;
    protected String surl;
    protected String displayTitle;

    public GridColumn(ValueStack stack, HttpServletRequest request, HttpServletResponse response) {
        super(stack, request, response);
    }

    public String getDefaultOpenTemplate() {
        return TEMPLATE;
    }

    protected String getDefaultTemplate() {
        return TEMPLATE_CLOSE;
    }

    public void evaluateExtraParams() {
        super.evaluateExtraParams();

        addParameterIfPresent(PARAM_NAME, this.name);
        addParameterIfPresent(PARAM_JSONMAP, this.jsonmap);
        addParameterIfPresent(PARAM_TITLE, this.title);
        addParameterIfPresent(PARAM_INDEX, this.index);
        addParameterIfPresent(PARAM_WIDTH, this.width);
        addParameterIfPresent(PARAM_EDITABLE, this.editable, Boolean.class);
        addParameterIfPresent(PARAM_EDITOPTIONS, this.editoptions);
        addParameterIfPresent(PARAM_EDITTYPE, this.edittype);
        addParameterIfPresent(PARAM_EDITRULES, this.editrules);
        addParameterIfPresent(PARAM_FIXED, this.fixed, Boolean.class);
        addParameterIfPresent(PARAM_FORMATTER, this.formatter);
        addParameterIfPresent(PARAM_FORMATOPTIONS, this.formatoptions);
        addParameterIfPresent(PARAM_FROZEN, this.frozen, Boolean.class);
        addParameterIfPresent(PARAM_SORTABLE, this.sortable, Boolean.class);
        addParameterIfPresent(PARAM_SORTTYPE, this.sorttype);
        addParameterIfPresent(PARAM_RESIZABLE, this.resizable, Boolean.class);
        addParameterIfPresent(PARAM_KEY, this.key, Boolean.class);
        addParameterIfPresent(PARAM_SEARCH, this.search, Boolean.class);
        addParameterIfPresent(PARAM_SEARCHOPTIONS, this.searchoptions);
        addParameterIfPresent(PARAM_SEARCHTYPE, this.searchtype);
        addParameterIfPresent(PARAM_HIDDEN, this.hidden, Boolean.class);
        addParameterIfPresent(PARAM_HIDEDLG, this.hidedlg, Boolean.class);
        addParameterIfPresent(PARAM_ALIGN, this.align);
        addParameterIfPresent(PARAM_FORMOPTIONS, this.formoptions);
        addParameterIfPresent(PARAM_SURL, this.surl);
        addParameterIfPresent(PARAM_DEFVAL, this.defval);
        addParameterIfPresent(PARAM_DISPLAY_TITLE, this.displayTitle, Boolean.class);

        Component grid = findAncestor(Grid.class);
        if (grid != null) {
            addParameter(PARAM_GRID, ((Grid) grid).getId());
        }
    }

    @Override
    @StrutsTagSkipInheritance
    public void setTheme(String theme) {
        super.setTheme(theme);
    }

    @Override
    public String getTheme() {
        return "jquery";
    }

    @StrutsTagAttribute(description = "Set the unique name in the grid for the column. This property is required. As well as other words used as property/event names, the reserved words (which cannot be used for names) include subgrid, cb and rn.", required = true)
    public void setName(String name) {
        this.name = name;
    }

    @StrutsTagAttribute(description = "Defines the json mapping for the column in the incoming json string.")
    public void setJsonmap(String jsonmap) {
        this.jsonmap = jsonmap;
    }

    @StrutsTagAttribute(description = "Column title")
    public void setTitle(String title) {
        this.title = title;
    }

    @StrutsTagAttribute(description = "Set the index name when sorting. Passed as sidx parameter.")
    public void setIndex(String index) {
        this.index = index;
    }

    @StrutsTagAttribute(description = "Set the initial width of the column, in pixels.")
    public void setWidth(String width) {
        this.width = width;
    }

    @StrutsTagAttribute(description = "Defines if the field is editable.", defaultValue = "false", type = "Boolean")
    public void setEditable(String editable) {
        this.editable = editable;
    }

    @StrutsTagAttribute(description = "Array of allowed options (attributes) for edittype option")
    public void setEditoptions(String editoptions) {
        this.editoptions = editoptions;
    }

    @StrutsTagAttribute(description = "Defines the edit type for inline and form editing Possible values: text, textarea, select, checkbox, password, button, image and file.")
    public void setEdittype(String edittype) {
        this.edittype = edittype;
    }

    @StrutsTagAttribute(description = "sets additional rules for the editable field. e.g {number:true, required: true, minValue:10, maxValue:100}")
    public void setEditrules(String editrules) {
        this.editrules = editrules;
    }

    @StrutsTagAttribute(description = "If set to true this option does not allow recalculation of the width of the column if shrinkToFit option is set to true. Also the width does not change if a setGridWidth method is used to change the grid width.", defaultValue = "false", type = "Boolean")
    public void setFixed(String fixed) {
        this.fixed = fixed;
    }

    @StrutsTagAttribute(description = "The predefined types (string) or custom function name that controls the format of this field. e.g.: integer, currency, date, checkbox")
    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

    @StrutsTagAttribute(description = "Format options can be defined for particular columns, overwriting the defaults from the language file.")
    public void setFormatoptions(String formatoptions) {
        this.formatoptions = formatoptions;
    }

    @StrutsTagAttribute(description = "If set to true determines that this column will be frozen.", defaultValue = "false", type = "Boolean")
    public void setFrozen(String frozen) {
        this.frozen = frozen;
    }

    @StrutsTagAttribute(description = "Defines is this can be sorted.", defaultValue = "true", type = "Boolean")
    public void setSortable(String sortable) {
        this.sortable = sortable;
    }

    @StrutsTagAttribute(description = "Used when datatype is local. Defines the type of the column for appropriate sorting.Possible values: int/integer - for sorting integer, float/number/currency - for sorting decimal numbers, date - for sorting date, text - for text sorting, function - defines a custom function for sorting.", defaultValue = "text")
    public void setSorttype(String sorttype) {
        this.sorttype = sorttype;
    }

    @StrutsTagAttribute(description = "Defines if the column can be re sized", defaultValue = "true", type = "Boolean")
    public void setResizable(String resizable) {
        this.resizable = resizable;
    }

    @StrutsTagAttribute(description = "In case if there is no id from server, this can be set as as id for the unique row id. Only one column can have this property. If there are more than one key the grid finds the first one and the second is ignored.", defaultValue = "false", type = "Boolean")
    public void setKey(String key) {
        this.key = key;
    }

    @StrutsTagAttribute(description = "When used in search modules, disables or enables searching on that column.", defaultValue = "true", type = "Boolean")
    public void setSearch(String search) {
        this.search = search;
    }

    @StrutsTagAttribute(description = "Defines the search options used searching. e.g. {sopt:['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc']}")
    public void setSearchoptions(String searchoptions) {
        this.searchoptions = searchoptions;
    }

    @StrutsTagAttribute(description = "Determines the search type of the field. Can be text - also a input element with type text is created and select - a select element is created.")
    public void setSearchtype(String searchtype) {
        this.searchtype = searchtype;
    }

    @StrutsTagAttribute(description = "Defines if this column is hidden at initialization.", defaultValue = "false", type = "Boolean")
    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    @StrutsTagAttribute(description = "If set to true this column will not appear in the modal dialog where users can choose which columns to show or hide.", defaultValue = "false", type = "Boolean")
    public void setHidedlg(String hidedlg) {
        this.hidedlg = hidedlg;
    }

    @StrutsTagAttribute(description = "Defines the alignment of the cell in the Body layer, not in header cell. Possible values: left, center, right., Default 'left'}")
    public void setAlign(String align) {
        this.align = align;
    }

    @StrutsTagAttribute(description = "Defines various options for form editing. e.g. { label:'My Label', elmprefix:'(*)', rowpos:1, colpos:2 }")
    public void setFormoptions(String formoptions) {
        this.formoptions = formoptions;
    }

    @StrutsTagAttribute(description = "The default value for the search field. This option is used only in Custom Searching and will be set as initial search.")
    public void setDefval(String defval) {
        this.defval = defval;
    }

    @StrutsTagAttribute(description = "Valid only in Custom Searching and edittype : 'select' and describes the url from where we can get already-constructed select element")
    public void setSurl(String surl) {
        this.surl = surl;
    }

    @StrutsTagAttribute(description = "If this option is false the title is not displayed in that column when we hover a cell with the mouse", defaultValue = "true", type = "Boolean")
    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }
}
