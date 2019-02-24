package net.rakusei.robot.felio.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class LoginTask extends AsyncTask<String, Void, String> {

    Context context = null;

    public LoginTask(Context con){
        context = con;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://mattermost.robot.rakusei.net/api/v4/users/login");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Accept","application/json");
            con.setRequestProperty("Content-Type","application/json");
            //con.setRequestProperty("User-Agent", params[2]);
            con.connect();
            JSONObject logindata =new JSONObject();
            logindata.put("login_id",params[0]);
            logindata.put("password", params[1]);
            OutputStreamWriter wr= new OutputStreamWriter(con.getOutputStream());
            wr.write(logindata.toString());
            wr.close();
            if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Map headers = con.getHeaderFields();
                Log.d("token",headers.get("token").toString().substring(1,headers.get("token").toString().length()-1));
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                String data = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    data += line;
                }
                JSONObject jo = new JSONObject(data);
                SharedPreferences pref = context.getSharedPreferences("main",Context.MODE_PRIVATE);
                pref.edit()
                        .putString("id",jo.getString("id"))
                        .putString("username",jo.getString("username"))
                        .putString("email",jo.getString("email"))
                        .putString("nickname",jo.getString("nickname"))
                        .putString("first_name",jo.getString("first_name"))
                        .putString("last_name",jo.getString("last_name"))
                        .putString("position",jo.getString("position"))
                        .putString("roles",jo.getString("roles"))
                        .putString("token",headers.get("token").toString().substring(1,headers.get("token").toString().length()-1))
                        .apply();
                Log.d("data",jo.toString(3));
            }
            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
        // might want to change "executed" for the returned string passed
        // into onPostExecute() but that is upto you
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}
