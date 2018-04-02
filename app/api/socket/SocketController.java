package controllers;

import java.util.concurrent.CompletableFuture;

import play.libs.streams.ActorFlow;
import play.mvc.*;
import play.libs.F;
import akka.actor.*;
import akka.stream.*;
import javax.inject.Inject;

import actors.JsonSocketActor;

import play.Logger;

public class SocketController extends Controller {

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public SocketController(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket jsonSocket() {
        return WebSocket.Json.accept(request ->
                ActorFlow.actorRef(JsonSocketActor::props, actorSystem, materializer)
        );
    }
}