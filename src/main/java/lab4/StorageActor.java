package lab4;
import java.util.*;
import akka.actor.ActorRef;
import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

public class StorageActor extends AbstractActor {
    private HashMap<Integer, ArrayList<StorageMSG>> data = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(
                        GetMSG.class,
                        req ->
                                getSender().tell(
                                        data.get(req.getPackageId()).toArray(),
                                        ActorRef.noSender()
                                )
                )

                .match(StorageCommand.class, msg -> {
                            if (data.containsKey(msg.getPackageID())) {
                                ArrayList<StorageMSG> tests = data.get(msg.getPackageID());
                                tests.add(msg.getStorageMSG());
                                data.put(msg.getPackageID(), tests);
                            } else {
                                ArrayList<StorageMSG> tests = new ArrayList<>();
                                tests.add(msg.getStorageMSG());
                                data.put(msg.getPackageID(), tests);
                            }
                        }
                ).build();
    }
}