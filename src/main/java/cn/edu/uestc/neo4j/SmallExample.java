package cn.edu.uestc.neo4j;

import cn.edu.uestc.DataSource;
import cn.edu.uestc.utils.DBManager;
import org.neo4j.driver.v1.*;

import java.sql.ResultSet;

import static org.neo4j.driver.v1.Values.parameters;

public class SmallExample {
    // Driver objects are thread-safe and are typically made available application-wide.
    Driver driver;

    public SmallExample(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    private void addPerson(String name) {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = driver.session()) {
            // Wrapping Cypher in an explicit transaction provides atomicity
            // and makes handling errors much easier.
            try (Transaction tx = session.beginTransaction()) {
                tx.run("MERGE (a:Person {name: {x}})", parameters("x", name));
                tx.success();  // Mark this write as successful.
            }
        }
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
            String sql = "select domain from app_domain where app_domain.app_id = 147890";
            ResultSet resultSet = (ResultSet) DBManager.execute(DataSource.APP_TEST_DB, sql);
            try {
                StringBuilder sb = new StringBuilder();
                while (resultSet.next()) {
                    String domain = resultSet.getString(1);
//                    String ip = resultSet.getString(2);
//                    String area = resultSet.getString(3);
//                    session.run(String.format("merge (a:Ip {name: '%s'}) on create set a.created = timestamp()", ip));
//                    session.run(String.format("merge (a:Area {name: '%s'}) on create set a.created = timestamp()", area));

                    // domain ->ip
                    session.run(String.format("match (a:App {name:'Soul'}), (b:Domain {name:'%s'}) create (a)-[:request]->(b) \n", domain));

                    // ip -> area
//                    session.run(String.format("match (a:Ip {name:'%s'}), (b:Area {name:'%s'}) create (a)-[:location]->(b) \n", ip, area));
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

    public static void main(String... args) {
        SmallExample example = new SmallExample("bolt://localhost:7687", "neo4j", "123456");
        example.addDomain();
        example.close();
    }
}