package util;

import play.libs.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;

public class Responses {
    public static ObjectNode createResponse(boolean ok, String response, Object body) {
         
        ObjectNode result = Json.newObject();
        result.put("isSuccessfull", ok);
        result.put("response", response);

        if (body instanceof String) {
            result.put("data", (String) body);
        }
        else {
            result.put("data", (JsonNode) body);
        }
 
        Logger.debug(result.toString());
        return result;
    }

    public static ObjectNode createResponse(boolean ok, String response) {
        
       ObjectNode result = Json.newObject();
       result.put("isSuccessfull", ok);
       result.put("response", response);

       Logger.debug(result.toString());
       return result;
   }
}