package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WealthItem
import com.example.data.model.WealthType
import kotlinx.coroutines.flow.Flow

@Dao
interface WealthDao {
    @Query("SELECT * FROM wealth_items ORDER BY id DESC")
    fun getAllWealthItems(): Flow<List<WealthItem>>

    @Query("SELECT * FROM wealth_items WHERE type = :type ORDER BY id DESC")
    fun getWealthItemsByType(type: WealthType): Flow<List<WealthItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWealthItem(item: WealthItem): Long

    @Update
    suspend fun updateWealthItem(item: WealthItem)

    @Query("DELETE FROM wealth_items WHERE id = :id")
    suspend fun deleteWealthItem(id: Long)

    @Query("SELECT * FROM wealth_items ORDER BY id DESC")
    suspend fun getAllWealthItemsSnapshot(): List<WealthItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWealthItems(items: List<WealthItem>)

    @Query("DELETE FROM wealth_items")
    suspend fun deleteAllWealthItems()
}
