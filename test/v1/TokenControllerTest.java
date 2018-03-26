package v1;

import model.JsonFieldConstants;
import api.v1.UserController;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;
import org.junit.Test;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import util.Neo4jDriver;

import org.neo4j.driver.v1.StatementResult;

import test.*;
import play.Logger;

public class TokenControllerTest {

    private static String userName = TestVariables.USER;
    private static String password = TestVariables.PASSWORD;
    private static String playerName = TestVariables.PLAYER;

    @Test
    public void successful() {
        Logger.debug("-- START -- TokenControllerTest.successful");

        ObjectMapper objectMapper = new ObjectMapper();
        String token = "";

        try {

            create:{
                Logger.debug("> Creating user + player");

                ObjectNode createJson = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\", \"playerName\" : \"" + playerName + "\" }", ObjectNode.class);

                ObjectNode createResponse = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/users", 
                        "POST",
                        createJson
                    ), ObjectNode.class
                );

                assertTrue(createResponse.get("isSuccessfull").asBoolean());
                assertTrue(createResponse.get("response").asInt() == HttpStatus.SC_OK);
            }

            login:{
                Logger.debug("> Logging in");

                ObjectNode json = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\" }", ObjectNode.class);
                
                ObjectNode response = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/login", 
                        "POST",
                        json
                    ), ObjectNode.class
                );

                assertTrue(response.get("isSuccessfull").asBoolean());  
                assertTrue(response.get("response").asInt() == HttpStatus.SC_OK);
                
                JsonNode data = response.get(JsonFieldConstants.ROOT);
                token = data.get("token").textValue();
                assertTrue(!token.isEmpty());
                assertTrue(token.length() == 36);
            }

            logout:{
                Logger.debug("> Logging out");

                ObjectNode json = objectMapper.readValue("{ \"token\" : \"" + token + "\" }", ObjectNode.class);
                
                ObjectNode response = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/logout", 
                        "DELETE",
                        json
                    ), ObjectNode.class
                );

                assertTrue(response.get("isSuccessfull").asBoolean());  
                assertTrue(response.get("response").asInt() == HttpStatus.SC_OK);
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            clean:{
                Logger.debug("> Cleaning - user");

                Neo4jDriver db = new Neo4jDriver();

                String cleanUser = 
                "MATCH (u:User)" + 
                " WHERE" + 
                "   u.name = $userName" + 
                " DETACH DELETE u" +
                " RETURN count(u) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("userName", userName);
        
                StatementResult cleanUserResult = db.runQuery(cleanUser, params);


                Logger.debug("> Cleaning - player");

                String cleanPlayer = 
                "MATCH (p:Player)" + 
                " WHERE" + 
                "   p.name = $playerName" + 
                " DETACH DELETE p" +
                " RETURN count(p) AS deleted";
    
                params.put("playerName", playerName);
        
                StatementResult cleanPlayerResult = db.runQuery(cleanPlayer, params);


                Logger.debug("> Cleaning - token");

                String cleanToken = 
                "MATCH (t:Token)" + 
                " WHERE" + 
                "   t.token = $token" + 
                " DETACH DELETE t" +
                " RETURN count(t) AS deleted";
    
                params.put("token", token);
        
                StatementResult cleanTokenResult = db.runQuery(cleanToken, params);
            }

            Logger.debug("-- END -- TokenControllerTest.successful");
            Logger.debug("");
        }
    }

    @Test
    public void failedUser() {
        Logger.debug("-- START -- TokenControllerTest.failedUser");
        
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String token = "";

            create:{
                Logger.debug("> Creating user + player");

                ObjectNode createJson = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\", \"playerName\" : \"" + playerName + "\" }", ObjectNode.class);

                ObjectNode createResponse = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/users", 
                        "POST",
                        createJson
                    ), ObjectNode.class
                );

                assertTrue(createResponse.get("isSuccessfull").asBoolean());
                assertTrue(createResponse.get("response").asInt() == HttpStatus.SC_OK);
            }

            login:{
                Logger.debug("> Logging in - failing");

                ObjectNode json = objectMapper.readValue("{ \"userName\" : \"" + "Erroll" + "\", \"password\" : \"" + password + "\" }", ObjectNode.class);
                
                ObjectNode response = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/login", 
                        "POST",
                        json
                    ), ObjectNode.class
                );

                assertTrue(response.get("isSuccessfull").asBoolean());  
                assertTrue(response.get("response").asInt() == HttpStatus.SC_NOT_FOUND);
            }

            clean:{
                Logger.debug("> Cleaning");

                Neo4jDriver db = new Neo4jDriver();

                String cleanQuery = 
                "MATCH (u:User)-[:Is]->(p:Player)" + 
                " WHERE" + 
                "   u.name = $userName" + 
                " DETACH DELETE u,p" +
                " RETURN count(p) + count(u) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("userName", userName);
        
                StatementResult cleanResult = db.runQuery(cleanQuery, params);

                assertTrue(cleanResult.single().get("deleted").asInt() == 2);
            }

            Logger.debug("-- END -- TokenControllerTest.failedUser");
            Logger.debug("");
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void failedPassword() {
        Logger.debug("-- START -- TokenControllerTest.failedPassword");
        
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String token = "";

            create:{
                Logger.debug("> Creating user + player");

                ObjectNode createJson = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\", \"playerName\" : \"" + playerName + "\" }", ObjectNode.class);

                ObjectNode createResponse = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/users", 
                        "POST",
                        createJson
                    ), ObjectNode.class
                );

                assertTrue(createResponse.get("isSuccessfull").asBoolean());
                assertTrue(createResponse.get("response").asInt() == HttpStatus.SC_OK);
            }

            login:{
                Logger.debug("> Logging in - failing");

                ObjectNode json = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + "ErrorPwd" + "\" }", ObjectNode.class);
                
                ObjectNode response = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/login", 
                        "POST",
                        json
                    ), ObjectNode.class
                );

                assertTrue(response.get("isSuccessfull").asBoolean());  
                assertTrue(response.get("response").asInt() == HttpStatus.SC_NOT_FOUND);
            }

            clean:{
                Logger.debug("> Cleaning");

                Neo4jDriver db = new Neo4jDriver();

                String cleanQuery = 
                "MATCH (u:User)-[:Is]->(p:Player)" + 
                " WHERE" + 
                "   u.name = $userName" + 
                " DETACH DELETE u,p" +
                " RETURN count(p) + count(u) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("userName", userName);
        
                StatementResult cleanResult = db.runQuery(cleanQuery, params);

                assertTrue(cleanResult.single().get("deleted").asInt() == 2);
            }

            Logger.debug("-- END -- TokenControllerTest.failedPassword");
            Logger.debug("");
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void failedLogout() {
        Logger.debug("-- START -- TokenControllerTest.failedLogout");
        
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            logout:{
                Logger.debug("> Logging out - failing");

                ObjectNode json = objectMapper.readValue("{ \"token\" : \"" + "Not-the-token" + "\" }", ObjectNode.class);
                
                ObjectNode response = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/logout", 
                        "DELETE",
                        json
                    ), ObjectNode.class
                );

                assertTrue(response.get("isSuccessfull").asBoolean());  
                assertTrue(response.get("response").asInt() == HttpStatus.SC_NOT_FOUND);
            }

            Logger.debug("-- END -- TokenControllerTest.failedLogout");
            Logger.debug("");
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}