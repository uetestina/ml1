/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.ControllerMapping;
import org.jtrfp.trcl.ctl.ControllerSink;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.ctl.ControllerSource;
import org.jtrfp.trcl.ctl.InputDevice;
import org.jtrfp.trcl.ctl.MappingListener;

public class ControllerInputDevicePanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 4252247402423635792L;
    private final InputDevice                  inputDevice;
    private static final String                NONE = "[none]";
    private final ControllerSinks controllerInputs;
    private JComboBox              destBox;
    private JTable table;
    private ControllerMapper       controllerMapper;
    private volatile boolean dispatching = false;
    //private ControllerConfiguration controllerConfiguration;
    
    private final Collection<String> sinkNameMonitoringCollection = new MonitorCollection();
    private final InputStateFeedbackMonitor inputStateFeedbackMonitor = new InputStateFeedbackMonitor();

    public ControllerInputDevicePanel(InputDevice id, ControllerSinks controllerSinks, ControllerMapper mapper) {
	if(id     == null)
	    throw new NullPointerException("Passed InputDevice intolerably null.");
	if(controllerSinks     == null)
	    throw new NullPointerException("Passed ControllerInputs intolerably null.");
	if(mapper == null)
	    throw new NullPointerException("Passed ControllerMapper intolerably null.");
	this.inputDevice = id;
	this.controllerInputs = controllerSinks;
	this.controllerMapper = mapper;
	this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	table   = new JTable();
	for(Columns col:Columns.values())
	    ((DefaultTableModel)table.getModel()).addColumn(col.getTitle());
	for(ControllerSource cs: inputDevice.getControllerSources())
	    cs.addPropertyChangeListener(inputStateFeedbackMonitor);
	destBox = new JComboBox();
	destBox.addItem(NONE);
	final TableColumnModel cModel = table.getColumnModel();
	cModel.getColumn(Columns.DEST  .ordinal()).setCellEditor(new DefaultCellEditor(destBox));
	cModel.getColumn(Columns.VALUE .ordinal()).setPreferredWidth(20);
	cModel.getColumn(Columns.SCALAR.ordinal()).setPreferredWidth(20);
	cModel.getColumn(Columns.OFFSET.ordinal()).setPreferredWidth(20);
	
	//Populate the table
	for( ControllerSource source : id.getControllerSources() )
	    ((DefaultTableModel)table.getModel()).addRow(new String[]{source.getName(),"?",NONE,"1.0","0.0"});
	
	table.getModel().addTableModelListener(new ControllerTableModelListener());
	
	mapper.addMappingListener(new ControllerMappingListener(), true);
	JScrollPane tableScrollPane = new JScrollPane(table);
	table.setFillsViewportHeight(true);
	this.add(tableScrollPane);
	controllerSinks.getSinkNames().addTarget(sinkNameMonitoringCollection, true);
    }//end ControllerInputDevicePanel
    
    private enum Columns{
	SOURCE("Source"),
	VALUE("Value"),
	DEST("Destination"),
	SCALAR("Scalar"),
	OFFSET("Offset");
	
	private String title;
	Columns(String title){
	    this.title=title;
	}
	public String getTitle(){
	    return title;
	}
    }//end Columns
    
    public static class ControllerConfiguration {
	private String intendedController = "[unnamed]";
	private HashMap<String,ConfEntry> entryMap = new HashMap<String,ConfEntry>();
	public ConfEntry getEntry(String controllerSourceName){
	    ConfEntry result = entryMap.get(controllerSourceName);
	    if( result == null ){
		result = new ConfEntry();
		result.setName(controllerSourceName);
		entryMap.put(controllerSourceName, result);
		}
	    return result;
	}//end getEntry(...)
	
	public static class ConfEntry{
	    private double scale = 1, offset = 0;
	    private String name = "[unnamed]", dest = NONE;
	    
	    public ConfEntry(){super();}
	    
	    public ConfEntry(String dest, String name, double scale, double offset){
		setDest(dest);
		setName(name);
		setScale(scale);
		setOffset(offset);
	    }//end constructor(...)
	    
	    public double getScale() {
	        return scale;
	    }
	    public void setScale(double scale) {
	        this.scale = scale;
	    }
	    public double getOffset() {
	        return offset;
	    }
	    public void setOffset(double offset) {
	        this.offset = offset;
	    }
	    public String getName() {
	        return name;
	    }
	    public void setName(String name) {
	        this.name = name;
	    }
	    public String getDest() {
	        return dest;
	    }
	    public void setDest(String dest) {
	        this.dest = dest;
	    }
	}//end ConfEntry

	public HashMap<String, ConfEntry> getEntryMap() {
	    return entryMap;
	}

	public void setEntryMap(HashMap<String, ConfEntry> entryMap) {
	    this.entryMap = entryMap;
	}

	public String getIntendedController() {
	    return intendedController;
	}

	public void setIntendedController(String intendedController) {
	    this.intendedController = intendedController;
	}
    }//end ControllerConfiguration
    
    private class MonitorCollection implements Collection<String>{

	@Override
	public int size() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> iterator() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(final String item) {
	    SwingUtilities.invokeLater(new Runnable(){

		@Override
		public void run() {
		    destBox.addItem(item);
		}});
	    return true;
	}//end add(...)

	@Override
	public boolean remove(final Object item) {
	    SwingUtilities.invokeLater(new Runnable(){

		@Override
		public void run() {
		    destBox.removeItem(item);
		}});
	    return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
	    final Collection<String> buffer = new ArrayList<>(c);
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    for(String s:buffer)
		        destBox.addItem(s);
		}});
	    return true;
	}//end addAll()

	/**
	 * Not guaranteed to remove every instance!
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
	    final Collection<?> buffer = new ArrayList<>(c);
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    for(Object s:buffer)
		        destBox.removeItem(s);
		}});
	    return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
	    SwingUtilities.invokeLater(new Runnable(){

		@Override
		public void run() {
		    destBox.removeAllItems();
		    destBox.addItem(NONE);
		}});
	}//end clear()
    }//end MonitorCollection
    
    private class ControllerTableModelListener implements TableModelListener{
	@Override
	public void tableChanged(TableModelEvent e) {
	    if(isDispatching())
		return;//Stops infinite recursion
	    final int row = e.getFirstRow();
	    if((e.getType()==TableModelEvent.UPDATE || e.getType()==TableModelEvent.INSERT) /*&& e.getSource() != ControllerInputDevicePanel.this */&& e.getColumn() != Columns.VALUE.ordinal()){
		final TableModel model = table.getModel();
		final String inputString = (String)model.getValueAt(row,Columns.DEST  .ordinal());
		final String srcString   = (String)model.getValueAt(row,Columns.SOURCE.ordinal());
		final double scale  = Double.parseDouble((String)model.getValueAt(row, Columns.SCALAR.ordinal()));
		final double offset = Double.parseDouble((String)model.getValueAt(row, Columns.OFFSET.ordinal()));
		//Update config
		//final ControllerConfiguration config = getControllerConfiguration();
		//final ConfEntry entry = config.getEntry(srcString);
		final ControllerSource controllerSource = inputDevice.getSourceByName(srcString);
		setDispatching(true);
		//if(e.getColumn() == Columns.DEST.ordinal() || e.getType() == TableModelEvent.INSERT){
		    controllerMapper.unmapControllerSource(controllerSource);
		    if(!inputString.contentEquals(NONE)){
			//entry.setDest  (inputString);
			//Update the actual settings
			final ControllerSink  controllerSink  = controllerInputs.getSink(inputString);
			controllerMapper.mapControllerSourceToInput(controllerSource, controllerSink, scale, offset);
		    }//end if(!NONE)
		//}//end if(DEST||INSERT)
		/*if(e.getColumn() == Columns.SCALAR.ordinal() || e.getType() == TableModelEvent.INSERT){
		    entry.setScale (scale);
		}
		if(e.getColumn() == Columns.OFFSET.ordinal() || e.getType() == TableModelEvent.INSERT){
		    entry.setOffset(offset);
		}*/
		setDispatching(false);
	    } else if(e.getType()==TableModelEvent.DELETE){
		/*
		final TableModel model = table.getModel();
		final String srcString   = (String)model.getValueAt(row,Columns.SOURCE.ordinal());
		final ControllerSource controllerSource = inputDevice.getSourceByName(srcString);
		controllerMapper.unmapControllerSource(controllerSource);
		*/
	    }
	}//end tableChanged(...)
    }//end ControllerTableModelListener
    
    private class ControllerMappingListener implements MappingListener<ControllerSource,ControllerMapping>{
	@Override
	public void mapped(ControllerSource key, ControllerMapping value) {
	    fireControllerSourceMapped(key,value);
	}

	@Override
	public void unmapped(ControllerSource key) {
	    fireControllerSourceUnmapped(key);
	}
    }//end ControllerMappingListener

    private void fireControllerSourceUnmapped(ControllerSource cSource){
	//Check for relevance to this panel
	final int row = getRowFor(cSource);
	if(row==-1)
	    return;//Ignore
	final TableModel model = table.getModel();
	
	//Update config
	/*
	final ControllerConfiguration config = getControllerConfiguration();
	final ConfEntry entry = config.getEntry(cSource.getName());
	entry.setDest  (NONE);
	entry.setOffset(0);
	entry.setScale (1.0);
	*/
	//Set actual settings
	model.setValueAt(NONE, row, Columns.DEST.ordinal());
	//Set scalar
	model.setValueAt("1.0", row, Columns.SCALAR.ordinal());
	//Set offset
	model.setValueAt("0.0", row, Columns.OFFSET.ordinal());
    }//end fireControllerSourceUnmapped()
    
    private int addTableRow(ControllerSource cSource, ControllerMapping mapping){
	final DefaultTableModel model = (DefaultTableModel)(table.getModel());
	final String name     = cSource.getName();
	//final ConfEntry entry = getControllerConfiguration().getEntry(name);
	final double scale    = mapping.getScale();
	final double offset   = mapping.getOffset();
	final String dest     = mapping.getControllerSink()==null?NONE:mapping.getControllerSink().getName();
	
	try{SwingUtilities.invokeAndWait(new Runnable(){
	    @Override
	    public void run() {
		setDispatching(true);
		model.addRow(new String[]{name,"?",dest+"",scale+"",offset+""});
		setDispatching(false);
	    }});}catch(Exception e){e.printStackTrace();}
	
	return model.getRowCount()-1;
    }//end addTableRow
    
    private void fireControllerSourceMapped(ControllerSource cSource, ControllerMapping value){
	//Check for relevance to this panel
	if(cSource.getInputDevice() != getInputDevice())
	    return; //Nope.
	final int row = getRowFor(cSource);//TODO: This is keeping us from having multiple mappings from a source
	if(row==-1){//This is a new one! Add it.
	    addTableRow(cSource, value);
	    return;
	    }
	updateTableRow(row, value.getControllerSink()==null?NONE:value.getControllerSink().getName(), value.getScale(), value.getOffset());
    }//end fireControllerSourceMapped(...)
    
    private void updateTableRow(int row, String controllerSinkName, double scale, double offset){
	setDispatching(true);
	final TableModel model = table.getModel();
	//Set destination
	model.setValueAt(controllerSinkName, row, Columns.DEST.ordinal());
	//Set scalar
	model.setValueAt(scale+"" , row, Columns.SCALAR.ordinal());
	//Set offset
	model.setValueAt(offset+"", row, Columns.OFFSET.ordinal());
	setDispatching(false);
    }//end updateTableRow(...)
    
    private int getRowFor(ControllerSource cSource){
	//Check for relevance to this panel
	if(cSource.getInputDevice() != inputDevice)
	    return -1;
	final int col = Columns.SOURCE.ordinal();
	final String sourceString = cSource.getName();
	int row = -1;
	final TableModel model = table.getModel();
	//Find the row containing this sourceString
	for(int i=0; i<model.getRowCount(); i++){
	    if(((String)model.getValueAt(i, col)).contentEquals(sourceString))
		row=i;
	}//end for(model rows)
	if(row==-1)
	    return -1; //Not found in this table. Ignore.
	return row;
    }//end getRowFor(...)
    
    private class InputStateFeedbackMonitor implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final TableModel model = table.getModel();
	    final int row = getRowFor((ControllerSource)evt.getSource());
	    if(row!=-1)
	      model.setValueAt(evt.getNewValue()+"", row, Columns.VALUE.ordinal());
	}
    }//end InputStateFeedbackMonitor
    
    public boolean isDispatching() {
        return dispatching;
    }

    public void setDispatching(boolean dispatching) {
        this.dispatching = dispatching;
    }

    public InputDevice getInputDevice() {
        return inputDevice;
    }

    /*public ControllerConfiguration getControllerConfiguration() {
        return controllerConfiguration;
    }*/
/*
    public void setControllerConfiguration(
    	ControllerConfiguration controllerConfiguration) {
	if(controllerConfiguration==null)
	    throw new NullPointerException("Controller config intolerably null");
        this.controllerConfiguration = controllerConfiguration;
        clearControllerConfiguration();
        applyControllerConfiguration();
    }
    */
    private void clearControllerConfiguration(){
	final TableModel model = table.getModel();
	for(int ri=0; ri<model.getRowCount(); ri++){
	    final String srcString   = (String)model.getValueAt(ri,Columns.SOURCE.ordinal());
	    final ControllerSource controllerSource = inputDevice.getSourceByName(srcString);
	    controllerMapper.unmapControllerSource(controllerSource);
	}//end for(rows)
	((DefaultTableModel)table.getModel()).setRowCount(0);
    }
    /*
    private void applyControllerConfiguration(){
	for(ControllerSource cs: inputDevice.getControllerSources()){
	    final String name = cs.getName();
	    final ConfEntry entry = getControllerConfiguration().getEntry(name);
	    final double scale    = entry.getScale();
	    final double offset   = entry.getOffset();
	    final String dest     = entry.getDest();
	    ((DefaultTableModel)table.getModel()).addRow(new String[]{name,"?",dest+"",scale+"",offset+""});
	}//end for(ControllerSources)
    }//end applyControllerConfiguration()
    */
}//end ControllerInputDevicePanel
