package net.rakusei.robot.felio.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import net.rakusei.robot.felio.MainActivity;
import net.rakusei.robot.felio.R;
import net.rakusei.robot.felio.database.AppDatabase;
import net.rakusei.robot.felio.model.Channel;
import net.rakusei.robot.felio.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserTask extends AsyncTask<String, Void, String> {

    Context context = null;

    public UserTask(Context con){
        context = con;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection con = null;
        try {
            URL url;
            if(params.length==0){
                url = new URL("https://mattermost.robot.rakusei.net/api/v4/users/me");
            }else{
                url = new URL("https://mattermost.robot.rakusei.net/api/v4/users/"+params[0]);
            }
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("Content-Type","application/json");
            con.setRequestProperty("authorization","BEARER "+context.getSharedPreferences("main",Context.MODE_PRIVATE).getString("token",""));
            //con.setRequestProperty("User-Agent", params[2]);
            con.connect();
            Log.d("UserTask#ResCode",con.getResponseCode()+"");
            if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                JSONObject ja = new JSONObject(data);
                AppDatabase db = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "felio").build();
                User user = new User();
                user.username = ja.getString("username");
                user.auth_service = ja.getString("auth_service");
                user.create_at = ja.getLong("create_at");
                user.delete_at = ja.getLong("delete_at");
                user.update_at = ja.getLong("update_at");
                user.email = ja.getString("email");
                if (!ja.has("email_verified")) {
                    ja.put("email_verified", true);
                }
                user.email_verified = ja.getBoolean("email_verified");
                if (!ja.has("failed_attempts")) {
                    ja.put("failed_attempts", 0);
                }
                user.failed_attempts = ja.getInt("failed_attempts");
                user.first_name = ja.getString("first_name");
                user.id = ja.getString("id");
                user.last_name = ja.getString("last_name");
                if (!ja.has("last_password_update")) {
                    ja.put("last_password_update", 0);
                }
                user.last_password_update = ja.getLong("last_password_update");
                if (!ja.has("last_picture_update")) {
                    ja.put("last_picture_update", 0);
                }
                user.last_picture_update = ja.getLong("last_picture_update");
                user.locale = ja.getString("locale");
                if (!ja.has("mfa_active")) {
                    ja.put("mfa_active", true);
                }
                user.mfa_active = ja.getBoolean("mfa_active");
                user.nickname = ja.getString("nickname");
                if (!ja.has("notify_props")) {
                    ja.put("notify_props", new JSONObject());
                }
                user.notify_props = ja.getJSONObject("notify_props").toString();
                if (!ja.has("props")) {
                    ja.put("props", new JSONObject());
                }
                user.props = ja.getJSONObject("props").toString();
                user.roles = ja.getString("roles");
                user.timezone = ja.getJSONObject("timezone").toString();
                db.userDao().insert(user);
                db.close();
                //Log.d("data",ja.toString(3));
                con.disconnect();
                return ja.toString();
            }else{
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
            JSONObject jo = new JSONObject(result);
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                if(context.getSharedPreferences("main",Context.MODE_PRIVATE).getString("id","").equals(jo.getString("id"))){
                    ((TextView)activity.findViewById(R.id.display_name)).setText(jo.getString("nickname"));
                    ((TextView)activity.findViewById(R.id.display_email)).setText(jo.getString("email"));
                    GlideUrl glideUrl = new GlideUrl("https://mattermost.robot.rakusei.net/api/v4/users/"+jo.getString("id")+"/image?_="+jo.getLong("last_picture_update"), new LazyHeaders.Builder()
                            .addHeader("authorization","BEARER "+context.getSharedPreferences("main",Context.MODE_PRIVATE).getString("token",""))
                            .build());
                    Glide.with(context).load(glideUrl).into((ImageView) activity.findViewById(R.id.imageView));
                }else{
                    if(jo.getString("nickname").equals("")){
                        activity.navigationView.getMenu().add(R.id.drawer_direct, 100, 0,jo.getString("first_name")+jo.getString("last_name")).setIcon(R.drawable.outline_person_outline_black_48);
                    }else {
                        activity.navigationView.getMenu().add(R.id.drawer_direct, 100, 0,jo.getString("nickname")).setIcon(R.drawable.outline_person_outline_black_48);
                    }
                }
            }
        }catch (JSONException e){
            Toast.makeText(context,result,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}