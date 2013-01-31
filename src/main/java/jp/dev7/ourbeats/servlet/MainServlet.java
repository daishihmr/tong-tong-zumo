package jp.dev7.ourbeats.servlet;

import static com.googlecode.actorom.dsl.Messaging.*;

import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import jp.dev7.ourbeats.actor.KillerActor;
import jp.dev7.ourbeats.actor.MatcherActor;
import jp.dev7.ourbeats.actor.NodeActor;
import jp.dev7.ourbeats.message.RequestIntroduceMessage;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.local.LocalTopology;
import com.googlecode.actorom.support.ThreadingPolicies;

@SuppressWarnings("serial")
public class MainServlet extends WebSocketServlet {

    private Topology topology;
    private Address matcherAddress;
    private Address killerAddress;

    @Override
    public void init() throws ServletException {
        super.init();

        topology = new LocalTopology("main", ThreadingPolicies.newOSThreadingPolicy(20));
        killerAddress = topology.spawnActor("killer", new KillerActor());
        matcherAddress = topology.spawnActor("matcher", new MatcherActor(killerAddress));

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(1000);
                        on(topology).send(new RequestIntroduceMessage()).to(matcherAddress);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        final NodeActor node = new NodeActor(matcherAddress, killerAddress);
        topology.spawnActor(getKey(), node);
        return node;
    }

    private String getKey() {
        return UUID.randomUUID().toString();
    }

}
