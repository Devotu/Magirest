package controllers;

import util.*;
import data.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.*;

import play.libs.streams.ActorFlow;
import play.mvc.*;
import play.libs.F;
import play.libs.F.*;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.stream.*;
import javax.inject.Inject;
import actors.JsonSocketActor;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

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

        return WebSocket.Json.acceptOrResult(request -> {

            String userName = request().getQueryString("username");
            String password = request().getQueryString("password");

            try {    
                Neo4jDriver db = new Neo4jDriver();

                if (UserStore.validateCredentials(userName, password, db)) {
                    Logger.debug("socket accepted");
                    return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef(JsonSocketActor::props, actorSystem, materializer)));
                } else {
                    Logger.debug("socket rejected");
                    return errorResult(Results.notFound("No such user/password found"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return errorResult(Results.internalServerError("Oops"));
            }

        });
    }

    private CompletionStage<Either<Result, Flow<JsonNode, JsonNode, ?>>> errorResult(Result error) {
        final Either<Result, Flow<JsonNode, JsonNode, ?>> left = Either.Left(error);
        return CompletableFuture.completedFuture(left);
    }
}