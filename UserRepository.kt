package com.example.synapseai.data.repository

import com.example.synapseai.data.model.User
import com.example.synapseai.data.model.UserSummary
import com.example.synapseai.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for user management operations.
 */
interface UserRepository {
    /**
     * Register a new user.
     * @param email User's email
     * @param username User's username
     * @param displayName User's display name
     * @param password Plain text password (will be hashed)
     * @param organization Optional organization
     * @return Result containing the created user or an error
     */
    suspend fun registerUser(
        email: String,
        username: String,
        displayName: String,
        password: String,
        organization: String? = null
    ): Result<User>

    /**
     * Authenticate a user with email/username and password.
     * @param emailOrUsername Email or username
     * @param password Plain text password
     * @return Result containing the authenticated user or an error
     */
    suspend fun login(emailOrUsername: String, password: String): Result<User>

    /**
     * Authenticate a user with a provider like Google.
     * @param provider The auth provider (e.g., "google")
     * @param token The auth token from the provider
     * @return Result containing the authenticated user or an error
     */
    suspend fun loginWithProvider(provider: String, token: String): Result<User>

    /**
     * Get the current authenticated user.
     * @return Flow of the current user, or null if not authenticated
     */
    fun getCurrentUser(): Flow<User?>

    /**
     * Logout the current user.
     */
    suspend fun logout()

    /**
     * Check if the user is authenticated.
     * @return Flow of boolean indicating authentication status
     */
    fun isAuthenticated(): Flow<Boolean>

    /**
     * Get user by ID.
     * @param userId User ID
     * @return Result containing the user or an error
     */
    suspend fun getUserById(userId: String): Result<User>

    /**
     * Get user by email.
     * @param email User email
     * @return Result containing the user or an error
     */
    suspend fun getUserByEmail(email: String): Result<User>

    /**
     * Get user by username.
     * @param username Username
     * @return Result containing the user or an error
     */
    suspend fun getUserByUsername(username: String): Result<User>

    /**
     * Update user profile.
     * @param userId User ID
     * @param displayName Optional new display name
     * @param avatarUrl Optional new avatar URL
     * @param jobTitle Optional new job title
     * @param organization Optional new organization
     * @param phoneNumber Optional new phone number
     * @return Result containing the updated user or an error
     */
    suspend fun updateUserProfile(
        userId: String,
        displayName: String? = null,
        avatarUrl: String? = null,
        jobTitle: String? = null,
        organization: String? = null,
        phoneNumber: String? = null
    ): Result<User>

    /**
     * Change user password.
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @return Result indicating success or an error
     */
    suspend fun changePassword(
        userId: String,
        currentPassword: String,
        newPassword: String
    ): Result<Boolean>

    /**
     * Initiate a password reset.
     * @param email User email
     * @return Result containing a reset token or an error
     */
    suspend fun initiatePasswordReset(email: String): Result<String>

    /**
     * Complete a password reset.
     * @param token Reset token
     * @param newPassword New password
     * @return Result indicating success or an error
     */
    suspend fun completePasswordReset(token: String, newPassword: String): Result<Boolean>

    /**
     * Verify a user's email.
     * @param token Verification token
     * @return Result indicating success or an error
     */
    suspend fun verifyEmail(token: String): Result<Boolean>

    /**
     * Send an email verification to a user.
     * @param userId User ID
     * @return Result containing a verification token or an error
     */
    suspend fun sendEmailVerification(userId: String): Result<String>

    /**
     * Search for users.
     * @param query Search query
     * @param organization Optional organization to filter by
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Result containing a list of user summaries or an error
     */
    suspend fun searchUsers(
        query: String,
        organization: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<UserSummary>>

    /**
     * Check if an email is available.
     * @param email Email to check
     * @return Result containing true if available or an error
     */
    suspend fun isEmailAvailable(email: String): Result<Boolean>

    /**
     * Check if a username is available.
     * @param username Username to check
     * @return Result containing true if available or an error
     */
    suspend fun isUsernameAvailable(username: String): Result<Boolean>

    /**
     * Record user login.
     * @param userId User ID
     * @return Result indicating success or an error
     */
    suspend fun recordUserLogin(userId: String): Result<Date>

    /**
     * Deactivate a user account.
     * @param userId User ID
     * @param reason Optional reason for deactivation
     * @return Result indicating success or an error
     */
    suspend fun deactivateUser(userId: String, reason: String? = null): Result<Boolean>

    /**
     * Reactivate a user account.
     * @param userId User ID
     * @return Result indicating success or an error
     */
    suspend fun reactivateUser(userId: String): Result<Boolean>
}
