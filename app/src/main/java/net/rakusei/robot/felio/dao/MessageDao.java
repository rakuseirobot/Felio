package net.rakusei.robot.felio.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.rakusei.robot.felio.model.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM message")
    List<Message> getAll();

    @Query("SELECT * FROM message WHERE channel_id = :channel_id")
    List<Message> getMessagesByChannel(String channel_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message message);

    @Insert
    void insertAll(Message... messages);

    @Delete
    void delete(Message messages);
}
