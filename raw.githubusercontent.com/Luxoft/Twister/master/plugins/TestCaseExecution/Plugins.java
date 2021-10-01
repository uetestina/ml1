/*
File: Plugins.java ; This file is part of Twister.
Version: 2.015

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
import javax.swing.JPanel;
import java.util.Hashtable;
import java.util.Iterator;
import com.twister.plugin.twisterinterface.TwisterPluginInterface;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JFrame;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonObject;
import java.net.URLClassLoader;
import java.util.Arrays;
import javax.swing.JSplitPane;
import java.awt.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.net.URLClassLoader;
import java.util.Properties;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import java.io.FileInputStream;
import com.twister.Item;
import javax.swing.JOptionPane;
import com.twister.CustomDialog;

/*
 * plugins panel displayed
 * on configtab when 
 * plugins button is pressed
 */
public class Plugins extends JPanel{
    private Hashtable plugins = new Hashtable(5,0.5f);
    private JScrollPane pluginsscroll;
    private JPanel plugintable, titleborder, downloadtable, localtable, remotetable2;
    public JSplitPane horizontalsplit, verticalsplit;
    private boolean clearcase = false;
    private boolean finished = true;

    public Plugins(){
        clearcase = isClearCaseEnabled();
        copyPreConfiguredPlugins();
        RunnerPluginsLoader.setClassPath();
        getPlugins();
        initComponents();
        loadRemotePluginList();
        
    }
    
    /*
     * method to copy plugins configuration file
     * to server 
     */
    public boolean uploadPluginsFile(){
        try{
            while(!finished){
                try{Thread.sleep(100);}
                catch(Exception e){e.printStackTrace();}
            }
            finished = false;
            DOMSource source = new DOMSource(RunnerRepository.getPluginsConfig());
            File file = new File(RunnerRepository.PLUGINSLOCALGENERALCONF);
            Result result = new StreamResult(file);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(source, result);
            System.out.println("Saving "+file.getName()+" to: "+RunnerRepository.USERHOME+"/twister/config/");
            FileInputStream in = new FileInputStream(file);
            RunnerRepository.uploadRemoteFile(RunnerRepository.USERHOME+"/twister/config/", in,null, file.getName(),false,null);
            finished = true;
            return true;}
        catch(Exception e){
            e.printStackTrace();
            finished = true;
            return false;
        }
    }
        
    /*
     * loads plugins from
     * PluginsLoader
     */
    public void getPlugins(){
        try{Iterator<TwisterPluginInterface> iterator = RunnerPluginsLoader.getPlugins();  
            TwisterPluginInterface plugin=null;
            String name;
            JsonArray pluginsarray;
            int size;
            String pluginname;
            while(iterator.hasNext()){
                try{plugin = iterator.next();}
                catch(Exception e){
                    System.out.println("Could not instatiate plugin");
                    e.printStackTrace();
                    continue;}
                name = plugin.getFileName();
                pluginsarray = RunnerRepository.getPlugins().getAsJsonArray();
                size = pluginsarray.size();
                for(int i=0;i<size;i++){
                    pluginname = pluginsarray.get(i).getAsString();
                    if(pluginname.equals(name)&&(plugins.get(pluginname)==null)){
                        plugins.put(pluginname,plugin);
                        break;
                    }
                }
            }}               
        catch(Exception e){
            e.printStackTrace();}
        }
          
    /*
     * Initialize and populate
     * Plugins panel
     */
    public void initComponents(){     
        setBorder(BorderFactory.createTitledBorder("Plugins"));
        RunnerPluginsLoader.setClassPath();
        titleborder = new JPanel();
        pluginsscroll = new JScrollPane();
        plugintable = new JPanel();
        plugintable.setBackground(Color.WHITE);
        downloadtable = new JPanel();
        localtable = new JPanel();
        localtable.setBackground(Color.WHITE);
        remotetable2 = new JPanel();   
        remotetable2.setBackground(Color.WHITE);
        downloadtable.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0)),
                                            "Download"));
        downloadtable.setLayout(new BoxLayout(downloadtable,
                                            BoxLayout.PAGE_AXIS));
        localtable.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0)),
                                            "Local"));
        localtable.setLayout(new GridBagLayout());
        remotetable2.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0)), "Remote"));
        remotetable2.setLayout(new GridBagLayout());
        titleborder.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0)), "Plugins"));
        titleborder.setLayout(new BoxLayout(titleborder, BoxLayout.LINE_AXIS));
        plugintable.setLayout(new GridBagLayout());
        pluginsscroll.setViewportView(plugintable);
        titleborder.add(pluginsscroll);
        verticalsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                        new JScrollPane(localtable),
                                        new JScrollPane(remotetable2));
        verticalsplit.setDividerLocation(0.5);
        horizontalsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,verticalsplit,
                                            new JScrollPane(plugintable));
        horizontalsplit.setDividerLocation(0.5);
        add(horizontalsplit);
        Iterator iterator = plugins.keySet().iterator();
        String name;
        String description;
        while(iterator.hasNext()){
            name = iterator.next().toString();
            TwisterPluginInterface plugin = (TwisterPluginInterface)plugins.get(name);
            description = plugin.getDescription(RunnerRepository.PLUGINSDIRECTORY);
            addPlugin(name,description,plugin);}
        addPlugin("ClearCase","ClearCase plugin",null);
        JLabel remotedescription = new JLabel("Remote plugins found on server");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipady = 20;
        remotetable2.add(remotedescription, gridBagConstraints);
        remotedescription = new JLabel("Local installed plugins ");
        remotedescription.setBackground(Color.WHITE);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipady = 20;
        localtable.add(remotedescription, gridBagConstraints);
    }            
            
    /*
     * checks plugins defined in .conf
     * and copies the one that are not found
     * localy
     */
    public void copyPreConfiguredPlugins(){
        try{JsonArray plugins = RunnerRepository.getPlugins().getAsJsonArray();
            int size = plugins.size();
            String pluginfile;
            File pluginsfolder = new File(RunnerRepository.PLUGINSDIRECTORY);
            String [] localplugins = pluginsfolder.list();
            boolean found;
            for(int i=0;i<size;i++){
                pluginfile = plugins.get(i).getAsString();
                found = false;
                for(String file:localplugins){
                    if(file.equals(pluginfile)){
                        File myfile = new File(RunnerRepository.PLUGINSDIRECTORY+
                                            RunnerRepository.getBar()+pluginfile);
                        try{
                            long remotesize = RunnerRepository.getRemoteFileSize(RunnerRepository.REMOTEPLUGINSDIR+"/"+pluginfile);
                            long localsize = myfile.length();
                            System.out.println("remote size: "+remotesize+" lcoalsize: "+localsize+" for plugin "+RunnerRepository.REMOTEPLUGINSDIR+"/"+pluginfile);
                            if(remotesize==0)continue;
                            
                            if(remotesize==localsize)found = true;
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        break;}}
                if(!found){
                    copyPlugin(pluginfile);}}}
        catch(Exception e){
            System.out.println("Could not get Plugins Array from local config");
            e.printStackTrace();}}
     
    /*
     * load the list of plugins
     * found on server into this
     * interface
     */
    public void loadRemotePluginList(){        
        String [] downloadedplugins=null;
        File pluginsfile = new File(RunnerRepository.PLUGINSDIRECTORY);
        if(pluginsfile.exists())downloadedplugins = pluginsfile.list();   
        ArrayList<String> list = getRemotePlugins();
        JPanel panel;
        JLabel lname;        
        for(String name:list){
            if(name.indexOf(".jar")==-1)continue;
            final String tempname = name;
            lname = new JLabel(name);
            lname.setBackground(Color.WHITE);
            final MyButton addremove = new MyButton("Download");
            addremove.setMyLabel(lname);
            for(String localfile:downloadedplugins){
                if(name.equals(localfile)){
                    JsonArray pluginsarray;
                    String pluginname;
                    pluginsarray = RunnerRepository.getPlugins().getAsJsonArray();
                    int size = pluginsarray.size();            
                    for(int i=0;i<size;i++){
                        pluginname = pluginsarray.get(i).getAsString();
                        if(name.equals(pluginname)){
                            addremove.setText("Remove");
                            break;
                        }
                    }
                }
            }
            if(addremove.getText().equals("Remove")){
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 1;
                if(PermissionValidator.canChangePlugins()){
                    localtable.add(addremove, gridBagConstraints);
                }
                gridBagConstraints.gridx = 0;
                localtable.add(lname, gridBagConstraints);
                int height = localtable.getComponentCount()*40;                
                localtable.setPreferredSize(new Dimension(240,height));
            }
            else{
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 1;
                if(PermissionValidator.canChangePlugins()){
                    remotetable2.add(addremove, gridBagConstraints);
                }
                
                gridBagConstraints.gridx = 0;
                remotetable2.add(lname, gridBagConstraints);
            }
            addremove.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev){
                    addRemovePlugin(addremove,tempname);}});}    
    }
        
        
   /*
    * manages adding or removing plugin
    * based on button press
    */   
    public void addRemovePlugin(MyButton addremove, String filename){
        File pluginfile = new File(RunnerRepository.PLUGINSDIRECTORY+
                                    RunnerRepository.getBar()+filename);
        if(addremove.getText().equals("Remove")){
            GridBagLayout layout = (GridBagLayout)localtable.getLayout();
            GridBagConstraints constraints = layout.getConstraints(addremove);
            GridBagConstraints constraints1 = layout.getConstraints(addremove.getMyLabel());
            localtable.remove(addremove);
            localtable.remove(addremove.getMyLabel());
            remotetable2.add(addremove,constraints);
            remotetable2.add(addremove.getMyLabel(),constraints1);
            MainPanel main = RunnerRepository.window.mainpanel;
            TwisterPluginInterface plugin = ((TwisterPluginInterface)plugins.get(filename));
            if(plugin!=null&&plugin.getContent()!=null){
                try{                    
                    Component comp;
                    for(int i=0;i<main.getTabCount();i++){
                        if(main.getComponentAt(i)==null)continue;
                        try{comp = ((JScrollPane)(main.getComponentAt(i))).getViewport().getView();
                            if(comp == plugin.getContent()){
                                main.removeTabAt(i);
                            }
                        } catch(Exception e){}
                    }                    
                }
                catch(Exception e){
                    System.out.println("There was a problem in removing "+
                        "the plugin with filename: "+filename);
                    e.printStackTrace();
                }
            }
            try{((TwisterPluginInterface)plugins.get(filename)).terminate();}
            catch(Exception e){
                System.out.println("There was a problem in terminatig"+
                    " the plugin with filename: "+filename);
                e.printStackTrace();}
            main.revalidate();
            main.repaint();
            localtable.revalidate();
            localtable.repaint();
            remotetable2.revalidate();
            remotetable2.repaint();
            RunnerRepository.removePlugin(filename);
            plugins.remove(filename);
            addremove.setText("Download");
            plugintable.removeAll();
            addPlugin("ClearCase","ClearCase plugin",null);
            Iterator iterator = plugins.keySet().iterator();
            String name;
            String description;
            while(iterator.hasNext()){
                name = iterator.next().toString();
                plugin = (TwisterPluginInterface)plugins.get(name);
                description = plugin.getDescription(RunnerRepository.PLUGINSDIRECTORY);
                addPlugin(name,description,plugin);}
                plugintable.revalidate();
                plugintable.repaint();
        }
        else{
            if(copyPlugin(filename)){
                addremove.setText("Remove");
                GridBagLayout layout = (GridBagLayout)remotetable2.getLayout();
                GridBagConstraints constraints = layout.getConstraints(addremove);
                GridBagConstraints constraints1 = layout.getConstraints(addremove.getMyLabel());
                remotetable2.remove(addremove);
                remotetable2.remove(addremove.getMyLabel());
                localtable.add(addremove,constraints);
                localtable.add(addremove.getMyLabel(),constraints1);
                localtable.revalidate();
                localtable.repaint();
                remotetable2.revalidate();
                remotetable2.repaint();
                plugintable.removeAll();
                addPlugin("ClearCase","ClearCase plugin",null);
                RunnerRepository.addPlugin(filename);
                RunnerPluginsLoader.setClassPath();
                getPlugins();
                Iterator iterator = plugins.keySet().iterator();
                String name;
                String description;
                while(iterator.hasNext()){
                    name = iterator.next().toString();
                    TwisterPluginInterface plugin = (TwisterPluginInterface)plugins.get(name);
                    description = plugin.getDescription(RunnerRepository.PLUGINSDIRECTORY);
                    addPlugin(name,description,plugin);
                }   
            }   
        }
    }
     
    /*
     * method te get plugins from server
     * as an ArrayList
     */
    public ArrayList<String> getRemotePlugins(){
        ArrayList list = new ArrayList<String>();
        Iterator iterator = plugins.keySet().iterator();
        String description;
        
        String [] plugins = RunnerRepository.getRemoteFolderContent(RunnerRepository.REMOTEPLUGINSDIR,null);
        int size;
        try{size= plugins.length;}
        catch(Exception e){
            System.out.println("No plugins");
            size=0;}
        if(size>0){
            for(String name:plugins){
                list.add(name);
            }
        }
        return list;}
            
    /*
     * method to add plugin 
     * to the downloaded pluginlist
     * @param tname - plugin name to display
     * @param tdescription - plugin descritpion to display
     */
    public void addPlugin(String tname,final String tdescription,
                            TwisterPluginInterface plugin){
        GridBagLayout layout = (GridBagLayout)plugintable.getLayout();
        int componentnr = plugintable.getComponentCount();
        Component component;
        GridBagConstraints constraints;
        if(componentnr>0){
            component = plugintable.getComponent(componentnr-1);
            constraints = layout.getConstraints(component);
            constraints.weightx = 0.1;
            constraints.weighty = 0.1;
            layout.setConstraints(component, constraints);
            component = plugintable.getComponent(componentnr-2);
            constraints = layout.getConstraints(component);
            constraints.weightx = 0.1;
            constraints.weighty = 0.1;
            layout.setConstraints(component, constraints);
            component = plugintable.getComponent(componentnr-3);
            constraints = layout.getConstraints(component);
            constraints.weightx = 0.1;
            constraints.weighty = 0.1;
            layout.setConstraints(component, constraints);
            component = plugintable.getComponent(componentnr-4);
            constraints = layout.getConstraints(component);
            constraints.weightx = 0.1;
            constraints.weighty = 0.1;
            layout.setConstraints(component, constraints);}            
        final MyCheck check = new MyCheck();
        check.setBackground(Color.WHITE);
        check.setText("Activate");
        check.setName(tname);
        if(tname.equals("ClearCase")&&clearcase){
            check.setSelected(true);
            new Thread(){
                public void run(){
                    while(RunnerRepository.initialized == false || RunnerRepository.window.mainpanel.p1.tabs == null){
                        try{Thread.sleep(200);}
                        catch(Exception e){e.printStackTrace();}
                    }
                pluginClicked(check);
                }}.start();
        }
        if(!PermissionValidator.canChangePlugins()){
            check.setEnabled(false);
        }
        JLabel name = new JLabel();
        name.setBackground(Color.WHITE);
        JTextArea description = new JTextArea();
        description.setBackground(Color.WHITE);
        JButton readmore = new JButton("Read more"); 
        GridBagConstraints gridBagConstraints = new GridBagConstraints();        
        gridBagConstraints.gridx = 0;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 25.0;
        plugintable.add(check, gridBagConstraints);
        name.setFont(new java.awt.Font("Tahoma", 1, 11)); 
        name.setText(tname);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 15;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 25.0;
        plugintable.add(name, gridBagConstraints);
        description.setFont(new Font("Monospaced", 0, 14)); 
        description.setEditable(false);
        description.setLineWrap(true);
        description.setTabSize(4);
        if(tdescription.length()<80)description.setText(tdescription);
        else description.setText(tdescription.substring(0, 80)+" ...");
        description.setWrapStyleWord(true);
        description.setAutoscrolls(false);
        description.setMinimumSize(new Dimension(10, 10));
        description.setOpaque(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 25.0;
        plugintable.add(description, gridBagConstraints);        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 25.0;
        plugintable.add(readmore, gridBagConstraints);        
        readmore.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                showReadMore(tdescription);}});        
        check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev){
                pluginClicked(check);}});
        if(isPluginEnabled(tname)){
            //wait for MainPanel to be initialized
            new Thread(){
                public void run(){
                    while(RunnerRepository.initialized == false || RunnerRepository.window.mainpanel.p1.tabs == null){
                        try{Thread.sleep(200);}
                        catch(Exception e){e.printStackTrace();}
                    }
                    String pluginname = check.getName();
                    TwisterPluginInterface plugin = (TwisterPluginInterface)plugins.
                                                    get(pluginname);
                    MainPanel main = RunnerRepository.window.mainpanel;
                    
                    Component comp;
                    boolean found = false;
                    for(int i=0;i<main.getTabCount();i++){
                        if(main.getComponentAt(i)==null)continue;
                        try{comp = ((JScrollPane)(main.getComponentAt(i))).getViewport().getView();
                            if(comp == plugin.getContent()){
                                found = true;
                                break;
                            }
                        } catch(Exception e){}
                    }
                    if(!found){check.doClick();
                    }
                    else check.setSelected(true);}
            }.start();
        }
        plugintable.revalidate();
        plugintable.repaint();    
        titleborder.revalidate();
        titleborder.repaint();
    }
        
    /*
     * method to show the readmore window
     * @param description - the description to show
     */
    public void showReadMore(String description){
        JFrame frame = new JFrame("Read More");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTextArea tdescription  = new JTextArea();
        tdescription.setText(description);
        tdescription.setFont(new Font("Monospaced", 0, 14)); 
        tdescription.setEditable(false);
        tdescription.setLineWrap(true);
        tdescription.setTabSize(4);
        tdescription.setWrapStyleWord(true);
        tdescription.setAutoscrolls(false);
        tdescription.setOpaque(false);
        frame.add(new JScrollPane(tdescription));
        frame.setBounds(200,100,800,600);
        frame.setVisible(true);
    }
    
    public boolean isClearCaseEnabled(){
        Document doc = RunnerRepository.getPluginsConfig();
        if(doc==null){
            return false;
        }
        NodeList list1 = doc.getElementsByTagName("Plugin");
        Element item,compare;
        Element rootElement = null;
        boolean found = false;
        for (int i = 0; i < list1.getLength(); i++) {
            item = (Element) list1.item(i);
            compare = (Element) item.getElementsByTagName("name").item(0);
            if (compare.getChildNodes().item(0).getNodeValue()
                    .equals("ClearCase")) {
                found = true;
                rootElement = item;
                break;
            }
        }
        if (!found) {
            return false;
        } else {
            item = (Element)rootElement.getElementsByTagName("status").item(0);
            String value = item.getChildNodes().item(0).getNodeValue();
            if(value.equals("enabled")){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void setClearCaseState(boolean value){
        Document doc = RunnerRepository.getPluginsConfig();
        NodeList list1 = doc.getElementsByTagName("Plugin");
        Element item,compare;
        Element rootElement = null;
        boolean found = false;
        for (int i = 0; i < list1.getLength(); i++) {
            item = (Element) list1.item(i);
            compare = (Element) item.getElementsByTagName("name").item(0);
            if (compare.getChildNodes().item(0).getNodeValue()
                    .equals("ClearCase")) {
                found = true;
                rootElement = item;
                break;
            }
        }
        if (!found) {
            rootElement = doc.createElement("Plugin");
            doc.getFirstChild().appendChild(rootElement);
            Element em2 = doc.createElement("name");
            em2.appendChild(doc.createTextNode("ClearCase"));
            rootElement.appendChild(em2);
            em2 = doc.createElement("jarfile");
            em2.appendChild(doc.createTextNode("ClearCasePlugin.jar"));
            rootElement.appendChild(em2);
            em2 = doc.createElement("pyfile");
            em2.appendChild(doc.createTextNode("ClearCasePlugin.py"));
            rootElement.appendChild(em2);
            em2 = doc.createElement("status");
            String status = "disabled";
            if(value)status = "enabled";
            em2.appendChild(doc.createTextNode(status));
            rootElement.appendChild(em2);
        } else {
            item = (Element)rootElement.getElementsByTagName("status").item(0);
            if(value){
                item.getChildNodes().item(0).setNodeValue("enabled");
            } else {
                item.getChildNodes().item(0).setNodeValue("disabled");
            }
        }
        try{
            DOMSource source = new DOMSource(doc);
            File file = new File(RunnerRepository.PLUGINSLOCALGENERALCONF);
            Result result = new StreamResult(file);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http:xml.apache.org/xslt}indent-amount","4");
            transformer.transform(source, result);
            FileInputStream in = new FileInputStream(file);
            RunnerRepository.uploadRemoteFile(RunnerRepository.USERHOME+"/twister/config/", in,null, file.getName(),false,null);
            in.close();
            System.out.println("Saved "+file.getName()+" to: "+
                    RunnerRepository.USERHOME+"/twister/config/");}
        catch(Exception e){
            e.printStackTrace();
        }
        RunnerRepository.openProjectFile();
        
    }
            
    /*
     * method to display or remove
     * plugin from mainpanel
     */
    public void pluginClicked(final MyCheck check){
        String pluginname = check.getName();
        try{
            System.out.println("Notifying CE that "+pluginname.replace(".jar", "")+" is in a "+check.isSelected()+" state");
            String result = RunnerRepository.getRPCClient().execute("start_stop_plugin", new Object[]{RunnerRepository.user,
                                                                         pluginname.replace(".jar", ""),check.isSelected()}).toString();
            if(result.indexOf("*ERROR*")!=-1){
                CustomDialog.showInfo(JOptionPane.ERROR_MESSAGE,RunnerRepository.window,"ERROR", result);
            }
        } catch (Exception e){
                e.printStackTrace();  
        }
        if(pluginname.equals("ClearCase")){
            clearcase = check.isSelected();
            if(clearcase){
                MainPanel mp = RunnerRepository.window.mainpanel;
                mp.p5 = new ClearCase(RunnerRepository.host,RunnerRepository.user,RunnerRepository.password);
                mp.p1.cp = new ClearCasePanel();
                mp.add(mp.p5, "ClearCase");
                mp.p1.tabs.add("ClearCase Tests", new JScrollPane(mp.p1.cp.tree));
            } else {  
                MainPanel mp = RunnerRepository.window.mainpanel;
                mp.remove(mp.p5);
                RunnerRepository.window.mainpanel.p1.tabs.remove(2);
                mp.p5 = null;
                mp.p1.cp = null;
            }
            setClearCaseState(clearcase);
            return;
        }
        final TwisterPluginInterface plugin = (TwisterPluginInterface)plugins.get(pluginname);
        final MainPanel main = RunnerRepository.window.mainpanel;
        if(check.isSelected()){
            new Thread(){
                public void run(){
                    ArrayList<Item> suites = null; 
                    ArrayList<Item> tests = null;
                    try{suites = RunnerRepository.getSuite();}
                    catch(Exception e){e.printStackTrace();}
                    try{tests =  RunnerRepository.getTestSuite();}
                    catch(Exception e){e.printStackTrace();}
                    plugin.init(suites,
                        tests,
                        RunnerRepository.getVariables(),
                        RunnerRepository.getPluginsConfig(),null);
                    main.addTab(plugin.getName(), new JScrollPane(plugin.getContent()));
                    main.revalidate();
                    main.repaint();
                }
            }.start();
        }
        else{
            if(plugin.getContent()!=null){
                try{Component comp;
                    for(int i=0;i<main.getTabCount();i++){
                        if(main.getComponentAt(i)==null)continue;
                        try{comp = ((JScrollPane)(main.getComponentAt(i))).getViewport().getView();
                            if(comp == plugin.getContent()){
                                main.removeTabAt(i);
                            }
                        } catch(Exception e){}
                    }
                }
                catch(Exception e){
                    System.out.println("There was a problem in removing "+
                        "the plugin with filename: "+plugin.getName());
                    e.printStackTrace();
                }
                plugin.terminate();
                main.revalidate();
                main.repaint();}
        }
    }
                
               
    /*
     * deletes plugins found
     * on local plugins directory
     * but not in config file
     */
    public static void deletePlugins(){
        File pluginsdirectory = new File(RunnerRepository.PLUGINSDIRECTORY);
        File [] downloadedplugins = pluginsdirectory.listFiles();
        boolean found;
        int size;
        String plugin;
        for(File availableplugin : downloadedplugins ){
            found = false;
            JsonArray plugins= null;
            try{plugins = RunnerRepository.getPlugins().getAsJsonArray();}
            catch(Exception e){
                System.out.println("Plugins list from config file is empty ");
                return;}
            size = plugins.size();            
            for(int i=0;i<size;i++){
                plugin = plugins.get(i).getAsString();
                if( availableplugin.getName().equals(plugin.substring(0,plugin.indexOf("."))+"_description.txt")||
                    availableplugin.getName().equals(plugin)){
                    found = true;
                    break;
                }
            }
            if(!found){
                availableplugin.delete();
            }
        }
    }
    
    /*
     * resize method to be called
     * by window resize
     */
    public void setDimension(Dimension dimension){
        int height = (int)dimension.getHeight();
        titleborder.setPreferredSize(new Dimension(245,height-10));
        titleborder.setMaximumSize(new Dimension(245,height-10));
        titleborder.setMinimumSize(new Dimension(245,height-10));        
        downloadtable.setPreferredSize(new Dimension(245,height));
        downloadtable.setMaximumSize(new Dimension(245,height));
        downloadtable.setMinimumSize(new Dimension(245,height));
    }
           
    /*
     * method to copy plugin jar
     * to local twister plugins directory
     * @param filename- the plugin filename to copy localy
     */
    public boolean copyPlugin(String filename){
        File file = new File(RunnerRepository.PLUGINSDIRECTORY+RunnerRepository.getBar()+filename);
        try{
            //get jar file
            System.out.print("Getting "+filename+" ....");
            byte [] buf = RunnerRepository.getRemoteFileContent(RunnerRepository.REMOTEPLUGINSDIR+"/"+filename, true,null);
            OutputStream out=new FileOutputStream(file);
            out.write(buf);
            out.flush();
            out.close();
            System.out.println("successfull");
            try{
                filename = filename.substring(0, filename.indexOf("."))+"_description.txt";
                System.out.print("Getting "+filename+" ....");
                String filecontent = new String(RunnerRepository.getRemoteFileContent(RunnerRepository.REMOTEPLUGINSDIR+"/"+filename, false,null));
                file = new File(RunnerRepository.PLUGINSDIRECTORY+RunnerRepository.getBar()+filename);
                out=new FileOutputStream(file);
                out.write(filecontent.getBytes());
                out.flush();
                out.close();
                System.out.println("successfull");
            } catch(Exception e){e.printStackTrace();}    
            
            return true;}
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Error in copying plugin file " +filename+ " localy");
            return false;}}
    /*
     * method to check if plugin with filename
     * is enabled in general plugins config
     */        
    public boolean isPluginEnabled(String filename){
        try{
            Document doc = RunnerRepository.getPluginsConfig();
            NodeList list1 = doc.getElementsByTagName("Plugin");
            Element item;
            Element compare;
            for(int i=0;i<list1.getLength();i++){
                item = (Element)list1.item(i);
                compare = (Element)item.getElementsByTagName("jarfile").item(0);
                if(compare.getChildNodes().item(0).getNodeValue().equals(filename)){
                    compare = (Element)item.getElementsByTagName("status").item(0);
                    if(compare.getChildNodes().item(0).getNodeValue().equals("enabled")){
                        return true;
                    }
                    return false;
                }
            }
            return false;}
        catch(Exception e){
            return false;
        }
    }
}
  
/*
 *extended JChecBox to 
 *hold a reference to the
 *plugin name
 */
class MyCheck extends JCheckBox{
    String name;
    
    /*
     * name setter
     */
    public void setName(String name){
        this.name= name;}
    
    /*
     * name getter
     */
    public String getName(){
        return name;}}
        
/*
 *extended JButton to 
 *hold a reference to the
 *filename
 */
class MyButton extends JButton{
    JLabel name;
    
    public MyButton(String name){
        super(name);
    }
    
    /*
     * name setter
     */
    public void setMyLabel(JLabel name){
        this.name= name;}
    
    /*
     * name getter
     */
    public JLabel getMyLabel(){
        return name;}}
