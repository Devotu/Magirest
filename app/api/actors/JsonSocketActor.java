package actors;

import akka.actor.*;

import com.fasterxml.jackson.databind.*;

import play.Logger;

public class JsonSocketActor extends AbstractActor {

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
                Logger.debug("Handling json: " + json.get("test").asText());
                out.tell(json.get("test"), self());
            })
            .matchAny(o -> Logger.debug("received invalid json"))
            .build();
    }
}