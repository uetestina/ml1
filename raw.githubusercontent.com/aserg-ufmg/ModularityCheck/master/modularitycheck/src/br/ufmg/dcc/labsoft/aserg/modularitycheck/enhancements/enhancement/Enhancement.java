package br.ufmg.dcc.labsoft.aserg.modularitycheck.enhancements.enhancement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import data.handler.CarryFileMemory;
import br.ufmg.dcc.labsoft.aserg.modularitycheck.bugparser.parser.Parser;
import br.ufmg.dcc.labsoft.aserg.modularitycheck.enhancements.data.clustering.chameleon.SClusterScript;
import br.ufmg.dcc.labsoft.aserg.modularitycheck.enhancements.processing.data.clustering.chameleon.CoChangeGraph;
import br.ufmg.dcc.labsoft.aserg.modularitycheck.enhancements.properties.util.Properties;
import br.ufmg.dcc.labsoft.aserg.modularitycheck.enhancements.properties.util.Utils;

public class Enhancement {

	public static String NUMBER;
	public static String line;
	

	/**
	 * @param args
	 *            args[0] -> root of commits and issues. For instance, Geronimo :
	 *            C:\Users\luciana\Dropbox\Testes\Geronimo args[1] -> directory that contains the issue reports
	 *            
	 *            C:\Users\luciana\Dropbox\Testes\Geronimo\commentsTest
	 * @param type
	 *            GIT or SVN
	 */
	public static boolean enhance(String[] args, int maxScattering,
			int minClusterSize, int type) throws Exception {

		try {
			countPackagesCommits(args[0], type, maxScattering);
			CoChangeGraph.retrieveGraph(args[0], type, false);
			System.gc();
			if(CoChangeGraph.getCountVertexes() > SClusterScript.MIN_SIZE_GRAPH){ 
				SClusterScript.runSCluster(args[1], CoChangeGraph.getCountVertexes(),
					minClusterSize);

			System.out.println("Extracting patterns from the co-change clusters ...");
			CoChangeGraph.retrieveGraph(args[0], type, true);
			System.gc();

			CarryFileMemory carry = new CarryFileMemory(
					Properties.getResultPath() + Properties.CLUSTER_GLUTO);
			StringBuilder size = new StringBuilder();
			String[] buffer = carry.carryCompleteFile();
			int i;

			for (String s : buffer) {
				i = s.split(",").length;
				size.append(String.valueOf(i)).append(Properties.NEW_LINE);
			}

			Utils.writeFile(size.toString(), Properties.getResultPath()
					+ Properties.SIZE_DATA);
			System.gc();

			
			return true;
			
			}else reportGraphSize();
			
		} catch (Exception e) {
			line = e.getMessage();
			//throw new Exception(e);
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.open(SWT.ERROR, Display.getDefault()
							.getActiveShell(), "Co-Change Graph", "There are not enough vertices to mine co-change clusters", SWT.OK);
				}
			});
			return false;	
		}
		return false;
	}

	private static boolean reportGraphSize() {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.open(SWT.ERROR, Display.getDefault()
						.getActiveShell(), "Co-Change Graph", "There are not enough vertices to mine co-change clusters", SWT.OK);
			}
		});
		return false;		
	}



	private static void countPackagesCommits(String root, int type,
			int maxScattering) {
		Properties.setDefaultPaths(root);
		
		System.out.println("Counting packages commits...");
		
		Properties.setFilesPath(Properties.FILES_COMMITS);
		ArrayList<String> classes = new ArrayList<String>();
		File[] listing = new File(Properties.getFilesPath()).listFiles();
		Map<Integer, Integer> hash = new HashMap<Integer, Integer>();
		ArrayList<File> delete = new ArrayList<File>();
		for (File commit : listing) {
			try {
				CarryFileMemory carry = new CarryFileMemory(
						commit.getAbsolutePath());
				String[] openCommit = carry.carryCompleteFile();

				carry = null;
				classes = new ArrayList<String>();

				for (String line : openCommit) {// reads the commit content
					String name = null;

					if (type == Parser.MANAGER_GIT) {
						if (Utils.isValid(line)
								&& line.contains(Properties.TRUNK)) {
							name = Utils.readPackage(line
									.split(Properties.TRUNK)[1]);
						}
					} else if (type == Parser.MANAGER_SVN) {
						if (Utils.isValid(line)) {
							name = Utils.readPackage(line);
						}
					} else
						break;
					if (name != null && !classes.contains(name)) {
						classes.add(name.toString());
					}

				}

				listing = null;
				System.gc();

				if (classes.size() == 0 || classes.size() > maxScattering) {
					delete.add(commit);
				}

				if (classes.size() > 0) {
					if (hash.containsKey(classes.size())) {
						hash.put(classes.size(), hash.get(classes.size()) + 1);
					} else
						hash.put(classes.size(), 1);
				} else
					return;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.gc();

		/**
		 * removes the scattering commits
		 */
		for (File file : delete) { file.deleteOnExit(); }

	}

}
