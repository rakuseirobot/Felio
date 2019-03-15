package net.rakusei.robot.felio.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import net.rakusei.robot.felio.MainActivity;
import net.rakusei.robot.felio.R;
import net.rakusei.robot.felio.database.AppDatabase;
import net.rakusei.robot.felio.model.Channel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChannelTask extends AsyncTask<String, Void, String> {

    Context context = null;

    public ChannelTask(Context con) {
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
            Log.d("ChannelTask#ResCode", con.getResponseCode() + "");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                JSONArray ja = new JSONArray(data);
                //Log.d("data",ja.toString(3));
                con.disconnect();
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

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONArray jo = new JSONArray(result);
            if (context instanceof MainActivity) {
                List<Channel> channels = new ArrayList<>();
                List<Channel> public_channels = new ArrayList<>();
                List<Channel> private_channels = new ArrayList<>();
                List<Channel> direct_channels = new ArrayList<>();
                for (int i = 0; jo.length() > i; i++) {
                    JSONObject channel_data = jo.getJSONObject(i);
                    if (!channel_data.has("type")) {
                        channel_data.put("type", "P");
                    }
                    if(channel_data.getString("display_name").equals("")){
                        channel_data.put("display_name",channel_data.getString("name"));
                    }

                    channels.add(new Channel(
                            channel_data.getInt("create_at"), channel_data.getString("creator_id"),
                            channel_data.getInt("delete_at"), channel_data.getString("display_name"),
                            channel_data.getString("header"), channel_data.getString("id"),
                            channel_data.getInt("last_post_at"), channel_data.getString("purpose"),
                            channel_data.getString("team_id"), channel_data.getInt("total_msg_count"),
                            channel_data.getString("type"), channel_data.getInt("update_at")));
                    Log.d(channels.get(channels.size() - 1).display_name, channels.get(channels.size() - 1).id);
                    if (channel_data.getString("type").equals("P")) {
                        private_channels.add(new Channel(
                                channel_data.getInt("create_at"), channel_data.getString("creator_id"),
                                channel_data.getInt("delete_at"), channel_data.getString("display_name"),
                                channel_data.getString("header"), channel_data.getString("id"),
                                channel_data.getInt("last_post_at"), channel_data.getString("purpose"),
                                channel_data.getString("team_id"), channel_data.getInt("total_msg_count"),
                                channel_data.getString("type"), channel_data.getInt("update_at")));
                    } else if (channel_data.getString("type").equals("O")) {
                        public_channels.add(new Channel(
                                channel_data.getInt("create_at"), channel_data.getString("creator_id"),
                                channel_data.getInt("delete_at"), channel_data.getString("display_name"),
                                channel_data.getString("header"), channel_data.getString("id"),
                                channel_data.getInt("last_post_at"), channel_data.getString("purpose"),
                                channel_data.getString("team_id"), channel_data.getInt("total_msg_count"),
                                channel_data.getString("type"), channel_data.getInt("update_at")));
                    } else if (channel_data.getString("type").equals("D")) {
                        direct_channels.add(new Channel(
                                channel_data.getInt("create_at"), channel_data.getString("creator_id"),
                                channel_data.getInt("delete_at"), channel_data.getString("display_name"),
                                channel_data.getString("header"), channel_data.getString("id"),
                                channel_data.getInt("last_post_at"), channel_data.getString("purpose"),
                                channel_data.getString("team_id"), channel_data.getInt("total_msg_count"),
                                channel_data.getString("type"), channel_data.getInt("update_at")));
                    }
                }

                new Thread(() -> {
                    AppDatabase db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "felio").fallbackToDestructiveMigration().build();
                    for (Channel channel : channels) {
                        db.channelDao().insert(channel);
                    }
                    db.close();
                }).start();

                MainActivity activity = (MainActivity) context;

                for (int i = 0; public_channels.size() > i; i++) {
                    activity.navigationView.getMenu().add(R.id.drawer_public, i, 0, public_channels.get(i).display_name).setIcon(R.drawable.outline_chat_bubble_outline_black_48);
                }
                for (int i = 0; private_channels.size() > i; i++) {
                    activity.navigationView.getMenu().add(R.id.drawer_private, public_channels.size() + 1 + i, 0, private_channels.get(i).display_name).setIcon(R.drawable.outline_chat_bubble_outline_black_48);
                }

                activity.channelList = channels;

                for (int i = 0; direct_channels.size() > i; i++) {
                    String[] channel_ids = direct_channels.get(i).display_name.split("__");
                    String target_id = "";
                    if(channel_ids[0].equals(context.getSharedPreferences("main",Context.MODE_PRIVATE).getString("id",""))){
                        target_id = channel_ids[1];
                    }else{
                        target_id = channel_ids[0];
                    }
                    new UserTask(context).execute(target_id);
                }


            }
        } catch (JSONException e) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}