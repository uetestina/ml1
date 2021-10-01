/*
File: Grafic.java ; This file is part of Twister.
Version: 3.0036

Copyright (C) 2012-2013 , Luxoft

Authors: Andrei Costachi <acostachi@luxoft.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import java.awt.FontMetrics;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.awt.Graphics;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.io.File;
import java.awt.Font;
import javax.swing.JTextField;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import javax.swing.JComboBox;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import javax.swing.SwingUtilities;
import java.awt.FontMetrics;
import java.awt.dnd.DropTarget;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Dimension;
import javax.swing.filechooser.FileFilter;
import javax.swing.InputMap;
import javax.swing.ComponentInputMap;
import javax.swing.KeyStroke;
import javax.swing.ActionMap;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import java.awt.Cursor;
import java.awt.dnd.DragSource;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowEvent;
import java.util.Comparator;
import java.awt.event.InputEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JFileChooser;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import com.twister.Item;
import javax.swing.JList;
import javax.swing.JScrollPane;
import com.twister.CustomDialog;
import com.twister.Configuration;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle;
import javax.swing.tree.DefaultTreeCellRenderer;

public class Grafic extends JPanel{
    private static final long serialVersionUID = 1L;
    private ArrayList <Integer> selected;
    public ArrayList <int []> selectedcollection = new ArrayList<int []>();
    private byte keypress;
    private JPopupMenu p = new JPopupMenu();
    private boolean foundfirstitem;
    private int y = 5;
    private String user;
    private boolean dragging=false;
    private ArrayList<Item>clone = new ArrayList<Item>();
    private int dragammount = 0;
    private boolean clearedSelection = false;
    private boolean dragscroll = true;
    private boolean scrolldown = false;
    private boolean scrollup = false;
    private int[] line = {-1,-1,-1,-1,-1};
    private boolean canrequestfocus = true;
    private int xStart, yStart;
    private boolean onlyOptionals;
    
    public Grafic(TreeDropTargetListener tdtl, String user){
        this.user=user;
        setFocusable(true);
        if(!user.equals("")){
            RunnerRepository.window.mainpanel.setTitleAt(0,
                (user.split("\\\\")[user.split("\\\\").length-1]).split("\\.")[0]);}
        add(p);
        DropTarget dropTarget = new DropTarget(this, tdtl);
        new Thread(){
            public void run(){
                automaticScroll();}}.start();
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseDragged(MouseEvent ev){
                if(!onlyOptionals){
                    mouseIsDragged(ev);}}});
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent ev){}
            public void mouseEntered(MouseEvent ev){
                if(canrequestfocus)new Thread(){
                    public void run(){
                        try{Thread.sleep(300);
                            Grafic.this.requestFocus();}
                        catch(Exception e){e.printStackTrace();}}}.start();
                dragscroll = true;}
            public void mouseExited(MouseEvent ev){
                dragscroll = false;
                keypress = 0;}
            public void mouseReleased(MouseEvent ev){
                mouseIsReleased(ev);}});
        addKeyListener(new KeyListener(){
            public void keyPressed(KeyEvent ev){
                if(ev.getKeyCode()==KeyEvent.VK_SHIFT){keypress=1;}
                if(ev.getKeyCode()==KeyEvent.VK_CONTROL){keypress=2;}
                if(ev.getKeyCode()==KeyEvent.VK_DELETE){removeSelected();}
                if(ev.getKeyCode()==KeyEvent.VK_UP){keyUpPressed();}
                if(ev.getKeyCode()==KeyEvent.VK_DOWN){keyDownPressed();}}
            public void keyReleased(KeyEvent ev){
                if(ev.getKeyCode()!=KeyEvent.VK_UP&&ev.getKeyCode()!=KeyEvent.VK_DOWN){
                    if(ev.getKeyCode()!=KeyEvent.VK_SHIFT)clearedSelection=false;
                    keypress=0;}}
            public void keyTyped(KeyEvent ev){}});}
       
    /*
     * handle up down press
     */
    public void keyDownPressed(){
        ArrayList <Integer> temp = new ArrayList <Integer>();  
        int last = selectedcollection.size()-1;
        if(last<0)return;
        for(int j=0;j<selectedcollection.get(last).length;j++){
            temp.add(new Integer(selectedcollection.get(last)[j]));}
        Item next = nextItem(getItem(temp,false));
        if(next!=null&&keypress!=2){
            if(keypress!=1){
                deselectAll();
                selectItem(next.getPos());
                if(next.getType()==2&&next.getPos().size()==1){
                    int userDefNr = next.getUserDefNr();
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(next);
                    RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(next);
                    if(userDefNr!=RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()){
                        System.out.println("Warning, suite "+next.getName()+
                            " has "+userDefNr+" fields while in database xml are defined "+
                            RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()+" fields");}
                    try{for(int i=0;i<userDefNr;i++){
                        RunnerRepository.window.mainpanel.p1.suitaDetails.
                            getDefPanel(i).setDescription(next.getUserDef(i)[1],true);}}
                    catch(Exception e){e.printStackTrace();}
                } else{
                    RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(next);
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(next);
                    if(next.getType()==2)RunnerRepository.window.mainpanel.p1.suitaDetails.setSuiteDetails(false);
                    else RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
                }}
            else{
                RunnerRepository.window.mainpanel.p1.suitaDetails.setGlobalDetails();
                RunnerRepository.window.mainpanel.p1.suitaDetails.clearDefs();
                RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(null);
                RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(null);
                if(!clearedSelection){
                    deselectAll();
                    clearedSelection = true;
                    selectItem(getItem(temp,false).getPos());}
                if(next.isSelected()){                            
                    int [] itemselected = selectedcollection.get(selectedcollection.size()-1);
                    Item theone = RunnerRepository.getSuita(itemselected[0]);
                    for(int j=1;j<itemselected.length;j++){theone = theone.getSubItem(itemselected[j]);}
                    theone.select(false);
                    selectedcollection.remove(selectedcollection.size()-1);}
                else selectItem(next.getPos());}
            RunnerRepository.window.mainpanel.p1.remove.setEnabled(true);
            repaint();}}
          
    /*
     * handle up key press
     */
    public void keyUpPressed(){
        ArrayList <Integer> temp = new ArrayList <Integer>();  
        int last = selectedcollection.size()-1;
        if(last<0)return;
        for(int j=0;j<selectedcollection.get(last).length;j++){
            temp.add(new Integer(selectedcollection.get(last)[j]));}
        Item next = previousItem(getItem(temp,false));
        if(next!=null&&keypress!=2){
            if(keypress!=1){
                deselectAll();
                selectItem(next.getPos());
                if(next.getType()==2&&next.getPos().size()==1){
                    int userDefNr = next.getUserDefNr();
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(next);
                    RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(next);
                    if(userDefNr!=RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()){
                        System.out.println("Warning, suite "+next.getName()+" has "+
                        userDefNr+" fields while in bd.xml are defined "+
                        RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()+" fields");}
                    try{for(int i=0;i<userDefNr;i++){
                            RunnerRepository.window.mainpanel.p1.suitaDetails.getDefPanel(i).
                                setDescription(next.getUserDef(i)[1],true);}}
                    catch(Exception e){e.printStackTrace();}}
                    
                else {
                    
                    RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(next);
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(next);
                    if(next.getType()==2)RunnerRepository.window.mainpanel.p1.suitaDetails.setSuiteDetails(false);
                    else RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
                }
            } else{
                RunnerRepository.window.mainpanel.p1.suitaDetails.setGlobalDetails();
                RunnerRepository.window.mainpanel.p1.suitaDetails.clearDefs();
                RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(null);
                RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(null);
                if(!clearedSelection){
                    deselectAll();
                    clearedSelection = true;
                    selectItem(getItem(temp,false).getPos());}
                if(next.isSelected()){                            
                    int [] itemselected = selectedcollection.get(selectedcollection.size()-1);
                    Item theone = RunnerRepository.getSuita(itemselected[0]);
                    for(int j=1;j<itemselected.length;j++){
                        theone = theone.getSubItem(itemselected[j]);}
                    theone.select(false);
                    selectedcollection.remove(selectedcollection.size()-1);}
                else selectItem(next.getPos());}
            RunnerRepository.window.mainpanel.p1.remove.setEnabled(true);
            repaint();}}
            
    /*
     * handles mouse released
     */
    public void mouseIsReleased(MouseEvent ev){
        clearDraggingLine();
        scrolldown = false;
        scrollup = false; 
        RunnerRepository.window.mainpanel.p1.suitaDetails.setGlobalDetails();
        RunnerRepository.window.mainpanel.p1.suitaDetails.clearDefs();
        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(null);
        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(null);
        dragammount=0;
        if(dragging){handleMouseDroped(ev.getY());}
        else handleClick(ev);}
            
    /*
     * handles mouse dragged
     */     
    public void mouseIsDragged(MouseEvent ev){
        if((ev.getModifiers() & InputEvent.BUTTON1_MASK) != 0){
            if(dragging){handleDraggingLine(ev.getX(),ev.getY());}
            else{//first time
                if(dragammount==0){
                    xStart = ev.getX();
                    yStart = ev.getY();}
                if(dragammount<3)dragammount++;
                else{dragammount=0;
                    getClickedItem(xStart,yStart);
                    if(selected.size()>0){
                            handleDraggedItems();}}}}}

    /*
     * Dragged items method
     * handles dragged items
     * and puts them in a clone array
     */                      
    public void handleDraggedItems(){
        if(getItem(selected,false).getType()!=0){
            setCursor(DragSource.DefaultCopyDrop);
            if(!getItem(selected,false).isSelected()){
                deselectAll();
                int [] temporary = new int[selected.size()];
                for(int m=0;m<temporary.length;m++){
                    temporary[m]=selected.get(m).intValue();}
                selectedcollection.add(temporary);}
            ArrayList <Integer> temp = new ArrayList <Integer>();
            for(int i=selectedcollection.size()-1;i>=0;i--){
                for(int j=0;j<selectedcollection.get(i).length;j++){
                    temp.add(new Integer(selectedcollection.get(i)[j]));}
                Item theone2 = getItem(temp,false);  
                clone.add(theone2);
                temp.clear();}                                
            removeSelected();
            dragging = true;}
        ArrayList<Item> unnecessary = new ArrayList<Item>();//remove from clone children of parents already selected                  
        for(int i=0;i<clone.size();i++){
            Item one = clone.get(i);
            ArrayList<Integer>pos = (ArrayList<Integer>)one.getPos().clone();
            while(pos.size()>1){
                pos.remove(pos.size()-1);
                boolean found = false;
                for(int j=0;j<clone.size();j++){
                    Item one2 = clone.get(j);
                    ArrayList<Integer>pos2 = (ArrayList<Integer>)one2.getPos().clone();
                    if(compareArrays(pos,pos2)){
                        unnecessary.add(clone.get(i));
                        found = true;
                        break;}}
                if(found)break;}}
        for(int i=0;i<unnecessary.size();i++){clone.remove(unnecessary.get(i));}}

            
    /*
     * handle automatic scrolling
     */
    public void automaticScroll(){
        while(RunnerRepository.run){
            if(scrolldown){
                int scrollvalue = RunnerRepository.window.mainpanel.p1.sc.pane.
                                    getVerticalScrollBar().getValue();
                RunnerRepository.window.mainpanel.p1.sc.pane.
                    getVerticalScrollBar().setValue(scrollvalue-10);}
            else if(scrollup){
                int scrollvalue = RunnerRepository.window.mainpanel.p1.sc.pane.
                                    getVerticalScrollBar().getValue();
                RunnerRepository.window.mainpanel.p1.sc.pane.
                    getVerticalScrollBar().setValue(scrollvalue+10);}
            try{Thread.sleep(60);}
            catch(Exception e){e.printStackTrace();}}}
    
    /*
     * clears draggin line from screen
     */
    public void clearDraggingLine(){
        line[0] = -1;
        line[1] = line[0];
        line[2] = line[0];
        line[3] = line[0];
        line[4] = line[0];}
        
    /*
     * arrange all items according to
     * tear/setup property
     */
    public void sortTearSetup(Item i){
        int size = i.getSubItemsNr();
        ArrayList<Item> pretemp = new ArrayList();
        ArrayList<Item> teartemp = new ArrayList();
        Item subitem;
        
        for(int j=0;j<size;j++){
            subitem = i.getSubItem(j);
            if(subitem.isPrerequisite()&&j!=0&&!i.getSubItem(j-1).isPrerequisite()){
                for(int k=j;k<size;k++){
                    if(i.getSubItem(k).isPrerequisite())pretemp.add(i.getSubItem(k));
                }
                break;
            }
        }
        
        for(int j=size-1;j>-1;j--){
            subitem = i.getSubItem(j);
            if(subitem.isTeardown()&&j!=size-1&&!i.getSubItem(j+1).isTeardown()){
                for(int k = j;k>-1;k--){
                    if(i.getSubItem(k).isTeardown())teartemp.add(i.getSubItem(k));
                }
                break;
            }
        }
        
        
        for(Item subitm:pretemp){
            i.getSubItems().remove(subitm);
        }
        for(Item subitm:teartemp){
            i.getSubItems().remove(subitm);
        }
        
        
        int arraysize = pretemp.size();
        size = i.getSubItemsNr();
        
        for(int j=0;j<size;j++){
            if(!i.getSubItem(j).isPrerequisite()){
                for(int k=arraysize-1;k>-1;k--){
                    i.insertSubItem(pretemp.get(k),j);
                }
                break;
            }
            if(j==(size-1)){
                for(int k=arraysize-1;k>-1;k--){
                    i.insertSubItem(pretemp.get(k),j);
                }
            }
        }
        if(size==0){
            for(int k=arraysize-1;k>-1;k--){
                i.insertSubItem(pretemp.get(k),0);
            }
        }
        
        arraysize = teartemp.size();
        size = i.getSubItemsNr();
        for(int j=size-1;j>-1;j--){
            if(!i.getSubItem(j).isTeardown()){
                for(int k=arraysize-1;k>-1;k--){
                    i.insertSubItem(teartemp.get(k),j+1);
                }
                break;
            }
            if(j==0){
                for(int k=arraysize-1;k>-1;k--){
                    i.insertSubItem(teartemp.get(k),j+1);
                }
            }
        }
        if(size==0){
            for(int k=arraysize-1;k>-1;k--){
                i.insertSubItem(teartemp.get(k),0);
            }
        }
        
        size = i.getSubItemsNr();
        for(int j=0;j<size;j++){
            subitem = i.getSubItem(j);
            subitem.updatePos(subitem.getPos().size()-1, new Integer(j));
        }
        for(Item itm:i.getSubItems()){
            if(i.getType()==2)sortTearSetup(itm);
        }
    }
        
    public void handleMouseDroped(int mouseY){
        Collections.sort(clone, new CompareItems());
        dragging=false;
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        int Xpos = 0;
        while(Xpos<getWidth()){
            getClickedItem(Xpos,mouseY);
            Xpos+=5;
            if(selected.size()!=0)break;}
        if(selected.size()==0){//not inserted in element @@@@@@@@@@@ 
            int y1 = mouseY;
            Item upper=null;
            while(y1>0){
                y1-=5;
                for(int x1=0;x1<getWidth();x1+=5){
                    getClickedItem(x1,y1);
                    if(selected.size()>0){
                        upper=getItem(selected,false);
                        if(upper!=null)break;}
                    if(upper!=null)break;}}
            if(upper!=null){
                if(upper.getType()==1){//inserted under tc
                    int index = upper.getPos().get(upper.getPos().size()-1).intValue();
                    int position = upper.getPos().size()-1;
                    ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone();
                    if(temp.size()>1)temp.remove(temp.size()-1);
                    Item parent = getItem(temp,false);                                
                    if(parent.getType()==1){
                        dropOnFirstLevel(upper);}  //if upper parent is tc=>is on level 0, will not have parent
                    else{ //parent is not tc=>is not on level 0, must insert to parent or after parent
                        if((parent.getSubItemsNr()-1==upper.getPos().get(upper.getPos().size()-1)) &&
                                !upper.getSubItem(0).isVisible()){//if tc is last in suite                                    
                            int Y = mouseY;
                            if(Y<upper.getRectangle().y+upper.getRectangle().getHeight()+5){//Should be inserted in upper parent/next in line
                                dropNextInLine(upper,parent,index,position);}
                            else{//Should be inserted after upper parent; exit one level
                                upper = parent;
                                position = upper.getPos().size();                                
                                int temp1 = upper.getPos().get(position-1);
                                if(upper.getPos().size()==1){dropOnFirstLevel(upper);}//Suite on level 0
                                else{dropOnUpperLevel(upper);}}}  //Suite with parent suite                           
                        else{dropNextInLine(upper, parent, index, position);}}}//tc is not last in suite, make drop after him
                else if(upper.getType()==2){//inserted under suite
                    int Y = mouseY;
                    if((upper.getSubItemsNr()>0&&upper.getSubItem(0).isVisible())){//suite is expander||has no children,must insert into it
                        dropFirstInSuita(upper);}//Should be inserted in suita
                    else if(Y<upper.getRectangle().y+upper.getRectangle().getHeight()+5||//closer to upper half 
                    (Y>upper.getRectangle().y+upper.getRectangle().getHeight()+5&&upper.getPos().size()>1 //farther from 5px half but suite is not last in parent
                    &&getFirstSuitaParent(upper,false).getSubItemsNr()-1>upper.getPos().get(upper.getPos().size()-1))){//must insert on same row after suite
                        int position = upper.getPos().size();                                
                        int temp1 = upper.getPos().get(position-1);
                        if(upper.getPos().size()==1){dropOnFirstLevel(upper);}//suite on level 0
                        else{int index = upper.getPos().get(upper.getPos().size()-1).intValue();//suite not on level 0
                            position = upper.getPos().size()-1;
                            ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone();
                            if(temp.size()>1)temp.remove(temp.size()-1);
                            Item parent = getItem(temp,false);                                        
                            dropNextInLine(upper, parent, index, position);}}
                    else{//suite is not expanded||has no children&&not made drop close to it, Should be inserted after suita parent(exit one level)
                        if(upper.getPos().size()==1) dropOnFirstLevel(upper);//Suite on level 0
                        else{//Suite not on level 0
                            if(getFirstSuitaParent(upper,false).getPos().size()>1)
                                {dropOnUpperLevel(getFirstSuitaParent(upper,false));} //parent of suite not on level 0
                            else dropOnFirstLevel(getFirstSuitaParent(upper,false));}}} //parent on level 0
                else if(upper.getType()==0){
                    int Y = mouseY;//upper is made parent of this prop and copy methods from drop under tc
                    Y-=upper.getRectangle().y+upper.getRectangle().getHeight();
                    ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone();
                    temp.remove(temp.size()-1);
                    Item prop = upper;
                    upper = getItem(temp,false);
                    Y+=upper.getRectangle().y+upper.getRectangle().getHeight();                                
                    int index = upper.getPos().get(upper.getPos().size()-1).intValue();
                    int position = upper.getPos().size()-1;
                    temp = (ArrayList<Integer>)upper.getPos().clone();
                    if(temp.size()>1)temp.remove(temp.size()-1);
                    Item parent = getItem(temp,false);                                
                    if(parent.getType()==1){dropOnFirstLevel(upper);}  //parent on level 0 because it is tc=>will not have paremt
                    else{ // parent is not tc=>not on level 0, must ad to parent or after parent
                        if(Grafic.getTcParent(prop,false).getSubItemsNr()-1==prop.getPos().get(prop.getPos().size()-1) &&
                            parent.getSubItemsNr()-1==upper.getPos().get(upper.getPos().size()-1)){//if prop is last in tc and tc is last in suite
                            if(Y<upper.getRectangle().y+upper.getRectangle().getHeight()+5){
                                dropNextInLine(upper, parent, index, position);}//Should be inserted in upper parent
                            else{//Should be inserted after upper parent
                                upper = parent;
                                position = upper.getPos().size();                                
                                int temp1 = upper.getPos().get(position-1);
                                if(upper.getPos().size()==1){dropOnFirstLevel(upper);}//suite on level 0
                                else{dropOnUpperLevel(upper);}}}//Suita with parent suita
                        else{dropNextInLine(upper, parent, index, position);}}}}//tc is not last in suite
            else{dropFirstElement();}}//upper is null
        else{//inserted in element
            if(getItem(selected,false).getType()==2){//inserted in suita
                dropFirstInSuita(getItem(selected,false));}//first element is not prerequisite, can insert on first level
            else if(getItem(selected,false).getType()==1){//inserted in tc
                Item item = getItem(selected,false);
                boolean up = isUpperHalf(item, mouseY);
                if(up){//in upper half of tc, tc is not prerequisite
                    Item upper = item; //tc the element in witch is made drop
                    int index = upper.getPos().get(upper.getPos().size()-1).intValue(); //last position value of tc
                    int position = upper.getPos().size()-1; //what nr is the element is the one from witch made drop
                    ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone(); //clone position of the lement in that was made drop
                    if(temp.size()>1)temp.remove(temp.size()-1); //delete last pos to get parent
                    Item parent = getItem(temp,false); //upper item parentul 
                    if(parent.getType()==2){dropPreviousInLine(upper,parent,index,position);}
                    else{
                        if(upper.getPos().get(0)>0){//must insert before upper, will not be first element
                            temp = (ArrayList<Integer>)upper.getPos().clone(); //clone position of the lement in that was made drop
                            temp.set(0,temp.get(0)-1);//the one before him
                            upper = getItem(temp,false); 
                            dropOnFirstLevel(upper);}
                        else{dropFirstElement();}}}//will be first element
                else{Item upper = item;//under tc
                    int index = upper.getPos().get(upper.getPos().size()-1).intValue();
                    int position = upper.getPos().size()-1;
                    ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone();
                    if(temp.size()>1)temp.remove(temp.size()-1);
                    Item parent = getItem(temp,false);
                    if(parent.getType()==1)dropOnFirstLevel(upper);//parent is tc=>on level 0
                    else dropNextInLine(upper, parent, index, position);}}//parent is not tc>can be added in line after parent
            else if(getItem(selected,false).getType()==0){//inserted in prop
                ArrayList<Integer> temp = (ArrayList<Integer>)getItem(selected,false).getPos().clone();
                temp.remove(temp.size()-1);                            
                Item upper = getItem(temp,false);
                int index = upper.getPos().get(upper.getPos().size()-1).intValue();
                int position = upper.getPos().size()-1;
                temp = (ArrayList<Integer>)upper.getPos().clone();
                if(temp.size()>1)temp.remove(temp.size()-1);
                Item parent = getItem(temp,false);
                if(parent.getType()==1)dropOnFirstLevel(upper);//parent is tc=>on level 0
                else dropNextInLine(upper, parent, index, position);}}            
        for(Item item:RunnerRepository.getSuite()){
            if(item.getType()==2){
                sortTearSetup(item);
            }
        }
        updateLocations(RunnerRepository.getSuita(0));
        repaint();
    
    }
       
    /*
     * checks if y is in the upper half
     * of item
     */
    public boolean isUpperHalf(Item item, int Y){
        int middle = item.getLocation()[1]+(int)item.getRectangle().getHeight()/2;
        if(Y<=middle)return true;
        return false;}
      
    /*
     * drops elements from clone 
     * as first lements in tree
     */
    public void dropFirstElement(){
        int temp1 = 0;
        y=10;
        foundfirstitem=true;
        for(int i=0;i<clone.size();i++){
           if(clone.get(i).getType()==1)continue;
           ArrayList<Integer> selected2 = new ArrayList<Integer>();
           selected2.add(new Integer(i));
           clone.get(i).setPos(selected2);                               
           for(int j = temp1;j<RunnerRepository.getSuiteNr();j++){
                RunnerRepository.getSuita(j).updatePos(0,new Integer(RunnerRepository.getSuita(j).
                                                        getPos().get(0).intValue()+1));}
           temp1++;
           clone.get(i).select(false);
           RunnerRepository.getSuite().add(clone.get(i).getPos().get(0), clone.get(i));}
        deselectAll();
        clone.clear();
        updateLocations(RunnerRepository.getSuita(0));
        repaint();}
       
    /*
     * drops elements from clone 
     * at first level under upper
     */
    public void dropOnFirstLevel(Item upper){
        int position = upper.getPos().size();                                
        int temp1 = upper.getPos().get(position-1);                                    
        for(int i=0;i<clone.size();i++){
           if(clone.get(i).getType()==1)continue;
           ArrayList<Integer> selected2 = (ArrayList<Integer>)upper.getPos().clone();
           selected2.set(0,new Integer(upper.getPos().get(0)+i+1));
           clone.get(i).setPos(selected2);
           for(int j = temp1+1;j<RunnerRepository.getSuiteNr();j++){
                RunnerRepository.getSuita(j).updatePos(0,new Integer(RunnerRepository.getSuita(j).
                                                                getPos().get(0).intValue()+1));}
           temp1++;
           clone.get(i).select(false);
           RunnerRepository.getSuite().add(clone.get(i).getPos().get(0), clone.get(i));}
        deselectAll();
        clone.clear();
        updateLocations(RunnerRepository.getSuita(0));
        repaint();}
        
    /*
     * drops elements from clone 
     * next in line after upper
     */    
    public void dropNextInLine(Item upper,Item parent,int index,int position){
        int temp1 = index+1;
        String [] ep = parent.getEpId();
        for(int i=0;i<clone.size();i++){
            ArrayList<Integer> selected2 = (ArrayList<Integer>)upper.getPos().clone();
            selected2.set(selected2.size()-1,new Integer(selected2.get(selected2.size()-1).intValue()+(i+1)));
            clone.get(i).setPos(selected2);
            for(int j = temp1;j<parent.getSubItemsNr();j++){
                parent.getSubItem(j).updatePos(position,new Integer(parent.getSubItem(j).
                                                                    getPos().get(position).intValue()+1));}
            temp1++;
            if(clone.get(i).getType()==2)clone.get(i).setEpId(ep);
            insertNewTC(clone.get(i).getName(),selected2,parent,clone.get(i));}    
        deselectAll();
        clone.clear();}
     
    /*
     * drops elements from clone 
     * previous in line before upper
     */
    public void dropPreviousInLine(Item upper, Item parent, int index, int position){
        int temp1 = index;  //value for last position of tc 
        ArrayList<Integer> selected2 = (ArrayList<Integer>)upper.getPos().clone();
        String [] ep = parent.getEpId();
        for(int i=0;i<clone.size();i++){//all elements from drag
            ArrayList<Integer> selected3 = (ArrayList<Integer>)selected2.clone(); //clone position of the lement in that was made drop
            selected3.set(selected3.size()-1,new Integer(selected3.get(selected3.size()-1).intValue()+i));
            clone.get(i).setPos(selected3);
            for(int j = temp1;j<parent.getSubItemsNr();j++){
                parent.getSubItem(j).updatePos(position,new Integer(parent.getSubItem(j).
                                                    getPos().get(position).intValue()+1));}
            temp1++;
            if(clone.get(i).getType()==2)clone.get(i).setEpId(ep);
            insertNewTC(clone.get(i).getName(),selected3,parent,clone.get(i));}    
        deselectAll();
        clone.clear();}
        
    /*
     * drops elements from clone 
     * on upper level from upper
     */
    public void dropOnUpperLevel(Item upper){
        int index = upper.getPos().get(upper.getPos().size()-1).intValue();
        int position = upper.getPos().size()-1;
        ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone();
        if(temp.size()>1)temp.remove(temp.size()-1);
        Item parent = getItem(temp,false);
        String [] ep = parent.getEpId();
        int temp1 = index+1;
        for(int i=0;i<clone.size();i++){
            ArrayList<Integer> selected2 = (ArrayList<Integer>)upper.getPos().clone();
            selected2.set(selected2.size()-1,new Integer(selected2.get(selected2.size()-1).intValue()+(i+1)));
            clone.get(i).setPos(selected2);
            for(int j = temp1;j<parent.getSubItemsNr();j++){
                parent.getSubItem(j).updatePos(position,new Integer(parent.getSubItem(j).
                                                    getPos().get(position).intValue()+1));}
            temp1++;
            if(clone.get(i).getType()==2)clone.get(i).setEpId(ep);
            insertNewTC(clone.get(i).getName(),selected2,parent,clone.get(i));}    
        deselectAll();
        clone.clear();}
        
        
    /*
     * drops elements from clone 
     * last in suite upper
     */
    public void dropLastInSuita(Item upper){
        int position = upper.getPos().size();
        Item parent = upper;
        String [] ep = upper.getEpId();
        for(int i=0;i<clone.size();i++){
            ArrayList<Integer> selected2 = (ArrayList<Integer>)upper.getPos().clone();
            selected2.add(new Integer(upper.getSubItemsNr()+i));
            clone.get(i).setPos(selected2);
            insertNewTC(clone.get(i).getName(),selected2,parent,clone.get(i));
            if(clone.get(i).getType()==2)clone.get(i).setEpId(ep);}
        deselectAll();
        clone.clear();}
        
    /*
     * drops elements from clone 
     * first in suite upper
     */
    public void dropFirstInSuita(Item upper){
        int position = upper.getPos().size();
        Item parent = upper;
        int temp1 = 0;
        String [] ep = upper.getEpId();
        for(int i=0;i<clone.size();i++){
            ArrayList<Integer> selected2 = (ArrayList<Integer>)upper.getPos().clone();
            selected2.add(new Integer(i));
            clone.get(i).setPos(selected2);
            for(int j = temp1;j<parent.getSubItemsNr();j++){
                parent.getSubItem(j).updatePos(position,new Integer(parent.getSubItem(j).
                                                    getPos().get(position).intValue()+1));}
            temp1++;
            insertNewTC(clone.get(i).getName(),selected2,parent,clone.get(i));
            if(clone.get(i).getType()==2)clone.get(i).setEpId(ep);}
        deselectAll();
        clone.clear();}
     
    /*
     * positions dragging line
     * inside suite
     */
    public void lineInsideSuita(Item item, int X){
//         line[0] = (int)(item.getRectangle().x+item.getRectangle().getWidth()/2-55);
        line[0] = (int)(item.getRectangle().x+item.getRectangle().getWidth()/2-5);
        line[1] = (int)(item.getRectangle().y+item.getRectangle().getHeight()+5);
        line[2] = X;
        line[3] = line[1];
        line[4] = (int)(item.getRectangle().y+item.getRectangle().getHeight()+5);}
        
    /*
     * positions dragging line
     * on suite
     */
    public void lineOnSuita(Item item, int X){
        line[0] = (int)(item.getRectangle().x+item.getRectangle().getWidth()-40);
        line[1] = (int)(item.getRectangle().y+item.getRectangle().getHeight()/2);
        line[2] = X;
        line[3] = line[1];
        line[4] = line[3];}
      
    /*
     * positions dragging line
     * under suite
     */
    public void lineUnderSuita(Item item,int X){
        line[0] = (int)(item.getRectangle().x-25);
        line[1] = (int)(item.getRectangle().y+item.getRectangle().getHeight()+5);
        line[2] = X;
        line[3] = line[1];
        if(getFirstSuitaParent(item,false)!=null)
            {line[4] = (int)(getFirstSuitaParent(item,false).getRectangle().y+
                                getFirstSuitaParent(item,false).getRectangle().getHeight()+5);}
        else line[4] = line[2];}
        
    /*
     * positions dragging line
     * above tc
     */
    public void lineAboveTc(Item item, int X){
        line[0] = (int)(item.getRectangle().x-25);
        line[1] = (int)(item.getRectangle().y-5);
        line[2] = X;
        line[3] = line[1];
        line[4] = line[1];}
     
    /*
     * positions dragging line
     * under item
     */    
    public void lineUnderItem(Item item, int X){
        line[0] = (int)(item.getRectangle().x-25);
        line[2] = X;
        if(item.getType()==1&&itemIsExpanded(item)){
            line[1] = (int)(item.getSubItem(item.getSubItemsNr()-1).getRectangle().y+
                        item.getSubItem(item.getSubItemsNr()-1).getRectangle().getHeight()+5);}
        else{line[1] = (int)(item.getRectangle().y+item.getRectangle().getHeight()+5);}
        line[3] = line[1];
        line[4] = (int)(item.getRectangle().y+item.getRectangle().getHeight()/2);}
     
    /*
     * positions dragging line
     * after upper parent
     */
    public void lineAfterUpperParent(Item item, int X){
        line[0] = (int)(getFirstSuitaParent(item,false).getRectangle().x-25);
        line[2] = X;
        if(item.getSubItemsNr()>0&&itemIsExpanded(item)){
            line[1] = (int)(item.getSubItem(item.getSubItemsNr()-1).getRectangle().y+
                            item.getSubItem(item.getSubItemsNr()-1).getRectangle().getHeight()+5);}
        else{line[1] = (int)(item.getRectangle().y+item.getRectangle().getHeight()+5);}
        line[3] = line[1];
        line[4] = (int)(getFirstSuitaParent(item,false).getRectangle().y+
                        getFirstSuitaParent(item,false).getRectangle().getHeight()/2);}
        
    /*
     * positions dragging line
     * after suite parent
     */    
    public void lineAfterSuitaParent(Item item, int X){
        line[0] = (int)(getFirstSuitaParent(getTcParent(item,false),false).getRectangle().x-25);
        line[2] = X;
        line[1] = (int)(item.getRectangle().y+item.getRectangle().getHeight()+5);
        line[3] = line[1];
        line[4] = (int)(getFirstSuitaParent(item,false).getRectangle().y+
                        getFirstSuitaParent(item,false).getRectangle().getHeight()/2);}
        
    /*
     * positions dragging line
     * after tc parent
     */    
    public void lineAfterTcParent(Item item, int X){
        line[0] = (int)(getTcParent(item,false).getRectangle().x-25);
        line[1] = (int)(getTcParent(item,false).getSubItem(getTcParent(item,false).
                                                getSubItemsNr()-1).getRectangle().y+getTcParent(item,false).
                                                getSubItem(getTcParent(item,false).getSubItemsNr()-1).
                                                getRectangle().getHeight()+5);
        line[2] = X;
        line[3] = line[1];
        line[4] = (int)(getTcParent(item,false).getRectangle().y+getTcParent(item,false).getRectangle().getHeight()/2);}
        
    /*
     * position dragging line
     * based on context
     */
    public void handleDraggingLine(int X, int Y){
        Item item = null;
        item = getUpperItem(X,Y);
        if(item==null){
            line[0] = 0;
            line[1] = 5;
            line[2] = X;
            line[3] = 5;}
        else{
            if(item.getType()==2){//it is suite
                if(item.getRectangle().intersects(new Rectangle(0,Y-1,getWidth(),2))){//touches suite
                    if(item.getSubItemsNr()>0&&itemIsExpanded(item)){//it is expanded
//                         if(item.getSubItem(0).isPrerequisite()){//first element is prerequisite, must insert after
//                             lineUnderItem(item.getSubItem(0), X);}
//                         else{
                            lineInsideSuita(item,X);
//                         }
                    }
                    else{lineOnSuita(item,X);}}//it is not expanded
                else if (item.getSubItemsNr()>0&&itemIsExpanded(item)){//not touching suite but suite is expanded
//                     if(item.getSubItem(0).isPrerequisite()){//first element is prerequisite, must insert after
//                             lineUnderItem(item.getSubItem(0), X);}
//                     else 
                    lineInsideSuita(item,X);}
                else if(item.getRectangle().y+item.getRectangle().getHeight()+5<=Y){//upper half, on level before suite 
                    if(item.getPos().size()==1||
                        getFirstSuitaParent(item,false).getSubItemsNr()-1>
                        item.getPos().get(item.getPos().size()-1)){
                        lineUnderItem(item,X);}//on level 0
                    else lineAfterUpperParent(item, X);}//not on level 0
                else{lineUnderSuita(item,X);}}//lower half insert after suite
            else if(item.getType()==1){//it is tc
                if(item.getRectangle().intersects(new Rectangle(0,Y-1,getWidth(),2))){//touches tc
//                     if(item.isPrerequisite()){//tc is prerequisite, must insert after
//                         lineUnderItem(item, X);}
//                     else{//tc is not prerequisite, must interpret pos
                        boolean up = isUpperHalf(item,Y);
                        if(up){lineAboveTc(item,X);}//touches and in upper half
                        else{lineUnderItem(item,X);}
//                     }
                }//touches and in lower half
                else{//didn't touch tc
                    if(getFirstSuitaParent(item,false)!=null&&
                    getFirstSuitaParent(item,false).getSubItemsNr()-1==item.getPos().get(item.getPos().size()-1)){//tc is last and has parent
                        if(Y<item.getRectangle().y+item.getRectangle().getHeight()+5){
                            lineUnderItem(item, X);}//Should be inserted in upper parent
                        else if(!itemIsExpanded(item)){lineAfterUpperParent(item,X);}}//must insert after tc parent 
                    else{lineUnderItem(item,X);}}}//not last element or is on level 0, must insert on same level
            else{//it is prop
                if(getFirstSuitaParent(getTcParent(item,false),false)!=null &&
                getTcParent(item,false).getSubItemsNr()-1==item.getPos().get(item.getPos().size()-1) &&
                getFirstSuitaParent(getTcParent(item,false),false).getSubItemsNr()-1==getTcParent(item,false).
                getPos().get(getTcParent(item,false).getPos().size()-1) &&
                Y>item.getRectangle().y+item.getRectangle().getHeight()+5){//prop is last in tc, tc is last in suita, and mouse is in lower half
                    lineAfterSuitaParent(item,X);}
                else{lineAfterTcParent(item,X);}}}//not last prop, can insert after tc with this prop
        repaint();
        if(dragscroll){
            scrolldown = false;
            scrollup = false; 
            if(Y-RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().getValue()<10){
                int scrollvalue = RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().getValue();
                scrolldown = true;
                RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().setValue(scrollvalue-10);}
            else if(Y-RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().getValue()>RunnerRepository.window.
            mainpanel.p1.sc.pane.getSize().getHeight()-15){
                int scrollvalue = RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().getValue();
                scrollup = true; 
                RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().setValue(scrollvalue+10);}}}
            
    public String getArrayString(ArrayList<Integer> selected2){
        StringBuffer string = new StringBuffer();
        for(Integer el:selected2){
            string.append(el.toString()+" ");}
        return string.toString();}
        
        
    /*
     * check if this item is expanded,
     * has subelements that are visible
     */
    public boolean itemIsExpanded(Item item){
        for(Item i:item.getSubItems()){
            if(i.isVisible())return true;
        }
        return false;
    }
     
    /*
     * gets the item above
     * X, Y position
     */
    public Item getUpperItem(int X, int Y){
        int y1 = Y;
        Item upper=null;
        while(y1>0){
            for(int x1=0;x1<getWidth();x1+=5){
                getClickedItem(x1,y1);
                if(selected.size()>0){
                    upper=getItem(selected,false);
                    if(upper!=null)break;}
                if(upper!=null)break;}
            y1-=5;}
        return upper;}
        
        
    /*
     * return next visible elment
     */
    public Item nextItem(Item item){
        if(item.getSubItemsNr()>0){
            for(int i=0;i<item.getSubItemsNr();i++){
                Item subitem = item.getSubItem(i);
                if(subitem.isVisible()){
                    return subitem;}
            }
            Item parent;
            if(item.getType()!=0)parent = getFirstSuitaParent(item, false);
            else parent = getTcParent(item, false);   
            if(parent!=null){
                return iterateBack(parent,item.getPos().get(item.getPos().size()-1));
            }
            return null;
        }
        Item parent;
        if(item.getType()!=0)parent = getFirstSuitaParent(item, false);
        else parent = getTcParent(item, false);
        if(parent!=null){
            return iterateBack(parent,item.getPos().get(item.getPos().size()-1));
        }
        return null;
    }
    
    /*
     * item - parent of item 
     * index - the index of item in parent
     */
    public Item iterateBack(Item item, int index){
        if(item.getSubItemsNr()-1>index){
            
            Item subitem = item.getSubItem(index+1);
            if(subitem.isVisible())return subitem;
            return iterateBack(item,index+1);
            
            
            
        }
        Item parent;
        if(item.getType()!=0)parent = getFirstSuitaParent(item, false);
        else parent = getTcParent(item, false);
        if(parent!=null){
            return iterateBack(parent,item.getPos().get(item.getPos().size()-1));
        }
        int nr = item.getPos().get(0);
        if(RunnerRepository.getSuiteNr()-1>nr){
            return RunnerRepository.getSuita(nr+1);
        }
        return null;
    }
        
        
    /*
     * previous item on same line
     * if none returns parent
     */
    public Item prevInLine(Item item){
        ArrayList <Integer> temp =(ArrayList <Integer>)item.getPos().clone();
        if(temp.size()>1){
            if(temp.get(temp.size()-1)>0){
                temp.set(temp.size()-1, temp.get(temp.size()-1)-1);
                Item previous = getItem(temp, false);
                if(previous.isVisible()){
                    return previous;
                }
                return prevInLine(previous);
            }
            else{
                temp.remove(temp.size()-1);                
                return getItem(temp, false);
            }
        }
        else if(temp.get(0)>0){
            return RunnerRepository.getSuita(temp.get(0)-1);
        }
        return item;
    }
    
    /*
     * the item immediately befor item
     */
    public Item previousItem(Item item){
        ArrayList <Integer> temp =(ArrayList <Integer>)item.getPos().clone();
        if(temp.size()>1){
            if(temp.get(temp.size()-1)>0){
                temp.set(temp.size()-1, temp.get(temp.size()-1)-1);
                Item previous = getItem(temp, false);
                if(previous.isVisible()){
                    return lastVisible(previous);
                }
                return previousItem(previous);
            }
            else{
                //if(item.isVisible())return item;
                temp.remove(temp.size()-1);
                
                return getItem(temp, false);
            }
        }
        else if(temp.get(0)>0){
            return lastVisible(RunnerRepository.getSuita(temp.get(0)-1));
        }
        return item;
    }
        
        
    
     
    /*
     * returns last visible element after 
     * element at pos
     */    
    public Item lastVisible(Item item){//last visible elem under pos
        if(item.getSubItemsNr()>0){
            for(int i=item.getSubItemsNr()-1;i>-1;i--){
                Item subitem = item.getSubItem(i);
                if(subitem.isVisible()){
                    return lastVisible(subitem);
                }
            }
            return item;
        }
        return item;
    }
        
        
     
      
    /*
     * prints this item and his 
     * subitems indices on screen
     */
    public static void printPos(Item item){
        if(item.getType()==0||item.getType()==1||item.getType()==2){
            System.out.print(item.getName()+" - ");
            for(int i=0;i<item.getPos().size();i++){
                System.out.print(item.getPos().get(i));}
            System.out.println();}
        if(item.getType()==1||item.getType()==2){
            for(int i=0;i<item.getSubItemsNr();i++){
                printPos(item.getSubItem(i));}}}
                
    public boolean compareArrays(ArrayList<Integer> one, ArrayList<Integer> two){
        if(one.size()!=two.size())return false;
        else{
            for(int i=0;i<one.size();i++){
                if(one.get(i)!=two.get(i))return false;}
            return true;}}
    
    public void handleClick(MouseEvent ev){
        if(ev.getButton()==1){
            if(RunnerRepository.getSuiteNr()==0)return;
            if(keypress==0){
                deselectAll();
                getClickedItem(ev.getX(),ev.getY());
                if(selected.size()>0){
                    selectItem(selected);
                    if(getItem(selected,false).getType()==2){
                        Item temp = getItem(selected,false);
                        int userDefNr = temp.getUserDefNr();
                        boolean root = false;
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(temp);
                        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(temp);
                        if(temp.getPos().size()==1){
                            root = true;
                            if(userDefNr!=RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()){
                                System.out.println("Warning, suite "+
                                    temp.getName()+" has "+userDefNr+" fields while in bd.xml are defined "+
                                    RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()+" fields");
                                if(RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()<userDefNr){
                                    temp.getUserDefs().subList(RunnerRepository.window.mainpanel.p1.suitaDetails.
                                    getDefsNr(),userDefNr).clear();}}
                            try{
                                for(int i=0;i<RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr();i++){
                                    if(temp.getUserDefNr()==i)break;
                                    RunnerRepository.window.mainpanel.p1.suitaDetails.getDefPanel(i).
                                                        setDescription(temp.getUserDef(i)[1],true);}}
                            catch(Exception e){e.printStackTrace();}   
                        }
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setSuiteDetails(root);
                    }
                    if(getItem(selected,false).getType()==1){
                        Item temp = getItem(selected,false);
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(temp);
                        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(temp);
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
                    }
                    if(getItem(selected,false).getCheckRectangle().intersects(
                                  new Rectangle(ev.getX()-1,ev.getY()-1,2,2))){
                        Item select = getItem(selected,false);
                        select.setCheck(!select.getCheck(),true);
                        
                        if(select.getCheck()&&select.getType()==1){//if element is tc and selected, select all upper suites
                            Item suita = getFirstSuitaParent(select, false);
                            while(suita!=null){
                                suita.setCheck(true, false);
                                suita = getFirstSuitaParent(suita, false);
                            }
                        }
//                         getItem(selected,false).setCheck(!getItem(selected,false).getCheck());
                    }
                    else if(getItem(selected,false).getSubItemsNr()>0&&ev.getClickCount()==2 &&
                            getItem(selected,false).getType()!=1){
                        if(getItem(selected,false).getType()==2 &&
                        !itemIsExpanded(getItem(selected,false))){
                            if(!onlyOptionals)getItem(selected,false).setVisibleTC();
                            else{
                                Item parent = getItem(selected,false);
                                for(Item i:parent.getSubItems()){
                                    if(i.isOptional()){
                                        i.setSubItemVisible(true);
                                        i.setSubItemVisible(true);
                                        i.setVisible(false);
                                    }
                                }
                            }
                        }
                        else getItem(selected,false).setVisible(
                            !itemIsExpanded(getItem(selected,false)));
                    }
                updateLocations(getItem(selected,false));}
                else{
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setGlobalDetails();}
                repaint();}
            else if(keypress==2){
                getClickedItem(ev.getX(),ev.getY());
                if(selected.size()>0){
                    int [] theone = new int[selected.size()];
                    for(int i=0;i<selected.size();i++){theone[i]= selected.get(i).intValue();}
                    Item theone1 = getItem(selected,false);
                    theone1.select(!theone1.isSelected());
                    if(theone1.isSelected())selectedcollection.add(theone);
                    else{
                        for(int m=0;m<selectedcollection.size();m++){
                            if(Arrays.equals(selectedcollection.get(m),theone)){
                                selectedcollection.remove(m);
                                break;}}}
                    if(selectedcollection.size()==0){
                        RunnerRepository.window.mainpanel.p1.remove.setEnabled(false);}
                    repaint();}}
            else{// selection with shift
                if(selected.size()>0){
                    deselectAll();
                    int [] theone1 = new int[selected.size()];
                    for(int i=0;i<selected.size();i++){theone1[i]= selected.get(i).intValue();}
                    getClickedItem(ev.getX(),ev.getY());
                    int [] theone2 = new int[selected.size()];
                    for(int i=0;i<selected.size();i++){theone2[i]= selected.get(i).intValue();}
                    if(theone1.length==theone2.length){
                        if(theone1.length>1){
                            int [] temp1,temp2;
                            temp1 = Arrays.copyOfRange(theone1,0,theone1.length-1);
                            temp2 = Arrays.copyOfRange(theone2,0,theone2.length-1);
                            if(Arrays.equals(temp1,temp2)){
                                int [] first,second;
                                if(theone2[theone2.length-1]>=theone1[theone1.length-1]){
                                    first = theone2;
                                    second = theone1;}
                                else{
                                    first = theone1;
                                    second = theone2;}
                                ArrayList<Integer>temp11 = new ArrayList<Integer>();
                                for(int i=0;i<temp1.length;i++)temp11.add(new Integer(temp1[i]));
                                Item parent = getItem(temp11,false);
                                for(int i=second[second.length-1];i<first[first.length-1]+1;i++){
                                    parent.getSubItem(i).select(true);
                                    int [] temporary = new int[parent.getSubItem(i).getPos().size()];
                                    for(int m=0;m<temporary.length;m++){
                                        temporary[m]=parent.getSubItem(i).getPos().get(m).intValue();}
                                    selectedcollection.add(temporary);}}}
                        else{
                            int first,second;
                            if(theone1[0]>=theone2[0]){
                                first = theone1[0];
                                second = theone2[0];}
                            else{
                                second = theone1[0];
                                first = theone2[0];}
                            for(int m=second;m<first+1;m++){
                                RunnerRepository.getSuita(m).select(true);
                                selectedcollection.add(new int[]{m});}}}
                    repaint();}}}
        if(ev.getButton()==3){
            getClickedItem(ev.getX(),ev.getY());            
            if((selected.size()==0)){
                if(RunnerRepository.getSuiteNr()>0){
                    deselectAll();                    
                    repaint();}
                noSelectionPopUp(ev);}
            else{if(!getItem(selected,false).isSelected()){
                    deselectAll();
                    selectItem(selected);
                    repaint();
                    Item temp = getItem(selected,false);
                    if(temp.getType()==0) propertyPopUp(ev,getItem(selected,false));
                    else if(temp.getType()==1){
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(temp);
                        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(temp);
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
                        tcPopUp(ev,getItem(selected,false));}
                    else{                        
                        boolean root = false;
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(temp);
                        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(temp);
                        if(temp.getPos().size()==1){//if it is a root suite
                            root = true;
                            int userDefNr = temp.getUserDefNr();   
                            if(userDefNr!=RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()){
                                System.out.println("Warning, suite "+
                                    temp.getName()+" has "+userDefNr+" fields while in bd.xml are defined "+
                                    RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()+" fields");
                                if(RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()<userDefNr){
                                    temp.getUserDefs().subList(RunnerRepository.window.mainpanel.p1.suitaDetails.
                                    getDefsNr(),userDefNr).clear();}}
                            try{    
                                for(int i=0;i<RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr();i++){
                                    if(temp.getUserDefNr()==i)break;
                                    RunnerRepository.window.mainpanel.p1.suitaDetails.getDefPanel(i).
                                                        setDescription(temp.getUserDef(i)[1],true);}}
                            catch(Exception e){e.printStackTrace();}
                        }
                        RunnerRepository.window.mainpanel.p1.suitaDetails.setSuiteDetails(root);
                        suitaPopUp(ev, temp);
                    }
                }
                else{if(selectedcollection.size()==1){
                        if(getItem(selected,false).getType()==0) propertyPopUp(ev,getItem(selected,false));
                        else if(getItem(selected,false).getType()==1){
                            Item temp = getItem(selected,false);
                            RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(temp);
                            RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(temp);
                            RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
                            tcPopUp(ev,getItem(selected,false));
                        }
                        else{
                            Item temp = getItem(selected,false);
                            boolean root = false;
                            RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(temp);
                            RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(temp);
                            if(temp.getPos().size()==1){//if it is a root suite
                                root = true;
                                int userDefNr = temp.getUserDefNr();                            
                                if(userDefNr!=RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()){
                                    System.out.println("Warning, suite "+
                                        temp.getName()+" has "+userDefNr+" fields while in bd.xml are defined "+
                                        RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()+" fields");
                                    if(RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr()<userDefNr){
                                        temp.getUserDefs().subList(RunnerRepository.window.mainpanel.p1.suitaDetails.
                                        getDefsNr(),userDefNr).clear();}}
                                try{    
                                    for(int i=0;i<RunnerRepository.window.mainpanel.p1.suitaDetails.getDefsNr();i++){
                                        if(temp.getUserDefNr()==i)break;
                                        RunnerRepository.window.mainpanel.p1.suitaDetails.getDefPanel(i).
                                                            setDescription(temp.getUserDef(i)[1],true);}}
                                catch(Exception e){e.printStackTrace();}}
                            RunnerRepository.window.mainpanel.p1.suitaDetails.setSuiteDetails(root);                            
                            suitaPopUp(ev,getItem(selected,false));
                        }
                    }
                    else{
                        multipleSelectionPopUp(ev);}}}}
        if(selectedcollection.size()>0)RunnerRepository.window.mainpanel.p1.remove.setEnabled(true);}
    
    /*
     * popup in case nothing 
     * is selected
     */
    public void noSelectionPopUp(final MouseEvent ev){ 
        p.removeAll();
        JMenuItem item = new JMenuItem("Add Suite");        
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev2){
                int y1 = ev.getY();
                Item upper=null;
                while(y1>0){
                    y1-=5;
                    getClickedItem(ev.getX(),y1);
                    if(selected.size()>0){
                            upper=getItem(selected,false);
                        if(upper!=null){
                            break;}}}
                if(upper!=null&&upper.getType()==1){
                    int index = upper.getPos().get(upper.getPos().size()-1).intValue();
                    int position = upper.getPos().size()-1;
                    ArrayList<Integer> temp = (ArrayList<Integer>)upper.getPos().clone();
                    if(temp.size()>1)temp.remove(temp.size()-1);
                    Item parent = getItem(temp,false);
                    int j = upper.getPos().get(upper.getPos().size()-1).intValue()+1;
                    for(;j<parent.getSubItemsNr();j++){   
                        parent.getSubItem(j).updatePos(position,
                        new Integer(parent.getSubItem(j).getPos().get(position).intValue()+1));}
                    (new AddSuiteFrame(Grafic.this, parent,index+1)).setLocation(ev.getX()-50,ev.getY()-50);}
                else{
                    (new AddSuiteFrame(Grafic.this, null,0)).setLocation((int)ev.getLocationOnScreen().getX()-50,
                                                                    (int)ev.getLocationOnScreen().getY()-50);}}});//add suite
        p.show(this,ev.getX(),ev.getY());}
        
    /*
     * popup in case property
     * is selected
     */
    public void propertyPopUp(MouseEvent ev,final Item prop){
        if(prop.getPos().get(prop.getPos().size()-1)!=0){
            p.removeAll();        
            JMenuItem item = new JMenuItem("Redefine");
            p.add(item);
            item.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){redefineProp(prop);}});
            item = new JMenuItem("Remove");
            p.add(item);
            item.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){
                    removeSelected();
                }});
            p.show(this,ev.getX(),ev.getY());}}
    
    /*
     * prompt user to redefine property
     */
    public void redefineProp(Item prop){
        boolean found = true;
        JTextField name = new JTextField(prop.getName());  
        if(prop.getName().equals("param"))name.setEnabled(false);
        JTextField value = new JTextField(prop.getValue());
        Object[] message = new Object[] {"Name", name, "Value", value};
        while(found){
            JPanel p = getPropPanel(name,value);            
            int resp = (Integer)CustomDialog.showDialog(p,JOptionPane.PLAIN_MESSAGE, 
                            JOptionPane.OK_CANCEL_OPTION, Grafic.this, "Property: value",null);
            if(resp == JOptionPane.OK_OPTION&&(!(name.getText()+value.getText()).equals(""))){
                if(name.getText().equals("param")&&name.isEnabled()){
                    CustomDialog.showInfo(JOptionPane.WARNING_MESSAGE, Grafic.this,
                                        "Warning", "'param' is used for parameters, please use a different name");
                    continue;}
                else found = false;
                FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", 0, 11));
                int width = metrics.stringWidth(name.getText()+":  "+value.getText()) + 40;
                prop.setName(name.getText());
                prop.setValue(value.getText());
                prop.getRectangle().setSize(width, (int)(prop.getRectangle().getHeight()));
                repaint();}
            else found = false;}}            
           
    /*
     * name value panel created
     * for adding props
     */        
    public JPanel getPropPanel(JTextField name, JTextField value){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel jPanel1 = new JPanel();
        JLabel jLabel3 = new JLabel();
        JPanel jPanel2 = new JPanel();
        JLabel jLabel4 = new JLabel();
        jPanel1.setLayout(new java.awt.BorderLayout());
        jLabel3.setText("Name: ");
        jPanel1.add(jLabel3, BorderLayout.CENTER);
        p.add(jPanel1);
        p.add(name);
        jPanel2.setLayout(new BorderLayout());
        jLabel4.setText("Value: ");
        jPanel2.add(jLabel4, BorderLayout.CENTER);
        p.add(jPanel2);
        p.add(value);
        return p;}
     
    /*
     * popup in case there
     * are multiple items selected
     */
    public void multipleSelectionPopUp(MouseEvent ev){
        p.removeAll();        
        JMenuItem menuitem = new JMenuItem("Remove");
        p.add(menuitem);
        menuitem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                removeSelected();}});
        final int nr = selectedcollection.size();
        final ArrayList<Integer>temp = new ArrayList<Integer>();
        byte type = areTheSame(nr);
        if(type!=-1){
            if(type!=0){
                menuitem = new JMenuItem("Switch Check");
                p.add(menuitem);
                menuitem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ev){
                        switchCheck();}});}
            if(type==1){
                menuitem = new JMenuItem("Add Configurations");
                p.add(menuitem);
                menuitem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ev){
                        addConfigurations(false);}});
                menuitem = new JMenuItem("Switch Runnable");
                p.add(menuitem);
                menuitem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ev){
                        switchRunnable();}});
                menuitem = new JMenuItem("Switch Optional");
                p.add(menuitem);
                menuitem.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent ev){
                        switchOptional();}});}}
        p.show(this,ev.getX(),ev.getY());}
        
    private void addConfigurations(boolean unitary){
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setPreferredSize(new Dimension(515,200));
        JLabel ep = new JLabel("Configurations: ");
        ep.setBounds(5,5,95,25);
        JList tep = new JList();
        JScrollPane scep = new JScrollPane(tep);
        scep.setBounds(100,5,400,180);
        p.add(ep);
        p.add(scep);
        String [] vecresult = RunnerRepository.window.mainpanel.p4.getTestConfig().tree.getFiles();
        tep.setModel(new DefaultComboBoxModel(vecresult));
        if(unitary){
            Item item=null;
            int nr = selectedcollection.size();
            ArrayList<Integer>temp = new ArrayList<Integer>();
            for(int i=0;i<nr;i++){
                temp.clear();
                int [] indices = selectedcollection.get(i);
                for(int j=0;j<indices.length;j++)temp.add(new Integer(indices[j]));
                item = getItem(temp,false);
            }
            ArrayList<String> array = new ArrayList<String>(Arrays.asList(vecresult));
            ArrayList<String> torem = new ArrayList<String>();
            for(int i=0;i<vecresult.length;i++){
                for(Configuration conf:item.getConfigurations()){
                    if(conf.getFile().equals(vecresult[i])){
                        torem.add(vecresult[i]);
                        break;
                    }
                }
            }
            for(String s:torem){
                array.remove(s);
            }
            Object [] resultingarray = array.toArray();
            tep.setModel(new DefaultComboBoxModel(resultingarray));
        }
        int resp = (Integer)CustomDialog.showDialog(p,JOptionPane.PLAIN_MESSAGE, 
                            JOptionPane.OK_CANCEL_OPTION, Grafic.this, "Test Configurations Files",null);
        if(resp == JOptionPane.OK_OPTION){
            Item item=null;
            int nr = selectedcollection.size();
            ArrayList<Integer>temp = new ArrayList<Integer>();
            for(int i=0;i<nr;i++){
                temp.clear();
                int [] indices = selectedcollection.get(i);
                for(int j=0;j<indices.length;j++)temp.add(new Integer(indices[j]));
                item = getItem(temp,false);
                for(Object selection:tep.getSelectedValues()){
                    boolean goon = true;
                    for(Configuration config:item.getConfigurations()){//check for existing file in configuration
                        if(config.getFile().equals(selection.toString())){
                            goon = false;
                            break;
                        }
                    }               
                    if(goon)item.getConfigurations().add(new Configuration(selection.toString()));
                }
            }
            TestConfigManagement tcm = RunnerRepository.window.mainpanel.p1.testconfigmngmnt;
            tcm.setParent(tcm.getConfigParent());
        }
        repaint();
    }
        
    public void switchOptional(){
        Item item=null;
        int nr = selectedcollection.size();
        ArrayList<Integer>temp = new ArrayList<Integer>();
        for(int i=0;i<nr;i++){
            temp.clear();
            int [] indices = selectedcollection.get(i);
            for(int j=0;j<indices.length;j++)temp.add(new Integer(indices[j]));
            item = getItem(temp,false);
            if(!item.isPrerequisite()&&!item.isTeardown())item.setOptional(!item.isOptional());}
        repaint();}
        
    /*
     * switch runnable for selected items
     */
    public void switchRunnable(){
        Item item=null;
        int nr = selectedcollection.size();
        ArrayList<Integer>temp = new ArrayList<Integer>();
        for(int i=0;i<nr;i++){
            temp.clear();
            int [] indices = selectedcollection.get(i);
            for(int j=0;j<indices.length;j++)temp.add(new Integer(indices[j]));
            item = getItem(temp,false);
            item.switchRunnable();}
        repaint();}
      
    /*
     * switch check for selected items
     */
    public void switchCheck(){
        Item item=null;
        int nr = selectedcollection.size();
        ArrayList<Integer>temp = new ArrayList<Integer>();
        for(int i=0;i<nr;i++){
            temp.clear();
            int [] indices = selectedcollection.get(i);
            for(int j=0;j<indices.length;j++)temp.add(new Integer(indices[j]));
            item = getItem(temp,false);
            item.setCheck(!item.getCheck(),true);}
        repaint();}
            
    /*
     * popup in case TC is selected
     */
    public void tcPopUp(MouseEvent ev, final Item tc){
        p.removeAll();
        JMenuItem item;
        item = new JMenuItem("Add Property");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                addTCProp(tc);}});
        item = new JMenuItem("Set Parameters");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                setParam(tc);}});
        item = new JMenuItem("Add Configurations");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                addConfigurations(true);}});
        item = new JMenuItem("Repeat");
        if(RunnerRepository.isMaster())p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                reapeatItem(tc);
            }});
        item = new JMenuItem("Remove");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                removeSelected();
            }});
        p.show(this,ev.getX(),ev.getY());}
       
    /*
     * popup in case suite is selected
     */
    public void suitaPopUp(MouseEvent ev,final Item suita){
        p.removeAll();
        JMenuItem item ;
        item = new JMenuItem("Add Suite");
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                new AddSuiteFrame(Grafic.this, suita,0);}});
        p.add(item);
        item = new JMenuItem("Expand");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                suita.setVisible(true);
                updateLocations(suita);
                repaint();}});
        item = new JMenuItem("Contract");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                int nr = suita.getSubItemsNr();
                for(int i=0;i<nr;i++){
                    suita.getSubItem(i).setVisible(false);}
                suita.setVisible(false);
                updateLocations(suita);
                repaint();}});
        item = new JMenuItem("Export");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                exportSuiteToPredefined(suita);
            }});
        item = new JMenuItem("Repeat");
        if(RunnerRepository.isMaster())p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                reapeatItem(suita);
            }});
        item = new JMenuItem("Remove");
        p.add(item);
        item.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                if(suita.getPos().size()==1){//este pe nivelul 0
                    int index = suita.getPos().get(0).intValue();
                    RunnerRepository.getSuite().remove(suita);                    
                    if(RunnerRepository.getSuiteNr()>=index){
                        for(int i= index;i<RunnerRepository.getSuiteNr();i++){
                            RunnerRepository.getSuita(i).updatePos(0,new Integer(RunnerRepository.getSuita(i).
                                                                           getPos().get(0).intValue()-1));}
                    if(RunnerRepository.getSuiteNr()>0){
                        RunnerRepository.getSuita(0).setLocation(new int[]{5,10});
                        updateLocations(RunnerRepository.getSuita(0));}
                    repaint();
                    selectedcollection.clear();}}
                else{int index = suita.getPos().get(suita.getPos().size()-1).intValue();//not on level 0
                    int position = suita.getPos().size()-1;
                    ArrayList<Integer> temp = (ArrayList<Integer>)suita.getPos().clone();
                    temp.remove(temp.size()-1);
                    Item parent = getItem(temp,false);
                    parent.getSubItems().remove(suita);                    
                    if(parent.getSubItemsNr()>=index){
                        for(int i = index;i<parent.getSubItemsNr();i++){
                            parent.getSubItem(i).updatePos(position,new Integer(parent.getSubItem(i).getPos().
                                                                                get(position).intValue()-1));}}
                    updateLocations(parent);
                    repaint();
                    selectedcollection.clear();}}});     
        p.show(this,ev.getX(),ev.getY());}
        
    //repeat item window
    public void reapeatItem(Item item){
        String respons = CustomDialog.showInputDialog(JOptionPane.INFORMATION_MESSAGE, 
                                                            JOptionPane.OK_CANCEL_OPTION, 
                                                            Grafic.this, "", "Number of times:");
        int times;
        try{times = Integer.parseInt(respons);}//exit if respons is not integer
        catch(Exception e){
            return;
        }
        if(times==0)times=1;//cannot set 0 as repeat
        item.setRepeat(times);
        
        FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.BOLD, 14));
        int width = metrics.stringWidth(times+"X "+item.getName())+40;
        item.getRectangle().setSize(width,(int)item.getRectangle().getHeight());
        if(item.isVisible())updateLocations(item);
        repaint();
        
        
    }
        
    /*
     * export this suite in predefined suites
     */
    public void exportSuiteToPredefined(Item suite){
        String user = CustomDialog.showInputDialog(JOptionPane.QUESTION_MESSAGE,
                                                JOptionPane.OK_CANCEL_OPTION, RunnerRepository.window,
                                                "File Name","Please enter suite file name");
        if(user!=null&&!user.equals("")){
            ArrayList<Item>array = new ArrayList<Item>();
            array.add(suite);
            boolean cond;
            if(RunnerRepository.isMaster()){
                cond = printXML(RunnerRepository.temp+RunnerRepository.getBar()+"Twister"+RunnerRepository.getBar()+"Users"+RunnerRepository.getBar()+user+".xml",
                         false,false,
                         RunnerRepository.window.mainpanel.p1.suitaDetails.stopOnFail(),
                         RunnerRepository.window.mainpanel.p1.suitaDetails.preStopOnFail(),
                         RunnerRepository.window.mainpanel.p1.suitaDetails.saveDB(),
                         RunnerRepository.window.mainpanel.p1.suitaDetails.getDelay(),
                         true,array,RunnerRepository.window.mainpanel.p1.suitaDetails.getProjectDefs(),
                         RunnerRepository.window.mainpanel.p1.suitaDetails.getGlobalDownloadType());
            } else {
                cond = printXML(RunnerRepository.temp+RunnerRepository.getBar()+"Twister"+RunnerRepository.getBar()+"Users"+RunnerRepository.getBar()+user+".xml",
                         false,false,
                         RunnerRepository.window.mainpanel.p1.suitaDetails.stopOnFail(),
                         RunnerRepository.window.mainpanel.p1.suitaDetails.preStopOnFail(),
                        RunnerRepository.window.mainpanel.p1.suitaDetails.saveDB(),
                         RunnerRepository.window.mainpanel.p1.suitaDetails.getDelay(),
                         true,array,RunnerRepository.window.mainpanel.p1.suitaDetails.getProjectDefs(),null);
            }
            if(cond){
                CustomDialog.showInfo(JOptionPane.PLAIN_MESSAGE, 
                                        RunnerRepository.window, "Success",
                                        "File successfully saved");
                RunnerRepository.window.mainpanel.p1.lp.refreshTree(100,100);
            }
                
            else CustomDialog.showInfo(JOptionPane.WARNING_MESSAGE, 
                                        RunnerRepository.window, "Warning", 
                                        "Warning, file not saved");}
    }
    
    /*
     * set tc prerequisite
     */
    public void setPreRequisites(Item tc){
        Item i = getFirstSuitaParent(tc, false);
        tc.setPrerequisite(true);
        tc.setTeardown(false);
        tc.setOptional(false);
        sortTearSetup(i);
        updateLocations(i);
        deselectAll();
        selectItem(tc.getPos());
        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(tc);
        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(tc);
        RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
        repaint();}
        
    public void unsetPrerequisite(Item tc){
        Item i = getFirstSuitaParent(tc, false);
        tc.setPrerequisite(false);
        sortTearSetup(i);
        updateLocations(i);
        deselectAll();
        selectItem(tc.getPos());
        repaint();
    }
            
            
    /*
     * set tc teardown
     */
    public void setTeardown(Item tc){
        Item i = getFirstSuitaParent(tc, false);
        tc.setTeardown(true);
        tc.setPrerequisite(false);
        sortTearSetup(i);
        updateLocations(i);
        deselectAll();
        selectItem(tc.getPos());
        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(tc);
        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(tc);
        RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
        repaint();}
        
    public void unsetTeardown(Item tc){
        Item i = getFirstSuitaParent(tc, false);
        tc.setTeardown(false);
        sortTearSetup(i);
        updateLocations(i);
        deselectAll();
        selectItem(tc.getPos());
        repaint();
    }
            
    public void setOptional(Item tc){
        if(tc.isOptional()){
            tc.setOptional(false);
        }
        else{
            tc.setOptional(true);
        }
        repaint();
    }
        
    /*
     * prompt user to add
     * prop to tc
     */
    public void addTCProp(Item tc){       
        boolean found = true;
        JTextField name = new JTextField();   
        JTextField value = new JTextField();
        while(found){            
            JPanel p = getPropPanel(name,value);
            int r = (Integer)CustomDialog.showDialog(p,JOptionPane.PLAIN_MESSAGE, 
                                                    JOptionPane.OK_CANCEL_OPTION, 
                                                    Grafic.this, "Property: value",null);
            if(r == JOptionPane.OK_OPTION&&(!(name.getText()+value.getText()).equals(""))){
                if(name.getText().equals("param")){
                    CustomDialog.showInfo(JOptionPane.WARNING_MESSAGE, Grafic.this, 
                                            "Warning", "'param' is used for parameters,"+
                                            " please use a different name");
                    continue;}
                else found = false;
                ArrayList <Integer> indexpos3 = (ArrayList <Integer>)tc.getPos().clone();
                indexpos3.add(new Integer(tc.getSubItemsNr()));
                Item property = new Item(name.getText(),0,-1,-1,0,20,indexpos3);
                property.setValue(value.getText());
                property.setSubItemVisible(false);
                tc.addSubItem(property);
                RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(tc);
                RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(tc);
                RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
            }
            else found = false;}}
            
    /*
     * prompt user to add
     * param to tc
     */
    public void setParam(Item tc){
        JTextField name = new JTextField();
        name.setText("param");
        name.setEnabled(false);
        JTextField value = new JTextField();
        Object[] message = new Object[] {"Name", name, "Value", value};
        JPanel p = getParamPanel(name,value);        
        int resp = (Integer)CustomDialog.showDialog(p,JOptionPane.QUESTION_MESSAGE, 
                                                    JOptionPane.OK_CANCEL_OPTION, Grafic.this, 
                                                    "Property: value",null);
        if(resp == JOptionPane.OK_OPTION){
            ArrayList <Integer> indexpos3 = (ArrayList <Integer>)tc.getPos().clone();
            indexpos3.add(new Integer(tc.getSubItemsNr()));
            Item property = new Item(name.getText(),0,-1,-1,0,20,indexpos3);
            property.setValue(value.getText());
            if(!tc.getSubItem(0).isVisible())property.setSubItemVisible(false);
            tc.addSubItem(property);
            updateLocations(tc);
            RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(tc);
            RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(tc);
            RunnerRepository.window.mainpanel.p1.suitaDetails.setTCDetails();
        }}
    
    /*
     * panel displayed on
     * twister tc setParam
     */  
    public JPanel getParamPanel(JTextField name,JTextField value){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JPanel jPanel1 = new JPanel();
        JLabel jLabel3 = new JLabel();
        JPanel jPanel2 = new JPanel();
        JLabel jLabel4 = new JLabel();
        jPanel1.setLayout(new java.awt.BorderLayout());
        jLabel3.setText("Name: ");
        jPanel1.add(jLabel3, BorderLayout.CENTER);
        p.add(jPanel1);
        p.add(name);
        jPanel2.setLayout(new BorderLayout());
        jLabel4.setText("Value: ");
        jPanel2.add(jLabel4, BorderLayout.CENTER);
        p.add(jPanel2);
        p.add(value);
        return p;}
            
    public void setCanRequestFocus(boolean canrequestfocus){
        this.canrequestfocus = canrequestfocus;}
        
           
    /*
     * remove selected items from
     * RunnerRepository suites array
     * update indexes for next elements after removed
     * update position in graph
     */
    public void removeSelected(){      
        if(selectedcollection.size()>0){
            ArrayList<Item> fordeletion = new ArrayList<Item>();                
            int selectednr = selectedcollection.size();
            for(int i=0;i<selectednr;i++){
                ArrayList<Integer> temp = new ArrayList<Integer>();
                int indexsize = selectedcollection.get(i).length;
                for(int j=0;j<indexsize;j++){
                    temp.add(new Integer(selectedcollection.get(i)[j]));}
                Item theone = getItem(temp,false);
                if(theone.getType()==0&&theone.getPos().get(theone.getPos().size()-1)==0){//must not delete prop on level 0
                    theone.select(false);}
                else fordeletion.add(theone);}
            ArrayList<Item> unnecessary = new ArrayList<Item>();
            for(int i=0;i<fordeletion.size();i++){
                Item one = fordeletion.get(i);
                ArrayList<Integer>pos = (ArrayList<Integer>)one.getPos().clone();
                if(pos.size()>1){
                    pos.remove(pos.size()-1);
                    Item parent = getItem(pos,false);
                    if(parent.isSelected()){unnecessary.add(fordeletion.get(i));}}}
            for(int i=0;i<unnecessary.size();i++){fordeletion.remove(unnecessary.get(i));}
            int deletenr = fordeletion.size();
            for(int i=0;i<deletenr;i++){
                Item theone = fordeletion.get(i);
                if(theone.getPos().size()==1){
                    int index = theone.getPos().get(0).intValue();
                    RunnerRepository.getSuite().remove(theone);                    
                    if(RunnerRepository.getSuiteNr()>=index){
                        for(int k = index;k<RunnerRepository.getSuiteNr();k++){
                            RunnerRepository.getSuita(k).updatePos(0,new Integer(RunnerRepository.getSuita(k).
                                                            getPos().get(0).intValue()-1));}}}
                else{
                    int index = theone.getPos().get(theone.getPos().size()-1).intValue();
                    int position = theone.getPos().size()-1;
                    ArrayList<Integer> temporary = (ArrayList<Integer>)theone.getPos().clone();
                    temporary.remove(temporary.size()-1);
                    Item parent = getItem(temporary,false);
                    parent.getSubItems().remove(theone);                    
                    if(parent.getSubItemsNr()>=index){
                        for(int k = index;k<parent.getSubItemsNr();k++){
                            parent.getSubItem(k).updatePos(position,new Integer(parent.getSubItem(k).
                                                            getPos().get(position).intValue()-1));}}}}
            if(RunnerRepository.getSuiteNr()>0){
                RunnerRepository.getSuita(0).setLocation(new int[]{5,10});
                updateLocations(RunnerRepository.getSuita(0));}
            selectedcollection.clear();
            deselectAll();
            RunnerRepository.window.mainpanel.p1.suitaDetails.setGlobalDetails();
            RunnerRepository.window.mainpanel.p1.suitaDetails.clearDefs();
            RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(null);
            RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(null);
            repaint();}}
        
    /*
     * assign epid to item and its
     * subsuites
     */
//     public void assignEpID(Item item,String ID){
//         if(item.getType()==2){
//             item.setEpId(ID);
//             for(int i=0;i<item.getSubItemsNr();i++){
//                 assignEpID(item.getSubItem(i),ID);}}}                
     
    /*
     * remove tc
     */
    public void removeTC(Item tc){ 
        if(tc.getPos().size()>1){//tc nu e pe nivelul 0
            int index = tc.getPos().get(tc.getPos().size()-1).intValue();
            int position = tc.getPos().size()-1;
            ArrayList<Integer> temp = (ArrayList<Integer>)tc.getPos().clone();
            temp.remove(temp.size()-1);
            Item parent = getItem(temp,false);
            parent.getSubItems().remove(tc);                    
            if(parent.getSubItemsNr()>=index){
                for(int i = index;i<parent.getSubItemsNr();i++){
                    parent.getSubItem(i).updatePos(position,new Integer(parent.getSubItem(i).
                                                    getPos().get(position).intValue()-1));}}
            updateLocations(parent);
            repaint();}
        else{//tc pe nivelul 0    
            int index = tc.getPos().get(0).intValue();
            RunnerRepository.getSuite().remove(tc);                    
            if(RunnerRepository.getSuiteNr()>=index){
                for(int i= index;i<RunnerRepository.getSuiteNr();i++){
                    RunnerRepository.getSuita(i).updatePos(0,new Integer(RunnerRepository.getSuita(i).
                                                        getPos().get(0).intValue()-1));}
            if(RunnerRepository.getSuiteNr()>0){
                RunnerRepository.getSuita(0).setLocation(new int[]{5,10});
                updateLocations(RunnerRepository.getSuita(0));}
            repaint();
            selectedcollection.clear();}}}
        
    /*
     * deselect all the items that are selected
     */
    public void deselectAll(){
        RunnerRepository.window.mainpanel.p1.remove.setEnabled(false);
        int selectednr = selectedcollection.size()-1;
        for(int i=selectednr ; i>=0 ; i--){
            int [] itemselected = selectedcollection.get(i);
            Item theone = RunnerRepository.getSuita(itemselected[0]);
            for(int j=1;j<itemselected.length;j++){
                theone = theone.getSubItem(itemselected[j]);}
            theone.select(false);
            selectedcollection.remove(i);}}
     
    /*
     * select item based on
     * his pos indices
     */
    public void selectItem(ArrayList <Integer> pos){
        getItem(pos,false).select(true);
        int [] theone1 = new int[pos.size()];
        for(int i=0;i<pos.size();i++){
            theone1[i]= pos.get(i).intValue();}
        selectedcollection.add(theone1);}
    
    /*
     * get item at position x, y in suites frame
     */
    public void getClickedItem(int x, int y){
        Rectangle r = new Rectangle(x-1,y-1,2,2);
        int suitenr = RunnerRepository.getSuiteNr();
        selected = new ArrayList<Integer>();
        for(int i=0;i<suitenr;i++){
            if(handleClicked(r,RunnerRepository.getSuita(i))){
                selected.add(i);
                break;}}
        if(selected.size()>0)Collections.reverse(selected);}
        
    /*
     * get item with pos indices
     * test - from mastersuites or suites
     */
    public static Item getItem(ArrayList <Integer> pos,boolean test){           
        Item theone1;
        if(!test)theone1 = RunnerRepository.getSuita(pos.get(0));
        else theone1 = RunnerRepository.getTestSuita(pos.get(0));
        for(int j=1;j<pos.size();j++){
            theone1 = theone1.getSubItem(pos.get(j));}
        return theone1;}
    
    /*
     * return if item or its
     * subitem have been clicked
     */
    public boolean handleClicked(Rectangle r, Item item){
        if(r.intersects(item.getRectangle())&&item.isVisible())return true;
        else{int itemnr = item.getSubItemsNr();
            for(int i=0;i<itemnr;i++){
                if(handleClicked(r,item.getSubItem(i))){
                    selected.add(i);
                    return true;}}
            return false;}}
    
    /*
     * handle update locations
     * from suita on
     */
    public void updateLocations(Item suita){
        ArrayList <Integer> selected2 = (ArrayList <Integer>)suita.getPos().clone();
        if(selected2.size()>1){
            int index = selected2.get(0);
            selected2.remove(0);
            for(int i=index;i<RunnerRepository.getSuiteNr();i++){  
                iterateThrough(RunnerRepository.getSuita(i),selected2);
                selected2 = null;}}
        else if(selected2.size()==1){
            for(int i=selected2.get(0);i<RunnerRepository.getSuiteNr();i++){
                iterateThrough(RunnerRepository.getSuita(i),null);
            }
        }
        y=10;
        foundfirstitem=false;
        updateScroll();}
    
    /*
     * returns previous item from item
     * half width locatiuon
     */
    public int calcPreviousPositions(Item item){
        ArrayList <Integer> pos = (ArrayList <Integer>)item.getPos().clone();
        if(pos.size()>1){
            pos.remove(pos.size()-1);
            Item temp = getItem(pos,false);
            if(temp.getType()==2){
                return temp.getLocation()[0]+(int)((temp.getRectangle().getWidth())/2+20);}
            return temp.getLocation()[0]+(int)(temp.getRectangle().getWidth()/2+20);}
        else{return 5;}}
    
    /*
     * positions item based on
     * previous postion
     */
    public void positionItem(Item item){        
        int x = calcPreviousPositions(item);
        item.setLocation(new int[]{x,y});
        y+=(int)(10+item.getRectangle().getHeight());}
    
    /*
     * used for calculating items positions
     * iterates through subitems and positions them
     * based previous location
     */
    public void iterateThrough(Item item, ArrayList <Integer> theone){
        int subitemsnr = item.getSubItemsNr();
        if(theone==null){
            if(item.isVisible()){
                if(!foundfirstitem)y=item.getLocation()[1];
                foundfirstitem = true;
                positionItem(item);}
            for(int i=0;i<subitemsnr;i++){    
                iterateThrough(item.getSubItem(i),null);}}
        else if(theone.size()>1){
            int index = theone.get(0);
            theone.remove(0);
            for(int i=index;i<subitemsnr;i++){
                iterateThrough(item.getSubItem(i),theone);
                theone=null;}}
        else if(theone.size()==1){
            int index = theone.get(0);
            for(int i=index;i<subitemsnr;i++){
                iterateThrough(item.getSubItem(i),null);}}}
            
    public void paint(Graphics g){
        g.setColor(Color.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());
        drawDraggingLine(g);
        g.setColor(Color.BLACK);
        int suitenr = RunnerRepository.getSuiteNr();
        for(int i=0;i<suitenr;i++){
            handlePaintItem(RunnerRepository.getSuita(i),g);}}
      
    /*
     * actual drawing of dragging line
     */
    public void drawDraggingLine(Graphics g){
        if(line[0]!=-1){
            g.setColor(new Color(150,150,150));
            g.drawLine(line[0],line[1],line[2],line[3]);
            g.drawLine(line[0],line[3],line[0],line[4]);}}
    
    
    /*
     * handle paint item
     * and subitems
     */
    public void handlePaintItem(Item item, Graphics g){
        drawItem(item,g);
        int subitemnr = item.getSubItemsNr();
        if(subitemnr>0){
            for(int i=0;i<subitemnr;i++){
                if(!item.getSubItem(i).isVisible())continue;
                handlePaintItem(item.getSubItem(i),g);}}}

    /*
     * handles drawing item based 
     * on item type
     */
    public void drawItem(Item item,Graphics g){
        Font font = ((DefaultTreeCellRenderer)RunnerRepository.window.mainpanel.p1.ep.tree.getCellRenderer()).getFont();
        font = font.deriveFont(12);
        g.setFont(font);
        g.setColor(Color.BLACK);
        if(item.isSelected()){
            g.setColor(new Color(220,220,220));
            g.fillRect((int)item.getRectangle().getX(),(int)item.getRectangle().getY(),
                        (int)item.getRectangle().getWidth(),(int)item.getRectangle().getHeight());
            g.setColor(Color.BLACK);
            g.drawRect((int)item.getRectangle().getX(),(int)item.getRectangle().getY(),
                        (int)item.getRectangle().getWidth(),(int)item.getRectangle().getHeight());}
        if(item.getType()==2){
            //draw repeat
            if(item.getRepeat()>1){
                g.drawString(item.getRepeat()+"X "+item.getName(),(int)item.getRectangle().getX()+45,
                        (int)item.getRectangle().getY()+18);
            } else {
                g.drawString(item.getName(),(int)item.getRectangle().getX()+45,
                        (int)item.getRectangle().getY()+18);
            }
            g.drawImage(RunnerRepository.getSuitaIcon(),(int)item.getRectangle().getX()+25,
                        (int)item.getRectangle().getY()+1,null);
            //draw dependency icon if necesary
            if(item.getDependencies().size()>0){
                g.drawImage(RunnerRepository.getDependencyIcon(),(int)item.getRectangle().getX()+28,
                        (int)item.getRectangle().getY()+4,null);
            }
        }
        else if(item.getType()==1){
            if(item.isPrerequisite()||item.isTeardown())g.setColor(Color.RED);
            else if(!item.isRunnable())g.setColor(Color.GRAY);
            //draw repeat
            if(item.getRepeat()>1){
                g.drawString(item.getRepeat()+"X "+item.getName(),(int)item.getRectangle().getX()+50,
                        (int)item.getRectangle().getY()+15);
            } else {
                g.drawString(item.getName(),(int)item.getRectangle().getX()+50,
                        (int)item.getRectangle().getY()+15);
            }
            
            g.setColor(Color.BLACK);
            g.drawImage(RunnerRepository.getTCIcon(),(int)item.getRectangle().getX()+25,
                        (int)item.getRectangle().getY()+2,null);
            if(item.isOptional()){
                g.drawImage(RunnerRepository.optional,(int)item.getRectangle().getX()+43,
                        (int)item.getRectangle().getY()+1,null);
            }
            //draw dependency icon if necesary
            if(item.getDependencies().size()>0){
                g.drawImage(RunnerRepository.getDependencyIcon(),(int)item.getRectangle().getX()+28,
                        (int)item.getRectangle().getY()+2,null);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("- ");
            for(Configuration st:item.getConfigurations()){
                if(st.isEnabled()){
                    sb.append(st.getFile());
                    sb.append("; ");
                }
            }
            if(sb.length()>0) sb.deleteCharAt(sb.length()-2);
            g.drawString(sb.toString(),(int)(item.getRectangle().getX()+item.getRectangle().getWidth()),
                        (int)(item.getRectangle().getY()+15));
            
        }
        else{if(item.getPos().get(item.getPos().size()-1).intValue()==0){
            g.drawImage(RunnerRepository.getPropertyIcon(),
                        (int)item.getRectangle().getX()+2,
                        (int)item.getRectangle().getY()+1,null);}
            g.drawString(item.getName()+" : "+item.getValue(),
                        (int)item.getRectangle().getX()+25,
                        (int)item.getRectangle().getY()+15);}
        if((item.getPos().size()!=1)){
            if(item.getType()==0 &&
            item.getPos().get(item.getPos().size()-1).intValue()!=0){}
            else{g.setColor(new Color(180,180,180));
                g.drawLine((int)item.getRectangle().getX()-25,
                            (int)(item.getRectangle().getY()+item.getRectangle().getHeight()/2),
                            (int)item.getRectangle().getX(),
                            (int)(item.getRectangle().getY()+item.getRectangle().getHeight()/2));
                ArrayList<Integer> temp = (ArrayList<Integer>)item.getPos().clone();
                if(temp.get(temp.size()-1)==0){
                    g.drawLine((int)item.getRectangle().getX()-25,
                                (int)(item.getRectangle().getY()+item.getRectangle().getHeight()/2),
                                (int)item.getRectangle().getX()-25,(int)(item.getRectangle().getY())-5);}
                else{temp.set(temp.size()-1,new Integer(temp.get(temp.size()-1).intValue()-1));
                    Item theone = prevInLine(item);
                    g.drawLine((int)item.getRectangle().getX()-25,
                                (int)(item.getRectangle().getY()+item.getRectangle().getHeight()/2),
                                (int)item.getRectangle().getX()-25,
                                (int)(theone.getRectangle().getY()+theone.getRectangle().getHeight()/2));}
                g.setColor(Color.BLACK);}}
        if(item.getType()!=0){
            g.drawRect((int)item.getCheckRectangle().getX(),
                        (int)item.getCheckRectangle().getY(),
                        (int)item.getCheckRectangle().getWidth(),
                        (int)item.getCheckRectangle().getHeight());
            if(item.getCheck()){
                Rectangle r = item.getCheckRectangle();
                int x2[] = {(int)r.getX(),(int)r.getX()+(int)r.getWidth()/2,
                            (int)r.getX()+(int)r.getWidth(),
                            (int)r.getX()+(int)r.getWidth()/2};
                int y2[] = {(int)r.getY()+(int)r.getHeight()/2,
                            (int)r.getY()+(int)r.getHeight(),
                            (int)r.getY(),(int)r.getY()+(int)r.getHeight()-5};
                g.fillPolygon(x2,y2,4);}}
        if(item.getType()==2){
            int x = (int)(item.getRectangle().getX()+item.getRectangle().getWidth());
            if(item.getEpId()!=null && item.getEpId().length>0){
                font = font.deriveFont(11);
                g.setFont(font);
                StringBuilder EP = new StringBuilder();
                for(String s:item.getEpId()){
                    EP.append(s+";");
                }
                EP.deleteCharAt(EP.length()-1);
                g.drawString(" - "+EP.toString(),(int)(item.getRectangle().getX()+item.getRectangle().getWidth()),
                            (int)(item.getRectangle().getY()+18));
                FontMetrics metrics = g.getFontMetrics(font);
                int adv = metrics.stringWidth(" - "+EP.toString());
                x += adv+2;}
            StringBuilder sb = new StringBuilder();
            sb.append("- ");
            for(Configuration st:item.getConfigurations()){
                if(st.isEnabled()){
                    sb.append(st.getFile());
                    sb.append("; ");
                }
            }
            if(sb.length()>0) sb.deleteCharAt(sb.length()-2);
            g.drawString(sb.toString(),x,
                        (int)(item.getRectangle().getY()+18));
        }
        }
     
    /*
     * changes suites file name and sets
     * name accordingly on tab
     */
    public void setUser(String user){
        if(user!=null){
            RunnerRepository.window.mainpanel.p1.setOpenedfile(new File(user).getName());
        } else {
            RunnerRepository.window.mainpanel.p1.setOpenedfile("");
        }
        RunnerRepository.window.mainpanel.p1.suitaDetails.setGlobalDetails();
        RunnerRepository.window.mainpanel.p1.suitaDetails.clearDefs();
        RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(null);
        RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(null);
        this.user = user;}
    
    /*
     * returns file name and path
     */
    public String getUser(){
        return user;}        
        
    /*
     * parses xml and represents in grafic
     */    
    public void parseXML(File file){
//         new XMLReader(file).parseXML(getGraphics(),false);
        new XMLReader(file).parseXML(getGraphics(),false,RunnerRepository.getSuite(),true);
    }
        
    /*
     * writes xml on file
     * if skip => master xml
     */
    public boolean printXML(String user, boolean skip,
                            boolean local, boolean stoponfail,boolean prestoponfail,
                            String savedb, String delay,boolean lib, ArrayList<Item> array,
                            String [][] projdefined,String downloadlibraryoption){
        //skip = true
        try{if(array==null)array = RunnerRepository.getSuite();
            XMLBuilder xml = new XMLBuilder(array);
            boolean cond;
            if(RunnerRepository.isMaster()){
                cond = xml.createXML(skip,stoponfail,prestoponfail,false,
                          RunnerRepository.window.mainpanel.p1.suitaDetails.getPreScript(),
                          RunnerRepository.window.mainpanel.p1.suitaDetails.getPostScript(),
                          savedb,delay,RunnerRepository.window.mainpanel.p1.suitaDetails.getGlobalLibs(),
                          projdefined,downloadlibraryoption);
            } else {
                cond = xml.createXML(skip,stoponfail,prestoponfail,false,
                          RunnerRepository.window.mainpanel.p1.suitaDetails.getPreScript(),
                          RunnerRepository.window.mainpanel.p1.suitaDetails.getPostScript(),
                          savedb,delay,
                          RunnerRepository.window.mainpanel.p1.suitaDetails.getGlobalLibs(),
                          projdefined,null);
            }
            if(!cond){
                return false;
            }
            return xml.writeXMLFile(user,local,false,lib);}
        catch(Exception e){
            e.printStackTrace();
            return false;}}
        
    public int countSubtreeNr(int nr, Object child){
        boolean cond; //if it is directory or file
        cond = RunnerRepository.window.mainpanel.p1.ep.tree.getModel().isLeaf((TreeNode)child);
        ArrayList <TreeNode>list = new ArrayList<TreeNode>();        
        while ((TreeNode)child != null) {
            list.add((TreeNode)child);
            child = ((TreeNode)child).getParent();}
        Collections.reverse(list);
        child = new TreePath(list.toArray());
        if(cond){return nr+1;}
        else{int nr1 = RunnerRepository.window.mainpanel.p1.ep.tree.getModel().
                            getChildCount(((TreePath)child).getLastPathComponent());
            for(int j=0;j<nr1;j++){
                nr = countSubtreeNr(nr,RunnerRepository.window.mainpanel.p1.ep.tree.
                                    getModel().getChild((TreeNode)((TreePath)child).getLastPathComponent(),j));}
        return nr;}}
        
    public void drop(int x, int y,String source){
        if(source.equals("tc")){
            deselectAll();
            requestFocus();
            int max = RunnerRepository.window.mainpanel.p1.ep.getSelected().length;
            if(max>0){
                for(int i=0;i<max;i++){
                    boolean cond = RunnerRepository.window.mainpanel.p1.ep.tree.getModel().
                                        isLeaf((TreeNode)RunnerRepository.window.mainpanel.p1.
                                        ep.getSelected()[i].getLastPathComponent());//has no children                
                    if(cond){
                        String name = RunnerRepository.window.mainpanel.p1.ep.
                                        getSelected()[i].getPath()[RunnerRepository.window.mainpanel.p1.ep.getSelected()[i].
                                                                    getPathCount()-2]+"/"+RunnerRepository.window.mainpanel.p1.
                                                                    ep.getSelected()[i].getPath()[RunnerRepository.window.
                                                                    mainpanel.p1.ep.getSelected()[i].getPathCount()-1];
                        
                        try{name = name.split(RunnerRepository.getTestSuitePath())[1];}
                        catch(Exception e){
                            System.out.println("Could not find projects path:"+RunnerRepository.getTestSuitePath()+" in filename:"+name);
                            e.printStackTrace();}
                        FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
                        Item newItem = new Item(name,1, -1, -1, metrics.stringWidth(name)+48, 20, null);
                        ArrayList<Integer> pos = new ArrayList <Integer>();
                        pos.add(new Integer(0));
                        ArrayList<Integer> pos2 = (ArrayList <Integer>)pos.clone();
                        pos2.add(new Integer(0));
                        Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,pos2);
                        property.setValue("true");
                        property.setSubItemVisible(false);
                        newItem.addSubItem(property);
                        newItem.setVisible(false);
                        clone.add(newItem);}
                    else{
                        subtreeTC((TreeNode)RunnerRepository.window.mainpanel.p1.ep.getSelected()[i].getLastPathComponent(),null,0,"tc");}}
                handleMouseDroped(y);
            }
        } else if(source.equals("lib")){
            deselectAll();
            requestFocus();
            int max = RunnerRepository.window.mainpanel.p1.lp.getSelected().length;
            if(max>0){
                ArrayList<Item>list = new ArrayList();
                for(int i=0;i<max;i++){
                    boolean cond = RunnerRepository.window.mainpanel.p1.lp.tree.getModel().
                                        isLeaf((TreeNode)RunnerRepository.window.mainpanel.p1.
                                        lp.getSelected()[i].getLastPathComponent());//has no children                
                    if(cond){
                        String name = RunnerRepository.window.mainpanel.p1.lp.
                                        getSelected()[i].getPath()[RunnerRepository.window.mainpanel.p1.lp.getSelected()[i].
                                                                    getPathCount()-2]+"/"+RunnerRepository.window.mainpanel.p1.
                                                                    lp.getSelected()[i].getPath()[RunnerRepository.window.
                                                                    mainpanel.p1.lp.getSelected()[i].getPathCount()-1];
                        
                        try{
                            String path = RunnerRepository.getPredefinedSuitesPath();
                            String content = RunnerRepository.readPredefinedProjectFile(name);
                            File file = new File(RunnerRepository.temp+RunnerRepository.getBar()+"Twister"+
                                                 RunnerRepository.getBar()+"Users"+RunnerRepository.getBar()+name.replace(path, ""));
                            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                            writer.write(content);
                            writer.close();
                            new XMLReader(file).parseXML(getGraphics(), false, list, false);
                            for(Item itm :list){
                                clone.add(itm);
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                handleMouseDroped(y);
                Item parent = getFirstSuitaParent(list.get(0), false);
                if(parent!=null)checkSameName(parent);
            }
        }else if(source.equals("clearcase")){
            deselectAll();
            requestFocus();
            int max = RunnerRepository.window.mainpanel.p1.cp.getSelected().length;
            if(max>0){
                for(int i=0;i<max;i++){
                    boolean cond = RunnerRepository.window.mainpanel.p1.cp.tree.getModel().
                                        isLeaf((TreeNode)RunnerRepository.window.mainpanel.p1.
                                        cp.getSelected()[i].getLastPathComponent());//has no children                
                    if(cond){
                        String name = RunnerRepository.window.mainpanel.p1.cp.
                                        getSelected()[i].getPath()[RunnerRepository.window.mainpanel.p1.cp.getSelected()[i].
                                                                    getPathCount()-2]+"/"+RunnerRepository.window.mainpanel.p1.
                                                                    cp.getSelected()[i].getPath()[RunnerRepository.window.
                                                                    mainpanel.p1.cp.getSelected()[i].getPathCount()-1];
                        FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
                        Item newItem = new Item(name,1, -1, -1, metrics.stringWidth(name)+48, 20, null);
                        newItem.setClearcase(true);
                        ArrayList<Integer> pos = new ArrayList <Integer>();
                        pos.add(new Integer(0));
                        ArrayList<Integer> pos2 = (ArrayList <Integer>)pos.clone();
                        pos2.add(new Integer(0));
                        Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,pos2);
                        property.setValue("true");
                        property.setSubItemVisible(false);
                        newItem.addSubItem(property);
                        newItem.setVisible(false);
                        clone.add(newItem);}
                    else{
                        subtreeTC((TreeNode)RunnerRepository.window.mainpanel.p1.cp.getSelected()[i].getLastPathComponent(),null,0,"clearcase");}}
                handleMouseDroped(y);
            }
        }
    }
    
    /*
     * check if this item has children 
     * with same name and prompt to rename
     */
    private void checkSameName(Item item){
        for(Item i:item.getSubItems()){
            for(Item j:item.getSubItems()){
                if(j.getType()!=2)continue;
                if(i.getName().equals(j.getName())&&i!=j){
                    if(i.getType()!=2)continue;
                    deselectAll();
                    selectItem(j.getPos());
                    RunnerRepository.window.mainpanel.p1.remove.setEnabled(true);
                    RunnerRepository.window.mainpanel.p1.sc.pane.getVerticalScrollBar().setValue((int)j.getCheckRectangle().getCenterY());
                    String name = "";
                    while(name==null||name.equals("")||name.equals(j.getName())){
                        name = CustomDialog.showInputDialog(JOptionPane.WARNING_MESSAGE,
                                                            JOptionPane.OK_CANCEL_OPTION, 
                                                            Grafic.this, "Warning", 
                                                            "There is a suite with the same name, please rename.");
                        for(Item k:item.getSubItems()){
                            if(name.equals(k.getName())){
                                name = "";
                            }
                        }
                    }
                    j.setName(name);
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setParent(j);
                    RunnerRepository.window.mainpanel.p1.testconfigmngmnt.setParent(j);
                    RunnerRepository.window.mainpanel.p1.suitaDetails.setSuiteDetails(false);
                    repaint();
                }
            }
        }
    }
            
    public ArrayList<int []> getSelectedCollection(){
        return selectedcollection;}
        
    public int subtreeTC(Object child, Item parent, int location, String source){
        if(source.equals("tc")){
            
            boolean cond; //if it is directory or file
            cond = RunnerRepository.window.mainpanel.p1.ep.tree.getModel().isLeaf((TreeNode)child);
            ArrayList <TreeNode>list = new ArrayList<TreeNode>();        
            while ((TreeNode)child != null){
                list.add((TreeNode)child);
                child = ((TreeNode)child).getParent();}
            Collections.reverse(list);
            child = new TreePath(list.toArray());
            if(cond){
                if(parent==null){//called from jtree drop
                    String name = ((TreePath)child).getPath()[((TreePath)child).getPathCount()-2]+
                                                                "/"+((TreePath)child).getPath()[((TreePath)child).
                                                                                                getPathCount()-1];
                    name = name.split(RunnerRepository.getTestSuitePath())[1];
                    FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
                    Item newItem = new Item(name,1, -1, -1, metrics.stringWidth(name)+48, 20, null);
                    ArrayList<Integer> pos = new ArrayList <Integer>();
                    pos.add(new Integer(0));
                    ArrayList<Integer> pos2 = (ArrayList <Integer>)pos.clone();
                    pos2.add(new Integer(0));
                    Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,pos2);
                    property.setValue("true");
                    property.setSubItemVisible(false);
                    newItem.addSubItem(property);
                    newItem.setVisible(false);
                    clone.add(newItem);
                    return 0;}
                addNewTC(((TreePath)child).getPath()[((TreePath)child).getPathCount()-2]+
                                                    "/"+((TreePath)child).getPath()[((TreePath)child).
                                                    getPathCount()-1],parent,location,source);
                return location+1;}
            else{int nr = RunnerRepository.window.mainpanel.p1.ep.tree.getModel().
                                    getChildCount(((TreePath)child).getLastPathComponent());
                for(int j=0;j<nr;j++){
                    location = subtreeTC(RunnerRepository.window.mainpanel.p1.ep.tree.getModel().
                                        getChild((TreeNode)((TreePath)child).getLastPathComponent(),j),
                                        parent,location,source);}
                return location;}
        } else if(source.equals("clearcase")){
            
            boolean cond; //if it is directory or file
            cond = RunnerRepository.window.mainpanel.p1.cp.tree.getModel().isLeaf((TreeNode)child);
            ArrayList <TreeNode>list = new ArrayList<TreeNode>();        
            while ((TreeNode)child != null){
                list.add((TreeNode)child);
                child = ((TreeNode)child).getParent();}
            Collections.reverse(list);
            child = new TreePath(list.toArray());
            if(cond){
                if(parent==null){//called from jtree drop
                    String name = ((TreePath)child).getPath()[((TreePath)child).getPathCount()-2]+
                                                                "/"+((TreePath)child).getPath()[((TreePath)child).
                                                                                                getPathCount()-1];
                    //name = name.split(RunnerRepository.window.mainpanel.getP5().root)[1];
                    FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
                    Item newItem = new Item(name,1, -1, -1, metrics.stringWidth(name)+48, 20, null);
                    newItem.setClearcase(true);
                    ArrayList<Integer> pos = new ArrayList <Integer>();
                    pos.add(new Integer(0));
                    ArrayList<Integer> pos2 = (ArrayList <Integer>)pos.clone();
                    pos2.add(new Integer(0));
                    Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,pos2);
                    property.setValue("true");
                    property.setSubItemVisible(false);
                    newItem.addSubItem(property);
                    newItem.setVisible(false);
                    clone.add(newItem);
                    return 0;}
                addNewTC(((TreePath)child).getPath()[((TreePath)child).getPathCount()-2]+
                                                    "/"+((TreePath)child).getPath()[((TreePath)child).
                                                    getPathCount()-1],parent,location,source);
                return location+1;}
            else{int nr = RunnerRepository.window.mainpanel.p1.cp.tree.getModel().
                                    getChildCount(((TreePath)child).getLastPathComponent());
                for(int j=0;j<nr;j++){
                    location = subtreeTC(RunnerRepository.window.mainpanel.p1.cp.tree.getModel().
                                        getChild((TreeNode)((TreePath)child).getLastPathComponent(),j),
                                        parent,location,source);}
                return location;}
        }
        return -1;
            
            
    }
    /*
     * adds new tc, accepts a file that is tc and suite pos in vector
     */
    public void addNewTC(String file,Item parent,int location,String source){
        if(source.equals("tc")){
            file = file.split(RunnerRepository.getTestSuitePath())[1];
            FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
            ArrayList <Integer> indexpos = (ArrayList <Integer>)parent.getPos().clone();
            indexpos.add(new Integer(location));
            Item tc = new Item(file,1,-1,-1,metrics.stringWidth(file)+48,20,indexpos);
            ArrayList <Integer> indexpos2 = (ArrayList <Integer>)indexpos.clone();
            indexpos2.add(new Integer(0));
            Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,indexpos2);
            property.setSubItemVisible(false);
            property.setValue("true");
            tc.addSubItem(property);
            if(parent.getSubItemsNr()>0){if(!parent.getSubItem(0).isVisible())tc.setSubItemVisible(false);}
            tc.setVisible(false);
            parent.insertSubItem(tc,location);
        } else if(source.equals("clearcase")){
            //file = file.split(RunnerRepository.window.mainpanel.getP5().root)[1];
            FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
            ArrayList <Integer> indexpos = (ArrayList <Integer>)parent.getPos().clone();
            indexpos.add(new Integer(location));
            Item tc = new Item(file,1,-1,-1,metrics.stringWidth(file)+48,20,indexpos);
            tc.setClearcase(true);
            ArrayList <Integer> indexpos2 = (ArrayList <Integer>)indexpos.clone();
            indexpos2.add(new Integer(0));
            Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,indexpos2);
            property.setSubItemVisible(false);
            property.setValue("true");
            tc.addSubItem(property);
            if(parent.getSubItemsNr()>0){if(!parent.getSubItem(0).isVisible())tc.setSubItemVisible(false);}
            tc.setVisible(false);
            parent.insertSubItem(tc,location);
        }
    
    }
      
    /*
     * inserts new tc, accepts a file that is tc and suite pos in vector
     */
    public void insertNewTC(String file,ArrayList <Integer> pos,Item parent,Item item){
        Item tc = null;
        if(item==null){
            file = file.split(RunnerRepository.getTestSuitePath())[1];
            FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.PLAIN, 13));
            tc = new Item(file,1,-1,-1,metrics.stringWidth(file)+48,20,pos);
            ArrayList<Integer>pos2 = (ArrayList <Integer>)pos.clone();
            pos2.add(new Integer(0));
            Item property = new Item("Running",0,-1,-1,(metrics.stringWidth("Running:  true"))+28,20,pos2);
            property.setSubItemVisible(false);
            property.setValue("true");
            tc.addSubItem(property);
            tc.setVisible(false);}
        else{tc=item;
            tc.selectAll(tc, false);}
        if(parent.getSubItemsNr()>0)if(!parent.getSubItem(0).isVisible())tc.setSubItemVisible(false);
        parent.insertSubItem(tc,pos.get(pos.size()-1));
        updateLocations(parent);
        repaint();}
        
    public byte areTheSame(int nr){
        final ArrayList<Integer>temp = new ArrayList<Integer>();
        Item item;
        boolean same = true;
        byte type = 3;
        for(int i=0;i<nr;i++){
            temp.clear();
            int [] indices = selectedcollection.get(i);
            for(int j=0;j<indices.length;j++)temp.add(new Integer(indices[j]));
            item = getItem(temp,false);
            if(type!=3&&type!=item.getType()){
                same = false;
                break;}
            else if(type==3)type = (byte)item.getType();}
        if(same)return type;
        else return -1;}
        
    public int getLastY(Item item, int height){
        if(height<=(item.getRectangle().getY()+item.getRectangle().getHeight())){
            height=(int)(item.getRectangle().getY()+item.getRectangle().getHeight());        
            int nr = item.getSubItemsNr()-1;
            for(int i=nr;i>=0;i--){
                if(item.getSubItem(i).isVisible()){height = getLastY(item.getSubItem(i),height);}}
            return height;}
        else return height;}
       
    /*
     * scroll update method
     */     
    public void updateScroll(){
        int y1=0;
        for(int i=0;i<RunnerRepository.getSuiteNr();i++){
            if(RunnerRepository.getSuita(i).isVisible())y1 = getLastY(RunnerRepository.getSuita(i),y1);}
        if(y1>getHeight()){
            setPreferredSize(new Dimension(425,y1+10));
            revalidate();}
        if(getHeight()>595){
            if(y1<getHeight()-10){
                setPreferredSize(new Dimension(425,y1+10));
                revalidate();}
            if(y1<595){
                setPreferredSize(new Dimension(445,595));
                revalidate();}}}
              
    public void addSuiteFromButton(){
        if(selectedcollection.size()==0)new AddSuiteFrame(Grafic.this, null,0);
        else{
            ArrayList <Integer> temp = new ArrayList <Integer>();
            for(int j=0;j<selectedcollection.get(0).length;j++){
                temp.add(new Integer(selectedcollection.get(0)[j]));}
            if(selectedcollection.size()>1||getItem(temp,false).getType()!=2){
                CustomDialog.showInfo(JOptionPane.WARNING_MESSAGE, Grafic.this,
                                        "Warning", "Please select only one suite.");}
            else new AddSuiteFrame(Grafic.this, getItem(temp,false),0);}}
            
    public void setOnlyOptionals(boolean value){
        onlyOptionals = value;
    }
            
    public boolean getOnlyOptionals(){
        return onlyOptionals;
    }
    
    public void showOptionals(Item item){
        if(item==null){
            for(Item i:RunnerRepository.getSuite()){
                showOptionals(i);
            }
        }
        else if(item.getType()==1){
            if(!onlyOptionals){
                item.setSubItemVisible(true);
                item.setVisible(false);
            }
            else if(!item.isOptional()){
                item.setSubItemVisible(false);
            }
        }
        else if(item.getType()==2){
            for(int i=0;i<item.getSubItemsNr();i++){
                showOptionals(item.getSubItem(i));}}}
                
    /*
     * gets the first upper suite parent
     */    
    public static Item getFirstSuitaParent(Item item, boolean test){
        ArrayList <Integer> temp = (ArrayList <Integer>)item.getPos().clone();
        if(temp.size()==1)return null;
        if(item.getType()!=0){
            temp.remove(temp.size()-1);
            return getItem(temp,test);}
        else{
            temp.remove(temp.size()-1);
            temp.remove(temp.size()-1);
            return getItem(temp,test);}}
            
            
    public static Item getTcParent(Item item, boolean test){
        ArrayList <Integer> temp = (ArrayList <Integer>)item.getPos().clone();
        if(item.getType()==0){
            temp.remove(temp.size()-1);
            return getItem(temp,test);}
        return null;}
        
    /*
     * gets the first suite parent
     * at the head of the Hierarchy
     */
    public static Item getParent(Item item, boolean test){
        if(item.getPos().size()>1){
            ArrayList <Integer> temp = new ArrayList <Integer>();
            temp.add(item.getPos().get(0));
            return getItem(temp,test);}
        else return null;}
        
    /*
     * check if item is available in suites array(maybe it was deleted)
     */
    public static boolean chekItemInArray(Item item, boolean test){
        ArrayList <Integer> temp = (ArrayList <Integer>)item.getPos().clone();
        Item suiteitem;
        if(temp.size()==1){
            if(test){
                suiteitem =  RunnerRepository.getTestSuite().get(temp.get(0));
            } else {
                suiteitem = RunnerRepository.getSuita(temp.get(0));
            }
        } else {
            if(item.getType()==0){//property
                temp.remove(temp.size()-1);
                temp.remove(temp.size()-1);
                suiteitem = getItem(temp,test);
                suiteitem = suiteitem.getSubItem(item.getPos().get(item.getPos().size()-2));
                suiteitem = suiteitem.getSubItem(item.getPos().get(item.getPos().size()-1));
            } else if(item.getType()==1){//tc
                temp.remove(temp.size()-1);
                suiteitem = getItem(temp,test);
                if(suiteitem!=null){
                    if(suiteitem.getSubItemsNr()>item.getPos().get(item.getPos().size()-1)){
                        suiteitem = suiteitem.getSubItem(item.getPos().get(item.getPos().size()-1));
                    } else {
                        suiteitem = null;
                    }
                }
            } else {//suite
                suiteitem = getItem(temp,test);
            }
        }
        return suiteitem==item;
    }
        
    class AddSuiteFrame extends JFrame{
        private static final long serialVersionUID = 1L;
        JButton ok ;
        JTextField namefield;
        JList <String>epidfield;
        JComponent mainwindow;
        
        public void okAction(Item suita,int pos){     
            if(namefield.getText().equals("")){//don't allow empty names
                CustomDialog.showInfo(JOptionPane.WARNING_MESSAGE, Grafic.this,
                                        "Error", "SUT name must not be empty!");
                    return;
            }
            FontMetrics metrics = getGraphics().getFontMetrics(new Font("TimesRoman", Font.BOLD, 14));                
            int width = metrics.stringWidth(namefield.getText());
            if(suita!=null){  
                if(pos==0){
                    for(int j = 0;j<suita.getSubItemsNr();j++){                        
                        suita.getSubItem(j).updatePos(suita.getPos().size(),
                                                      new Integer(suita.getSubItem(j).getPos().
                                                                  get(suita.getPos().size()).intValue()+1));}
                    ArrayList <Integer> indexpos = (ArrayList <Integer>)suita.getPos().clone();
                    indexpos.add(new Integer(0));
                    Item item = new Item(namefield.getText(),2, -1,5, width+40,25 , indexpos);
                    if(suita.getSubItemsNr()>0&&!suita.getSubItem(0).isVisible())item.setSubItemVisible(false);
                    item.setEpId(suita.getEpId());
                    suita.insertSubItem(item,0);
                    Grafic.this.updateLocations(suita);}
                else{ArrayList <Integer> indexpos = (ArrayList <Integer>)suita.getPos().clone();
                    indexpos.add(new Integer(pos));
                    Item item = new Item(namefield.getText(),2, -1,5, width+40,25 , indexpos);
                    if(suita.getSubItemsNr()>0&&!suita.getSubItem(0).isVisible())item.setSubItemVisible(false);
                    item.setEpId(suita.getEpId());
                    suita.insertSubItem(item,pos);
                    Grafic.this.updateLocations(suita);}}
            else{ArrayList <Integer> indexpos = new ArrayList <Integer>();
                indexpos.add(new Integer(RunnerRepository.getSuiteNr()));
                Item item = new Item(namefield.getText(),2, -1, 5, width+40,25 , indexpos);
                
                
                String [] selected = new String[epidfield.getSelectedValuesList().size()];
                for(int i=0;i<epidfield.getSelectedValuesList().size();i++){
                    selected[i] = epidfield.getSelectedValuesList().get(i).toString();
                }
                item.setEpId(selected);
                RunnerRepository.addSuita(item);
                if(RunnerRepository.getSuiteNr()>1)Grafic.this.updateLocations(RunnerRepository.getSuita(RunnerRepository.getSuiteNr()-2));
                else Grafic.this.updateLocations(RunnerRepository.getSuita(0));}
            Grafic.this.setCanRequestFocus(true);
            (SwingUtilities.getWindowAncestor(ok)).dispose();
            Grafic.this.repaint();}
        
        public AddSuiteFrame(final JComponent mainwindow,final Item suita,final int pos){
            JLabel name = new JLabel();
            JLabel EPId = new JLabel();
            namefield = new JTextField();
            JScrollPane jScrollPane1 = new JScrollPane();
            String [] vecresult =  RunnerRepository.window.mainpanel.p4.getSut().sut.getSutTree().getSutsName();
            if(vecresult!=null){
                int size = vecresult.length;
                for(int i=0;i<size;i++){
                    vecresult[i] = vecresult[i].replace(".user", "(user)");
                    vecresult[i] = vecresult[i].replace(".system", "(system)");
                }
            }
            epidfield = new JList<String>(vecresult);
            try{epidfield.setSelectedIndex(0);}
            catch(Exception e){e.printStackTrace();}
            ok = new JButton();    
            name.setText("Suite Name:");    
            EPId.setText("SUT Name:");    
            jScrollPane1.setViewportView(epidfield);    
            ok.setText("OK");  
            JPanel main  = new JPanel();
            GroupLayout layout = new GroupLayout(main);
            main.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(name)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(namefield))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(EPId)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(ok, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(name)
                        .addComponent(namefield, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(EPId)
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(ok)
                    .addContainerGap())
            );
            add(main);
            addWindowFocusListener(new WindowFocusListener(){
                public void windowLostFocus(WindowEvent ev){
                    toFront();}
                    public void windowGainedFocus(WindowEvent ev){}});
            setAlwaysOnTop(true);
            Grafic.this.setCanRequestFocus(false);
            ok.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){
                    okAction(suita,pos);}});
            addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    Grafic.this.setCanRequestFocus(true);
                    (SwingUtilities.getWindowAncestor(ok)).dispose();}});
            Action actionListener = new AbstractAction(){
                public void actionPerformed(ActionEvent actionEvent){
                    JButton source = (JButton) actionEvent.getSource();
                    okAction(suita,pos);}};                    
            InputMap keyMap = new ComponentInputMap(ok);
            keyMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "action");            
            ActionMap actionMap = new ActionMapUIResource();
            actionMap.put("action", actionListener);
            SwingUtilities.replaceUIActionMap(ok, actionMap);
            SwingUtilities.replaceUIInputMap(ok, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap);            
            setBounds(400,300,300,400);
            setVisible(true);
            namefield.requestFocus();
        }
    }
}
            
class CompareItems implements Comparator{   
    public int compare(Object emp1, Object emp2){ 
        return ((Item)emp1).getName().compareToIgnoreCase(((Item)emp2).getName());}}            
            
class XMLFilter extends FileFilter {
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");}    
    public String getDescription() {return ".xml files";}}