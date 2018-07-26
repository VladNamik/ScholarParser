package web;


import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class ScholarParser {
    private static final String USER_CLASS = "gsc_1usr gs_scl";
    private static final String USER_NAME_CLASS = "gsc_oai_name";
    public static final String[] USER_AGENTS = new String[]{
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2226.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2224.3 Safari/537.36",
            HttpConnection.DEFAULT_UA,
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20130401 Firefox/31.0",
            "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.2 (KHTML, like Gecko) ChromePlus/4.0.222.3 Chrome/4.0.222.3 Safari/532.2",
            "Mozilla/5.0 (Windows; U; Windows NT 6.1; x64; fr; rv:1.9.2.13) Gecko/20101203 Firebird/3.6.13",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 7.0; InfoPath.3; .NET CLR 3.1.40767; Trident/6.0; en-IN)",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 7.0; InfoPath.3; .NET CLR 3.1.40767; Trident/6.0; en-IN)",
            "Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16",
            "Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52",
            "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25"
    };
    public static int currentlyParsedPagesCount = 0;
    public static Random random = new Random();

    public interface OnUpdateParseStatusListener {
        void onUpdate(int lastAddedUserNumber);
    }

    public static List<GSCUser> parse(String url, OnUpdateParseStatusListener parseStatusListener) throws IOException{
        List<GSCUser> users = new ArrayList<>();

        Element buttonNext;
        while (true) {
            Document document = Jsoup.connect(url).userAgent(getUserAgent()).timeout(60000).get();
            currentlyParsedPagesCount++;
            buttonNext = document.getElementById("gsc_authors_bottom_pag").getElementsByTag("button").get(1);
            List<GSCUser> usersOnePage = parsePage(document);
            users.addAll(usersOnePage);
            if (parseStatusListener != null) {
                parseStatusListener.onUpdate(users.size());
            }
            if (!buttonNext.hasAttr("onclick")) {
                break;
            }
            url = getNextURL(buttonNext.attr("onclick"));
        }
        return users;
    }


    public static Pair<List<GSCUser>, List<Integer>> parseUsersByIds(List<GSCUser> users, OnUpdateParseStatusListener parseStatusListener) throws IOException{
        int index = 1;
        List<Integer> notParsedUsersIds = new ArrayList<>();
        for (GSCUser user: users) {
            try {
                parseUser(user.getUserPageURL(), user);
            } catch (NullPointerException | IllegalArgumentException e) {
                notParsedUsersIds.add(index + 1);
            }
            parseStatusListener.onUpdate(index);
            index++;
        }
        return new Pair<>(users, notParsedUsersIds);
    }


    public static List<GSCUser> parse(String url) throws IOException{
        return parse(url, null);
    }


    public static List<GSCUser> parsePage(String url) throws IOException{
        currentlyParsedPagesCount++;
        return parsePage(Jsoup.connect(url).userAgent(getUserAgent()).timeout(60000).get());
    }

    private static List<GSCUser> parsePage(Document document) throws IOException{
        Elements elements = document.getElementsByClass(USER_CLASS);

        List<GSCUser> users = new ArrayList<>();
        GSCUser user = null;
        for(Element element: elements) {
            user = new GSCUser(element.getElementsByClass(USER_NAME_CLASS).first().text(),
                    element.getElementsByClass(USER_NAME_CLASS).first().getElementsByTag("a").first().attr("abs:href"));
            user = parseUser(user.getUserPageURL(), user);
            users.add(user);
        }

        return users;
    }

    public static GSCUser parseUser(String url, GSCUser user) throws IOException {
        //по 100 статей на страницу, чтобы быстрее узнать общее кол-во статей
        url = url + "&cstart=0&pagesize=100";
        Document document = Jsoup.connect(url).userAgent(getUserAgent()).timeout(60000).get();  // 1 minute timeout
        currentlyParsedPagesCount++;

        Element tableElement = document.body().getElementById("gsc_rsb_st");
        if (tableElement == null) {
            user.setCitationStatistics(0);
            user.setHIndex(0);
        } else {
            Elements rowsElements = tableElement.getElementsByTag("tr");
            user.setCitationStatistics(Integer.parseInt(rowsElements.get(1).getElementsByTag("td").get(1).text()));
            user.setHIndex(Integer.parseInt(rowsElements.get(2).getElementsByTag("td").get(1).text()));
        }

        int presence = 0;
        Element titleRangeElement = document.getElementById("gsc_a_nn");
        if (titleRangeElement != null) {
            String title_range = titleRangeElement.text().trim();
            if (!title_range.equals("")) {
                presence = Integer.parseInt(title_range.split("–")[1]);
            }
            int k = 1;
            while (presence == k * 100) {
                url = url.replace("cstart=" + Integer.toString((k - 1) * 100), "cstart=" + Integer.toString(k * 100));
                document = Jsoup.connect(url).userAgent(getUserAgent()).timeout(60000).get();
                try {
                    Thread.sleep(Math.abs(random.nextInt()) % 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentlyParsedPagesCount++;
                titleRangeElement = document.getElementById("gsc_a_nn");
                if (titleRangeElement == null) {
                    break;
                }
                title_range = titleRangeElement.text().trim();
                if (!title_range.equals("")) {
                    presence = Integer.parseInt(title_range.split("–")[1]);
                }
                k++;
            }
        }
        user.setPresence(presence);

        return user;
    }

    private static String getNextURL(String onClickText) {
        return "https://scholar.google.com.ua" + onClickText.substring(onClickText.indexOf("'") + 1, onClickText.lastIndexOf("'")).replace("\\x3d", "=").replace("\\x26", "&");
    }

    private static String getUserAgent() {
        return USER_AGENTS[Math.abs(random.nextInt()) % USER_AGENTS.length];
    }
}
