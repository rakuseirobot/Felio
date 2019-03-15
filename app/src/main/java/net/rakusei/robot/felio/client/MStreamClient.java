package net.rakusei.robot.felio.client;

import android.content.Context;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MStreamClient extends WebSocketClient {

    public String token = "";

    private List<MStreamListener> listeners = new ArrayList<MStreamListener>();

    public void addListener(MStreamListener toAdd) {
        listeners.add(toAdd);
    }

    public MStreamClient(Context con) throws URISyntaxException {
        super(new URI("wss://mattermost.robot.rakusei.net:443/api/v4/websocket"));
        token = con.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", "");
        this.setConnectionLostTimeout(999999);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d("WebSocket", "opened connection");
        try {
            this.send(
                    new JSONObject()
                            .put("seq", 1)
                            .put("action", "authentication_challenge")
                            .put("data", new JSONObject().put("token", token)).toString());
        } catch (JSONException e) {

        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
        try {

            if (new JSONObject(message).has("event")) {

                switch (new JSONObject(message).getString("event")) {
                    // status_change
                    case "hello":
                        Log.d("WebSocket", "Authed!");
                        break;
                    case "posted":
                        for (MStreamListener hl : listeners)
                            hl.posted(new JSONObject(message).getJSONObject("data"));
                        break;
                    case "typing":
                        JSONObject jo = new JSONObject(message);
                        for (MStreamListener hl : listeners)
                            hl.onTyping(jo.getJSONObject("data").getString("user_id"), jo.getJSONObject("broadcast").getString("channel_id"), jo.getInt("seq"));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("WebSocket", "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    // An interface to be implemented by everyone interested in "Hello" events
    public interface MStreamListener {
        void posted(JSONObject jo);

        void onTyping(String user_id, String channel_id, int seq);
    }
}
