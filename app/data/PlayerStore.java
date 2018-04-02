package data;

import model.*;
import util.Neo4jDriver;

import java.util.HashMap;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import static org.neo4j.driver.v1.Values.parameters;

import play.Logger;

public class PlayerStore {

    //Basics
	public static int persist(Player player, Neo4jDriver db) throws Exception {

        player.id = Neo4jDriver.getUniqueId(db);

        String query =  
        "CREATE" + 
            " (n:Player { id:$id, name:$name })" + 
        " RETURN n";

        HashMap<String, Object> params = new HashMap<>();

        Logger.debug(player.id + ", " + player.name);
        
        params.put("id", player.id);
        params.put("name", player.name);

        StatementResult result = db.runQuery(query, params);

        Node n = result.single().get("n").asNode();

        Logger.debug("Created player with id: " + n.get("id").asInt());
        
        //Return
        return n.get("id").asInt();
    }

    private static int get(Token token, Neo4jDriver db) {
       
        String query = 
        "MATCH (t:Token)-[:Grants]->(p:Player)" + 
        " WHERE t.token = $token" + 
        " RETURN p";

        HashMap<String, Object> params = new HashMap<>();

        StatementResult result = db.runQuery(query, params);

        Node p = result.single().get("p").asNode();

        return result.single().get("exist").asInt();
    }


    //Queries
    public static boolean playerNameExist(String playerName, Neo4jDriver db) throws Exception {

        String queryExisting = 
            "MATCH (p:Player)" + 
            " WHERE p.name = $playerName" + 
            " RETURN count(p) AS exist";

        HashMap<String, Object> params = new HashMap<>();
        params.put("playerName", playerName);

        StatementResult queryExistingResult = db.runQuery(queryExisting, params);

        return queryExistingResult.single().get("exist").asInt() > 0;
    }
}