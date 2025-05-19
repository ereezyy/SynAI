package com.example.synapseai.data.repository

import com.example.synapseai.data.model.OperationType
import com.example.synapseai.data.model.SyncOperation
import com.example.synapseai.data.model.SyncStatus
import com.example.synapseai.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for managing sync operations.
 */
interface SyncRepository {
    /**
     * Queue a new operation for syncing.
     * @param entityType The type of entity being synced.
     * @param entityId The ID of the entity being synced.
     * @param operationType The type of operation being performed.
     * @param payload The payload of the operation.
     * @param priority The priority of the operation (higher values synced first).
     * @param metadata Additional metadata for the operation.
     * @return Result containing the created SyncOperation or an error.
     */
    suspend fun queueOperation(
        entityType: String,
        entityId: String,
        operationType: OperationType,
        payload: String,
        priority: Int = 0,
        metadata: Map<String, String> = emptyMap()
    ): Result<SyncOperation>

    /**
     * Get a specific operation by ID.
     * @param operationId The ID of the operation to retrieve.
     * @return Result containing the SyncOperation or an error.
     */
    suspend fun getOperation(operationId: String): Result<SyncOperation>

    /**
     * Delete a sync operation.
     * @param operationId The ID of the operation to delete.
     * @return Result indicating success or an error.
     */
    suspend fun deleteOperation(operationId: String): Result<Boolean>

    /**
     * Update the status of a sync operation.
     * @param operationId The ID of the operation to update.
     * @param status The new status of the operation.
     * @param syncedAt The time the operation was synced (if completed).
     * @return Result containing the updated SyncOperation or an error.
     */
    suspend fun updateOperationStatus(
        operationId: String,
        status: SyncStatus,
        syncedAt: Date? = null
    ): Result<SyncOperation>

    /**
     * Increment the retry count for an operation.
     * @param operationId The ID of the operation to update.
     * @return Result containing the updated SyncOperation or an error.
     */
    suspend fun incrementRetryCount(operationId: String): Result<SyncOperation>

    /**
     * Get all pending operations.
     * @param entityType Optional filter by entity type.
     * @param operationType Optional filter by operation type.
     * @return Result containing a list of pending SyncOperations or an error.
     */
    suspend fun getPendingOperations(
        entityType: String? = null,
        operationType: OperationType? = null
    ): Result<List<SyncOperation>>

    /**
     * Get all operations in a specific status.
     * @param status The status to filter by.
     * @param entityType Optional filter by entity type.
     * @param operationType Optional filter by operation type.
     * @return Result containing a list of SyncOperations or an error.
     */
    suspend fun getOperationsByStatus(
        status: SyncStatus,
        entityType: String? = null,
        operationType: OperationType? = null
    ): Result<List<SyncOperation>>

    /**
     * Get all operations for a specific entity.
     * @param entityType The type of entity.
     * @param entityId The ID of the entity.
     * @return Result containing a list of SyncOperations or an error.
     */
    suspend fun getOperationsForEntity(
        entityType: String,
        entityId: String
    ): Result<List<SyncOperation>>

    /**
     * Get a flow of operations by status.
     * @param status The status to filter by.
     * @return Flow emitting lists of SyncOperations when they change.
     */
    fun observeOperationsByStatus(status: SyncStatus): Flow<List<SyncOperation>>

    /**
     * Get a flow of pending operations counts.
     * @return Flow emitting the count of pending operations.
     */
    fun observePendingOperationsCount(): Flow<Int>

    /**
     * Get the count of operations in a specific status.
     * @param status The status to count.
     * @return Result containing the count or an error.
     */
    suspend fun getOperationsCount(status: SyncStatus): Result<Int>

    /**
     * Clear all sync operations in a specific status.
     * @param status The status of operations to clear.
     * @return Result indicating success or an error.
     */
    suspend fun clearOperationsByStatus(status: SyncStatus): Result<Int>

    /**
     * Process next batch of pending operations.
     * @param batchSize The number of operations to process.
     * @return Result containing a list of operations that were processed or an error.
     */
    suspend fun processNextBatch(batchSize: Int = 10): Result<List<SyncOperation>>
}
