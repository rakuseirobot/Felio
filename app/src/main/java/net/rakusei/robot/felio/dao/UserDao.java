package net.rakusei.robot.felio.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.rakusei.robot.felio.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE id = :id")
    List<User> getMessagesByChannel(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}