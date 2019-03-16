package net.rakusei.robot.felio.model;

public class UserStatus {
    public String id = "";
    public STATUS status = STATUS.OFFLINE;
    public boolean manual = false;
    public long last_activity_at = 0;

    public enum STATUS {
        ONLINE,
        AWAY,
        OFFLINE,
        DND
    }

    public UserStatus(String id, String status, boolean manual, long last_activity_at) {
        this.id = id;
        switch (status) {
            case "online":
                this.status = STATUS.ONLINE;
                break;
            case "away":
                this.status = STATUS.AWAY;
                break;
            case "offline":
                this.status = STATUS.OFFLINE;
                break;
            case "dnd":
                this.status = STATUS.DND;
                break;
        }
        this.manual = manual;
        this.last_activity_at = last_activity_at;
    }

}
