package io.jari.dumpert.api;
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

    /**
     * getFrontpage fetches the frontpage items and parses them into a Item array.
     */
    public static Item[] getFrontpage() throws IOException {
        Document document = Jsoup.connect("https://www.dumpert.nl/").get();
        Elements elements = document.select(".dump-cnt .dumpthumb");

        ArrayList<Item> itemArrayList = new ArrayList<Item>();
        for(Element element : elements) {
            Item item = new Item();
            item.url = element.attr("href");
            item.title = element.select("h1").first().text();
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
                Document imageDocument = Jsoup.connect(item.url).get();
                item.imageUrl = imageDocument.select("img.player").first().attr("src");
            }
            itemArrayList.add(item);
        }

        Item[] returnList = new Item[itemArrayList.size()];
        itemArrayList.toArray(returnList);
        return returnList;
    }
}
