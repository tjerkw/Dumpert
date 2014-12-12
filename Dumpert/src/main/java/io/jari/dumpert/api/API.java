package io.jari.dumpert.api;
import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JARI.IO
 * Date: 11-12-14
 * Time: 21:58
 */
public class API {
    static String TAG = "DAPI";
    /**
     * getFrontpage fetches the frontpage items and parses them into a Item array.
     */
    public static Item[] getFrontpage(Integer page) throws IOException {
        Document document = Jsoup.connect("http://www.dumpert.nl/" + ((page != 0) ? page : "")).get();
        Elements elements = document.select(".dump-cnt .dumpthumb");

        ArrayList<Item> itemArrayList = new ArrayList<Item>();
        for(Element element : elements) {
            Item item = new Item();
            item.url = element.attr("href");
            item.title = element.select("h1").first().text();
            Log.d(TAG, "Parsing '"+item.url+"'");
            item.description = element.select("p.description").first().text();
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
        return returnList;
    }

    public static Item[] getFrontpage() throws IOException {
        return API.getFrontpage(0);
    }

    public static ItemInfo getItemInfo(String url) throws IOException, JSONException {
        Document document = Jsoup.connect(url).get();
        String rawFiles = document.select(".videoplayer").first().attr("data-files");
        rawFiles = new String(Base64.decode(rawFiles, Base64.DEFAULT), "UTF-8");
        JSONObject files = new JSONObject(rawFiles);
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.tabletVideo = files.getString("tablet");
        return itemInfo;
    }
}
