package net.rakusei.robot.felio.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Channel {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "create_at")
    public long create_at;

    @ColumnInfo(name = "creator_id")
    public String creator_id;

    @ColumnInfo(name = "delete_at")
    public long delete_at;

    @ColumnInfo(name = "display_name")
    public String display_name;

    @ColumnInfo(name = "header")
    public String header;

    @ColumnInfo(name = "last_post_at")
    public long last_post_at;

    @ColumnInfo(name = "purpose")
    public String purpose;

    @ColumnInfo(name = "team_id")
    public String team_id;

    @ColumnInfo(name = "total_msg_count")
    public int total_msg_count;

    @ColumnInfo(name = "type")
    public int type; // 0=public,1=private,2=direct

    @ColumnInfo(name = "update_at")
    public long update_at;

    @ColumnInfo(name = "lastSynctime")
    public long lastSynctime = 0;

    public Channel() {
    }

    public Channel(long create_at, String creator_id, long delete_at, String display_name, String header, String id,
                   long last_post_at, String purpose, String team_id, int total_msg_count, String type, long update_at){
        this.create_at = create_at;
        this.creator_id = creator_id;
        this.delete_at = delete_at;
        this.display_name = display_name;
        this.header = header;
        this.id = id;
        this.last_post_at = last_post_at;
        this.purpose = purpose;
        this.team_id = team_id;
        this.total_msg_count = total_msg_count;
        if(type.equals("D")){
            this.type = 2;
        }else if(type.equals("P")){
            this.type = 1;
        }else{
            this.type = 0;
        }
        this.update_at = update_at;
    }

}
