/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package scouter.client.xlog.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.*;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import scouter.client.Images;
import scouter.client.model.TextProxy;
import scouter.client.model.XLogData;
import scouter.client.model.XLogProxy;
import scouter.client.net.INetReader;
import scouter.client.net.TcpProxy;
import scouter.client.popup.EditableMessageDialog;
import scouter.client.popup.SQLFormatDialog;
import scouter.client.server.ServerManager;
import scouter.client.util.*;
import scouter.client.xlog.actions.OpenXLogProfileJob;
import scouter.io.DataInputX;
import scouter.lang.CountryCode;
import scouter.lang.pack.MapPack;
import scouter.lang.pack.XLogPack;
import scouter.lang.pack.XLogTypes;
import scouter.lang.step.*;
import scouter.lang.value.DecimalValue;
import scouter.lang.value.MapValue;
import scouter.net.RequestCmd;
import scouter.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


public class XLogFlowView extends ViewPart {
	
	public final static String ID = XLogFlowView.class.getName();
	
	private GraphViewer viewer = null;
	
	private String date;
	
	boolean showSql = true, showApicall = true;
	

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		parent.addControlListener(new ControlListener() {
			public void controlResized(ControlEvent e) {
				viewer.applyLayout();
			}
			public void controlMoved(ControlEvent e) {
			}
		});
		viewer = new GraphViewer(parent, SWT.NONE);
		viewer.setContentProvider(new GraphContentProvider());
		viewer.setLabelProvider(new GraphLabelProvider());
		viewer.setLayoutAlgorithm(new HorizontalTreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof DependencyElement) {
					DependencyElement de = (DependencyElement) o;
					switch (de.type) {
						case SERVICE:
							new OpenXLogProfileJob(XLogFlowView.this.getViewSite().getShell().getDisplay(), date, de.id, de.tags.getInt("serverId")).schedule();
							break;
						case SQL:
							List<StyleRange> srList = new ArrayList<StyleRange>();
							StringBuffer sb = new StringBuffer();
							String sql = de.tags.getText("sql");
							sb.append(sql);
							String error = null;
							if (de.error != 0) {
								error = TextProxy.error.getLoadText(date, de.error, de.tags.getInt("serverId"));
							}
							new SQLFormatDialog().show(sb.toString(), error);
							break;
						case API_CALL:
							srList = new ArrayList<StyleRange>();
							sb = new StringBuffer();
							String apicall = de.name;
							sb.append(apicall);
							if (de.error != 0) {
								error = TextProxy.error.getLoadText(date, de.error, de.tags.getInt("serverId"));
								if (StringUtil.isNotEmpty(error)) {
									sb.append("\n");
									srList.add(new StyleRange(sb.length(), error.length(), ColorUtil.getInstance().getColor("red"), null, SWT.BOLD));
									sb.append(error);
								}
							}
							new EditableMessageDialog().show("API Call", sb.toString(), srList);
							break;
						case DISPATCH:
						case THREAD:
							new OpenXLogProfileJob(XLogFlowView.this.getViewSite().getShell().getDisplay(), date, de.id, de.tags.getInt("serverId")).schedule();
							break;
//						case DISPATCH:
//							srList = new ArrayList<StyleRange>();
//							sb = new StringBuffer();
//							String dispatch = de.name;
//							sb.append(dispatch);
//							if (de.error != 0) {
//								error = TextProxy.error.getLoadText(date, de.error, de.tags.getInt("serverId"));
//								if (StringUtil.isNotEmpty(error)) {
//									sb.append("\n");
//									srList.add(new StyleRange(sb.length(), error.length(), ColorUtil.getInstance().getColor("red"), null, SWT.BOLD));
//									sb.append(error);
//								}
//							}
//							new EditableMessageDialog().show("Dispatch", sb.toString(), srList);
//							break;
					}
				}
			}
		});
		
		IToolBarManager man = getViewSite().getActionBars().getToolBarManager();
		Action sqlFilterAct = new Action("Show SQL", IAction.AS_CHECK_BOX) {
			public void run() {
				showSql = isChecked();
				viewer.refresh();
				viewer.applyLayout();
			}
		};
		sqlFilterAct.setImageDescriptor(ImageUtil.getImageDescriptor(Images.database));
		man.add(sqlFilterAct);
		sqlFilterAct.setChecked(true);
		Action apiFilterAct = new Action("Show API Call", IAction.AS_CHECK_BOX) {
			public void run() {
				showApicall = isChecked();
				viewer.refresh();
				viewer.applyLayout();
			}
		};
		apiFilterAct.setImageDescriptor(ImageUtil.getImageDescriptor(Images.link));
		man.add(apiFilterAct);
		apiFilterAct.setChecked(true);
		viewer.setFilters(new ViewerFilter[] {filter});
	}
	
	public void loadByTxId(String date, long txid) {
		this.date = date;
		this.setPartName("Service Flow - "  + Hexa32.toString32(txid));
		MapPack param = new MapPack();
		param.put("date", date);
		param.put("txid", txid);
		new ProcessDependencyTask(RequestCmd.XLOG_READ_BY_TXID, param).schedule();
	}
	
	public void loadByGxId(String date, long gxId) {
		this.date = date;
		this.setPartName("Service Flow - "  + Hexa32.toString32(gxId));
		MapPack param = new MapPack();
		param.put("date", date);
		param.put("gxid", gxId);
		new ProcessDependencyTask(RequestCmd.XLOG_READ_BY_GXID, param).schedule();
	}
	
	class ProcessDependencyTask extends Job {
		
		final String requestCmd;
		final MapPack param;
		LongKeyLinkedMap<DependencyElement> rootMap = new LongKeyLinkedMap<DependencyElement>();
		LongKeyLinkedMap<DependencyElement> serviceMap = new LongKeyLinkedMap<DependencyElement>();

		public ProcessDependencyTask(String requestCmd, MapPack param) {
			super("Processing Dependencies Task");
			this.requestCmd = requestCmd;
			this.param = param;
		}

		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Collecting XLog/Profile....", IProgressMonitor.UNKNOWN);
			Iterator<Integer> itr = ServerManager.getInstance().getOpenServerList().iterator();
			while (itr.hasNext()) {
				final int serverId = itr.next();
				TcpProxy tcp = TcpProxy.getTcpProxy(serverId);
				try {
					tcp.process(requestCmd, param, new INetReader() {
						public void process(DataInputX in) throws IOException {
							XLogPack xlog = (XLogPack) in.readPack();
							ElementType eType = ElementType.SERVICE;
							boolean excludeObjName = false;
							switch (xlog.xType) {
								case XLogTypes.ASYNCSERVLET_DISPATCHED_SERVICE:
									eType = ElementType.DISPATCH;
									excludeObjName = true;
									break;
								case XLogTypes.BACK_THREAD2:
									eType = ElementType.THREAD;
									excludeObjName = true;
									break;
							}

							DependencyElement serviceElement = new DependencyElement(eType, xlog.txid);
							String objName = TextProxy.object.getLoadText(date, xlog.objHash, serverId);
							String serviceName = TextProxy.service.getLoadText(date, xlog.service, serverId);
							serviceElement.elapsed = xlog.elapsed;
							if(excludeObjName) {
								serviceElement.name = serviceName;
							} else {
								serviceElement.name = serviceName + "\n(" + objName + ")";
							}
							serviceElement.error = xlog.error;
							serviceElement.xtype = xlog.xType;
							serviceElement.threadName = TextProxy.hashMessage.getLoadText(date, xlog.threadNameHash, serverId);
							serviceElement.tags.put("caller", xlog.caller);
							serviceElement.tags.put("ip", IPUtil.toString(xlog.ipaddr));
							serviceElement.tags.put("serverId", serverId);
							if (StringUtil.isNotEmpty(xlog.countryCode)) {
								serviceElement.tags.put("country", CountryCode.getCountryName(xlog.countryCode));
								serviceElement.tags.put("city", TextProxy.city.getLoadText(date, xlog.city, serverId));
							}
							serviceMap.put(xlog.txid, serviceElement);
						}
					});
				} catch (Throwable th) {
					ConsoleProxy.errorSafe(th.toString());
				} finally {
					TcpProxy.putTcpProxy(tcp);
				}
			}
			
			LongEnumer txidEnumer = serviceMap.keys();
			while (txidEnumer.hasMoreElements()) {
				long txid = txidEnumer.nextLong();
				final DependencyElement serviceElement = serviceMap.get(txid);
				final int serverId = (int) serviceElement.tags.getLong("serverId");
				if (serverId == 0) continue;
				TcpProxy tcp = TcpProxy.getTcpProxy(serverId);
				try {
					MapPack param = new MapPack();
					param.put("date", date);
					param.put("txid", new DecimalValue(txid));
					tcp.process(RequestCmd.TRANX_PROFILE_FULL, param, new INetReader() {
						public void process(DataInputX in) throws IOException {
							byte[] buff = in.readBlob();
							Step[] steps = Step.toObjects(buff);
							if (steps == null) return;
							for (Step step : steps) {
								stepToElement(serviceElement, step, serverId);
							}
						}
					});
				} catch (Exception e) {
					ConsoleProxy.errorSafe(e.toString());
				} finally {
					TcpProxy.putTcpProxy(tcp);
				}
				if (serviceMap.size() == 1 || serviceElement.tags.getLong("caller") == 0) {
					String ip = serviceElement.tags.getText("ip");
					if (StringUtil.isNotEmpty(ip)) {
						int id = HashUtil.hash(ip);
						DependencyElement ipElement = rootMap.get(id);
						if (ipElement == null) {
							ipElement = new DependencyElement(ElementType.USER, id);
							if (StringUtil.isNotEmpty(serviceElement.tags.getText("country"))) {
								ipElement.name = ip + "\n(" +  serviceElement.tags.getText("city") + ", " + serviceElement.tags.getText("country") + ")";
							} else {
								ipElement.name = ip;
							}
							rootMap.put(id, ipElement);
						}
						ipElement.addChild(serviceElement);
					} else {
						long id = TimeUtil.getCurrentTime();
						DependencyElement dummyElement = new DependencyElement(ElementType.USER, id);
						dummyElement.name = "???.???.???.???";
						rootMap.put(id, dummyElement);
					}
				}
			}
			ExUtil.exec(viewer.getGraphControl(), new Runnable() {
				public void run() {
					viewer.setInput(rootMap);
				}
			});
			return Status.OK_STATUS;
		}
		
		private void stepToElement(final DependencyElement serviceElement, Step step, final int serverId) {
			switch (step.getStepType()) {
			case StepEnum.APICALL:
			case StepEnum.APICALL2:
				ApiCallStep apicallstep = (ApiCallStep) step;
				DependencyElement apiElement = new DependencyElement(ElementType.API_CALL, apicallstep.txid + apicallstep.hash);
				apiElement.elapsed = apicallstep.elapsed;
				apiElement.error = apicallstep.error;
				apiElement.name = TextProxy.apicall.getLoadText(date, apicallstep.hash, serverId);
				apiElement.tags.put("serverId", serverId);
				apiElement.address = apicallstep.address;

				if (apicallstep.txid != 0) {
					DependencyElement calledService = serviceMap.get(apicallstep.txid);
					if (calledService != null) {
						calledService.address = apicallstep.address;
						serviceElement.addChild(calledService);
					} else {
						serviceElement.addChild(apiElement);
					}
				} else {
					serviceElement.addChild(apiElement);
				}
				break;
			case StepEnum.SPANCALL:
				SpanCallStep spanCallstep = (SpanCallStep) step;
				DependencyElement spanCallElement = new DependencyElement(ElementType.API_CALL, spanCallstep.txid + spanCallstep.hash);
				spanCallElement.elapsed = spanCallstep.elapsed;
				spanCallElement.error = spanCallstep.error;
				spanCallElement.name = TextProxy.apicall.getLoadText(date, spanCallstep.hash, serverId);
				spanCallElement.tags.put("serverId", serverId);
				spanCallElement.address = spanCallstep.address;

				if (spanCallstep.txid != 0) {
					DependencyElement calledService = serviceMap.get(spanCallstep.txid);
					if (calledService != null) {
						calledService.address = spanCallstep.address;
						serviceElement.addChild(calledService);
					} else {
						serviceElement.addChild(spanCallElement);
					}
				} else {
					serviceElement.addChild(spanCallElement);
				}
				break;
			case StepEnum.DISPATCH:
				DispatchStep dispatchStep = (DispatchStep) step;
				DependencyElement dispatchElement = new DependencyElement(ElementType.DISPATCH, dispatchStep.txid + dispatchStep.hash);
				dispatchElement.elapsed = dispatchStep.elapsed;
				dispatchElement.error = dispatchStep.error;
				dispatchElement.name = TextProxy.apicall.getLoadText(date, dispatchStep.hash, serverId);
				dispatchElement.tags.put("serverId", serverId);
				if (dispatchStep.txid != 0) {
					DependencyElement calledService = serviceMap.get(dispatchStep.txid);
					if (calledService != null) {
						serviceElement.addChild(calledService);
					} else {
						serviceElement.addChild(dispatchElement);
					}
				} else {
					serviceElement.addChild(dispatchElement);
				}
				break;
			case StepEnum.THREAD_CALL_POSSIBLE:
				ThreadCallPossibleStep tcStep = (ThreadCallPossibleStep) step;

				XLogData threadStepXlog = null;
				if(tcStep.txid != 0L) {
					threadStepXlog = XLogProxy.getXLogData(serverId, date, tcStep.txid);
				}
				if(threadStepXlog != null) {
					tcStep.threaded = 1;
					tcStep.elapsed = threadStepXlog.p.elapsed;
				}

				if(tcStep.threaded == 0) break;
				DependencyElement tcElement = new DependencyElement(ElementType.DISPATCH, tcStep.txid + tcStep.hash);
				tcElement.elapsed = tcStep.elapsed;
				tcElement.name = TextProxy.apicall.getLoadText(date, tcStep.hash, serverId);
				tcElement.tags.put("serverId", serverId);
				if (tcStep.txid != 0) {
					DependencyElement calledService = serviceMap.get(tcStep.txid);
					if (calledService != null) {
						serviceElement.addChild(calledService);
					} else {
						serviceElement.addChild(tcElement);
					}
				} else {
					serviceElement.addChild(tcElement);
				}
				break;
			case StepEnum.APICALL_SUM:
				ApiCallSum apicallsum = (ApiCallSum) step;
				DependencyElement apiSumElement = new DependencyElement(ElementType.API_CALL, apicallsum.hash);
				apiSumElement.dupleCnt = apicallsum.count;
				apiSumElement.elapsed = (int) apicallsum.elapsed;
				apiSumElement.error = apicallsum.error;
				apiSumElement.name = TextProxy.apicall.getLoadText(date, apicallsum.hash, serverId);
				apiSumElement.tags.put("serverId", serverId);
				serviceElement.addChild(apiSumElement);
				break;
			case StepEnum.SQL:
			case StepEnum.SQL2:
			case StepEnum.SQL3:
				SqlStep sqlstep = (SqlStep) step;
				DependencyElement sqlElement = new DependencyElement(ElementType.SQL, sqlstep.hash);
				sqlElement.elapsed = sqlstep.elapsed;
				String table = TextProxy.sql_tables.getLoadText(date, sqlstep.hash, serverId);
				String sql = TextProxy.sql.getLoadText(date, sqlstep.hash, serverId).trim();
				sqlElement.name =  StringUtil.isNotEmpty(table) ? table : StringUtil.truncate(sql, 20) + "...";
				//sqlElement.name =  table;
				sqlElement.error = sqlstep.error;
				sqlElement.tags.put("serverId", serverId);
				sqlElement.tags.put("sql", sql);
				serviceElement.addChild(sqlElement);
				break;
			case StepEnum.SQL_SUM:
				SqlSum sqlsum = (SqlSum) step;
				DependencyElement sqlSumElement = new DependencyElement(ElementType.SQL, sqlsum.hash);
				sqlSumElement.dupleCnt = sqlsum.count;
				sqlSumElement.elapsed = (int) sqlsum.elapsed;
				sqlSumElement.error = sqlsum.error;
				table = TextProxy.sql_tables.getLoadText(date, sqlsum.hash, serverId);
				sql = TextProxy.sql.getLoadText(date, sqlsum.hash, serverId).trim();
				sqlSumElement.name =  StringUtil.isNotEmpty(table) ? table : StringUtil.truncate(sql, 20) + "...";
				sqlSumElement.tags.put("serverId", serverId);
				sqlSumElement.tags.put("sql", sql);
				serviceElement.addChild(sqlSumElement);
				break;
			case StepEnum.THREAD_SUBMIT:
				ThreadSubmitStep tsStep = (ThreadSubmitStep) step;
				TcpProxy tcp = TcpProxy.getTcpProxy(serverId);
				try {
					MapPack param = new MapPack();
					param.put("date", date);
					param.put("txid", tsStep.txid);
					tcp.process(RequestCmd.TRANX_PROFILE_FULL, param, new INetReader() {
						public void process(DataInputX in) throws IOException {
							byte[] buff = in.readBlob();
							Step[] steps = Step.toObjects(buff);
							if (steps == null) return;
							for (Step step : steps) {
								stepToElement(serviceElement, step, serverId);
							}
						}
					});
				} catch (Throwable th) {
					ConsoleProxy.errorSafe(th.toString());
				} finally {
					TcpProxy.putTcpProxy(tcp);
				}
				break;
			}
		}
	}

	public void setFocus() {
	}
	
	ViewerFilter filter = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (element instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) element;
				switch (de.type) {
					case SQL:
						return showSql;
					case API_CALL:
					case DISPATCH:
						return showApicall;
				}
			}
			return true;
		}
	};
	
	enum ElementType {
		USER,
		SERVICE,
		API_CALL,
		SQL,
		DISPATCH,
		THREAD
	}
	
	public static class DependencyElement {
		
		ElementType type;
		long id;
		int dupleCnt = 1;
		
		LinkedMap<Long, DependencyElement> childMap = new LinkedMap<Long, DependencyElement>();
		
		public String name;
		public int elapsed;
		public int error;
		public byte xtype;
		public String address;
		public String threadName;
		
		MapValue tags = new MapValue();
		
		DependencyElement(ElementType type, long id) {
			this.type = type;
			this.id = id;
		}
		
		public void addChild(DependencyElement child) {
			DependencyElement obj = childMap.get(child.id);
			if (obj == null) {
				childMap.put(child.id, child);
			} else {
				obj.dupleCnt += child.dupleCnt;
				obj.elapsed += child.elapsed;
				if (child.error != 0) {
					obj.error = child.error;
				}
			}
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DependencyElement other = (DependencyElement) obj;
			if (id != other.id)
				return false;
			return true;
		}
	}
	
	class GraphContentProvider implements IGraphEntityContentProvider {
		
		LongKeyLinkedMap<DependencyElement> root;

		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput != null) {
				root = (LongKeyLinkedMap<DependencyElement>) newInput;
			}
		}

		public Object[] getElements(Object inputElement) {
			ArrayList<DependencyElement> list = new ArrayList<DependencyElement>();
			LongEnumer ipEnumer = root.keys();
			while (ipEnumer.hasMoreElements()) {
				long id = ipEnumer.nextLong();
				DependencyElement ipElement = root.get(id);
				list.add(ipElement);
				collectElement(ipElement.childMap, list);
			}
			return list.toArray(new DependencyElement[list.size()]);
		}

		public Object[] getConnectedTo(Object entity) {
			if (entity instanceof DependencyElement) {
				ArrayList<DependencyElement> list = new ArrayList<DependencyElement>();
				DependencyElement element = (DependencyElement) entity;
				Enumeration<DependencyElement> values = element.childMap.values();
				while (values.hasMoreElements()) {
					DependencyElement de = values.nextElement();
					list.add(de);
				}
				return list.toArray(new DependencyElement[list.size()]);
			}
			return null;
		}
		
	}
	
	private void collectElement(LinkedMap<Long, DependencyElement> childMap, ArrayList<DependencyElement> list) {
		Enumeration<DependencyElement> values = childMap.values();
		while (values.hasMoreElements()) {
			DependencyElement element = values.nextElement();
			list.add(element);
			collectElement(element.childMap, list);
		}
	}
	
	class GraphLabelProvider extends LabelProvider implements IEntityStyleProvider, IEntityConnectionStyleProvider {
		public String getText(Object element) {
			if (element instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) element;
				if (de.name == null) de.name = "";
				switch(de.type) {
					case SQL:
					case API_CALL:
						String name1 = de.name.trim().replaceAll("[\r\n]+", " ").replaceAll("\\s+", " ");
						if (name1.length() > 40 && name1.length() <= 65) {
							name1 = name1.substring(0, 30) + "\n   " + name1.substring(30);
						} else if(name1.length() > 65) {
							name1 = name1.substring(0, 30) + "\n   " + name1.substring(30, 60) + "...";
						}
						return name1;
					case DISPATCH:
					case THREAD:
						String name2 = de.name.trim().replaceAll("[\r\n]+", " ").replaceAll("\\s+", " ");
						if (name2.length() > 40 && name2.length() <= 65) {
							name2 = name2.substring(0, 30) + "\n   " + name2.substring(30);
						} else if(name2.length() > 65) {
							name2 = name2.substring(0, 30) + "\n   " + name2.substring(30, 60) + "...";
						}
						if(de.threadName != null) {
							name2 = name2 + "\n  <" + de.threadName + ">";
						}
						return name2;
				}
				String elementText;
				if(de.address != null) {
					elementText = de.name + "\n  : " + de.address;
				} else {
					elementText = de.name;
				}
				return elementText;

			} else if (element instanceof EntityConnectionData) {
				if (((EntityConnectionData) element).dest instanceof DependencyElement) {
					DependencyElement de = (DependencyElement) ((EntityConnectionData) element).dest;
					String elapsed = FormatUtil.print(de.elapsed, "#,###") + " ms";
					if (de.dupleCnt > 1) {
						return "(" + de.dupleCnt + ") " + elapsed; 
					} else {
						return elapsed;
					}
				}
			}
			return null;
		}
		
		public Image getImage(Object element) {
			if (element instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) element ;
				switch (de.type) {
					case USER:
						return Images.CONFIG_USER;
					case SERVICE:
						return Images.server;
					case API_CALL:
						return Images.link;
					case SQL :
						return Images.database;
					case DISPATCH:
						return Images.arrow_right;
					case THREAD:
						return Images.timer;
				}
			}
			return null;
		}

		public Color getNodeHighlightColor(Object entity) {
			return ColorUtil.getInstance().getColor(SWT.COLOR_WHITE);
		}

		public Color getBorderColor(Object entity) {
			if (entity instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) entity;
				if (de.type == ElementType.SERVICE) {
					if(de.xtype == XLogTypes.ASYNCSERVLET_DISPATCHED_SERVICE || de.xtype == XLogTypes.BACK_THREAD2) {
						return ColorUtil.getInstance().getColor(SWT.COLOR_GRAY);
					} else {
						return ColorUtil.getInstance().getColor(SWT.COLOR_DARK_BLUE);
					}
				} else {
					return ColorUtil.getInstance().getColor(SWT.COLOR_WHITE);
				}
			}
			return null;
		}

		public Color getBorderHighlightColor(Object entity) {
			return null;
		}

		public int getBorderWidth(Object entity) {
			if (entity instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) entity;
				if (de.type == ElementType.SERVICE) {
					return 1;
				}
			}
			return 0;
		}

		public Color getBackgroundColour(Object entity) {
			if (entity instanceof DependencyElement) {
				return ColorUtil.getInstance().getColor(SWT.COLOR_WHITE);
			}
			return null;
		}

		public Color getForegroundColour(Object entity) {
			if (entity instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) entity;
				if (de.xtype == XLogTypes.ASYNCSERVLET_DISPATCHED_SERVICE || de.xtype == XLogTypes.BACK_THREAD2) {
					return ColorUtil.getInstance().getColor(SWT.COLOR_DARK_GRAY);
				}
			}
			return null;
		}

		public IFigure getTooltip(Object entity) {
			return null;
		}

		public boolean fisheyeNode(Object entity) {
			return false;
		}

		public int getConnectionStyle(Object src, Object dest) {
			return ZestStyles.CONNECTIONS_DIRECTED;
		}

		public Color getColor(Object src, Object dest) {
			if (dest instanceof DependencyElement) {
				DependencyElement de = (DependencyElement) dest;
				if (de.error != 0) {
					return ColorUtil.getInstance().getColor(SWT.COLOR_RED);
				}
			}
			return null;
		}

		public Color getHighlightColor(Object src, Object dest) {
			return null;
		}

		public int getLineWidth(Object src, Object dest) {
			return 0;
		}
	}
	
	public static void main(String[] args) {
		String str = "SELECT * FROM (		SELECT CEIL(ROWNUM  '@' ) pg , cc.* FROM (		SELECT a.news_no";
		System.out.println(str.replaceAll("\\s+", " "));
	}
}
