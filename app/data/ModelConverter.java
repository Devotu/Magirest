package data;

import model.*;

import org.neo4j.driver.v1.types.Node;

/**
 * ModelConverter
 * Util class responsible for converting from Neo4j results to App Model
 */
public class ModelConverter {

    public static User toUser(Node n){

        return new User( 
            n.get("id").asInt(), 
            n.get("name").asString(), 
            n.get("password").asString() 
        );
    }
}