package jp.dev7.ourbeats.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.dev7.ourbeats.actor.NodeActor.Status;
import jp.dev7.ourbeats.message.HelloMessage;
import jp.dev7.ourbeats.message.RequestIntroduceMessage;

import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.annotation.AddressInstance;
import com.googlecode.actorom.annotation.OnMessage;
import com.googlecode.actorom.annotation.TopologyInstance;

public class MatcherActor {

    @TopologyInstance
    private Topology topology;

    @AddressInstance
    private Address address;

    private Address killer;

    private final List<NodeActor> waitingNodes = new CopyOnWriteArrayList<NodeActor>();

    public MatcherActor(Address killer) {
        this.killer = killer;
    }

    @OnMessage(type = HelloMessage.class)
    public void onHello(HelloMessage message) {
        waitingNodes.add(message.getNode());
    }

    @OnMessage(type = RequestIntroduceMessage.class)
    public void onRequestIntroduce(RequestIntroduceMessage message) {
        final List<NodeActor> removed = new ArrayList<NodeActor>();
        for (NodeActor node : waitingNodes) {
            if (node.getStatus() == Status.Disconnected) {
                removed.add(node);
            }
        }
        waitingNodes.removeAll(removed);

        if (waitingNodes.size() < 2) {
            return;
        }
        topology.spawnActor(genKey(), new RefereeActor(killer, waitingNodes.remove(0), waitingNodes.remove(0)));
    }

    private String genKey() {
        return UUID.randomUUID().toString();
    }

}
