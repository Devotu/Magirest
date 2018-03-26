package api.v1;

import util.Responses;
import util.Neo4jDriver;
import model.*;
import data.DeckStore;
import data.TokenStore;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.exceptions.*;


import static org.neo4j.driver.v1.Values.parameters;

import play.mvc.*;
import play.libs.Json;

import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.naming.directory.InvalidAttributesException;

import java.io.*;
import java.net.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.HttpStatus;

import play.Logger;

public class DeckController extends Controller {

    public Result createDeck() {

        String status = String.valueOf(HttpStatus.SC_METHOD_FAILURE);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode returnNode = mapper.createObjectNode();

        try {

            JsonNode json = request().body().asJson();
            Token token = mapper.readValue(json.get("token").toString(), Token.class);
            Logger.debug(token.token);
            Deck deck = mapper.readValue(json.get("deck").toString(), Deck.class);
            Logger.debug(deck.name);

            Neo4jDriver db = new Neo4jDriver();

            //Verify token
            if(TokenController.verify(token, db)){

                //Persist
                int deckId = DeckStore.addToPlayer(deck, token, db);
                returnNode.put("id", deckId);
                status = String.valueOf(HttpStatus.SC_OK);
            }
            else {
                status = String.valueOf(HttpStatus.SC_UNAUTHORIZED);
            }

        } catch (InvalidAttributesException e) {
            status = String.valueOf(HttpStatus.SC_BAD_REQUEST);
            e.printStackTrace();
        } catch (Exception e) {
            status = String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        return created(Responses.createResponse(true, status, returnNode));
    }

    // //TODO get decks belonging to user x
    // public Result getDecks(){

    //     StringBuilder result = new StringBuilder();
    //     ObjectMapper mapper = new ObjectMapper();
    //     ObjectNode returnNode = mapper.createObjectNode();

    //     Neo4j neo4j = new Neo4j();

    //     try {
    //         String query = "{\"statements\" : [ { \"statement\" : \"MATCH (n:Deck) RETURN n\" } ] }";
    //         QueryResult queryResult = neo4j.query(query);

    //         result.append(queryResult.getStatus());

    //         //Find query content data
    //         List<JsonNode> rows = queryResult.getData().findValues("row");

    //         //Convert to api objects
    //         List<Deck> decks = rows.stream().map(d -> new Deck((ObjectNode) d.get(0))).collect(Collectors.toList());

    //         //Convert to api json
    //         JsonNode decksNode = mapper.readTree( mapper.writeValueAsString( decks ) );

    //         returnNode.put("decks", decksNode);

    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }

    //     return created(Responses.createResponse(true, result.toString(), returnNode));
    // }

    // public Result getDeck(int id){

    //     StringBuilder result = new StringBuilder();

    //     try {
    //         CredentialsProvider provider = new BasicCredentialsProvider();
    //         UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("neo4j", "neoe4j");
    //         provider.setCredentials(AuthScope.ANY, credentials);

    //         CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();

    //         try {
    //             HttpGet httpget = new HttpGet("Use query" + "/" + id);

    //             Logger.debug("Executing request " + httpget.getRequestLine());
    //             CloseableHttpResponse response = httpclient.execute(httpget);
    //             try {
    //                 result.append( response.getStatusLine() );
    //                 result.append( EntityUtils.toString(response.getEntity()) );
    //             } finally {
    //                 response.close();
    //             }
    //         } finally {
    //             httpclient.close();
    //         }

    //     } catch (Exception e) {
    //         //TODO: handle exception
    //     }

    //     return created(Responses.createResponse( true, result.toString() ));
    // }

}