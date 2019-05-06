package cn.edu.uestc.neo4j;

import org.neo4j.driver.v1.*;

public class MyTest {
    public Driver driver;

    MyTest() {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "123456";
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public static void main(String[] args) {
        MyTest test = new MyTest();
        String cql = "match n = allshortestpaths((小讯:朋友圈 {姓名:\"小讯\"})-[*..9]->(小锐:朋友圈 {姓名:\"小锐\"})) return n";
        StatementResult result = test.execute(cql, Values.parameters());
        while (result.hasNext()) {
            Record record = result.next();
            // Values can be extracted from a record by index or name.
//            System.out.println(record.get("姓名").asString());
            record.fields().forEach(f-> System.out.println(f.key() + " " + f.value()));
        }

    }

    public StatementResult execute(String cql, Value values) {
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult statementResult = transaction.run(cql, values);
        transaction.success();
        session.close();
        return statementResult;
    }
}
