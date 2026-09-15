package com.rexosphere.haptictotp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Storage boundary. The base app ships an in-memory implementation so the team
 * can work on channels and UX immediately. A persistent, encrypted
 * implementation (Android Keystore / iOS Keychain) is a separate task.
 */
interface AccountRepository {
    val accounts: StateFlow<List<Account>>
    fun get(id: String): Account? = accounts.value.firstOrNull { it.id == id }
    suspend fun add(account: Account)
    suspend fun remove(id: String)
}

class InMemoryAccountRepository(initial: List<Account> = emptyList()) : AccountRepository {
    private val state = MutableStateFlow(initial)
    override val accounts: StateFlow<List<Account>> = state

    override suspend fun add(account: Account) = state.update { it + account }

    override suspend fun remove(id: String) = state.update { list -> list.filterNot { it.id == id } }
}

object DemoData {
    /** The RFC 4226 / 6238 test secret ("12345678901234567890"), so results are checkable against the RFC tables. */
    const val RFC_SECRET_BASE32 = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"

    val demoAccount = Account(
        id = "demo",
        label = "demo@example.com",
        issuer = "Demo (RFC secret)",
        secretBase32 = RFC_SECRET_BASE32,
    )
}
