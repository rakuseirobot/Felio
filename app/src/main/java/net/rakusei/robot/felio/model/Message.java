package net.rakusei.robot.felio.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Message {
    @PrimaryKey
    public int _id;

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
}
