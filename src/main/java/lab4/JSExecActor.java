package lab4;
import javafx.util.Pair;
import akka.actor.ActorRef;
import javax.script.Invocable;
import akka.actor.AbstractActor;
import javax.script.ScriptEngine;
import akka.japi.pf.ReceiveBuilder;
import javax.script.ScriptException;
import javax.script.ScriptEngineManager;


public class JSExecActor extends AbstractActor {
    private static final String JS_ENGINE = "nashorn";
    private static final String WRONG_ANSWER = "WRONG ANSWER!";
    private static final String CORRECT_ANSWER = "CORRECT ANSWER!";

    @Override
    public Receive createReceive() {

        return ReceiveBuilder.create().match(ExecuteMSG.class, m -> {
            Pair<Integer, FunctionPackage> msg = m.getMsg();
            int index = msg.getKey();
            FunctionPackage functionPackage = msg.getValue();
            Test test = functionPackage.getTests()[index];
            ScriptEngine engine = new ScriptEngineManager().getEngineByName(JS_ENGINE);
            try{
                engine.eval(functionPackage.getJSScript());
            } catch (ScriptException e){
                e.printStackTrace();
            }
            Invocable invocable = (Invocable) engine;
            String res = invocable.invokeFunction(functionPackage.getFunctionName(), test.getParams()).toString();

            String check = WRONG_ANSWER;
            if(res.equals(test.getExpectedResult())){
                check = CORRECT_ANSWER;
            }

            StorageMSG storageMSG = new StorageMSG(
                    res,
                    test.getExpectedResult(),
                    check,
                    test.getParams(),
                    test.getTestName()
            );
            StorageCommand storageCommand = new StorageCommand(functionPackage.getPackageId(), storageMSG);
            getSender().tell(storageCommand, ActorRef.noSender());
        }).build();
    }
}