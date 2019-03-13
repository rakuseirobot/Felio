package net.rakusei.robot.felio;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.rakusei.robot.felio.activity.BaseActivity;
import net.rakusei.robot.felio.activity.LoginActivity;
import net.rakusei.robot.felio.adapter.MessageAdapter;
import net.rakusei.robot.felio.client.MStreamClient;
import net.rakusei.robot.felio.client.MTClient;
import net.rakusei.robot.felio.database.AppDatabase;
import net.rakusei.robot.felio.model.Channel;
import net.rakusei.robot.felio.model.Message;
import net.rakusei.robot.felio.model.User;
import net.rakusei.robot.felio.task.ChannelTask;
import net.rakusei.robot.felio.task.SyncAllMessagesTask;
import net.rakusei.robot.felio.task.TeamsTask;
import net.rakusei.robot.felio.task.UserTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public NavigationView navigationView;

    public List<Channel> channelList = null;

    public Handler handler = new Handler();

    public String selectingChannel = "tgxqoq8zajdt3q7c3sz9m8n55a";

    public MessageAdapter adapter;

    public List<Message> sortedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!getSharedPreferences("main", MODE_PRIVATE).contains("token")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        new TeamsTask(this).execute();
        new ChannelTask(this).execute();
        new UserTask(this).execute();
        //new SyncAllMessagesTask(this).execute();
        new Thread(() -> {
            setMessage("tgxqoq8zajdt3q7c3sz9m8n55a");
        }).start();

        findViewById(R.id.btn_send).setOnClickListener((v) -> {
            String message = ((TextView) findViewById(R.id.et_message)).getText().toString();
            new Thread(() -> {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("channel_id", selectingChannel);
                    jo.put("message", message);
                    new MTClient(MainActivity.this, "POST", "/posts", jo.toString());
                    handler.post(() -> {
                        ((TextView) findViewById(R.id.et_message)).setText("");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        new Thread(() -> {
            try {
                MStreamClient client = new MStreamClient(MainActivity.this);
                client.connect();
                client.addListener(new MStreamClient.MStreamListener() {
                    @Override
                    public void posted(JSONObject jo) {
                        try {
                            if (new JSONObject(jo.getString("post")).getString("channel_id").equals(selectingChannel)) {
                                handler.post(() -> {
                                    try {
                                        final ListView listview = findViewById(R.id.message_listView);
                                        JSONObject postData = new JSONObject(jo.getString("post"));
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
                                        sortedList.add(message);
                                        adapter.notifyDataSetChanged();
                                        listview.setSelection(adapter.getCount() - 1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        } catch (JSONException e) {

                        }
                    }

                    @Override
                    public void onTyping(String user_id, String channel_id, int seq) {
                        try {
                            AppDatabase db = Room.databaseBuilder(MainActivity.this.getApplicationContext(), AppDatabase.class, "felio").build();
                            User user = db.userDao().getUserById(user_id);
                            handler.post(() -> {
                                ((TextView) findViewById(R.id.inputting_textview)).setText(user.getName() + " is typing...");
                            });
                            db.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Log.d("websocket", client.getURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setMessage(String channelid) {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4/channels/" + channelid + "/posts?page=0&per_page=50");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("authorization", "BEARER " + MainActivity.this.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""));
            con.connect();
            Log.d("MessagesTask#Res", con.getResponseCode() + "");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                con.disconnect();
                JSONObject jo = new JSONObject(data);
                JSONObject ja_1 = jo.getJSONObject("posts");
                AppDatabase db = Room.databaseBuilder(this.getApplicationContext(), AppDatabase.class, "felio").build();
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
                    //Log.d("message", message.toString());
                    db.messageDao().insert(message);
                }
                db.close();
                List<Message> messageList = db.messageDao().getMessagesByChannel(selectingChannel);
                sortedList = messageList.stream().sorted(
                        (o1, o2) -> Long.compare(o1.create_at, o2.create_at)).collect(Collectors.toList());

                handler.post(() -> {
                    final ListView listview = findViewById(R.id.message_listView);
                    adapter = new MessageAdapter(MainActivity.this);
                    adapter.setMessageList(sortedList);
                    listview.setAdapter(adapter);
                    listview.setSelection(adapter.getCount() - 1);
                });
                //Log.d("data",ja.toString(3));
            } else {
                con.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        Log.d("hi", "onShowKeyboard");
        ListView listview = findViewById(R.id.message_listView);
        listview.setSelection(adapter.getCount() - 1);
    }

    @Override
    protected void onHideKeyboard() {
        ListView listview = findViewById(R.id.message_listView);
        listview.setSelection(adapter.getCount() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (channelList == null) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
            return false;
        }
        new Thread(() -> {
            Channel c = null;
            boolean isUser = false;
            for (Channel channel : channelList) {
                if (channel.display_name.equals(item.getTitle())) {
                    c = channel;
                }
            }
            User selectedUser = null;
            if (c == null) {
                AppDatabase db = Room.databaseBuilder(this.getApplicationContext(), AppDatabase.class, "felio").build();
                List<User> users = db.userDao().getAll();
                for (User user : users) {
                    String user_name = user.nickname;
                    if (user_name.equals("")) {
                        user_name = user.first_name + user.last_name;
                    }
                    if (item.getTitle().equals(user_name)) {
                        for (Channel channel : channelList) {
                            if (channel.display_name.contains(user.id)) {
                                c = channel;
                                isUser = true;
                                selectedUser = user;
                            }
                        }
                    }
                }
                db.close();
            }
            User finalSelectedUser = selectedUser;
            Channel finalC = c;
            boolean finalIsUser = isUser;
            handler.post(() -> {
                if (finalC == null) {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
                }
                if (finalIsUser) {
                    Toast.makeText(this, finalSelectedUser.getName(), Toast.LENGTH_LONG).show();
                    new Thread(() -> {
                        selectingChannel = finalC.id;
                        setMessage(finalC.id);
                    }).start();
                } else {
                    new Thread(() -> {
                        selectingChannel = finalC.id;
                        setMessage(finalC.id);
                    }).start();
                    Toast.makeText(this, finalC.display_name, Toast.LENGTH_LONG).show();
                }
                Log.d("onItemSelected", finalC.display_name + ":" + finalC.id);
            });
        }).start();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
