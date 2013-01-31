package jp.dev7.ourbeats.actor;

import static com.googlecode.actorom.dsl.Messaging.*;
import jp.dev7.ourbeats.message.KillMeMessage;

import com.googlecode.actorom.ExitActorMessage;
import com.googlecode.actorom.ExitReason;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.annotation.OnMessage;
import com.googlecode.actorom.annotation.TopologyInstance;

public class KillerActor {

    @TopologyInstance
    private Topology topology;

    @OnMessage(type = KillMeMessage.class)
    public void onKillMe(KillMeMessage message) {
        on(topology).send(new ExitActorMessage(message.getFrom(), ExitReason.KILL)).to(message.getFrom());
    }

}
