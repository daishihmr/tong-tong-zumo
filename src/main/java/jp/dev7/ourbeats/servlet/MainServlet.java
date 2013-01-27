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

    private Topology topology = new LocalTopology("main",
            ThreadingPolicies.newOSThreadingPolicy(20));
    private MatcherActor matcher;
    private Address killerAddress;

    @Override
    public void init() throws ServletException {
        super.init();
        matcher = new MatcherActor();
        KillerActor killer = new KillerActor();
        final Address matcherAddress = topology.spawnActor("matcher", matcher);
        killerAddress = topology.spawnActor("killer", killer);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(1000);
                        on(topology).send(new RequestIntroduceMessage()).to(
                                matcherAddress);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request,
            String protocol) {
        NodeActor node = new NodeActor(matcher, killerAddress);
        synchronized (topology) {
            topology.spawnActor(UUID.randomUUID().toString(), node);
        }
        return node;
    }

}
