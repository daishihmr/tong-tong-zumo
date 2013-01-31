package jp.dev7.ourbeats.actor;

import static com.googlecode.actorom.dsl.Messaging.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.dev7.ourbeats.actor.NodeActor.Status;
import jp.dev7.ourbeats.message.ConnectedMessage;
import jp.dev7.ourbeats.message.KillMeMessage;
import net.arnx.jsonic.JSON;

import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.annotation.AddressInstance;
import com.googlecode.actorom.annotation.OnSpawn;
import com.googlecode.actorom.annotation.TopologyInstance;

public class RefereeActor {

    @TopologyInstance
    private Topology topology;

    @AddressInstance
    private Address address;

    private Address killer;

    private final NodeActor node0;
    private final NodeActor node1;

    private String receiveData0;
    private String receiveData1;

    public RefereeActor(Address killer, NodeActor node0, NodeActor node1) {
        this.killer = killer;
        this.node0 = node0;
        this.node1 = node1;
    }

    @OnSpawn
    public void onSpawn() {
        on(topology).send(new ConnectedMessage(address, this, 0)).to(node0.getAddress());
        on(topology).send(new ConnectedMessage(address, this, 1)).to(node1.getAddress());
    }

    synchronized public void receive(NodeActor from, String data) throws IOException {
        if (from == node0 && receiveData0 == null) {
            receiveData0 = data;
        } else if (from == node1 && receiveData1 == null) {
            receiveData1 = data;
        }

        if (receiveData0 != null && receiveData1 != null) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("inputs", new Object[] { JSON.decode(receiveData0), JSON.decode(receiveData1) });

            final String result = JSON.encode(map);
            node0.receive(result);
            node1.receive(result);

            receiveData0 = null;
            receiveData1 = null;
        }
    }

    public void bye() {
        node0.setStatus(Status.Left);
        node1.setStatus(Status.Left);
        on(topology).send(new KillMeMessage(address)).to(killer);
    }

}
