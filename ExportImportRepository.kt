package com.example.synapseai.data.repository

import android.net.Uri
import com.example.synapseai.data.model.ExportData
import com.example.synapseai.util.Result
import java.io.InputStream
import java.util.Date

/**
 * Repository interface for handling export and import operations.
 */
interface ExportImportRepository {

    /**
     * Export types that the app supports.
     */
    enum class ExportFormat {
        JSON,
        CSV,
        PDF
    }

    /**
     * Export meetings and preferences to a file.
     * @param uri The destination URI for the exported file.
     * @param format The format to export the data in.
     * @param exportPreferences Whether to include user preferences in the export.
     * @param startDate Optional start date to filter meetings.
     * @param endDate Optional end date to filter meetings.
     * @param tags Optional tags to filter meetings.
     * @return Result containing a success boolean or an error.
     */
    suspend fun exportData(
        uri: Uri,
        format: ExportFormat,
        exportPreferences: Boolean = true,
        startDate: Date? = null,
        endDate: Date? = null,
        tags: List<String>? = null
    ): Result<Boolean>

    /**
     * Import meetings and preferences from a file.
     * @param uri The source URI for the import file.
     * @param importPreferences Whether to import user preferences.
     * @param overwriteExistingMeetings Whether to overwrite existing meetings with the same ID.
     * @return Result containing the imported data summary or an error.
     */
    suspend fun importData(
        uri: Uri,
        importPreferences: Boolean = true,
        overwriteExistingMeetings: Boolean = false
    ): Result<ImportSummary>

    /**
     * Import meetings from a CSV file.
     * @param uri The source URI for the CSV file.
     * @param overwriteExistingMeetings Whether to overwrite existing meetings with the same ID.
     * @return Result containing the imported data summary or an error.
     */
    suspend fun importFromCSV(
        uri: Uri,
        overwriteExistingMeetings: Boolean = false
    ): Result<ImportSummary>

    /**
     * Import meetings from a calendar file (ICS).
     * @param uri The source URI for the ICS file.
     * @param overwriteExistingMeetings Whether to overwrite existing meetings with the same ID.
     * @return Result containing the imported data summary or an error.
     */
    suspend fun importFromCalendarFile(
        uri: Uri,
        overwriteExistingMeetings: Boolean = false
    ): Result<ImportSummary>

    /**
     * Check if a file is a valid SynapseAI export.
     * @param uri The URI of the file to check.
     * @return Result containing a boolean indicating if the file is a valid export.
     */
    suspend fun isValidExportFile(uri: Uri): Result<Boolean>

    /**
     * Get a summary of the file contents without importing.
     * @param uri The URI of the file to preview.
     * @return Result containing an ImportPreview or an error.
     */
    suspend fun previewImportFile(uri: Uri): Result<ImportPreview>

    /**
     * Create a backup of user preferences.
     * @param uri The destination URI for the backup file.
     * @return Result containing a success boolean or an error.
     */
    suspend fun backupUserPreferences(uri: Uri): Result<Boolean>

    /**
     * Restore user preferences from a backup.
     * @param uri The source URI for the backup file.
     * @return Result containing a success boolean or an error.
     */
    suspend fun restoreUserPreferences(uri: Uri): Result<Boolean>
}

/**
 * Data class containing summary information about an import operation.
 */
data class ImportSummary(
    val meetingsImported: Int,
    val meetingsUpdated: Int,
    val meetingsSkipped: Int,
    val preferencesImported: Boolean,
    val errorMessages: List<String> = emptyList()
)

/**
 * Data class containing preview information about a file to be imported.
 */
data class ImportPreview(
    val meetingsCount: Int,
    val hasPreferences: Boolean,
    val dateRange: Pair<Date, Date>? = null,
    val tags: List<String> = emptyList(),
    val format: ExportImportRepository.ExportFormat,
    val isValidFormat: Boolean,
    val potentialIssues: List<String> = emptyList()
)
