package api.v1;

import util.*;
import model.*;
import data.*;
import v1.ErrorCodes;

import play.mvc.*;
import play.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.*;

import static org.neo4j.driver.v1.Values.parameters;

import org.apache.http.HttpStatus;

public class UserController extends Controller {

    public static String ERROR_CODE_USER_EXIST = "User name already in use.";
    public static String ERROR_CODE_PLAYER_EXIST = "Player name already in use.";



    public static int getUserId(String userName, String password, Neo4jDriver db) throws Exception {

        String queryExisting = 
            "MATCH (u:User)" + 
            " WHERE" + 
            "   u.name = $userName AND" + 
            "   u.password = $hashedPassword" + 
            " RETURN u.id AS id";

        HashMap<String, Object> params = new HashMap<>();
        params.put("userName", userName);

        String hashedPassword = Encryption.get_SHA_512(password);             
        params.put("hashedPassword", hashedPassword);

        StatementResult queryExistingResult = db.runQuery(queryExisting, params);

        try {
            return queryExistingResult.single().get("id").asInt();
        } catch (NoSuchRecordException e) {
            return 0;
        }        
    }

    public Result create() {

        String status = String.valueOf(HttpStatus.SC_METHOD_FAILURE);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnNode = mapper.createObjectNode();

        JsonNode json = request().body().asJson();
        String userName = json.get("userName").textValue();
        String password = json.get("password").textValue();
        String playerName = json.get("playerName").textValue();

        try {
            Neo4jDriver db = new Neo4jDriver();

            creator: { 
                if (!Encryption.verifyPassword(password)){
                    status = String.valueOf(HttpStatus.SC_LOCKED);
                    returnNode.put(JsonFieldConstants.PROBLEM, "Password to short, minimum length is " + Encryption.PASSWORD_MIN_LENGTH + "characters.");
                    break creator;
                }

                if (UserStore.userNameExist(userName, db)){
                    status = String.valueOf(HttpStatus.SC_LOCKED);
                    returnNode.put(JsonFieldConstants.PROBLEM, ERROR_CODE_USER_EXIST);
                    break creator;
                }

                if (PlayerStore.playerNameExist(playerName, db)){
                    status = String.valueOf(HttpStatus.SC_LOCKED);
                    returnNode.put(JsonFieldConstants.PROBLEM, ERROR_CODE_PLAYER_EXIST);
                    break creator;
                }

                String queryCreate =  
                "CREATE" + 
                    " (u:User {id:$userId, name:$userName, password:$hashedPassword, created:TIMESTAMP()})" + 
                    "-[:Is]->" + 
                    "(p:Player {id:$playerId, name:$playerName})" + 
                " RETURN u,p";

                HashMap<String, Object> params = new HashMap<>();
                params.put("userName", userName);

                int userId = Neo4jDriver.getUniqueId(db);
                params.put("userId", userId);

                params.put("playerName", playerName);

                int playerId = Neo4jDriver.getUniqueId(db);                
                params.put("playerId", playerId);

                String hashedPassword = Encryption.get_SHA_512(password);             
                params.put("hashedPassword", hashedPassword);

                StatementResult createResult = db.runQuery(queryCreate, params);

                if (createResult.keys().contains("u") && createResult.keys().contains("p")) {
                    
                    status = String.valueOf(HttpStatus.SC_OK);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return created(Responses.createResponse(true, status, returnNode));
    }


    public Result delete() {

        String status = String.valueOf(HttpStatus.SC_METHOD_FAILURE);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnNode = mapper.createObjectNode();

        JsonNode json = request().body().asJson();
        Logger.debug(json.toString());
        String userName = json.get("userName").textValue();
        String password = json.get("password").textValue();

        try {
            Neo4jDriver db = new Neo4jDriver();

            destructor: { 

                if (!UserStore.userNameExist(userName, db)){
                    status = String.valueOf(HttpStatus.SC_NOT_FOUND);
                    returnNode.put(JsonFieldConstants.PROBLEM, "Cannot find user.");
                    break destructor;
                }

                if (!UserStore.validateCredentials(userName, password, db)){
                    status = String.valueOf(HttpStatus.SC_NOT_FOUND);
                    returnNode.put(JsonFieldConstants.PROBLEM, "Incorrect password.");
                    break destructor;
                }

                //Player, decks etc. will remain in db for others to use
                String deleteExistingQuery = 
                "MATCH (u:User)" + 
                " WHERE" + 
                "   u.name = $userName AND" + 
                "   u.password = $hashedPassword" + 
                " DETACH DELETE u" +
                " RETURN count(u) AS deleted";
    
                HashMap<String, Object> params = new HashMap<>();
                params.put("userName", userName);
        
                String hashedPassword = Encryption.get_SHA_512(password);             
                params.put("hashedPassword", hashedPassword);
        
                StatementResult deleteExistingResult = db.runQuery(deleteExistingQuery, params);

                if (deleteExistingResult.single().get("deleted").asInt() > 0) {
                    
                    status = String.valueOf(HttpStatus.SC_OK);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return created(Responses.createResponse(true, status, returnNode));
    }


    public JsonNode create(JsonNode json) {

        String status = String.valueOf(HttpStatus.SC_METHOD_FAILURE);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnNode = mapper.createObjectNode();

        String userName = json.get("userName").textValue();
        String password = json.get("password").textValue();
        String playerName = json.get("playerName").textValue();

        try {
            Neo4jDriver db = new Neo4jDriver();

            creator: { 
                if (!Encryption.verifyPassword(password)){
                    status = String.valueOf(HttpStatus.SC_LOCKED);
                    returnNode.put(JsonFieldConstants.PROBLEM, "Password to short, minimum length is " + Encryption.PASSWORD_MIN_LENGTH + " characters.");
                    break creator;
                }

                if ("".equals(userName)){
                    status = String.valueOf(HttpStatus.SC_BAD_REQUEST);
                    returnNode.put(JsonFieldConstants.PROBLEM, "userName: " + ErrorCodes.PARAMETER_EMPTY.code() + ErrorCodes.EXPECTED_STRING.code());
                    break creator;
                }

                if (UserStore.userNameExist(userName, db)){
                    status = String.valueOf(HttpStatus.SC_LOCKED);
                    returnNode.put(JsonFieldConstants.PROBLEM, ERROR_CODE_USER_EXIST);
                    break creator;
                }

                if ("".equals(playerName)){
                    status = String.valueOf(HttpStatus.SC_BAD_REQUEST);
                    returnNode.put(JsonFieldConstants.PROBLEM, "playerName: " + ErrorCodes.PARAMETER_EMPTY.code() + ErrorCodes.EXPECTED_STRING.code());
                    break creator;
                }

                if (PlayerStore.playerNameExist(playerName, db)){
                    status = String.valueOf(HttpStatus.SC_LOCKED);
                    returnNode.put(JsonFieldConstants.PROBLEM, ERROR_CODE_PLAYER_EXIST);
                    break creator;
                }

                User inputUser = new User(0, userName, password);
                int userId = UserStore.persist(inputUser, db);
                User user = UserStore.getUser(userId, db);

                Player inputPlayer = new Player(0, playerName);
                int playerId = UserStore.addIsPlayer(user, inputPlayer, db);

                if (playerId != 0) {
                    
                    status = String.valueOf(HttpStatus.SC_OK);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        returnNode.put("status", status);

        return returnNode;
    }
}