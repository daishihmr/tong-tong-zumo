package jp.dev7.ourbeats.actor;

import static com.googlecode.actorom.dsl.Messaging.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.dev7.ourbeats.message.ConnectedMessage;
import jp.dev7.ourbeats.message.InitMessage;
import jp.dev7.ourbeats.message.KillMeMessage;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.eclipse.jetty.websocket.WebSocket;

import com.googlecode.actorom.Address;
import com.googlecode.actorom.Topology;
import com.googlecode.actorom.annotation.AddressInstance;
import com.googlecode.actorom.annotation.OnMessage;
import com.googlecode.actorom.annotation.TopologyInstance;

public class NodeActor implements WebSocket.OnTextMessage {
    public static enum Status {
        /** 対戦者を待っている */
        Wait,
        /** 接続中 */
        Connecting,
        /** 相手が切断した後 */
        Left,
        /** 切断済み */
        Disconnected,
    }

    @TopologyInstance
    private Topology topology;

    @AddressInstance
    private Address address;

    private Status status = Status.Wait;
    private Connection connection;

    private MatcherActor matcher;
    private RefereeActor referee;
    private NodeActor partner;
    private Address killer;

    public NodeActor(MatcherActor parent, Address killerAddress) {
        this.matcher = parent;
        this.killer = killerAddress;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public void onOpen(Connection connection) {
        this.connection = connection;
        on(topology).send(new InitMessage(address, this)).to(
                matcher.getAddress());
    }

    @Override
    public void onClose(int closeCode, String message) {
        on(topology).send(new KillMeMessage(address)).to(killer);
        if (referee != null) {
            referee.bye();
        }
        status = Status.Disconnected;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @OnMessage(type = ConnectedMessage.class)
    public void onConnected(ConnectedMessage message) throws IOException {
        this.referee = message.getReferee();
        this.partner = message.getPartner();
        this.status = Status.Connecting;
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "connected");
        map.put("youare", message.getNumber());
        connection.sendMessage(JSON.encode(map));
    }

    @Override
    public void onMessage(String data) {
        try {
            switch (status) {
            case Wait:
                sendTextMessage("waiting");
                break;
            case Connecting:
                this.referee.receive(this, data);
                break;
            case Left:
                sendTextMessage("left");
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive(String data) throws IOException {
        connection.sendMessage(data);
    }

    private void sendTextMessage(String message) throws JSONException,
            IOException {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", message);
        connection.sendMessage(JSON.encode(map));
    }

}
