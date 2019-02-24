package net.rakusei.robot.felio.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import net.rakusei.robot.felio.MainActivity;
import net.rakusei.robot.felio.R;
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