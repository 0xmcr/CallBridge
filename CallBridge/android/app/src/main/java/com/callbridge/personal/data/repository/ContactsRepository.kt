package com.callbridge.personal.data.repository

import com.callbridge.personal.data.local.dao.ContactDao
import com.callbridge.personal.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactsRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    fun searchContacts(query: String): Flow<List<ContactEntity>> {
        return contactDao.searchContacts(query.trim())
    }

    fun getContactById(id: String): Flow<ContactEntity?> {
        return contactDao.getContactById(id)
    }

    fun getContactByPhone(phone: String): Flow<ContactEntity?> {
        return contactDao.getContactByPhone(phone)
    }

    suspend fun saveContact(contact: ContactEntity) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: ContactEntity): Int {
        return contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity): Int {
        return contactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(id: String): Int {
        return contactDao.deleteContactById(id)
    }
}
