package ms.mattschlenkrich.billsprojectionv2.common.functions

import java.security.MessageDigest

class SecurityUtils {
    /**
     * Hashes a string using SHA-256.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}