package net.rakusei.robot.felio.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.rakusei.robot.felio.model.Channel;

import java.util.List;

@Dao
public interface ChannelDao {
    @Query("SELECT * FROM channel")
    List<Channel> getAll();

    @Query("SELECT * FROM channel WHERE id = :channel_id limit 1")
    Channel getChannelById(String channel_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Channel channel);

    @Insert
    void insertAll(Channel... channels);

    @Delete
    void delete(Channel channel);
}
