package com.valerij.notepad.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("""
        SELECT * FROM NoteEntity
        WHERE deleted = 0
            AND ( title LIKE '%' || :query || '%' 
            OR content LIKE '%' || :query || '%'
            )
        ORDER BY pinned DESC, createdAt DESC
    """)
    fun getAllNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM NoteEntity WHERE deleted = 1 ORDER BY createdAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM NoteEntity WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("DELETE FROM NoteEntity WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<String>)
}