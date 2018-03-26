package api.v1;

import util.Responses;
import util.Encryption;
import util.Constants;
import util.Neo4jDriver;
import model.Player;

import play.mvc.*;
import play.Logger;

import java.util.List;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.neo4j.driver.v1.StatementResult;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.HttpStatus;


public class PlayerController extends Controller {

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