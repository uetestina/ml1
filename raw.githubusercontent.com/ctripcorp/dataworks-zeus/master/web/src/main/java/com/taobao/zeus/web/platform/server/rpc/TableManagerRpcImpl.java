package com.taobao.zeus.web.platform.server.rpc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.THEAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sencha.gxt.data.shared.loader.FilterConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
import com.taobao.zeus.client.ZeusException;
import com.taobao.zeus.jobs.JobContext;
import com.taobao.zeus.jobs.sub.conf.ConfUtil;
import com.taobao.zeus.jobs.sub.tool.DataPreviewJob;
import com.taobao.zeus.model.Profile;
import com.taobao.zeus.store.HDFSManager;
import com.taobao.zeus.store.HierarchyProperties;
import com.taobao.zeus.store.ProfileManager;
import com.taobao.zeus.store.TableManager;
import com.taobao.zeus.util.ZeusStringUtil;
import com.taobao.zeus.web.LoginUser;
import com.taobao.zeus.web.platform.client.module.tablemanager.TablePreviewModel;
import com.taobao.zeus.web.platform.client.module.tablemanager.component.Tuple;
import com.taobao.zeus.web.platform.client.module.tablemanager.model.PartitionModel;
import com.taobao.zeus.web.platform.client.module.tablemanager.model.TableColumnModel;
import com.taobao.zeus.web.platform.client.module.tablemanager.model.TableModel;
import com.taobao.zeus.web.platform.client.util.GwtException;
import com.taobao.zeus.web.platform.shared.rpc.TableManagerService;

/**
 * @author gufei.wzy 2012-9-17
 */
public class TableManagerRpcImpl implements TableManagerService {

	@Autowired
	private TableManager tableManager;
	@Autowired
	private ProfileManager profileManager;

	public static final String LINE_DELIMITER_KEY = "line.delim";
	public static final String FIELD_DELIMITER_KEY = "field.delim";
	public static final char DEFAULT_FIELD_DELIM = '\001';

	private static Logger log = LoggerFactory
			.getLogger(TableManagerRpcImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.zeus.web.platform.shared.rpc.TableManagerService#getTableModel
	 * (java.lang.String)
	 */
	@Override
	public TableModel getTableModel(String dbName, String tableName) {
		return convert(tableManager.getTable(dbName, tableName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.zeus.web.platform.shared.rpc.TableManagerService#getPagingTables
	 * (com.sencha.gxt.data.shared.loader.PagingLoadConfig, java.lang.String)
	 */
	@Override
	public PagingLoadResult<TableModel> getPagingTables(
			FilterPagingLoadConfig loadConfig, String uid, String dbName) throws GwtException {
		List<TableModel> tl;
		int offset = loadConfig.getOffset();
		int limit = loadConfig.getLimit();
		String name = "";
		if (loadConfig.getFilters() != null
				&& !loadConfig.getFilters().isEmpty()) {
			for (FilterConfig f : loadConfig.getFilters()) {
				if (f.getField().equals("name")) {
					name = f.getValue().trim();
				}
			}
		}
		// ???????????????????????????
		if (name.replace(" ", "").length() >= 3) {
			int totalNum = 0;
			try {
				totalNum = tableManager.getTotalNumber(dbName, name);
				tl = convertList(tableManager.getPagingTables(dbName, name, offset,
						limit));
			} catch (ZeusException e) {
				throw new GwtException("????????????????????????", e);
			}
			return new PagingLoadResultBean<TableModel>(tl, totalNum,
					loadConfig.getOffset());
		} else {
			return new PagingLoadResultBean<TableModel>(
					Collections.<TableModel> emptyList(), 0,
					loadConfig.getOffset());
		}

	}

	private List<TableModel> convertList(List<Table> tl) {
		if (tl == null) {
			return null;
		}
		List<TableModel> tml = new ArrayList<TableModel>();
		for (Table t : tl) {
			tml.add(convert(t));
		}
		return tml;
	}

	/**
	 * ???hive.metastore????????????Table???????????????TableModel
	 * 
	 * @param t
	 * @return
	 */
	private TableModel convert(Table t) {
		if (t == null) {
			return null;
		}
		TableModel tm = new TableModel();
		tm.setName(t.getTableName());
		tm.setDbName(t.getDbName());
		tm.setCreateDate(new Date(t.getCreateTime() * 1000L));
		tm.setOwner(t.getOwner());
		StorageDescriptor sd = t.getSd();
		tm.setPath(sd.getLocation());
		tm.setSerDeClass(sd.getSerdeInfo().getSerializationLib());

		// ???????????????????????????????????????????????????ascll??????
		String fieldDelim = tansToStringIfInt(sd.getSerdeInfo().getParameters()
				.get(FIELD_DELIMITER_KEY));
		String lineDelim = tansToStringIfInt(sd.getSerdeInfo().getParameters()
				.get(LINE_DELIMITER_KEY));
		tm.setFieldDelim(fieldDelim);
		tm.setLineDelim(lineDelim);

		tm.setInputFormat(sd.getInputFormat());
		tm.setComment(t.getParameters().get("comment"));
		tm.setCols(convert(sd.getCols()));
		return tm;
	}

	/**
	 * ???fieldSchema?????????ColumnModel
	 * 
	 * @param fs
	 * @return ColumnModel
	 */
	private TableColumnModel convert(FieldSchema fs) {
		if (fs == null) {
			return null;
		}
		TableColumnModel cm = new TableColumnModel();
		cm.setName(fs.getName());
		cm.setType(fs.getType());
		cm.setDesc(fs.getComment());
		return cm;
	}

	private List<TableColumnModel> convert(List<FieldSchema> fss) {
		if (fss == null) {
			return null;
		}
		List<TableColumnModel> l = new ArrayList<TableColumnModel>();
		for (FieldSchema fs : fss) {
			l.add(convert(fs));
		}
		return l;
	}

	@Override
	public List<PartitionModel> getPartitions(TableModel t) throws GwtException {
		List<PartitionModel> pml = null;
		try {
			Table tb = tableManager.getTable(t.getDbName(), t.getName());
			if(tb.getPartitionKeysSize()<=0){
				//???????????????????????????????????????
				PartitionModel pm = new PartitionModel();
				pm.setName("????????????");
				pm.setCols(t.getCols());
				pm.setCompressed(true);
				pm.setFieldDelim(t.getFieldDelim());
				pm.setLineDelim(t.getLineDelim());
				pm.setInputFormat(t.getInputFormat());
				pm.setPath(t.getPath());
				pm.setSerDeClass(t.getSerDeClass());
				pml = new ArrayList<PartitionModel>(1);
				pml.add(pm);
				return pml;
			}
			List<Partition> pl = tableManager.getPartitions(t.getDbName(), t.getName(), 40);
			pml = new ArrayList<PartitionModel>(pl.size());

			for (Partition p : pl) {
				PartitionModel pm = new PartitionModel();
				StorageDescriptor sd = p.getSd();
				// ????????????
				List<String> pks = new ArrayList<String>(
						tb.getPartitionKeysSize());
				Iterator<String> vi = p.getValuesIterator();
				// ??????????????????
				for (FieldSchema fs : tb.getPartitionKeys()) {
					pks.add(fs.getName() + "=" + vi.next());
				}
				pm.setPath(sd.getLocation());
				// ???'/'???????????????????????????????????????
				pm.setName(StringUtils.join(pks.toArray(), '/'));
				pm.setSerDeClass(sd.getSerdeInfo().getSerializationLib());

				// ???????????????????????????????????????????????????ascll??????
				String fieldDelim = tansToStringIfInt(sd.getSerdeInfo()
						.getParameters().get(FIELD_DELIMITER_KEY));
				String lineDelim = tansToStringIfInt(sd.getSerdeInfo()
						.getParameters().get(LINE_DELIMITER_KEY));
				pm.setFieldDelim(fieldDelim);
				pm.setLineDelim(lineDelim);
				
				pm.setCols(convert(sd.getCols()));
				pm.setInputFormat(sd.getInputFormat());
				pm.setCompressed(sd.isCompressed());
				pml.add(pm);
			}
		} catch (ZeusException e) {
			log.error("????????????????????????", e);
			throw new GwtException("????????????????????????", e);
		}
		return pml;
	}

	private boolean isInteger(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ???????????????????????????????????????????????????ascll?????????????????????String
	 * 
	 * @param s
	 * @return
	 */
	private String tansToStringIfInt(String s) {
		if (isInteger(s)) {
			return new StringBuffer().append(
					(char) Integer.valueOf(s).intValue()).toString();
		}
		return s;
	}

	@Override
	public TablePreviewModel getPreviewData(PartitionModel model)
			throws GwtException {
		try {
			TablePreviewModel result;
			String path = model.getPath();
			JobContext jobContext = JobContext.getTempJobContext(JobContext.SYSTEM_RUN);
			jobContext.setProperties(new HierarchyProperties(
					new HashMap<String, String>()));
			jobContext.getProperties().setProperty("preview.hdfs.path", path);
			jobContext.getProperties().setProperty("preview.hdfs.inputFormat",
					model.getInputFormat());
			jobContext.getProperties().setProperty("preview.hdfs.isCompressed",
					String.valueOf(model.isCompressed()));
			Profile profile = profileManager.findByUid(LoginUser.getUser()
					.getUid());
			if (profile != null) {
				String ugi = profile.getHadoopConf().get(
						"hadoop.hadoop.job.ugi");
				jobContext.getProperties().setProperty(
						"preview.hadoop.job.ugi", ugi);
			}
			DataPreviewJob job = new DataPreviewJob(jobContext);
			job.run();
			String logContent = job.getJobContext().getJobHistory().getLog()
					.getContent();
			// log.error("---\n" + logContent + "============\n");
			List<Tuple<Integer, List<String>>> datas = new ArrayList<Tuple<Integer, List<String>>>();
			int count = 0;
			// rcfile??????FieldDelim????????????????????????delim '\001'
			char fieldDelim = model.getFieldDelim() == null ? DEFAULT_FIELD_DELIM
					: model.getFieldDelim().charAt(0);
			for (String line : logContent.split("\n")) {
				if (line.startsWith("ZEUS# [output]")) {
					String data = line.substring("ZEUS# [output]".length());
					String[] fields = ZeusStringUtil.split(data, fieldDelim);
					// log.error("fields:" + fields + "; FieldDelim:"
					// + (int) fieldDelim);
					if (fields.length == 0) {
						continue;
					}
					List<String> list = new ArrayList<String>();
					for (int i = 0; i < fields.length
							&& i < model.getCols().size(); i++) {
						list.add(fields[i]);
					}
					datas.add(new Tuple<Integer, List<String>>(count, list));
				}
				count++;
			}
			result = new TablePreviewModel();
			List<String> headers = new ArrayList<String>();
			for (TableColumnModel col : model.getCols()) {
				headers.add(col.getName());
			}
			result.setHeaders(headers);
			result.setData(datas);
			return result;
		} catch (Exception e) {
			log.error("data preview error", e);
			throw new GwtException(e.getMessage(), e);
		}

	}

	@Override
	public PartitionModel fillPartitionSize(PartitionModel p) {
		try{
		DecimalFormat df1 = new DecimalFormat("0 B");
		DecimalFormat df2 = new DecimalFormat("0.000 KB");
		DecimalFormat df3 = new DecimalFormat("0.000 MB");
		DecimalFormat df4 = new DecimalFormat("0.000 GB");
		Profile profile = profileManager.findByUid(LoginUser.getUser()
				.getUid());
		String ugi = null;
		if (profile != null) {
			ugi = profile.getHadoopConf().get(
					"hadoop.hadoop.job.ugi");
		}		
		long size = HDFSManager.getPathSize(p.getPath(), ugi);
		String sizeString = "";
		log.info("file size: " + size);
		if (size < 0) {
			sizeString = "??????????????????????????????";
		} else if (size < 1024) {
			sizeString = df1.format(size);
		} else if (size / 1024 < 1024) {
			sizeString = df2.format(size / 1024.0);
		} else if (size / 1024 / 1024 < 1024) {
			sizeString = df3.format(size / 1024.0 / 1024.0);
		} else if (size / 1024 / 1024 < 1024 * 1024) {
			sizeString = df4.format(size / 1024.0 / 1024.0 / 1024.0);
		}

		p.setSize(sizeString);
		return p;
		}catch(Exception e){
			log.error("fillPartitionSize error is ",e);
			throw e;
		}
		
	}
}
