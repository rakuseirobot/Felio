package net.rakusei.robot.felio.model;

public class Channel {

    public long create_at;
    public String creator_id;
    public long delete_at;
    public String display_name;
    public String header;
    public String id;
    public long last_post_at;
    public String purpose;
    public String team_id;
    public int total_msg_count;
    public CHANNEL_TYPE type;
    public long update_at;

    enum CHANNEL_TYPE{
        PUBLIC,
        PRIVATE,
        DIREACT
    }

    public Channel(long create_at,String creator_id,long delete_at,String display_name,String header,String id,
                   long last_post_at,String purpose,String team_id,int total_msg_count,String type,long update_at){
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
            this.type = CHANNEL_TYPE.DIREACT;
        }else if(type.equals("P")){
            this.type = CHANNEL_TYPE.PRIVATE;
        }else{
            this.type = CHANNEL_TYPE.PUBLIC;
        }
        this.update_at = update_at;
    }
}
