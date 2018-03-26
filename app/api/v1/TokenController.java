package api.v1;

import util.*;
import model.*;
import api.v1.UserController;

import play.mvc.*;
import play.Logger;

import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.HttpStatus;

import org.neo4j.driver.v1.exceptions.*;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;


public class TokenController extends Controller {

    public Result login(){

        String status = String.valueOf(HttpStatus.SC_NOT_FOUND);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnNode = mapper.createObjectNode();

        try {

            JsonNode json = request().body().asJson();
            String userName = json.get("userName").textValue();
            String password = json.get("password").textValue();

            Logger.debug(userName);
            Logger.debug(password);
            Logger.debug(Encryption.get_SHA_512(password));

                if (!userName.isEmpty() && !password.isEmpty()) {
                    
                    Neo4jDriver db = new Neo4jDriver();
                    int userId = UserController.getUserId(userName, password, db);

                    Logger.debug("Found userId: " + userId);

                    if (userId != 0) {

                        String query = 
                            " MATCH (u:User)-[:Is]->(p:Player) " +
                            " WHERE u.id = $userId " + 
                            " MERGE (p)<-[:Grants]-(t:Token) " +
                            "   ON CREATE " + 
                            "       SET " + 
                            "       t.token = $uuid, " +
                            "       t.valid = (timestamp() + " + Constants.TOKEN_DURATION_MS + ") " + 
                            "   ON MATCH " + 
                            "       SET " + 
                            "       t.valid = (timestamp() + " + Constants.TOKEN_DURATION_MS + ") " + 
                            " RETURN t.token AS token";

                        HashMap<String, Object> params = new HashMap<>();
                        params.put("userId", userId);    

                        String uuid = UUID.randomUUID().toString();
                        params.put("uuid", uuid);
            
                        StatementResult result = db.runQuery(query, params);
            
                        if (result.keys().contains("token")) {

                            returnNode.put("token", result.single().get("token").asString());                    
                            status = String.valueOf(HttpStatus.SC_OK);
                        }
                        
                    } else {

                        status = String.valueOf(HttpStatus.SC_NOT_FOUND);
                    }
                }

            } catch (Exception e) {
                status = String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace();
            }

        return created(Responses.createResponse(true, status, returnNode));
    }


    public Result logout(){
        
        String status = String.valueOf(HttpStatus.SC_NOT_FOUND);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnNode = mapper.createObjectNode();

        try {

            JsonNode json = request().body().asJson();
            Token token = mapper.readValue(json.get("token").toString(), Token.class);

            if (!token.token.isEmpty()) {

                Neo4jDriver db = new Neo4jDriver();

                String query = 
                " MATCH (t:Token) " + 
                " WHERE t.token = $token" + 
                " DETACH DELETE t " +
                " RETURN t ";

                HashMap<String, Object> params = new HashMap<>();     
                params.put("token", token.token);

                StatementResult result = db.runQuery(query, params);

                Logger.debug("--- got result");
                result.keys().stream().forEach(k -> Logger.debug(k));
                Logger.debug("result: " + result.toString());

                if (result.hasNext()) {
                    
                    status = String.valueOf(HttpStatus.SC_OK);
                } 
                else {
                    Logger.debug("--- not found");
                    status = String.valueOf(HttpStatus.SC_NOT_FOUND);
                }
            }

        } catch (Exception e) {
            status = String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        return created(Responses.createResponse(true, status, returnNode));
    }


    public static boolean verify(Token token, Neo4jDriver db){

        try {

            Logger.debug("Verifying token: " + token);

            if (!token.token.isEmpty()) {

                String query = 
                    " MATCH (t:Token) " + 
                    " WHERE " + 
                    "   t.token = $token " + 
                    " RETURN t";

                HashMap<String, Object> params = new HashMap<>();
                params.put("token", token.token); 
                
                StatementResult result = db.runQuery(query, params);

                if (result.hasNext()) {

                    Node tokenNode = result.single().get("t").asNode();

                    Logger.debug("Token valid through: " + tokenNode.get("valid").asLong() );
                    Logger.debug("Current time: " +(System.currentTimeMillis()) );
    
                    if (tokenNode.get("valid").asLong() < (System.currentTimeMillis()) ) {
                        return true;
                    }
                }
            }

            } catch (Exception e) {
                e.printStackTrace();
            }

        return false;
    }

    private static boolean extend(Token token, Neo4jDriver db){

        try {

            Logger.debug("Extending token: " + token);

            if (!token.token.isEmpty()) {

                String query = 
                    " MATCH (t:Token) " + 
                    " WHERE " + 
                    "   t.token = $token " + 
                    " SET t.valid = TIMESTAMP() " +
                    " RETURN t";

                HashMap<String, Object> params = new HashMap<>();
                params.put("token", token.token); 
                
                StatementResult result = db.runQuery(query, params);

                if (result.hasNext()) {

                    return true;
                }
            }

            } catch (Exception e) {
                e.printStackTrace();
            }

        return false;
    }
}