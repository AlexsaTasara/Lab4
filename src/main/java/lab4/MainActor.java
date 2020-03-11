package lab4;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;

public class MainActor extends AbstractActor {
    private final static int NUM_ROUND_ROBIN_POOL = 5;
    private final ActorRef storage;
    private final ActorRef executors;

    public MainActor() {
        executors = getContext().actorOf(new RoundRobinPool(NUM_ROUND_ROBIN_POOL).props(Props.create(JSExecActor.class)));
        storage = getContext().actorOf(Props.create(StorageActor.class));
    }

    @Override
    public Receive createReceive() {

        return ReceiveBuilder.create().match(
                FunctionPackage.class, pack -> {
                    int len = pack.getTests().length;
                    for (int idx = 0; idx < len; idx++) {
                        executors.tell(new ExecuteMSG(idx, pack), storage);
                    }
                })
                .match(GetMSG.class, req -> storage.tell(req, sender())).build();
    }
}