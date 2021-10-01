/**
 * (C) 2011-2012 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 *
 * Authors:
 *   danchen <danchen@taobao.com>
 *
 */
package com.taobao.sqlautoreview;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


/*
 * function:����MySQL database,table,columns,index��Ԫ����
 * author:danchen
 * create time:2011/10/31
 */
public class MySQLMetaData implements IMetaData{
	//log4j��־
	private static Logger logger = Logger.getLogger(MySQLMetaData.class);
	
	String IP;
	int port=3306;
	String dbname;
	String user;
	String password;
	Connection conn;
	//��Ҫ���ڲ��ҵ�
	HashMap<String,List<Index_Node>> hash_index;
    HashMap<String,List<Column_Node>> hash_column;
	
	//���캯��
	public MySQLMetaData(String IP,int port,String dbname,String user,String password)
	{
		
		this.IP=IP;
		this.port=port;
		this.dbname=dbname;
		this.user=user;
		this.password=password;
		this.hash_column = new HashMap<String,List<Column_Node>>();
		this.hash_index = new HashMap<String,List<Index_Node>>();
		this.conn = getConnection();
	}
	
	//���캯��
	public MySQLMetaData()
	{
		HandleXMLConf productdbConfig = new HandleXMLConf("productdb.xml");
		this.IP=productdbConfig.getDbConfigIP();
		this.port=productdbConfig.getDbConfigPort();
		this.dbname=productdbConfig.getDbConfigDbname();
		this.user=productdbConfig.getDbConfigUser();
		this.password=productdbConfig.getDbConfigPassword();
		this.hash_column = new HashMap<String,List<Column_Node>>();
		this.hash_index = new HashMap<String,List<Index_Node>>();
		this.conn = getConnection();
	}

	//������ݿ�����
    public Connection getConnection()
    {
 	   String JDriver = "com.mysql.jdbc.Driver";
 	   String conURL="jdbc:mysql://"+IP+":"+port+"/"+dbname;
 	   try {
            Class.forName(JDriver);
        }
        catch(ClassNotFoundException cnf_e) {  
        	// ����Ҳ���������
        	logger.error("Driver Not Found: ", cnf_e);
        }

        try {
            Connection conn = DriverManager.getConnection(conURL, user, password);  
            return conn;
        }
        catch(SQLException se)
        {
           logger.error("�޷����������ݿ������", se);
     	   return null;
        } 
    }
    
    //������ӵ���Ч��
   	public boolean checkConnection() 
    {
   		if(this.conn==null){
   			return false;
   		}else {
   			return true;
		}	
   	}
   	
   	//�����ر����ݿ�����
   	public boolean closeConnection()
   	{
   		try {
			this.conn.close();
		} catch (SQLException e) {
			logger.warn("�ر����ݿ����ӳ�������:",e);
		}
   		return true;
   	}
   	
    //���쵥�����indexԪ����
    public List<Index_Node> buildIndexMetaData(String tablename)
    {
    	//��ȡindex information
    	try
    	{
    	   String command="select TABLE_SCHEMA,TABLE_NAME,NON_UNIQUE,INDEX_SCHEMA,INDEX_NAME,SEQ_IN_INDEX,COLUMN_NAME,CARDINALITY,INDEX_TYPE from information_schema.STATISTICS where table_name='"+tablename+"'"+" and TABLE_SCHEMA='"+this.dbname+"';";
    	   //System.out.println(command);
    	   Statement stmt = conn.createStatement();
	       stmt.execute(command);
	       ResultSet rs = stmt.getResultSet();
	       
	       //Listֻ��һ���ӿ�,LinkedList����һ�������ʵ��
	       List<Index_Node> list_index = new LinkedList<Index_Node>();
	       while(rs.next())
	       {
	    	   Index_Node in = new Index_Node();
	    	   in.table_schema = rs.getString("TABLE_SCHEMA");
	    	   in.table_name = rs.getString("TABLE_NAME");
	    	   in.non_unique = rs.getInt("NON_UNIQUE");
	    	   in.index_schema = rs.getString("INDEX_SCHEMA");
	    	   in.index_name = rs.getString("INDEX_NAME");
	    	   in.seq_in_index = rs.getInt("SEQ_IN_INDEX");
	    	   in.column_name = rs.getString("COLUMN_NAME");
	    	   in.Cardinality = rs.getInt("CARDINALITY");
	    	   in.index_type = rs.getString("INDEX_TYPE");
	           list_index.add(in);
	       }
	       
	       rs.close();
           stmt.close();
           return list_index;
    	}
        catch(SQLException e)
        {
     	   e.printStackTrace();
     	   return null;
        }
    }
    
    //���쵥�����columnԪ����
    public List<Column_Node> buildColumnMetaData(String tablename)
    {
    	String command;
    	String all_col_names="";
    	String all_dis_col_names="";
    	//��ѯ���е�����,�Լ���������data type
    	try
    	{
    		//information_schema.COLUMNS�����ͼ����bug,������ͬ����������,5.0,5.1�������������,����һ��group by��ȥ��
    		command="select COLUMN_NAME,DATA_TYPE,IS_NULLABLE,COLUMN_TYPE from information_schema.COLUMNS where table_name='"+tablename+"'"+" and TABLE_SCHEMA='"+this.dbname+"' ";
    		command=command+"group by COLUMN_NAME order by ORDINAL_POSITION;";
    		Statement stmt = conn.createStatement();
 	        stmt.execute(command);
 	        ResultSet rs = stmt.getResultSet();
 	        
 	        List<Column_Node> list_column = new LinkedList<Column_Node>();
 	        while(rs.next())
 	        {
 	        	Column_Node cn = new Column_Node();
 	        	cn.column_name = rs.getString("COLUMN_NAME");
 	        	cn.column_type = rs.getString("COLUMN_TYPE");
 	        	cn.data_type = rs.getString("DATA_TYPE");
 	        	cn.is_nullable = rs.getString("IS_NULLABLE");
 	        	cn.sample_card = 0;
 	        	list_column.add(cn);
 	        	
 	        	//ƴ�����е�����
 	        	all_col_names=all_col_names+","+cn.column_name;
 	        	all_dis_col_names=all_dis_col_names+",count(distinct("+cn.column_name+"))";
 	        }
 	        
 	        rs.close();
 	        stmt.close();
 	        
 	        if(all_col_names.length()==0)
 	        {
 	        	return null;
 	        }
 	        
 	       //������������,Ĭ�ϲ���10000��
 	        String col;
 	        command="select "+all_dis_col_names.substring(1)+" from ";
 	        command=command+"(select "+all_col_names.substring(1)+" from "+tablename+" limit 10000) aa;";
 	        logger.debug(command);
 	        Statement stmt2 = conn.createStatement();
	        stmt2.execute(command);
	        ResultSet rs2 = stmt2.getResultSet();
	        while(rs2.next())
	        {
	        	//ֻ��һ����¼,����ֻ��ѭ��һ��
	        	for(int i=0;i<list_column.size();i++)
	        	{
	        	   col="count(distinct("+list_column.get(i).column_name+"))";
	        	   list_column.get(i).sample_card = rs2.getInt(col);
	        	}
	        }
	        
	        rs2.close();
	        stmt2.close();
	        
	        return list_column;
 	        
    	}
    	 catch(SQLException e)
         {
      	   e.printStackTrace();
      	   return null;
         }
    }
    
    //����һ���������Ԫ����column+index of one table
    public void buildTableMetaData(String tablename)
    {
    	String real_tablename=findMatchTable(tablename);
    	if(real_tablename==null){
    		logger.warn("table doesn't exist.");
    		return;
    	}else if(real_tablename.equals(tablename)){
    		//������������
        	hash_index.put(tablename,buildIndexMetaData(tablename));
        	//����������
        	hash_column.put(tablename, buildColumnMetaData(tablename));
    	}else{
    		//������������
        	hash_index.put(tablename,buildIndexMetaData(real_tablename));
        	//����������
        	hash_column.put(tablename, buildColumnMetaData(real_tablename));
    	}
    	
    }
    
    //������������б��Ԫ����
    public void buildDBMetaData()
    {
    	String command;
    	command="select table_name from information_schema.tables where table_schema='"+dbname+"';";
    	try
    	{
    	   Statement stmt = conn.createStatement();
	       stmt.execute(command);
	       ResultSet rs = stmt.getResultSet();
	       while(rs.next())
	       {
	    	   buildTableMetaData(rs.getString("TABLE_NAME"));
	       }
	       
	       rs.close();
	       stmt.close();
    	}
    	 catch(SQLException e)
         {
      	   e.printStackTrace();
         }
    	 
    }
    
    //���ݱ���,����,���ָ���е�Ԫ����
    public Column_Node searchColumnMetaData(String tablename,
    		                               String column_name)
    {
    	//���������column�б�
    	List<Column_Node> list_column=hash_column.get(tablename);
    	Column_Node cn;
    	for(Iterator<Column_Node> r=list_column.iterator();r.hasNext();)
    	{
    		cn = r.next();
    		if(cn.column_name.equals(column_name)==true)
    		{
    			//�ҵ����column,ֱ�ӷ���
    			return cn;
    		}
    	}
    	
    	return null;
    }
    
    //���ݱ���,�������ָ�����ϴ��ڵ�������Ԫ����,һ���п����Ѵ����ڶ����������,���Է��ص���һ��List
    public List<Index_Node> searchIndexMetaData(String tablename,String column_name)
    {
    	//������������е�index�б�
    	List<Index_Node> list_index=hash_index.get(tablename);
    	List<Index_Node> list_column_index = new LinkedList<Index_Node>();
    	Index_Node in;
    	for(Iterator<Index_Node> r=list_index.iterator();r.hasNext();)
    	{
    		in = r.next();
    		if(in.column_name.equals(column_name)==true)
    		{
    			list_column_index.add(in);
    		}
    	}
    	return list_column_index;
    }
    
    //��������Ԫ�������Ƿ����
    public boolean checkTableExist(String tablename)
    {
    	//���������column�б�
    	List<Column_Node> list_column=hash_column.get(tablename);
    	if(list_column == null) {
    		return false;
    	}
    	if(list_column.isEmpty()==true)
    		return false;
    	else {
			return true;
		}
    }
    
    /*
     * ��������ҵ�����ݿ����Ƿ����,�п��ֿܷ�ֱ�,��ʽtablename_XXXX
     * ʹ����TDDL�����п��ܻ�����������tablename
     * ��Щtablename����SQLMAP�У���Ҫת�������ʹ��
     * ���ʵ�ʵı���,��SQL�еı�����һ��,��Ҫ���ϲ�֪��
     */
    public String findMatchTable(String tablename)
    {
    	String command;
    	String tmp_tablename;
    	//����������ƥ��tablename_xxxx�ֿ�ֱ�ı���
    	List<String> list_tablename=new LinkedList<String>();;
    	//is_find 0:�ҵ���ͬ�ı��� 1:�ֿ�ֱ� 2:û���ҵ�����
    	int is_find=-1;
    	command="select table_name from information_schema.tables where table_schema='"+dbname+"' and table_name='"+tablename+"';";
    	try
    	{
    	   Statement stmt = conn.createStatement();
	       stmt.execute(command);
	       ResultSet rs = stmt.getResultSet();
	       if(rs.next()){
	    	   //table exist
	    	   is_find=0;
	       }else{
	    	   //table not exist,try another way
	    	   command="select table_name from information_schema.tables where table_schema='"+dbname+"' and table_name like '"+tablename+"_%';";
	    	   stmt.execute(command);
	    	   ResultSet tmp_rs=stmt.getResultSet();
	    	   //������ʽhttp://developer.51cto.com/art/200902/110238.htm
	    	   Pattern pattern = Pattern.compile("^"+tablename+"_[0-9]+$");
	    	   while(tmp_rs.next()){
	    		   tmp_tablename=tmp_rs.getString("table_name");
	    		   Matcher matcher = pattern.matcher(tmp_tablename);
	    		   if(matcher.find()){
	    			   list_tablename.add(tmp_tablename);
	    		   }
	    	   }
	    	   if(list_tablename.size()!=0){
	    		   is_find=1;
	    	   }else {
				   is_find=2;
			}
	    	   tmp_rs.close();
	       }
	       
	       rs.close();
	       stmt.close();
    	}
    	 catch(SQLException e)
         {
      	   logger.error("",e);
         }
    	 
    	 //����ֵ
    	 if(is_find==0){
    		 return tablename;
    	 }else if(is_find==1){
    		 return findBestTableName(list_tablename);
    	 }else {
    		 return null;
    	 }
    }
    
    /*
     * �ҳ�����ʼ���meta data�ķֱ�
     */
    private String findBestTableName(List<String> list_tablename) {
    	String bestTableName="";
    	//�ȶ���һ���Ƚϴ��ֵ,�ֱ���ȡ��ô����±�
    	int table_min_index=100000000;
    	String tmp_tablename;
    	//���������ȫ���
    	if(list_tablename==null || list_tablename.size()==0){
    		logger.warn("empty list_tablename parameter.");
    		return null;
    	}
    	//ȡ�����зֱ���±�,������С���±�ı���
    	Pattern pattern = Pattern.compile("[0-9]+$");
    	String matchString;
    	for(Iterator<String> iterator=list_tablename.iterator();iterator.hasNext();){
    		tmp_tablename=iterator.next();
    		Matcher matcher = pattern.matcher(tmp_tablename);
    		if(matcher.find()){
    			matchString=matcher.group();
    			if(table_min_index>Integer.valueOf(matchString)){
    				table_min_index=Integer.valueOf(matchString);
    				bestTableName=tmp_tablename;
    			}
    		}
    	}
    		
		return bestTableName;
	}
    
    //����һ�����ȥ�غϺ��������
    private String getIndexNameByTable(String tablename)
    {
    	String returnStr="";
    	List<String> list_distinct_index=new LinkedList<String>();
    	String command="select distinct(INDEX_NAME) from information_schema.STATISTICS where table_name='"+tablename+"'"+" and TABLE_SCHEMA='"+this.dbname+"';";
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(command);
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				list_distinct_index.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.warn("getIndexNameByTable:",e);
		}
		
		for(Iterator<String> iterator=list_distinct_index.iterator();iterator.hasNext();)
		{
			returnStr=returnStr+getIndexedCol(tablename,iterator.next());
		}
		
		returnStr=tablename+":"+returnStr;
		return returnStr;
    }
    
    //���һ�������������ֶ���
    private String getIndexedCol(String tablename,String index_name)
    {
    	String index="";
    	String command="select COLUMN_NAME from information_schema.STATISTICS where table_name='"+tablename+"' and index_name='"+index_name+"' and TABLE_SCHEMA='"+this.dbname+"'";
    	command=command+" order by SEQ_IN_INDEX asc;";
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(command);
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				if(index.equals("")==true){
				   index=rs.getString(1);
				}
				else{
					index=index+","+rs.getString(1);
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.warn("getIndexNameByTable:",e);
		}
		
		index=index_name+"("+index+");";
		return index;
    }
    
    //���������ϵ�����,Ϊǰ��ʹ��
    //ż�����ε�����ʹ��,ÿ��ִ����,�͹ر�����
	public String getIndexesByTableName(String tablenames) {
		try {
			if(conn.isClosed()){
				logger.error("database connection is closed.");
				return "";
			}
		} catch (SQLException e) {
			logger.error("MySQLMetaData connection is not get.",e);
			return "";
		}
    	String returnStr="";
    	String table_name="";
    	if(tablenames==null || tablenames.equals("")==true){
    		logger.warn("getIndexesByTableName:tablenames=null or tablenames= ");
    		return null;
    	}
		String[] array_tablename=tablenames.split(",");
		for(int i=0;i<array_tablename.length;i++)
		{
			table_name=findMatchTable(array_tablename[i]);
			if(table_name==null){
				logger.warn("getIndexesByTableName:"+table_name+" doesn't exist.");
				continue;
			}
			if(returnStr.equals("")==true){
				returnStr=getIndexNameByTable(table_name);
			}else {
				returnStr=returnStr+"|"+getIndexNameByTable(table_name);
			}
		}
		logger.debug(returnStr);
		if(closeConnection()==false){
			logger.warn("���ӹر�ʧ��");
		}
		return returnStr;
	}
    
	/*
	 * ���ر�����,��ȡһ�������������
	 * ��̨Merge index����
	 */
	public String getIndexesByTableName2(String tablenames) 
	{
    	String returnStr="";
    	String table_name="";
    	if(tablenames==null || tablenames.equals("")==true){
    		logger.warn("getIndexesByTableName:tablenames=null or tablenames= ");
    		return null;
    	}
		String[] array_tablename=tablenames.split(",");
		for(int i=0;i<array_tablename.length;i++)
		{
			table_name=findMatchTable(array_tablename[i]);
			if(table_name==null){
				logger.warn("getIndexesByTableName:"+table_name+" doesn't exist.");
				continue;
			}
			if(returnStr.equals("")==true){
				returnStr=getIndexNameByTable(table_name);
			}else {
				returnStr=returnStr+"|"+getIndexNameByTable(table_name);
			}
		}
		logger.debug(returnStr);
		
		return returnStr;
	}
	
    //print table metadata
    public void printTableMetaData(String tablename)
    {
    	//���������index�б�
    	List<Index_Node> list_index=hash_index.get(tablename);
    	//���������column�б�
    	List<Column_Node> list_column=hash_column.get(tablename);
    	
    	System.out.println("Table:"+tablename+" meta data information as follows:");
    	System.out.println("-----------------------------------------------------");
    	
    	System.out.println("index information");
    	Index_Node in;
    	for(Iterator<Index_Node> r=list_index.iterator();r.hasNext();)
    	{
    		in = r.next();
    		System.out.println();
    		System.out.println("database:"+in.table_schema);
    		System.out.println("table_name:"+in.table_name);
    		System.out.println("no_unique:"+in.non_unique);
    		System.out.println("index_schema:"+in.index_schema);
    		System.out.println("index_name:"+in.index_name);
    		System.out.println("seq_in_index:"+in.seq_in_index);
    		System.out.println("column_name:"+in.column_name);
    		System.out.println("Cardinality:"+in.Cardinality);
    		System.out.println("index_type:"+in.index_type);
    	}
    	
    	System.out.println("column information");
    	Column_Node cn;
    	for(Iterator<Column_Node> k=list_column.iterator();k.hasNext();)
    	{
    		cn = k.next();
    		System.out.println();
    		System.out.println("column_name:"+cn.column_name);
    		System.out.println("column_type:"+cn.column_type);
    		System.out.println("data_type:"+cn.data_type);
    		System.out.println("is_nullable:"+cn.is_nullable);
    		System.out.println("sample_card:"+cn.sample_card);
    		
    	}
    }
    
    //print all tables metadata information in a database
    public void printDBMetaData()
    {
    	String command;
    	command="select table_name from information_schema.tables where table_schema='"+dbname+"';";
    	try
    	{
    	   Statement stmt = conn.createStatement();
	       stmt.execute(command);
	       ResultSet rs = stmt.getResultSet();
	       while(rs.next())
	       {
	    	   printTableMetaData(rs.getString("TABLE_NAME"));
	       }
	       
	       rs.close();
	       stmt.close();
    	}
    	 catch(SQLException e)
         {
      	   e.printStackTrace();
         }
    }
    
}
