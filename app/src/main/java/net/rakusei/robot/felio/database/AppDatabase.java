package net.rakusei.robot.felio.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import net.rakusei.robot.felio.dao.MessageDao;
import net.rakusei.robot.felio.model.Message;

@Database(entities = {Message.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
}
