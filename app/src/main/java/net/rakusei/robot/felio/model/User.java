package net.rakusei.robot.felio.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {

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

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "first_name")
    public String first_name;

    @ColumnInfo(name = "last_name")
    public String last_name;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "email_verified")
    public Boolean email_verified;

    @ColumnInfo(name = "auth_service")
    public String auth_service;

    @ColumnInfo(name = "roles")
    public String roles;

    @ColumnInfo(name = "locale")
    public String locale;

    @ColumnInfo(name = "notify_props")
    public String notify_props;

    @ColumnInfo(name = "props")
    public String props;

    @ColumnInfo(name = "last_password_update")
    public long last_password_update;

    @ColumnInfo(name = "last_picture_update")
    public long last_picture_update;

    @ColumnInfo(name = "failed_attempts")
    public long failed_attempts;

    @ColumnInfo(name = "mfa_active")
    public boolean mfa_active;

    @ColumnInfo(name = "timezone")
    public String timezone;

    @NonNull
    @Override
    public String toString() {
        return "User { id=" + id + ",username=" + username + ",email=" + email + "}";
    }
}
