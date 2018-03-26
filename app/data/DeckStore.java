package data;

import model.*;
import util.Neo4jDriver;

import java.util.HashMap;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.exceptions.*;

import static org.neo4j.driver.v1.Values.parameters;

import play.Logger;

public class DeckStore {

    //Basics
	private static int persist(Deck deck, Neo4jDriver db) throws Exception {

        deck.id = Neo4jDriver.getUniqueId(db);

        String query = 
        " CREATE " + 
        "   (d:Deck {id:$id, name: $name, format: $format, black: $black, white: $white, red: $red, green: $green, blue: $blue ,colorless: $colorless, theme: $theme, created: TIMESTAMP(), active:true} ) " +
        " RETURN d";

        HashMap<String, Object> params = new HashMap<>();

        Logger.debug(deck.id + ", " + deck.name + ", " + deck.format + ", " + deck.blackCards + ", " + deck.theme);
        
        params.put("id", deck.id);
        params.put("name", deck.name);
        params.put("format", deck.format);
        params.put("black", deck.blackCards);
        params.put("white", deck.whiteCards);
        params.put("red", deck.redCards);
        params.put("green", deck.greenCards);
        params.put("blue", deck.blueCards);
        params.put("colorless", deck.colorlessCards);
        params.put("theme", deck.theme);

        StatementResult result = db.runQuery(query, params);

        Node d = result.single().get("d").asNode();

        Logger.debug("Created deck with id: " + d.get("id").asInt());
        
        //Return
        return d.get("id").asInt();
    }

    //Creators
    public static int addToPlayer(Deck deck, Token token, Neo4jDriver db) throws Exception { //TODO addToPlayer
        
        Logger.debug("Adding relationship between " + token.token + ", " + deck.id);

        //Deck
        int deckId = persist(deck, db);

        //Relationship
        String query = 
        " MATCH " + 
        "   (t:Token)-[:Grants]->(p:Player), (d:Deck)" + 
        " WHERE" + 
        "   t.token = $token AND" + 
        "   d.id = $deckId" + 
        " CREATE " + 
        "   (p)-[:Has]->(d) " +
        " RETURN d";

        HashMap<String, Object> params = new HashMap<>();

        
        params.put("token", token.token);
        params.put("deckId", deckId);

        StatementResult result = db.runQuery(query, params);

        Node d = result.single().get("d").asNode();

        Logger.debug("Added deck with id: " + d.get("id").asInt());  
        
        return d.get("id").asInt();
    }


}