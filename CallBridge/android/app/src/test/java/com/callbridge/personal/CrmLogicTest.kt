package com.callbridge.personal

import com.callbridge.personal.data.local.entity.ContactEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CrmLogicTest {

    @Test
    fun contactSearch_matchesNameCompanyOrPhone() {
        val contacts = listOf(
            ContactEntity(displayName = "John Miller", phoneE164 = "+12025550143", company = "Acme Logistics"),
            ContactEntity(displayName = "Sarah Jenkins", phoneE164 = "+14155550189", company = "BlueStar Media"),
            ContactEntity(displayName = "David Chen", phoneE164 = "+12125550177", company = "Global Trade")
        )

        // Query by name
        val nameResults = contacts.filter {
            it.displayName.contains("john", ignoreCase = true) ||
            (it.company != null && it.company.contains("john", ignoreCase = true)) ||
            it.phoneE164.contains("john")
        }
        assertThat(nameResults).hasSize(1)
        assertThat(nameResults.first().displayName).isEqualTo("John Miller")

        // Query by company
        val companyResults = contacts.filter {
            it.displayName.contains("bluestar", ignoreCase = true) ||
            (it.company != null && it.company.contains("bluestar", ignoreCase = true)) ||
            it.phoneE164.contains("bluestar")
        }
        assertThat(companyResults).hasSize(1)
        assertThat(companyResults.first().displayName).isEqualTo("Sarah Jenkins")

        // Query by phone digits
        val phoneResults = contacts.filter {
            it.displayName.contains("0177", ignoreCase = true) ||
            (it.company != null && it.company.contains("0177", ignoreCase = true)) ||
            it.phoneE164.contains("0177")
        }
        assertThat(phoneResults).hasSize(1)
        assertThat(phoneResults.first().displayName).isEqualTo("David Chen")
    }

    @Test
    fun contactList_sortsAlphabeticallyCaseInsensitive() {
        val contacts = listOf(
            ContactEntity(displayName = "bob", phoneE164 = "+1111111111"),
            ContactEntity(displayName = "Alice", phoneE164 = "+1222222222"),
            ContactEntity(displayName = "charlie", phoneE164 = "+1333333333")
        )

        val sorted = contacts.sortedBy { it.displayName.lowercase() }
        assertThat(sorted.map { it.displayName }).containsExactly("Alice", "bob", "charlie").inOrder()
    }
}
