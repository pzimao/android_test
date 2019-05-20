package cn.edu.uestc.neo4j;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;

import java.sql.ResultSet;

import static org.neo4j.driver.v1.Values.parameters;

public class SmallExample2 {
    // Driver objects are thread-safe and are typically made available application-wide.
    Driver driver;

    public SmallExample2(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    private void printPeople(String initial) {
        try (Session session = driver.session()) {

            // Auto-commit transactions are a quick and easy way to wrap a read.
            StatementResult result = session.run(
                    "MATCH (a:Person) WHERE a.name STARTS WITH {x} RETURN a.name AS name",
                    parameters("x", initial));
            // Each Cypher execution returns a stream of records.
            while (result.hasNext()) {
                Record record = result.next();
                // Values can be extracted from a record by index or name.
                System.out.println(record.get("name").asString());
            }

        }
    }

    public void addApp() {
        String sql = "select * from app_domain";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        try {
            Session session = driver.session();
            int count = 0;
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String domain = resultSet.getString(2);
                int label = resultSet.getInt(3);
                session.run(String.format("match (a:App {id:%d}), (b:Domain {name:'%s'}) create (a)-[:request {type:%d}]->(b)", id, domain, label));
                System.out.println(count++);
            }
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDomain() {
        int count = 0;
        try (Session session = driver.session()) {
            String sql = "select distinct domain_desc from domain";
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
            try {
                StringBuilder sb = new StringBuilder();
                while (resultSet.next()) {
                    String web = resultSet.getString(1);
                    session.run(String.format("merge (web:Web {name:'%s'}) on create set web.created = timestamp()", web));
//                    session.run(String.format("match (ip:Ip {name:'%s'}), (area:Area {name:'%s'}) create (ip)-[:Locate]->(area)", ip, area));
//                    session.run(String.format("merge (webName:WebName {name:'%s'}) on create set webName.created = timestamp()", desc));
//                    String ip = resultSet.getString(2);
//                    String area = resultSet.getString(3);
//                    session.run(String.format("merge (a:Ip {name: '%s'}) on create set a.created = timestamp()", ip));
//                    session.run(String.format("merge (a:Area {name: '%s'}) on create set a.created = timestamp()", area));

                    // domain ->ip
//                    session.run(String.format("match (domain:Domain {name:'%s'}), (webName:WebName {name:'%s'}) merge (domain)-[:Record]->(webName) \n", domain, webName));
//                    session.run(String.format("match (app:App {name:'知到'}), (domain:Domain {name:'%s'})  create (app)-[r1:request {type:%d}]->(domain)", domain, type));

                    // ip -> area
//                    session.run(String.format("match (domain:Domain {name:'%s'}), (ip:Ip {name:'%s'}) create (domain)-[:resolving]->(ip) \n", domain, ip));
//                    session.run(String.format("match (a:Ip {name:'%s'}), (b:Area {name:'%s'}) merge (a)-[:locate]->(b) \n", ip, area));
//                    session.run(String.format("match (a:App {name: 'Soul'}), (b:Domain {name:'%s'}) create (a)-[:request]->(b) \n", domain));

                    System.out.println(count++);
                }
                System.out.println("done");
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        // Closing a driver immediately shuts down all open connections.
        driver.close();
    }

    public void addService() throws Exception {
        String sql = "select * from scanservice";
        ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
        Session session = driver.session();
        while (resultSet.next()) {
            String content = resultSet.getString(3);
            System.out.println(content);
            JSONObject jsonObject = new JSONObject(content);
            JSONArray resultArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject result = resultArray.getJSONObject(i);

                // IP
                String ip = result.getString("ip");
                // 硬件
                String hardWare = result.getString("hardware");
                // 系统版本
                String osVersion = result.getString("os_version");
                // 到这里先存IP
                session.run(String.format("merge (ip:Ip {name: '%s'}) on create set ip.hardware = '%s', ip.osVersion = '%s'", ip, hardWare, osVersion));
                // 端口
                JSONArray portArray = result.getJSONArray("ports");
                for (int j = 0; j < portArray.length(); j++) {
                    JSONObject portObject = portArray.getJSONObject(j);

                    int port = portObject.getInt("port");
                    // 到这里存端口
                    session.run(String.format("merge (port:Port {name: %s}) on create set port.created = timestamp()", port));

                    // todo 认为固定有4条属性
                    String product = portObject.getString("product");
                    String protocol = portObject.getString("protocol");
                    String service = portObject.getString("service");
                    String version = portObject.getString("version");
                    // 连接IP和端口
                    session.run(String.format("match (ip:Ip {name:'%s'}), (port:Port {name:%s}) create (ip)-[:Open {product:'%s', protocol:'%s', service:'%s', version:'%s'}]->(port)", ip, port, product, protocol, service, version));
                }
            }
        }
    }

    public static void main(String... args) throws Exception {
        SmallExample2 example = new SmallExample2("bolt://localhost:7687", "neo4j", "123456");
        example.addDomain();
        example.close();
    }
}