package cn.edu.uestc;

public enum DataSource {
    CRAWLER_DB(1, "crawlerDB", "select id, app_name, packagename, dl_url from app_info "),
    PACKAGE_DB(2, "packageDB", "select id, app_name, packagename, dl_url from app_info2 "),
    URL_DB(3, "urlDB", "select id, app_name, packagename, dl_url from app_info3 "),

    APP_TEST_DB(0, "appTestDB", "");

    int index;
    String name;
    String querySql;

    DataSource(int index, String name, String querySql) {
        this.index = index;
        this.name = name;
        this.querySql = querySql;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public String getQuerySql() {
        return querySql;
    }
}
