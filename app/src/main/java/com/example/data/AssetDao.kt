package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY assetTag ASC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Query("SELECT * FROM assets WHERE category = :category ORDER BY assetTag ASC")
    fun getAssetsByCategory(category: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE assetTag LIKE '%' || :query || '%' OR hardwareModel LIKE '%' || :query || '%' OR serialNumber LIKE '%' || :query || '%' OR assignedUser LIKE '%' || :query || '%' OR ipAddress LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchAssets(query: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("UPDATE assets SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateAssetStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())
}
