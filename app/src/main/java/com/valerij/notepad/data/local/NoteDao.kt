package com.valerij.notepad.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("""
        SELECT * FROM NoteEntity
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%'
        ORDER BY pinned DESC
    """)
    fun getAllNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notes: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM NoteEntity WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?
}