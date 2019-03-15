package net.rakusei.robot.felio.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Entity
public class Message {

    public Message(JSONObject postData) throws JSONException {
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

        this.channel_id = postData.getString("channel_id");
        this.create_at = postData.getLong("create_at");
        this.delete_at = postData.getLong("delete_at");
        this.edit_at = postData.getLong("edit_at");
        this.update_at = postData.getLong("update_at");
        this.file_ids = postData.getJSONArray("file_ids").toString();
        this.filenames = postData.getJSONArray("filenames").toString();
        this.hashtag = postData.getString("hashtag");
        this.id = postData.getString("id");
        this.message = postData.getString("message");
        this.metadata = postData.getJSONObject("metadata").toString();
        this.original_id = postData.getString("original_id");
        this.parent_id = postData.getString("parent_id");
        this.pending_post_id = postData.getString("pending_post_id");
        this.props = postData.getJSONObject("props").toString();
        this.root_id = postData.getString("root_id");
        this.type = postData.getString("type");
        this.user_id = postData.getString("user_id");
    }

    public Message() {
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "create_at")
    public long create_at;

    @ColumnInfo(name = "update_at")
    public long update_at;

    @ColumnInfo(name = "delete_at")
    public long delete_at;

    @ColumnInfo(name = "edit_at")
    public long edit_at;

    @ColumnInfo(name = "user_id")
    public String user_id;

    @ColumnInfo(name = "channel_id")
    public String channel_id;

    @ColumnInfo(name = "root_id")
    public String root_id;

    @ColumnInfo(name = "parent_id")
    public String parent_id;

    @ColumnInfo(name = "original_id")
    public String original_id;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "props")
    public String props;

    @ColumnInfo(name = "hashtag")
    public String hashtag;

    @ColumnInfo(name = "filenames")
    public String filenames;

    @ColumnInfo(name = "file_ids")
    public String file_ids;

    @ColumnInfo(name = "pending_post_id")
    public String pending_post_id;

    @ColumnInfo(name = "metadata")
    public String metadata;

    @NonNull
    @Override
    public String toString() {
        return "Message { id=" + id + ",user_id=" + user_id + ",message=" + message + "}";
    }
}
