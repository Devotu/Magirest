package data;

import model.*;
import util.*;

import java.util.HashMap;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.exceptions.*;

import static org.neo4j.driver.v1.Values.parameters;

import play.Logger;

public class UserStore {

    //Basics
	public static int persist(User user, Neo4jDriver db) throws Exception {

        int userId = Neo4jDriver.getUniqueId(db);

        String query =  
        "CREATE" + 
            " (n:User {id:$id, name:$name, password:$hashedPassword, created:TIMESTAMP()})" + 
        " RETURN n";

        HashMap<String, Object> params = new HashMap<>();

        Logger.debug(user.id + ", " + user.name);
        
        params.put("id", userId);
        params.put("name", user.name);

        StatementResult result = db.runQuery(query, params);

        Node n = result.single().get("n").asNode();

        Logger.debug("Created user with id: " + n.get("id").asInt());
        
        //Return
        return n.get("id").asInt();
    }

    //Util
    public static boolean userNameExist(String name, Neo4jDriver db) throws Exception {

        String query = "MATCH (u:User)" + " WHERE u.name = $name" + " RETURN count(u) AS exist";

        HashMap<String, Object> params = new HashMap<>();
        params.put("name", name);

        StatementResult result = db.runQuery(query, params);

        return result.single().get("exist").asInt() > 0;
    }

    public static boolean validateCredentials(String userName, String password, Neo4jDriver db) throws Exception {

        String query = 
            "MATCH (u:User)" + 
            " WHERE" + 
            "   u.name = $userName AND" + 
            "   u.password = $hashedPassword" + 
            " RETURN count(u) AS exist";

        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", userName);

        String hashedPassword = Encryption.get_SHA_512(password);             
        params.put("hashedPassword", hashedPassword);

        StatementResult result = db.runQuery(query, params);

        return result.single().get("exist").asInt() > 0;
    }

    //Creators
    public static int addIsPlayer(User user, Player player, Neo4jDriver db) throws Exception {
        
        Logger.debug("Adding user " + user.name + " - Is - " + player.name);

        //Player
        int playerId = PlayerStore.persist(player, db);

        if (playerId > 0) {
            
            //Relationship
            String query = 
            " MATCH " + 
            "   (u:User), (p:Player)" + 
            " WHERE" + 
            "   u.id = $userId AND" + 
            "   p.id = $playerId" + 
            " CREATE " + 
            "   (u)-[r:Is]->(p) " +
            " RETURN count(r) AS created";

            HashMap<String, Object> params = new HashMap<>();

            params.put("userId", user.id);
            params.put("playerId", player.id);

            StatementResult result = db.runQuery(query, params);

            Node r = result.single().get("r").asNode();

            Logger.debug("Added user " + user.name + " is " + player.name + " " + r.toString());  
            
            if (r != null) {
                return playerId;
            }


        }

        db.clearErrors();

        return 0;
    }

    public static User getUser(int id, Neo4jDriver db){

        String query = 
            "MATCH (u:User)" + 
            " WHERE" + 
            "   u.id = $id " + 
            " RETURN u";

        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);

        StatementResult result = db.runQuery(query, params);

        Node n = result.single().get("n").asNode();

        User user = ModelConverter.toUser(n);

        Logger.debug( "Got User: " + user.name );

        return user;
    }
}