package net.rakusei.robot.felio.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import net.rakusei.robot.felio.dao.ChannelDao;
import net.rakusei.robot.felio.dao.MessageDao;
import net.rakusei.robot.felio.dao.UserDao;
import net.rakusei.robot.felio.model.Channel;
import net.rakusei.robot.felio.model.Message;
import net.rakusei.robot.felio.model.User;

@Database(entities = {User.class, Message.class, Channel.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract MessageDao messageDao();

    public abstract ChannelDao channelDao();
}
