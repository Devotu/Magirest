package util;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.*;
import java.util.List;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;

public class Neo4jDriver implements AutoCloseable {
    private static String DB_CONF_PATH = "conf/db.conf";
    private final Driver driver;

    private String protocol;
    private String host;
    private String port;
    private String username;
    private String password;

    public Neo4jDriver() {
        loadConf();
        driver = GraphDatabase.driver(this.protocol + "://" + this.host + ":" + this.port,
                AuthTokens.basic(this.username, this.password));
    }

    public void loadConf() {

        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader br = new BufferedReader(new FileReader(DB_CONF_PATH))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            ObjectNode confJson = (ObjectNode) mapper.readTree(sb.toString());
            ObjectNode settings = (ObjectNode) confJson.findValue("main");
            this.protocol = settings.get("protocol").asText();
            this.host = settings.get("host").asText();
            this.port = settings.get("port").asText();
            this.username = settings.get("username").asText();
            this.password = settings.get("password").asText();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    public StatementResult runQuery(final String query, final HashMap<String, Object> params) {
        
        Logger.debug("Running query : " + query);
        if (params != null) {
            for (Object object : params.values()) {
                Logger.debug("Parameter: " + object.toString());
            }
        }

        try (Session session = driver.session())
        {
            return session.run(query, params);
        }
    }

    public int getUniqueId() throws Exception {

        try {
            Neo4jDriver db = new Neo4jDriver();

            return getUniqueId(db);

        } catch (Exception ex) {
            throw ex;
        }
    }

    public static int getUniqueId(Neo4jDriver db) throws Exception {

        try {

            String query = "" + "MERGE (id:GlobalUniqueId) " + 
                "ON CREATE SET id.count = 1 " + 
                "ON MATCH SET id.count = id.count + 1 " + 
                "RETURN id.count AS generated_id ";

            StatementResult result = db.runQuery(query, null);

            return result.single().get(0).asInt();

        } catch (Exception ex) {
            throw ex;
        }
    }
}