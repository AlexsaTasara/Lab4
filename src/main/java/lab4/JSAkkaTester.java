package lab4;
import akka.NotUsed;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import scala.concurrent.Future;
import akka.stream.javadsl.Flow;
import akka.stream.ActorMaterializer;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import java.util.concurrent.CompletionStage;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.marshallers.jackson.Jackson;

public class  JSAkkaTester extends AllDirectives {

    static ActorRef mainActor;
    private static final int SERVER_PORT = 8080;
    private static final int TIMEOUT_MILLIS = 5000;
    private static final String ROUTES = "routes";
    private static final String LOCALHOST = "localhost";
    private static final String PACKAGE_ID = "packageId";
    private static final String POST_MESSAGE = "Message was posted";
    private static final String SERVER_INFO = "Server online on localhost:8080/\n PRESS ANY KEY TO STOP";

    public static void main(String[] args) throws Exception {
        ActorSystem system = ActorSystem.create(ROUTES);
        mainActor = system.actorOf(Props.create(MainActor.class));
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        JSAkkaTester app = new JSAkkaTester();
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.jsTesterRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, SERVER_PORT),
                materializer
        );

        System.out.println(SERVER_INFO);
        System.in.read();

        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());
    }

    private Route jsTesterRoute() {
        return concat(
                get(
                        () -> parameter(PACKAGE_ID, (packageId) ->
                                {
                                    Future<Object> result = Patterns.ask(mainActor,
                                            new GetMSG(Integer.parseInt(packageId)),
                                            TIMEOUT_MILLIS);
                                    return completeOKWithFuture(result, Jackson.marshaller());
                                }
                        )
                ),
                post(
                        () -> entity(Jackson.unmarshaller(FunctionPackage.class),
                                msg -> {
                                    mainActor.tell(msg, ActorRef.noSender());
                                    return complete(POST_MESSAGE);
                                })));
    }
}