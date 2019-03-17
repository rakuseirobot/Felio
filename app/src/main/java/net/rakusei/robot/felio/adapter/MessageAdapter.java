package net.rakusei.robot.felio.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import net.rakusei.robot.felio.MainActivity;
import net.rakusei.robot.felio.R;
import net.rakusei.robot.felio.activity.ImageDetailActivity;
import net.rakusei.robot.felio.database.AppDatabase;
import net.rakusei.robot.felio.model.Message;
import net.rakusei.robot.felio.model.User;
import net.rakusei.robot.felio.task.UserTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater = null;
    List<Message> messageList;

    public MessageAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public void addMessage(Message message) {
        this.messageList.add(message);
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messageList.get(position).create_at;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.d("getView", position + "");
        //if (convertView == null) {
        convertView = layoutInflater.inflate(R.layout.item_message, parent, false);
        User user = null;
        for (User user1 : ((MainActivity) context).users) {
            if (messageList.get(position).user_id.equals(user1.id)) {
                user = user1;
                break;
            }
        }
        if (user != null) {
            ((TextView) convertView.findViewById(R.id.userNametextView)).setText(user.getName());
            ((TextView) convertView.findViewById(R.id.userMessagetextView)).setText(messageList.get(position).message);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss", Locale.JAPAN);
            ((TextView) convertView.findViewById(R.id.userTimeStamptextView)).setText(formatter.format(messageList.get(position).create_at));
            GlideUrl glideUrl = new GlideUrl("https://mattermost.robot.rakusei.net/api/v4/users/" + user.id + "/image?_=" + user.last_picture_update, new LazyHeaders.Builder()
                    .addHeader("authorization", "BEARER " + context.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""))
                    .build());
            Glide.with(context).load(glideUrl).circleCrop().into((ImageView) convertView.findViewById(R.id.userimageView));
            try {
                JSONArray jsonArray = new JSONArray(messageList.get(position).file_ids);
                if (jsonArray.length() >= 1) {
                    glideUrl = new GlideUrl("https://mattermost.robot.rakusei.net/api/v4/files/" + jsonArray.getString(0), new LazyHeaders.Builder()
                            .addHeader("authorization", "BEARER " + context.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""))
                            .build());
                    Glide.with(context).load(glideUrl).into((ImageView) convertView.findViewById(R.id.userMessageimageView));
                    convertView.findViewById(R.id.userMessageimageView).setOnClickListener((v) -> {
                        try {
                            context.startActivity(new Intent(context, ImageDetailActivity.class).setData(Uri.parse("https://mattermost.robot.rakusei.net/api/v4/files/" + jsonArray.getString(0))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    convertView.findViewById(R.id.userMessageimageView).setLayoutParams(new android.widget.RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, 0));
                }
            } catch (Exception e) {
                e.printStackTrace();
                convertView.findViewById(R.id.userMessageimageView).setLayoutParams(new android.widget.RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.MATCH_PARENT, 0));
            }
        } else {
            new UserTask(context).execute(new String[]{messageList.get(position).user_id});
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss", Locale.JAPAN);
            ((TextView) convertView.findViewById(R.id.userTimeStamptextView)).setText(formatter.format(messageList.get(position).create_at));
            ((TextView) convertView.findViewById(R.id.userNametextView)).setText("Unknown");
            ((TextView) convertView.findViewById(R.id.userMessagetextView)).setText("");
        }
        //}
        return convertView;
    }
}