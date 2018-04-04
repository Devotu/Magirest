package actors;

import akka.actor.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import play.Logger;

public class JsonSocketActor extends AbstractActor {

    public enum Action {
        create, read, update, delete, close, nil;
    }

    public static Props props(ActorRef out) {
        return Props.create(JsonSocketActor.class, out);
    }

    private final ActorRef out;

    public JsonSocketActor(ActorRef out) {
        this.out = out;
    }

    @Override
    public Receive createReceive() {

        Logger.debug("Got json socket");

        return receiveBuilder()
            .match(JsonNode.class, json -> {

                Action action = Action.nil;
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode returnNode = mapper.createObjectNode();

                try {
                    Logger.debug(json.get("action").asText());
                    action = Action.valueOf(json.get("action").asText());
                    Logger.debug(action.toString());
                    boolean isRead = action == Action.read;
                    Logger.debug("Action is read: " + isRead);

                    if(action == Action.close){
                        Logger.debug("closing socket");
                        self().tell(PoisonPill.getInstance(), self());
                    }

                    if (action == Action.read) {
                        String message = json.get("message").asText();
                        Logger.debug("Got message: " + message);
                        returnNode.put("message", message);
                    }

                } catch (Exception e) {
                    Logger.debug("No action specified");
                    returnNode.put("error", "No action specified");
                }

                out.tell(returnNode, self());
            })
            .matchAny(o -> Logger.debug("received invalid json"))
            .build();
    }
}