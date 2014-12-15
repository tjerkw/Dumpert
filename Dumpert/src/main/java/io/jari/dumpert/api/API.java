package io.jari.dumpert.api;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import io.jari.dumpert.Utils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JARI.IO
 * Date: 11-12-14
 * Time: 21:58
 */
public class API {
    static String TAG = "DAPI";

    static Object getFromCache(Context context, String key) {
        String raw = context.getSharedPreferences("dumpert", 0).getString(key, null);
        if(raw == null) return null;
        return SerializeObject.stringToObject(raw);
    }

    static void saveToCache(Context context, String key, Serializable object) {
        context.getSharedPreferences("dumpert", 0).edit().putString(key, SerializeObject.objectToString(object)).apply();
    }

    /**
     * getFrontpage fetches the frontpage items and parses them into a Item array.
     * Returns cache if in offline mode.
     */
    public static Item[] getFrontpage(Integer page, Context context) throws IOException {
        if(Utils.isOffline(context)) {
            Object cacheObj = API.getFromCache(context, "frontpage_"+page);
            //if no cached data present, return empty array
            if(cacheObj == null) return new Item[0];
            else {
                return (Item[])cacheObj;
            }
        }
        Document document = Jsoup.connect("http://www.dumpert.nl/" + ((page != 0) ? page : "")).get();
        Elements elements = document.select(".dump-cnt .dumpthumb");

        ArrayList<Item> itemArrayList = new ArrayList<Item>();
        for(Element element : elements) {
            Item item = new Item();
            item.url = element.attr("href");
            item.title = element.select("h1").first().text();
            Log.d(TAG, "Parsing '"+item.url+"'");
            item.description = element.select("p.description").first().html();
            item.thumbUrl = element.select("img").first().attr("src");
            item.date = element.select("date").first().text();
            item.stats = element.select("p.stats").first().text();
            item.photo = element.select(".foto").size() > 0;
            item.video = element.select(".video").size() > 0;
            if(item.video)
                item.imageUrl = item.thumbUrl.replace("sq_thumbs", "stills");
            else if(item.photo) {
                //get the image itself from it's url.
                //sadly no other way to get full hq image :'(
                Log.d(TAG, "Got image, requesting "+item.url);
                Document imageDocument = Jsoup.connect(item.url).get();
                item.imageUrl = imageDocument.select("img.player").first().attr("src");
            }
            itemArrayList.add(item);
        }

        Item[] returnList = new Item[itemArrayList.size()];
        itemArrayList.toArray(returnList);

        saveToCache(context, "frontpage_"+page, returnList);

        return returnList;
    }

    public static Item[] getFrontpage(Context context) throws IOException {
        return API.getFrontpage(0, context);
    }

    public static ItemInfo getItemInfo(String url) throws IOException, JSONException {
        Document document = Jsoup.connect(url).get();
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.itemId = document.select("body").first().attr("data-itemid");
        String rawFiles = document.select(".videoplayer").first().attr("data-files");
        rawFiles = new String(Base64.decode(rawFiles, Base64.DEFAULT), "UTF-8");
        JSONObject files = new JSONObject(rawFiles);
        itemInfo.tabletVideo = files.getString("tablet");
        return itemInfo;
    }

    public static Comment[] getComments(String itemId) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://dumpcomments.geenstijl.nl/"+itemId+".js");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String file = httpclient.execute(httpget, responseHandler);
        Pattern pattern = Pattern.compile("comments\\.push\\('(<[p|footer|article].*)'\\);");
        Matcher matcher = pattern.matcher(file);
        StringBuilder rawDoc = new StringBuilder();
        while(matcher.find()) {
            rawDoc.append(matcher.group(1));
        }
        Document document = Jsoup.parse(rawDoc.toString());
        ArrayList<Comment> comments = new ArrayList<Comment>();
        Elements elements = document.select("article");
        for(Element element : elements) {
            Comment comment = new Comment();
            comment.id = element.attr("id");
            comment.content = element.select("p").first().html();
            String footer = element.select("footer").first().text();
            StringTokenizer tokenizer = new StringTokenizer(footer, "|");
            comment.author = tokenizer.nextToken().trim();
            comment.time = tokenizer.nextToken().trim();
            comments.add(comment);
        }

        Comment[] returnArr = new Comment[comments.size()];
        comments.toArray(returnArr);
        return returnArr;
    }
}
