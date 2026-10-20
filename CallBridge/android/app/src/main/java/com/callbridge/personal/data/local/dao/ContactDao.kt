package com.callbridge.personal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.callbridge.personal.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("""
        SELECT * FROM contacts 
        WHERE displayName LIKE '%' || :query || '%' 
           OR phoneE164 LIKE '%' || :query || '%' 
           OR (company IS NOT NULL AND company LIKE '%' || :query || '%')
        ORDER BY displayName COLLATE NOCASE ASC
    """)
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    fun getContactById(id: String): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE phoneE164 = :phone LIMIT 1")
    fun getContactByPhone(phone: String): Flow<ContactEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity): Int

    @Delete
    suspend fun deleteContact(contact: ContactEntity): Int

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: String): Int
}
