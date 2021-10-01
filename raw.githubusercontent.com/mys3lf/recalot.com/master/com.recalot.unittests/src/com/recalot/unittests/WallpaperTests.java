package com.recalot.unittests;

import com.recalot.common.Helper;
import com.recalot.common.communication.User;
import com.recalot.common.interfaces.model.rec.Recommender;
import com.recalot.unittests.helper.WebRequest;
import com.recalot.unittests.helper.WebResponse;
import com.sun.org.apache.xpath.internal.SourceTree;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import flexjson.transformer.IterableTransformer;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by matthaeus.schmedding on 24.04.2015.
 */
public class WallpaperTests {
    private String Path = "data/";
    private String TrackingPath = "track/";
    private String UsersPath = "users/";
    private String ItemsPath = "items/";
    private String InteractionsPath = "interactions/";


    public static String HOST = "http://localhost:8080/";
    public static String SourcesPath = "sources/";
    public static String SourceId = "wallpaper-src";
    public static String PathSeparator = "/";

    public static String JsonMimeType = "application/json; charset=UTF-8";

    public boolean isConnected = false;


    @Before
    public void checkIfSQLIsConnectedAndConnectIfNot() throws UnsupportedEncodingException {

        if (!isConnected) {
            try {

                WebResponse response = WebRequest.execute(HOST + SourcesPath + SourceId);
                if (response == null) {
                    initializeSQLSource();

                    Thread.sleep(2000);
                }

            } catch (Exception e) {
                initializeSQLSource();
            }

            isConnected = true;
        }
    }

    private void initializeSQLSource() throws UnsupportedEncodingException {
        Map<String, String> params = new Hashtable<>();
        params.put("data-builder-id", "mysql");
        params.put("source-id", SourceId);
        params.put("sql-server", "mysql://localhost:3306");
        params.put("sql-database", "wallpaper");
        params.put("sql-username", "root");
        params.put("sql-password", "mysqlpassword");

        WebResponse response = WebRequest.execute(WebRequest.HTTPMethod.PUT, HOST + SourcesPath, params);

        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);
    }

    @Test
    public void checkItemsAndFillWhenEmpty() {
        String host = HOST;
//        String host = "http://api.recalot.com/";
        String sourceId = SourceId;
        //      String sourceId = "wallpaper";
        WebResponse response = WebRequest.execute(host + SourcesPath + sourceId);
        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);

        HashMap map = new JSONDeserializer<HashMap>().deserialize(response.getBody());

        Integer itemsCount = (Integer) map.get("itemsCount");
        if (itemsCount > 0) {

        } else {

            try (BufferedReader br = new BufferedReader(new FileReader("C:\\Privat\\3_Uni\\5_Workspaces\\recalot.com\\data\\wallpaperwide.js"))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();

                ArrayList items = new JSONDeserializer<ArrayList>().deserialize(everything);
                JSONSerializer serializer = new JSONSerializer().transform(new IterableTransformer(), "Iterable.class").exclude("class", "*.class", "__type", "Id").include("Categories", "Tags");

                Long start = System.currentTimeMillis();
                int i = 0;

                HashMap<String, String> content = new HashMap<>();
                //    content.put("type", "dummy");

                //    response = WebRequest.execute(WebRequest.HTTPMethod.PUT, HOST + Path + SourcesPath + SourceId + PathSeparator + UsersPath, content);
                //    HashMap savedUser = new JSONDeserializer<HashMap>().deserialize(response.getBody());
                String userId = "1";

                WebRequest.Debug = false;

                for (Object item : items) {
                    String result = serializer.serialize(item);

                    content = new HashMap<>();
                    content.put("content", result);

                    String itemId = "" + ((HashMap) item).get("Id");

                    Integer itemIdInt = Integer.parseInt(itemId);
                    if (itemIdInt != null) {
                        //wallpaperwide ids should be 100.000.000 or higher
                        itemIdInt += 100000000;
                        itemId = "" + itemIdInt;
                    }
                    content.put("item-id", itemId);

                    response = WebRequest.execute(WebRequest.HTTPMethod.PUT, host + Path + SourcesPath + sourceId + PathSeparator + ItemsPath, content);

                    i++;
                    if (i % 1000 == 0) {
                        Long whole = System.currentTimeMillis() - start;
                        System.out.println("Users so far: " + whole + "ms. Avg. per user: " + (1.0 * whole / i) + "ms");
                    }
                }

                System.out.println("Users complete: " + (System.currentTimeMillis() - start) + "ms. Avg. per user: " + (1.0 * (System.currentTimeMillis() - start) / i) + "ms");

                int id = 1;
                start = System.currentTimeMillis();
                i = 0;
                if (false)
                    for (Object item : items) {
                        String itemId = "" + id++;

                        String rating = (String) ((HashMap) item).get("Rating");
                        String ratingCount = (String) ((HashMap) item).get("RatingCount");

                        if (rating != null && !rating.isEmpty() && ratingCount != null && !ratingCount.isEmpty()) {
                            Double r = Double.parseDouble(rating);
                            Integer count = Integer.parseInt(ratingCount.replace("(", "").replace(" vote)", "").replace(" votes)", ""));
                            for (int j = 0; j < count; j++) {
                                Map<String, String> params = new Hashtable<>();
                                params.put("type", "rating");
                                params.put("value", "" + r);

                                response = WebRequest.execute(WebRequest.HTTPMethod.POST, host + TrackingPath + SourcesPath + sourceId + PathSeparator + UsersPath + userId + PathSeparator + ItemsPath + itemId, params);

                                i++;

                                if (i % 1000 == 0) {
                                    Long whole = System.currentTimeMillis() - start;
                                    System.out.println("Interactions so far: " + whole + "ms. Avg. per interaction: " + (1.0 * whole / i) + "ms");
                                }
                            }
                        }
                    }

                System.out.println("Interactions complete: " + (System.currentTimeMillis() - start) + "ms. Avg. per interaction: " + (1.0 * (System.currentTimeMillis() - start) / i) + "ms");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void putInteractions() {
       // String host = HOST;
              String host = "http://api.recalot.com/";
        String sourceId = SourceId;
        //      String sourceId = "wallpaper";
        WebResponse response = WebRequest.execute(host + SourcesPath + sourceId);
        assertNotNull(response);
        assertEquals(response.getContentType(), JsonMimeType);
        assertNotNull(response.getBody());
        assertEquals(response.getResponseCode(), 200);

        HashMap map = new JSONDeserializer<HashMap>().deserialize(response.getBody());


        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Privat\\3_Uni\\wallpaper-data-10082015.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();

            ArrayList items = new JSONDeserializer<ArrayList>().deserialize(everything);
            JSONSerializer serializer = new JSONSerializer().transform(new IterableTransformer(), "Iterable.class").exclude("class", "*.class", "__type", "Id");


            HashMap<String, String> content;

            WebRequest.Debug = false;

            for (Object item : items) {
                HashMap itemMap = (HashMap) item;

                boolean userExists = false;
                try {

                    response = WebRequest.execute(WebRequest.HTTPMethod.GET, host + Path + SourcesPath + SourceId + PathSeparator + UsersPath + itemMap.get("userId"));
                    if (response != null) {
                        userExists = true;
                    }
                } catch (Exception e) {

                }

                if(!userExists) {
                    content = new HashMap<>();
                    content.put("user-id", "" + itemMap.get("userId"));
                    response = WebRequest.execute(WebRequest.HTTPMethod.PUT, host + Path + SourcesPath + SourceId + PathSeparator + UsersPath, content);
                }

                content = new HashMap<>();
                for(Object key: itemMap.keySet()) {
                    String k = (String)key;
                    if(!k.equals("content")){
                        content.put(k, "" + itemMap.get(key));
                    }
                }


                response = WebRequest.execute(WebRequest.HTTPMethod.POST, host + TrackingPath + SourcesPath + SourceId + PathSeparator + UsersPath + itemMap.get("userId") + PathSeparator + ItemsPath + itemMap.get("itemId"), content);
            }

     } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testMostPopularRecommender() throws UnsupportedEncodingException {
        String id = "wallpaper-mostpopular";
        Map<String, String> params = new Hashtable<>();
        params.put(Helper.Keys.SourceId, SourceId);
        params.put(Helper.Keys.SourceId, "wallpaper-src");
        params.put(Helper.Keys.RecommenderBuilderId, "wallpaper-mp");
        params.put(Helper.Keys.ID, id);
        params.put("topN", "20");

        HashMap train = RecommenderTests.sendTrain(id, params);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        HashMap rec = RecommenderTests.getRecommendations(id, params);
    }

    @Test
    public void testSurveyMostPopularRecommender() throws UnsupportedEncodingException {
        String id = "wallpaper-survey-test";
        Map<String, String> params = new Hashtable<>();
        params.put(Helper.Keys.SourceId, SourceId);
        params.put(Helper.Keys.SourceId, "wallpaper-src");
        params.put(Helper.Keys.RecommenderBuilderId, "wallpaper-survey");
        params.put(Helper.Keys.ID, id);
        params.put("topN", "20");

        HashMap train = RecommenderTests.sendTrain(id, params);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        HashMap rec = RecommenderTests.getRecommendations(id, params);
    }

}
