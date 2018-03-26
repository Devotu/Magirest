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

public class UserControllerTest {

    private static String userName = TestVariables.USER;
    private static String password = TestVariables.PASSWORD;
    private static String playerName = TestVariables.PLAYER;

    @Test
    public void successful() {
        Logger.debug("-- START -- UserControllerTest.successful");

        ObjectMapper objectMapper = new ObjectMapper();

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

            delete:{
                Logger.debug("> Deleting user");

                ObjectNode deleteJson = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\" }", ObjectNode.class);
                
                ObjectNode deleteResponse = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/users", 
                        "DELETE",
                        deleteJson
                    ), ObjectNode.class
                );

                assertTrue(deleteResponse.get("isSuccessfull").asBoolean());  
                assertTrue(deleteResponse.get("response").asInt() == HttpStatus.SC_OK);
            }

            clean:{
                Logger.debug("> Cleaning");

                Neo4jDriver db = new Neo4jDriver();

                String cleanQuery = 
                "MATCH (p:Player)" + 
                " WHERE" + 
                "   p.name = $playerName" + 
                " DETACH DELETE p" +
                " RETURN count(p) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("playerName", playerName);
        
                StatementResult cleanResult = db.runQuery(cleanQuery, params);

                assertTrue(cleanResult.single().get("deleted").asInt() == 1);
            }

            Logger.debug("-- END -- UserControllerTest.successful");
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void existing() {
        Logger.debug("-- START -- UserControllerTest.userExist");
        
        ObjectMapper objectMapper = new ObjectMapper();

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

            create_user_fail:{
                Logger.debug("> Failing creation user");

                ObjectNode createJson = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\", \"playerName\" : \"" + playerName + "\" }", ObjectNode.class);

                ObjectNode createResponse = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/users", 
                        "POST",
                        createJson
                    ), ObjectNode.class
                );

                assertTrue(createResponse.get("isSuccessfull").asBoolean());
                assertTrue(createResponse.get("response").asInt() == HttpStatus.SC_LOCKED);

                JsonNode data = createResponse.get(JsonFieldConstants.ROOT);
                String problem = data.get(JsonFieldConstants.PROBLEM).textValue();
                assertTrue(UserController.ERROR_CODE_USER_EXIST.equals(problem));
            }

            clean_user:{
                Logger.debug("> Cleaning user");

                Neo4jDriver db = new Neo4jDriver();

                String cleanQuery = 
                "MATCH (u:User)" + 
                " WHERE" + 
                "   u.name = $userName" + 
                " DETACH DELETE u" +
                " RETURN count(u) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("userName", userName);
        
                StatementResult cleanResult = db.runQuery(cleanQuery, params);

                assertTrue(cleanResult.single().get("deleted").asInt() == 1);
            }

            create_player_fail:{
                Logger.debug("> Failing creation user");

                ObjectNode createJson = objectMapper.readValue("{ \"userName\" : \"" + userName + "\", \"password\" : \"" + password + "\", \"playerName\" : \"" + playerName + "\" }", ObjectNode.class);

                ObjectNode createResponse = objectMapper.readValue(
                    Requests.makeRequest(
                        TestVariables.BASE_URL + "/users", 
                        "POST",
                        createJson
                    ), ObjectNode.class
                );

                assertTrue(createResponse.get("isSuccessfull").asBoolean());
                assertTrue(createResponse.get("response").asInt() == HttpStatus.SC_LOCKED);

                JsonNode data = createResponse.get(JsonFieldConstants.ROOT);
                String problem = data.get(JsonFieldConstants.PROBLEM).textValue();
                assertTrue(UserController.ERROR_CODE_PLAYER_EXIST.equals(problem));
            }

            clean_player:{
                Logger.debug("> Cleaning player");

                Neo4jDriver db = new Neo4jDriver();

                String cleanQuery = 
                "MATCH (p:Player)" + 
                " WHERE" + 
                "   p.name = $playerName" + 
                " DETACH DELETE p" +
                " RETURN count(p) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("playerName", playerName);
        
                StatementResult cleanResult = db.runQuery(cleanQuery, params);

                assertTrue(cleanResult.single().get("deleted").asInt() == 1);
            }

            Logger.debug("-- END -- UserControllerTest.userExist");
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}