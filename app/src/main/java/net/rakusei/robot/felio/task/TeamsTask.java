package net.rakusei.robot.felio.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.rakusei.robot.felio.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class TeamsTask extends AsyncTask<String, Void, String> {

    Context context = null;

    public TeamsTask(Context con){
        context = con;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4/users/me/teams");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            //con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("Content-Type","application/json");
            con.setRequestProperty("authorization","BEARER "+context.getSharedPreferences("main",Context.MODE_PRIVATE).getString("token",""));
            //con.setRequestProperty("User-Agent", params[2]);
            con.connect();
            if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                JSONArray ja = new JSONArray(data);
                Log.d("data",ja.toString(3));
                return ja.getJSONObject(0).toString();
            }
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject jo = new JSONObject(result);
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.getSupportActionBar().setTitle(jo.getString("display_name"));
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