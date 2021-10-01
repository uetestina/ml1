/*
 TagRecommender:
 A framework to implement and evaluate algorithms for the recommendation
 of tags.
 Copyright (C) 2013 Dominik Kowald
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package engine;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import common.Bookmark;
import common.IntMapComparator;
import file.BookmarkReader;
import file.BookmarkSplitter;
import file.BookmarkWriter;

public class EngineUtils {

	public static BookmarkReader getSortedBookmarkReader(String path, String filename) {
		BookmarkReader reader = new BookmarkReader(0, false);
		reader.readFile(path, filename);		
		
		Collections.sort(reader.getBookmarks());
		
		String sortedFile = filename + "_sorted";
		BookmarkWriter.writeSample(reader, reader.getBookmarks(), path, sortedFile, null, true);
		reader = new BookmarkReader(0, false);
		reader.readFile(path, sortedFile);
		
		try {
			String deletePath = "";
			if (path == null) {
				deletePath = "./data/csv/" + sortedFile + ".txt";
			} else {
				deletePath = path + sortedFile;
			}
			if (!new File(deletePath).delete()) {
				System.out.println("Problem while deleting sorted temp-file");
			}
		} catch (Exception e) {
			System.out.println("Problem while deleting sorted temp-file");
		}
		
		return reader;
	}
	
	public static List<Integer> getFilterTags(boolean filterOwnEntities, BookmarkReader reader, String user, String resource) {
		List<Integer> filterTags = new ArrayList<Integer>();
		if (filterOwnEntities && user != null) {
			if (resource != null) {
				int userID = -1;
				if (user != null) {
					userID = reader.getUsers().indexOf(user);
				}
				int resID = -1;
				if (resource != null) {
					resID = reader.getResources().indexOf(resource);
				}
				filterTags = Bookmark.getTagsOfBookmark(reader.getBookmarks(), userID, resID);
			}/* else {
				if (userMap != null) {
					filterTags = new ArrayList<Integer>(userMap.keySet());
				}
			}*/
		}
		
		return filterTags;
	}
	
	public static Map<Integer, Double> calcTopEntities(BookmarkReader reader, EntityType type) {
		Map<Integer, Double> map = new LinkedHashMap<>();
		Map<Integer, Integer> countMap = new LinkedHashMap<Integer, Integer>();
		List<Integer> entityCounts = null;
		if (type == EntityType.TAG) {
			entityCounts = reader.getTagCounts();
		} else if (type == EntityType.RESOURCE) {
			entityCounts = reader.getResourceCounts();
		} else {
			entityCounts = reader.getUserCounts();
		}
		
		Integer countSum = 0;
		for (int i = 0; i < entityCounts.size(); i++) {
			countMap.put(i, entityCounts.get(i));
			countSum += entityCounts.get(i);
		}

		Map<Integer, Integer> sortedCountMap = new TreeMap<Integer, Integer>(new IntMapComparator(countMap));
		sortedCountMap.putAll(countMap);
		for (Map.Entry<Integer, Integer> entry : sortedCountMap.entrySet()) {
			map.put(entry.getKey(), ((double) entry.getValue()) / countSum);
		}
		return map;
	}
}
