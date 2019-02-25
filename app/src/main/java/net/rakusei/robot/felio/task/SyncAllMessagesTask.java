package net.rakusei.robot.felio.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Room;

import net.rakusei.robot.felio.database.AppDatabase;
import net.rakusei.robot.felio.model.Channel;
import net.rakusei.robot.felio.model.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SyncAllMessagesTask extends AsyncTask<String, Void, String> {

    Context context = null;

    public SyncAllMessagesTask(Context con) {
        context = con;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4/users/me/teams/f5hjiemfabb49kak19t6o1sc3e/channels");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("authorization", "BEARER " + context.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""));
            //con.setRequestProperty("User-Agent", params[2]);
            con.connect();
            Log.d("SyncAllMessagesTask#Res", con.getResponseCode() + "");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                JSONArray ja = new JSONArray(data);
                con.disconnect();
                List<Channel> channels = new ArrayList<>();
                for (int i = 0; ja.length() > i; i++) {
                    JSONObject channel_data = ja.getJSONObject(i);
                    Log.d("channel", channel_data.toString(3));
                    if (!channel_data.has("type")) {
                        channel_data.put("type", "P");
                    }
                    if (channel_data.getString("display_name").equals("")) {
                        channel_data.put("display_name", channel_data.getString("name"));
                    }
                    channels.add(new Channel(
                            channel_data.getInt("create_at"), channel_data.getString("creator_id"),
                            channel_data.getInt("delete_at"), channel_data.getString("display_name"),
                            channel_data.getString("header"), channel_data.getString("id"),
                            channel_data.getInt("last_post_at"), channel_data.getString("purpose"),
                            channel_data.getString("team_id"), channel_data.getInt("total_msg_count"),
                            channel_data.getString("type"), channel_data.getInt("update_at")));
                    ;
                }

                for (int i = 0; channels.size() > i; i++) {
                    Channel channel = channels.get(i);
                    int total_msg_count = channel.total_msg_count;
                    int try_count = ((total_msg_count - (total_msg_count % 200)) / 200) + 1;
                    for (int j = 0; try_count > j; j++) {
                        String res = getMessageHistoryForChannel(channel.id, j);
                        if (res.equals("")) {
                            // ERROR
                            Log.d("inFor", "ERROR");
                        } else {
                            JSONObject jo = new JSONObject(res);
                            JSONObject ja_1 = jo.getJSONObject("posts");
                            AppDatabase db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "messages").build();
                            Iterator<String> keys = ja_1.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject postData = ja_1.getJSONObject(key);
                                if (!postData.has("file_ids")) {
                                    postData.put("file_ids", new JSONArray());
                                }
                                if (!postData.has("filenames")) {
                                    postData.put("filenames", new JSONArray());
                                }
                                if (!postData.has("hashtag")) {
                                    postData.put("hashtag", "");
                                }
                                if (!postData.has("metadata")) {
                                    postData.put("metadata", new JSONObject());
                                }
                                Message message = new Message();
                                message.channel_id = postData.getString("channel_id");
                                message.create_at = postData.getLong("create_at");
                                message.delete_at = postData.getLong("delete_at");
                                message.edit_at = postData.getLong("edit_at");
                                message.update_at = postData.getLong("update_at");
                                message.file_ids = postData.getJSONArray("file_ids").toString();
                                message.filenames = postData.getJSONArray("filenames").toString();
                                message.hashtag = postData.getString("hashtag");
                                message.id = postData.getString("id");
                                message.message = postData.getString("message");
                                message.metadata = postData.getJSONObject("metadata").toString();
                                message.original_id = postData.getString("original_id");
                                message.parent_id = postData.getString("parent_id");
                                message.pending_post_id = postData.getString("pending_post_id");
                                message.props = postData.getJSONObject("props").toString();
                                message.root_id = postData.getString("root_id");
                                message.type = postData.getString("type");
                                message.user_id = postData.getString("user_id");
                                Log.d("message", message.toString());
                                db.messageDao().insert(message);
                            }
                            db.close();
                        }
                        Thread.sleep(1000);
                    }
                }
                //Log.d("data",ja.toString(3));
                return ja.toString();
            } else {
                con.disconnect();
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

    public String getMessageHistoryForChannel(String channel_id, int page) {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4/channels/" + channel_id + "/posts?page=" + page + "&per_page=200");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("authorization", "BEARER " + context.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""));
            //con.setRequestProperty("User-Agent", params[2]);
            con.connect();
            Log.d("getMHisForC#Res", con.getResponseCode() + "");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                JSONObject ja = new JSONObject(data);
                con.disconnect();
                return ja.toString();
            } else {
                con.disconnect();
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}