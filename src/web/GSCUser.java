package web;


public class GSCUser {
    private String name;
    private String userPageURL;
    private int citationStatistics;
    private int hIndex;
    private int presence;

    public GSCUser() {
    }

    public GSCUser(String name, String userPageURL) {
        this.name = name;
        this.userPageURL = userPageURL;
    }

    public String getName() {
        return name;
    }

    public String getShortedName() {
//        String shortedName = name.replaceAll("[^A-Za-zА-Яа-яІіЇїЄєҐґ' ]+", "");
        String shortedName = name.replaceAll("[.]+", "").split("[,!;:?/\\\\]")[0];
        String[] nameParts = shortedName.split(" ");
        shortedName = "";
        int addedNameParts = 0;
        for (String namePart: nameParts) {
            if (namePart.length() > 2) {
                shortedName += namePart + " ";
                addedNameParts++;
            }
            if (addedNameParts >= 2) {
                break;
            }
        }
        return shortedName.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserPageURL() {
        return userPageURL;
    }

    public void setUserPageURL(String userPageURL) {
        this.userPageURL = userPageURL;
    }

    public String getId() {
        return userPageURL.substring(userPageURL.indexOf("user=") + 5).split("&")[0];
    }


    public int getCitationStatistics() {
        return citationStatistics;
    }

    public void setCitationStatistics(int citationStatistics) {
        this.citationStatistics = citationStatistics;
    }

    public int getHIndex() {
        return hIndex;
    }

    public void setHIndex(int hIndex) {
        this.hIndex = hIndex;
    }

    public int getPresence() {
        return presence;
    }

    public void setPresence(int presence) {
        this.presence = presence;
    }

    @Override
    public String toString() {
        return "GSCUser{" +
                "name='" + name + '\'' +
                ", shorted name ='" + getShortedName() + '\'' +
                ", userPageURL='" + userPageURL + '\'' +
                ", citationStatistics='" + citationStatistics + '\'' +
                ", hIndex'" + hIndex + '\'' +
                ", presence='" + presence + '\'' +
                ", id='" + getId() + '\'' +
                '}';
    }

    public static String getUserPageURLFromId(String id) {
        return "https://scholar.google.com.ua/citations?user=" + id + "&hl=uk";
    }
}
