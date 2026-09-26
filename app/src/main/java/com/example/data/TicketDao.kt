package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY CASE priority WHEN 'P1' THEN 1 WHEN 'P2' THEN 2 WHEN 'P3' THEN 3 ELSE 4 END, updatedAt DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): TicketEntity?

    @Query("SELECT * FROM tickets WHERE status = :status ORDER BY updatedAt DESC")
    fun getTicketsByStatus(status: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE priority = :priority ORDER BY updatedAt DESC")
    fun getTicketsByPriority(priority: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR ticketNumber LIKE '%' || :query || '%' OR reporterName LIKE '%' || :query || '%'")
    fun searchTickets(query: String): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<TicketEntity>)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)

    @Query("UPDATE tickets SET status = :newStatus, resolutionNotes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTicketStatus(id: Long, newStatus: String, notes: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tickets SET isStarred = NOT isStarred WHERE id = :id")
    suspend fun toggleStar(id: Long)

    @Query("SELECT COUNT(*) FROM tickets WHERE status = 'Open' OR status = 'In Progress'")
    fun getActiveTicketsCount(): Flow<Int>
}
