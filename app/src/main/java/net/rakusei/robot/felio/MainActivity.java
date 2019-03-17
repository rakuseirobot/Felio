package net.rakusei.robot.felio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
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
import net.rakusei.robot.felio.model.UserStatus;
import net.rakusei.robot.felio.task.ChannelTask;
import net.rakusei.robot.felio.task.TeamsTask;
import net.rakusei.robot.felio.task.UserTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public NavigationView navigationView;

    public List<Channel> channelList = null;

    public Handler handler = new Handler();

    public String selectingChannel = "tgxqoq8zajdt3q7c3sz9m8n55a";

    public MessageAdapter adapter;

    public List<Message> sortedList;

    public long typing_reasion = 0;

    public Map<String, UserStatus> statusMap = new HashMap<>();

    public long fail_streaming_time = 0;

    private static final int READ_REQUEST_CODE = 42;

    public List<User> users = null;

    public JSONObject uploadImage = null;

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

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(MainActivity.this.getApplicationContext(), AppDatabase.class, "felio").build();
            users = db.userDao().getAll();
            db.close();
        }).start();

        /*
        new Thread(()->{
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "online");
                new MTClient(MainActivity.this, "PUT", "/users/"+getSharedPreferences("main", Context.MODE_PRIVATE).getString("id", "")+"/status", jsonObject.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();*/

        new Thread(() -> {
            AppDatabase db = Room.databaseBuilder(MainActivity.this.getApplicationContext(), AppDatabase.class, "felio").fallbackToDestructiveMigration().build();
            List<User> users = db.userDao().getAll();
            JSONArray jsonArray = new JSONArray();
            for (User user : users) {
                jsonArray.put(user.id);
            }
            try {
                MTClient client = new MTClient(MainActivity.this, "POST", "/users/status/ids", jsonArray.toString());
                if (client.con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //Log.d("UserStatus",new JSONArray(client.getResponse()).toString(3));
                    JSONArray jsonArray1 = new JSONArray(client.getResponse());
                    for (int i = 0; jsonArray1.length() > i; i++) {
                        JSONObject jsonObject = jsonArray1.getJSONObject(i);
                        UserStatus status = new UserStatus(jsonObject.getString("user_id"), jsonObject.getString("status"), jsonObject.getBoolean("manual"), jsonObject.getLong("last_activity_at"));
                        statusMap.put(status.id, status);
                    }
                } else {
                    Log.d("UserStatus", "Got " + client.con.getResponseCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        findViewById(R.id.btn_send).setOnClickListener((v) -> {
            String message = ((TextView) findViewById(R.id.et_message)).getText().toString();
            new Thread(() -> {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("channel_id", selectingChannel);
                    jo.put("message", message);
                    if (uploadImage != null) {
                        jo.put("file_ids", new JSONArray().put(uploadImage.getString("id")));
                        uploadImage = null;
                    }
                    new MTClient(MainActivity.this, "POST", "/posts", jo.toString());
                    handler.post(() -> {
                        ImageView imageView = findViewById(R.id.send_image_preview_view);
                        int w = 100, h = 100;
                        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                        Bitmap bmp = Bitmap.createBitmap(w, h, conf);
                        imageView.setImageBitmap(bmp);
                        ((TextView) findViewById(R.id.et_message)).setText("");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        findViewById(R.id.add_image_image_view).setOnClickListener((v) -> {
            // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            // Filter to only show results that can be "opened", such as a file (as opposed to a list of contacts or timezones)
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers, it would be "*/*".
            intent.setType("image/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
        });

        openStreaming();

        ((ListView) findViewById(R.id.message_listView)).setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 0=stopped 1=down 2=up
                // Toast.makeText(MainActivity.this,"スクロール中 (scrollState="+scrollState+")",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    //Toast.makeText(MainActivity.this,"最後尾まできた",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("onActivityResult", "Uri: " + uri.toString());
                Uri finalUri = uri;
                new Thread(() -> {
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(finalUri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();
                        JSONObject jsonObject = upload(image, selectingChannel);
                        uploadImage = jsonObject;
                        Drawable drawable = Glide.with(MainActivity.this).load(image).submit(100, image.getHeight() / (image.getWidth() / 100)).get();
                        handler.post(() -> {
                            ImageView imageView = findViewById(R.id.send_image_preview_view);
                            Glide.with(MainActivity.this).load(drawable).into(imageView);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                //showImage(uri);
            }
        }
    }

    public void openStreaming() {
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
                                        Message message = new Message(postData);
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
                            AppDatabase db = Room.databaseBuilder(MainActivity.this.getApplicationContext(), AppDatabase.class, "felio").fallbackToDestructiveMigration().build();
                            User user = db.userDao().getUserById(user_id);
                            handler.post(() -> {
                                if (selectingChannel.equals(channel_id)) {
                                    ((TextView) findViewById(R.id.inputting_textview)).setText(user.getName() + " is typing...");
                                    typing_reasion = System.currentTimeMillis();
                                    handler.postDelayed(() -> {
                                        if (typing_reasion + 3 <= System.currentTimeMillis()) {
                                            ((TextView) findViewById(R.id.inputting_textview)).setText("");
                                        }
                                    }, 2000);
                                }
                            });
                            db.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void statusChanged(String user_id, String status) {
                        statusMap.put(user_id, new UserStatus(user_id, status, false, System.currentTimeMillis()));
                        try {
                            AppDatabase db = Room.databaseBuilder(MainActivity.this.getApplicationContext(), AppDatabase.class, "felio").fallbackToDestructiveMigration().build();
                            User user = db.userDao().getUserById(user_id);
                            Log.d(user.getName(), "==>" + status);
                            handler.post(() -> {
                                Menu menu = navigationView.getMenu();
                                for (int i = 0; menu.size() > i; i++) {
                                    MenuItem item = menu.getItem(i);
                                    if (item.getTitle().toString().equals(user.getName())) {
                                        Log.d("item#", item.getTitle().toString());
                                        SpannableString spanString = new SpannableString(user.getName());
                                        switch (statusMap.get(user_id).status) {
                                            case ONLINE:
                                                spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorOnline)), 0, spanString.length(), 0);
                                                break;
                                            case OFFLINE:
                                                spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorOffline)), 0, spanString.length(), 0);
                                                break;
                                            case AWAY:
                                                spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorAway)), 0, spanString.length(), 0);
                                                break;
                                            case DND:
                                                spanString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorDND)), 0, spanString.length(), 0);
                                                break;
                                        }
                                        item.setTitle(spanString);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Log.d("websocket", client.getURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
                /*if (MainActivity.this.fail_streaming_time + 1000 > System.currentTimeMillis()) {
                    handler.post(() -> {
                        Toast.makeText(MainActivity.this, "エラー起きすぎ。反省しろks", Toast.LENGTH_LONG).show();
                    });
                } else {*/
                MainActivity.this.fail_streaming_time = System.currentTimeMillis();
                openStreaming();
                //}
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
                AppDatabase db = Room.databaseBuilder(this.getApplicationContext(), AppDatabase.class, "felio").fallbackToDestructiveMigration().build();
                Iterator<String> keys = ja_1.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject postData = ja_1.getJSONObject(key);
                    Message message = new Message(postData);
                    //Log.d("message", message.toString());
                    db.messageDao().insert(message);
                }
                Channel channel = db.channelDao().getChannelById(channelid);
                channel.lastSynctime = System.currentTimeMillis();
                db.channelDao().insert(channel);
                db.close();
                List<Message> messageList = db.messageDao().getMessagesByChannel(selectingChannel);
                messageList.sort(new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return Long.compare(o1.create_at, o2.create_at);
                    }
                });
                sortedList = messageList;

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
            Log.d("onItemSelected", item.getTitle().toString());
            Channel c = null;
            boolean isUser = false;
            for (Channel channel : channelList) {
                if (channel.display_name.equals(item.getTitle())) {
                    c = channel;
                }
            }
            User selectedUser = null;
            if (c == null) {
                AppDatabase db = Room.databaseBuilder(this.getApplicationContext(), AppDatabase.class, "felio").fallbackToDestructiveMigration().build();
                List<User> users = db.userDao().getAll();
                for (User user : users) {
                    String user_name = user.nickname;
                    if (user_name.equals("")) {
                        user_name = user.first_name + user.last_name;
                    }
                    if (item.getTitle().toString().equals(user_name)) {
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
                if (!selectingChannel.equals(finalC.id)) {
                    uploadImage = null;
                    ImageView imageView = findViewById(R.id.send_image_preview_view);
                    int w = 100, h = 100;
                    Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                    Bitmap bmp = Bitmap.createBitmap(w, h, conf);
                    imageView.setImageBitmap(bmp);
                }
                if (finalIsUser) {
                    Toast.makeText(this, finalSelectedUser.getName(), Toast.LENGTH_LONG).show();
                    new Thread(() -> {
                        selectingChannel = finalC.id;
                        setMessage(finalC.id);
                    }).start();
                    getSupportActionBar().setTitle(finalSelectedUser.getName());
                } else {
                    new Thread(() -> {
                        selectingChannel = finalC.id;
                        setMessage(finalC.id);
                    }).start();
                    getSupportActionBar().setTitle("#" + finalC.display_name);
                    Toast.makeText(this, finalC.display_name, Toast.LENGTH_LONG).show();
                }
                Log.d("onItemSelected", finalC.display_name + ":" + finalC.id);
            });
        }).start();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public JSONObject upload(Bitmap bitmap, String channel_id) throws Exception {

        String crlf = "\r\n";
        String twoHyphens = "--";
        String boundary = "uygntf8dfrd7eb5ebsx";

        HttpURLConnection httpUrlConnection = null;
        URL url = new URL("https://mattermost.robot.rakusei.net/api/v4/files");
        httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
        httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        httpUrlConnection.setRequestProperty("authorization", "BEARER " + MainActivity.this.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""));

        httpUrlConnection.connect();

        DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
        request.writeBytes("--" + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"channel_id\"" + crlf +
                crlf +
                selectingChannel + crlf);
        request.writeBytes("--" + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"client_ids\"" + crlf +
                crlf +
                "uid67acffc4-5519-4de1-b728-861218914445" + crlf);
        request.writeBytes("--" + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"files\"; filename=\"image-39e60bac-2c3b-4b39-b789-0b4ae509a6d9.jpg\"; filename*=\"utf-8''image-39e60bac-2c3b-4b39-b789-0b4ae509a6d9.jpg\"" + crlf +
                "Content-Type: image/jpeg" + crlf + crlf);

        //I want to send only 8 bit black & white bitmaps
        /*byte[] pixels = new byte[bitmap.getWidth() * bitmap.getHeight()];
        for (int i = 0; i < bitmap.getWidth(); ++i) {
            for (int j = 0; j < bitmap.getHeight(); ++j) {
                //we're interested only in the MSB of the first byte,
                //since the other 3 bytes are identical for B&W images
                pixels[i + j] = (byte) ((bitmap.getPixel(i, j) & 0x80) >> 7);
            }
        }

        request.write(pixels);*/

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, bos);
        byte[] bitmapdata = bos.toByteArray();

        request.write(bitmapdata);

        request.writeBytes(crlf + twoHyphens + boundary + twoHyphens + crlf);
        request.flush();
        request.close();

        InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());

        BufferedReader responseStreamReader =
                new BufferedReader(new InputStreamReader(responseStream));

        String line = "";
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        String response = stringBuilder.toString();
        Log.d("upload#rescode", httpUrlConnection.getResponseCode() + "");
        Log.d("upload", new JSONObject(response).toString(3));
        responseStream.close();
        httpUrlConnection.disconnect();
        return new JSONObject(response).getJSONArray("file_infos").getJSONObject(0);
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(()->{
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "offline");
                new MTClient(MainActivity.this, "PUT", "/users/"+getSharedPreferences("main", Context.MODE_PRIVATE).getString("id", "")+"/status", jsonObject.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new Thread(()->{
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "online");
                new MTClient(MainActivity.this, "PUT", "/users/"+getSharedPreferences("main", Context.MODE_PRIVATE).getString("id", "")+"/status", jsonObject.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        new Thread(()->{
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", "away");
                new MTClient(MainActivity.this, "PUT", "/users/"+getSharedPreferences("main", Context.MODE_PRIVATE).getString("id", "")+"/status", jsonObject.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }*/
}
