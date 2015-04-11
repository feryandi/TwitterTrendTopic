import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.ListIterator;

public class MatchEngine {
    private ArrayList<Category> keyword = new ArrayList<>();
    private API api = new API();
    private String mainKey;

    public void addCategory ( Category c ) {
        keyword.add(c);
    }

    public void run (String query, int amount) {
        JSONDecode(query);

        api.fetch(mainKey, amount);
        StringMatcher(api);
        api.sortTweet();

        JSONEncode();
    }

    public API getAPI () {
        return api;
    }

    public void JSONDecode (String JSON) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(JSON);
            JSONObject input = (JSONObject)obj;

            mainKey = (input.get("hashtag")).toString();

            JSONArray category = (JSONArray)input.get("category");
            for ( int i = 0; i < category.size(); ++i ) {
                JSONObject subcategory = (JSONObject)category.get(i);
                Category cat = new Category((i+1), subcategory.get("name").toString());

                JSONArray keywords = (JSONArray)subcategory.get("keys");
                for ( int j = 0; j < keywords.size(); ++j ) {
                    cat.addKey(keywords.get(j).toString());
                }

                addCategory(cat);
            }

        } catch (ParseException pe) {
            System.out.println("Exception");
        }
    }

    public String JSONEncode () {
        JSONArray output = new JSONArray();
        JSONObject tweet = new JSONObject();

        for ( int i = 0; i < api.getArrSize(); ++i ) {
            tweet.put("user", api.getUser(i));
            tweet.put("msg", api.getMsg(i));
            tweet.put("date", (api.getDate(i)).toString()); /*Need display convention*/
            tweet.put("category", api.getCategory(i));

            output.add(tweet);
        }

        System.out.println(output);

        return output.toJSONString();
    }

    public void StringMatcher ( API a ) {

        ListIterator<Tweet> tweetIterator = a.getTweetData().listIterator();
        while( tweetIterator.hasNext() ) {
            Tweet t = tweetIterator.next();
            boolean found = false;
            int catID = 0;

            ListIterator<Category> categoryIterator = keyword.listIterator();
            while ( ( !found ) && ( categoryIterator.hasNext() ) ) {
                ++catID;
                Category c = categoryIterator.next();

                ListIterator<String> keyIterator = (c.key).listIterator();
                while ( ( !found ) && ( keyIterator.hasNext() ) ) {
                    String k = keyIterator.next();

                    /* CHANGE CODE BELOW TO KMP OR BOOYER-MOYES */
                    if ( ((t.msg).toLowerCase()).matches("(.*)" + k.toLowerCase() + "(.*)") ) {
                        t.category = catID;

                        //System.out.println("FOUND : " + t.msg);

                        found = true;
                    }
                    /* CHANGE CODE ABOVE TO KMP OR BOOYER-MOYES */

                }

            }
        }
    }
}
