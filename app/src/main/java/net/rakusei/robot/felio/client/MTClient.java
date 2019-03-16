package net.rakusei.robot.felio.client;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MTClient {

    public HttpURLConnection con = null;
    //JSONObject json = new JSONObject();

    public String json = "";

    public MTClient(Context context, String method, String path) throws Exception {
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4" + path);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("authorization", "BEARER " + context.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""));
            con.connect();
            Log.d("MTask#Res", con.getResponseCode() + "");
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            String data = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                data += line;
            }
            json = data;
            Log.d("MTClient", json);
            con.disconnect();
        } catch (Exception e) {
            throw e;
        }
    }

    public MTClient(Context context, String method, String path, String output) throws Exception {
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4" + path);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("authorization", "BEARER " + context.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""));
            con.connect();
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(output);
            wr.close();
            Log.d("MTask#Res", con.getResponseCode() + "");
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            String data = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                data += line;
            }
            json = data;
            con.disconnect();
        } catch (Exception e) {
            throw e;
        }
    }

    public String getResponse() {
        return json;
    }
}
