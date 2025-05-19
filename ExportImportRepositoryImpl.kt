package com.example.synapseai.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.synapseai.data.model.ExportData
import com.example.synapseai.data.model.Meeting
import com.example.synapseai.data.model.MeetingExport
import com.example.synapseai.data.model.UserPreferencesExport
import com.example.synapseai.data.model.toExport
import com.example.synapseai.data.model.toMeeting
import com.example.synapseai.data.model.toUserPreferences
import com.example.synapseai.util.ErrorHandler
import com.example.synapseai.util.ErrorHandler
import com.example.synapseai.util.Result
import com.example.synapseai.util.runCatching
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
/**
 * Implementation of the ExportImportRepository interface.
 * Handles exporting and importing meetings and preferences.
 */
@Singleton
class ExportImportRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meetingRepository: MeetingRepository,
    private val preferencesRepository: PreferencesRepository
) : ExportImportRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val csvDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    override suspend fun exportData(
        uri: Uri,
        format: ExportImportRepository.ExportFormat,
        exportPreferences: Boolean,
        startDate: Date?,
        endDate: Date?,
        tags: List<String>?
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // Get all meetings based on filters
            var meetings = meetingRepository.getAllMeetings()

            // Apply filters
            if (startDate != null) {
                meetings = meetings.filter { it.date >= startDate }
            }
            if (endDate != null) {
                meetings = meetings.filter { it.date <= endDate }
            }
            if (tags != null && tags.isNotEmpty()) {
                meetings = meetings.filter { meeting ->
                    meeting.tags?.any { tag -> tags.contains(tag) } ?: false
                }
            }
            when (format) {
                ExportImportRepository.ExportFormat.JSON -> exportToJson(uri, meetings, exportPreferences)
                ExportImportRepository.ExportFormat.CSV -> exportToCsv(uri, meetings)
                ExportImportRepository.ExportFormat.PDF -> {
                    Log.i("SynapseAI-Export", "Starting PDF export. URI: $uri, Meetings count: ${meetings.size}")
                    exportToPdf(uri, meetings)
                }
            }

            true
        }
    }

    /**
     * Generate a preview of the PDF export without writing to file
     * Returns a bitmap image of the first page that can be shown to the user
     */
    suspend fun generatePdfPreview(
        meetings: List<Meeting>
    ): Result<android.graphics.Bitmap> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // Create a temporary file for the PDF
            val tempFile = java.io.File.createTempFile("preview", ".pdf")
            tempFile.deleteOnExit()

            // Generate the PDF to the temp file
            java.io.FileOutputStream(tempFile).use { outputStream ->
                // Use the same PDF generation code but limit to first page
                val pdfWriter = PdfWriter(outputStream)
                val pdf = PdfDocument(pdfWriter)
                val document = Document(pdf, PageSize.A4)

                // Add title
                val title = Paragraph("Meeting Export (Preview)")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
                document.add(title)

                // Add export info
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val exportInfo = Paragraph("Preview generated on: ${dateFormat.format(Date())}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(30f)
                document.add(exportInfo)

                // Add meeting count
                val countInfo = Paragraph("Total Meetings: ${meetings.size}")
                    .setFontSize(12f)
                    .setMarginBottom(20f)
                document.add(countInfo)

                // Sample table with limited entries (just first few meetings)
                val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 20f, 15f, 50f)))
                    .setWidth(UnitValue.createPercentValue(100f))

                // Add table headers
                table.addHeaderCell(createHeaderCell("Title"))
                table.addHeaderCell(createHeaderCell("Date"))
                table.addHeaderCell(createHeaderCell("Duration"))
                table.addHeaderCell(createHeaderCell("Summary"))

                // Add just a few rows for preview
                meetings.take(3).forEach { meeting ->
                    val durationMinutes = meeting.durationMs / 60000
                    val durationFormatted = "${durationMinutes}m"

                    table.addCell(createCell(meeting.title))
                    table.addCell(createCell(dateFormat.format(meeting.date)))
                    table.addCell(createCell(durationFormatted))
                    table.addCell(createCell(meeting.summary?.take(50)?.plus("...") ?: "No summary available"))
                }

                document.add(table)

                // Preview note
                document.add(Paragraph("This is a preview. The full export will contain all meetings and details.")
                    .setFontSize(10f)
                    .setItalic()
                    .setMarginTop(20f))

                document.close()
            }

            // Convert the first page of the PDF to a bitmap
            val renderer = android.graphics.pdf.PdfRenderer(
                android.os.ParcelFileDescriptor.open(
                    tempFile,
                    android.os.ParcelFileDescriptor.MODE_READ_ONLY
                )
            )
            val page = renderer.openPage(0)

            // Create a bitmap with the page dimensions
            val bitmap = android.graphics.Bitmap.createBitmap(
                page.width * 2,
                page.height * 2,
                android.graphics.Bitmap.Config.ARGB_8888
            )

            // Render the page to the bitmap (at 2x scale for higher resolution)
            page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()

            // Clean up the temp file
            tempFile.delete()

            bitmap
        }
    }

    override suspend fun importData(
        uri: Uri,
        importPreferences: Boolean,
        overwriteExistingMeetings: Boolean
    ): Result<ImportSummary> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // Determine file type
            val filename = getFileName(uri)
            val extension = filename.substringAfterLast('.', "")

            val result = when (extension.lowercase()) {
                "json" -> importFromJson(uri, importPreferences, overwriteExistingMeetings)
                "csv" -> importFromCSV(uri, overwriteExistingMeetings).getOrThrow()
                "ics" -> importFromCalendarFile(uri, overwriteExistingMeetings).getOrThrow()
                else -> throw ErrorHandler.SynapseException.ImportException("Unsupported file format: $extension")
            }

            result
        }
    }

    override suspend fun importFromCSV(
        uri: Uri,
        overwriteExistingMeetings: Boolean
    ): Result<ImportSummary> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            var imported = 0
            var updated = 0
            var skipped = 0
            val errors = mutableListOf<String>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Read header
                    val header = reader.readLine()?.split(",") ?: throw ErrorHandler.SynapseException.ImportException("Invalid CSV file: missing header")
                    val titleIndex = header.indexOf("title")
                    val dateIndex = header.indexOf("date")
                    val durationIndex = header.indexOf("duration_ms")
                    val notesIndex = header.indexOf("notes")
                    val tagsIndex = header.indexOf("tags")

                    if (titleIndex == -1 || dateIndex == -1) {
                        throw ErrorHandler.SynapseException.ImportException("Invalid CSV file: missing required columns (title, date)")
                    }

                    // Read rows
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        try {
                            val values = line!!.split(",")
                            val title = values.getOrNull(titleIndex)?.trim() ?: ""
                            val dateStr = values.getOrNull(dateIndex)?.trim() ?: ""
                            val durationMs = values.getOrNull(durationIndex)?.trim()?.toLongOrNull() ?: 0L
                            val notes = values.getOrNull(notesIndex)?.trim()
                            val tagsStr = values.getOrNull(tagsIndex)?.trim()
                            val tags = tagsStr?.split("|")?.map { it.trim() }

                            if (title.isNotEmpty() && dateStr.isNotEmpty()) {
                                val date = try {
                                    csvDateFormat.parse(dateStr) ?: Date()
                                } catch (e: Exception) {
                                    errors.add("Invalid date format for entry: $title")
                                    Date()
                                }

                                val meeting = Meeting(
                                    title = title,
                                    date = date,
                                    durationMs = durationMs,
                                    notes = notes,
                                    tags = tags
                                )

                                val existingMeeting = meetingRepository.getMeetingByTitleAndDate(title, date)
                                if (existingMeeting != null) {
                                    if (overwriteExistingMeetings) {
                                        meetingRepository.updateMeeting(
                                            existingMeeting.copy(
                                                durationMs = durationMs,
                                                notes = notes,
                                                tags = tags
                                            )
                                        )
                                        updated++
                                    } else {
                                        skipped++
                                    }
                                } else {
                                    meetingRepository.createMeeting(meeting)
                                    imported++
                                }
                            }
                        } catch (e: Exception) {
                            errors.add("Error processing row: ${e.message}")
                        }
                    }
                }
            } ?: throw IOException("Could not open input stream for URI: $uri")

            ImportSummary(
                meetingsImported = imported,
                meetingsUpdated = updated,
                meetingsSkipped = skipped,
                preferencesImported = false,
                errorMessages = errors
            )
        }
    }

    override suspend fun importFromCalendarFile(
        uri: Uri,
        overwriteExistingMeetings: Boolean
    ): Result<ImportSummary> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // This is a placeholder implementation for ICS calendar import
            // In a real implementation, we would use a library like iCal4j to parse ICS files

            val errors = mutableListOf<String>()
            errors.add("Calendar import functionality is not fully implemented")

            ImportSummary(
                meetingsImported = 0,
                meetingsUpdated = 0,
                meetingsSkipped = 0,
                preferencesImported = false,
                errorMessages = errors
            )
        }
    }

    override suspend fun isValidExportFile(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            try {
                val filename = getFileName(uri)
                val extension = filename.substringAfterLast('.', "").lowercase()

                if (extension != "json") {
                    return@runCatching false
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val firstLine = reader.readLine() ?: return@runCatching false
                        // Check if it's a JSON object start and likely contains appVersion field
                        firstLine.contains("{") && firstLine.contains("appVersion")
                    }
                } ?: false
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun previewImportFile(uri: Uri): Result<ImportPreview> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val filename = getFileName(uri)
            val extension = filename.substringAfterLast('.', "").lowercase()

            when (extension) {
                "json" -> previewJsonFile(uri)
                "csv" -> previewCsvFile(uri)
                "ics" -> previewCalendarFile(uri)
                else -> throw ErrorHandler.SynapseException.ImportException("Unsupported file format: $extension")
            }
        }
    }

    override suspend fun backupUserPreferences(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val preferences = preferencesRepository.getUserPreferences().first()
            val preferencesExport = preferences.toExport()
            val backupData = json.encodeToString(preferencesExport)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(backupData)
                    writer.flush()
                }
            } ?: throw IOException("Could not open output stream for URI: $uri")

            true
        }
    }

    override suspend fun restoreUserPreferences(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            var jsonContent = ""
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    jsonContent = reader.readText()
                }
            } ?: throw IOException("Could not open input stream for URI: $uri")

            val preferencesExport = json.decodeFromString<UserPreferencesExport>(jsonContent)
            val preferences = preferencesExport.toUserPreferences()

            with(preferencesRepository) {
                updateNotificationPreferences(
                    preferences.enableMeetingReminders,
                    preferences.enableTranscriptionNotifications,
                    preferences.enableSummarizationNotifications
                )
                updateDefaultReminderTimes(preferences.defaultReminderTimes)
                updateThemePreferences(
                    preferences.useDarkMode,
                    preferences.useSystemTheme,
                    preferences.primaryColor
                )
                updateOtherPreferences(
                    preferences.autoStartRecording,
                    preferences.keepAudioAfterTranscription
                )
            }

            true
        }
    }

    /**
     * Export data to a JSON file.
     */
    private suspend fun exportToJson(
        uri: Uri,
        meetings: List<Meeting>,
        exportPreferences: Boolean
    ) {
        val meetingsExport = meetings.map { it.toExport() }
        val preferencesExport = if (exportPreferences) {
            preferencesRepository.getUserPreferences().first().toExport()
        } else {
            null
        }

        val exportData = ExportData(
            appVersion = getAppVersion(),
            exportDate = System.currentTimeMillis(),
            userPreferences = preferencesExport,
            meetings = meetingsExport
        )

        val jsonString = json.encodeToString(exportData)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                writer.write(jsonString)
                writer.flush()
            }
        } ?: throw IOException("Could not open output stream for URI: $uri")
    }

    /**
     * Export data to a CSV file.
     */
    private suspend fun exportToCsv(uri: Uri, meetings: List<Meeting>) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                // Write header
                writer.write("title,date,duration_ms,recording_path,transcription_path,is_transcribed,is_summarized,summary,notes,tags\n")

                // Write entries
                meetings.forEach { meeting ->
                    val tags = meeting.tags?.joinToString("|") ?: ""
                    val dateStr = csvDateFormat.format(meeting.date)
                    val escapedTitle = escapeForCsv(meeting.title)
                    val escapedSummary = escapeForCsv(meeting.summary ?: "")
                    val escapedNotes = escapeForCsv(meeting.notes ?: "")

                    writer.write(
                        "$escapedTitle,$dateStr,${meeting.durationMs},${meeting.recordingPath ?: ""},${meeting.transcriptionPath ?: ""}," +
                        "${meeting.isTranscribed},${meeting.isSummarized},$escapedSummary,$escapedNotes,$tags\n"
                    )
                }

                writer.flush()
            }
        } ?: throw IOException("Could not open output stream for URI: $uri")
    }
/**
 * Export data to a PDF file with pagination and compression.
 */
private suspend fun exportToPdf(uri: Uri, meetings: List<Meeting>) {
    Log.d("SynapseAI-Export", "Starting PDF export process for ${meetings.size} meetings")
    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            Log.d("SynapseAI-Export", "Output stream opened successfully")

            // Initialize PDF writer with compression
            val writerProperties = com.itextpdf.kernel.pdf.WriterProperties()
                .setCompressionLevel(9) // Maximum compression
                .useSmartMode() // Optimize PDF structure

            Log.d("SynapseAI-Export", "Creating PDF document with compression")
            val pdfWriter = PdfWriter(outputStream, writerProperties)
            val pdf = PdfDocument(pdfWriter)

            // Add document info
            val info = pdf.documentInfo
            info.title = "SynapseAI Meeting Export"
            info.author = "SynapseAI App"
            info.subject = "Meeting Data Export"
            info.creator = "SynapseAI Export Tool"

            // Setup page size and margins
            val document = Document(pdf, PageSize.A4)
            document.setMargins(36f, 36f, 54f, 36f) // Top margin larger for header

            // Setup pagination and headers/footers
            Log.d("SynapseAI-Export", "Setting up pagination and headers/footers")
            val font = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA
            )

            // Add event handlers for headers and footers on each page
            pdf.addEventHandler(com.itextpdf.kernel.events.PdfDocumentEvent.END_PAGE,
                object : com.itextpdf.kernel.events.IEventHandler {
                    override fun handleEvent(event: com.itextpdf.kernel.events.Event) {
                        val docEvent = event as com.itextpdf.kernel.events.PdfDocumentEvent
                        val page = docEvent.page
                        val pageSize = page.pageSize
                        val canvas = com.itextpdf.kernel.pdf.canvas.PdfCanvas(page)

                        // Footer with page number
                        canvas.beginText()
                            .setFontAndSize(font, 9f)
                            .moveText(pageSize.width / 2 - 40, 20f)
                            .showText("Page ${pdf.getPageNumber(page)} of ")
                            .endText()

                        // Header with export title
                        canvas.beginText()
                            .setFontAndSize(font, 9f)
                            .moveText(pageSize.width / 2 - 50, pageSize.height - 20f)
                            .showText("SynapseAI Meeting Export")
                            .endText()
                    }
                }
            )

            // Add total pages number once document is complete
            pdf.addEventHandler(com.itextpdf.kernel.events.PdfDocumentEvent.END_DOCUMENT,
                object : com.itextpdf.kernel.events.IEventHandler {
                    override fun handleEvent(event: com.itextpdf.kernel.events.Event) {
                        val docEvent = event as com.itextpdf.kernel.events.PdfDocumentEvent
                        val pdf = docEvent.document
                        val totalPages = pdf.numberOfPages
                        Log.d("SynapseAI-Export", "Adding page numbers: total pages = $totalPages")

                        // Add total pages to each page footer
                        for (i in 1..totalPages) {
                            val page = pdf.getPage(i)
                            val canvas = com.itextpdf.kernel.pdf.canvas.PdfCanvas(page)
                            canvas.beginText()
                                .setFontAndSize(font, 9f)
                                .moveText(page.pageSize.width / 2 + 10, 20f)
                                .showText("$totalPages")
                                .endText()
                        }
                    }
                }
            )

            // Add title
            Log.d("SynapseAI-Export", "Adding document title and metadata")
            val title = Paragraph("Meeting Export")
                .setFontSize(20f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(title)

            // Add export info
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val exportInfo = Paragraph("Exported on: ${dateFormat.format(Date())}")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(30f)
            document.add(exportInfo)

            // Add meeting count
            val countInfo = Paragraph("Total Meetings: ${meetings.size}")
                .setFontSize(12f)
                .setMarginBottom(20f)
            document.add(countInfo)

            // Create meeting table with automatic width calculation
            Log.d("SynapseAI-Export", "Creating summary table of all meetings")
            val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 20f, 15f, 50f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setAutoLayout() // Allow table to break across pages

            // Add table headers
            table.addHeaderCell(createHeaderCell("Title"))
            table.addHeaderCell(createHeaderCell("Date"))
            table.addHeaderCell(createHeaderCell("Duration"))
            table.addHeaderCell(createHeaderCell("Summary"))

            // Add data rows
            meetings.forEach { meeting ->
                val durationMinutes = meeting.durationMs / 60000
                val durationFormatted = "${durationMinutes}m"

                table.addCell(createCell(meeting.title))
                table.addCell(createCell(dateFormat.format(meeting.date)))
                table.addCell(createCell(durationFormatted))
                table.addCell(createCell(meeting.summary ?: "No summary available"))
            }

            document.add(table)

            // Add detailed meeting section with a page break
            Log.d("SynapseAI-Export", "Adding detailed meeting section with individual meeting details")
            document.add(com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4))
            document.add(Paragraph("Meeting Details")
                .setFontSize(16f)
                .setBold()
                .setMarginTop(10f)
                .setMarginBottom(20f))

            // Add each meeting with full details
            meetings.forEachIndexed { index, meeting ->
                Log.d("SynapseAI-Export", "Adding details for meeting ${index + 1}/${meetings.size}: ${meeting.title}")
                addMeetingDetails(document, meeting)

                // Check if we should add a page break based on remaining space
                val yPosition = document.getPdfDocument().lastPage.pageSize.height - document.topMargin() -
                               document.bottomMargin() - 20f // 20f is a buffer

                if (yPosition < 100f) { // If less than 100 points left on page
                    document.add(com.itextpdf.layout.element.AreaBreak(com.itextpdf.kernel.geom.PageSize.A4))
                }
            }

            Log.d("SynapseAI-Export", "Closing PDF document")
            document.close()
            Log.i("SynapseAI-Export", "PDF export completed successfully: ${uri.lastPathSegment}")
        } ?: throw IOException("Could not open output stream for URI: $uri")
    } catch (e: Exception) {
        Log.e("SynapseAI-Export", "Error exporting to PDF: ${e.message}", e)
        throw ErrorHandler.SynapseException.ExportException("Failed to export to PDF: ${e.message}", e)
    }
}

private fun createHeaderCell(text: String): Cell {
    return Cell()
            .add(Paragraph(text).setBold())
            .setTextAlignment(TextAlignment.CENTER)
            .setBackgroundColor(DeviceRgb(240, 240, 240))
            .setPadding(5f)
    }

    /**
     * Create a regular cell for the PDF table
     */
    private fun createCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text).setFontSize(10f))
            .setPadding(5f)
    }

    /**
     * Add detailed meeting information to the PDF document
     */
    private fun addMeetingDetails(document: Document, meeting: Meeting) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        // Meeting title
        document.add(Paragraph(meeting.title)
            .setFontSize(14f)
            .setBold()
            .setMarginTop(20f))

        // Date and duration
        val durationMinutes = meeting.durationMs / 60000
        document.add(Paragraph("Date: ${dateFormat.format(meeting.date)} (${durationMinutes} minutes)")
            .setFontSize(10f)
            .setMarginBottom(10f))

        // Tags
        if (!meeting.tags.isNullOrEmpty()) {
            document.add(Paragraph("Tags: ${meeting.tags.joinToString(", ")}")
                .setFontSize(10f)
                .setMarginBottom(10f))
        }

        // Summary if available
        if (!meeting.summary.isNullOrBlank()) {
            document.add(Paragraph("Summary")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f))

            document.add(Paragraph(meeting.summary)
                .setFontSize(10f)
                .setMarginBottom(10f))
        }

        // Key points if available
        if (meeting.keyPoints.isNotEmpty()) {
            document.add(Paragraph("Key Points")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f))

            meeting.keyPoints.forEachIndexed { index, point ->
                document.add(Paragraph("${index + 1}. $point")
                    .setFontSize(10f)
                    .setMarginLeft(15f))
            }
            document.add(Paragraph("").setMarginBottom(10f))
        }

        // Action items if available
        if (meeting.actionItems.isNotEmpty()) {
            document.add(Paragraph("Action Items")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f))

            meeting.actionItems.forEachIndexed { index, item ->
                document.add(Paragraph("${index + 1}. $item")
                    .setFontSize(10f)
                    .setMarginLeft(15f))
            }
            document.add(Paragraph("").setMarginBottom(10f))
        }

        // Notes if available
        if (!meeting.notes.isNullOrBlank()) {
            document.add(Paragraph("Notes")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f))

            document.add(Paragraph(meeting.notes)
                .setFontSize(10f)
                .setMarginBottom(10f))
        }

        // Add separator
        document.add(Paragraph("")
            .setBorderBottom(SolidBorder(ColorConstants.LIGHT_GRAY, 1))
            .setMarginBottom(10f))
    }

    /**
     * Import data from a JSON file.
     */
    private suspend fun importFromJson(
        uri: Uri,
        importPreferences: Boolean,
        overwriteExistingMeetings: Boolean
    ): ImportSummary {
        var jsonContent = ""
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                jsonContent = reader.readText()
            }
        } ?: throw IOException("Could not open input stream for URI: $uri")

        val exportData = json.decodeFromString<ExportData>(jsonContent)
        var imported = 0
        var updated = 0
        var skipped = 0
        val errors = mutableListOf<String>()
        var preferencesImported = false

        // Import preferences if requested
        if (importPreferences && exportData.userPreferences != null) {
            try {
                val preferences = exportData.userPreferences.toUserPreferences()

                with(preferencesRepository) {
                    updateNotificationPreferences(
                        preferences.enableMeetingReminders,
                        preferences.enableTranscriptionNotifications,
                        preferences.enableSummarizationNotifications
                    )
                    updateDefaultReminderTimes(preferences.defaultReminderTimes)
                    updateThemePreferences(
                        preferences.useDarkMode,
                        preferences.useSystemTheme,
                        preferences.primaryColor
                    )
                    updateOtherPreferences(
                        preferences.autoStartRecording,
                        preferences.keepAudioAfterTranscription
                    )
                }

                preferencesImported = true
            } catch (e: Exception) {
                errors.add("Failed to import preferences: ${e.message}")
            }
        }

        // Import meetings
        for (meetingExport in exportData.meetings) {
            try {
                val meeting = meetingExport.toMeeting()
                val existingMeeting = meetingRepository.getMeetingByTitleAndDate(meeting.title, meeting.date)

                if (existingMeeting != null) {
                    if (overwriteExistingMeetings) {
                        meetingRepository.updateMeeting(
                            existingMeeting.copy(
                                durationMs = meeting.durationMs,
                                recordingPath = meeting.recordingPath,
                                transcriptionPath = meeting.transcriptionPath,
                                isTranscribed = meeting.isTranscribed,
                                isSummarized = meeting.isSummarized,
                                keyPoints = meeting.keyPoints,
                                actionItems = meeting.actionItems,
                                summary = meeting.summary,
                                notes = meeting.notes,
                                tags = meeting.tags
                            )
                        )
                        updated++
                    } else {
                        skipped++
                    }
                } else {
                    meetingRepository.createMeeting(meeting)
                    imported++
                }
            } catch (e: Exception) {
                errors.add("Failed to import meeting '${meetingExport.title}': ${e.message}")
            }
        }

        return ImportSummary(
            meetingsImported = imported,
            meetingsUpdated = updated,
            meetingsSkipped = skipped,
            preferencesImported = preferencesImported,
            errorMessages = errors
        )
    }

    /**
     * Preview a JSON import file.
     */
    private suspend fun previewJsonFile(uri: Uri): ImportPreview {
        var jsonContent = ""
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                jsonContent = reader.readText()
            }
        } ?: throw IOException("Could not open input stream for URI: $uri")

        try {
            val exportData = json.decodeFromString<ExportData>(jsonContent)

            var startDate: Date? = null
            var endDate: Date? = null
            val allTags = mutableSetOf<String>()

            exportData.meetings.forEach { meetingExport ->
                val date = Date(meetingExport.date)
                if (startDate == null || date < startDate) {
                    startDate = date
                }
                if (endDate == null || date > endDate) {
                    endDate = date
                }

                meetingExport.tags?.let { allTags.addAll(it) }
            }

            val dateRange = if (startDate != null && endDate != null) {
                Pair(startDate!!, endDate!!)
            } else {
                null
            }

            return ImportPreview(
                meetingsCount = exportData.meetings.size,
                hasPreferences = exportData.userPreferences != null,
                dateRange = dateRange,
                tags = allTags.toList(),
                format = ExportImportRepository.ExportFormat.JSON,
                isValidFormat = true
            )
        } catch (e: Exception) {
            return ImportPreview(
                meetingsCount = 0,
                hasPreferences = false,
                format = ExportImportRepository.ExportFormat.JSON,
                isValidFormat = false,
                potentialIssues = listOf("Invalid JSON format: ${e.message}")
            )
        }
    }

    /**
     * Preview a CSV import file.
     */
    private suspend fun previewCsvFile(uri: Uri): ImportPreview {
        val issues = mutableListOf<String>()
        var meetingsCount = 0
        val allTags = mutableSetOf<String>()
        var startDate: Date? = null
        var endDate: Date? = null
        var isValidFormat = true

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                try {
                    // Read header
                    val header = reader.readLine()?.split(",") ?: throw ErrorHandler.SynapseException.ImportException("Invalid CSV file: missing header")
                    val titleIndex = header.indexOf("title")
                    val dateIndex = header.indexOf("date")
                    val tagsIndex = header.indexOf("tags")

                    if (titleIndex == -1 || dateIndex == -1) {
                        isValidFormat = false
                        issues.add("Missing required columns: title, date")
                        return@use
                    }

                    // Read rows
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        try {
                            val values = line!!.split(",")
                            val dateStr = values.getOrNull(dateIndex)?.trim() ?: ""
                            val tagsStr = values.getOrNull(tagsIndex)?.trim()

                            if (dateStr.isNotEmpty()) {
                                val date = try {
                                    csvDateFormat.parse(dateStr)
                                } catch (e: Exception) {
                                    issues.add("Invalid date format: $dateStr")
                                    continue
                                }

                                date?.let {
                                    if (startDate == null || it < startDate) {
                                        startDate = it
                                    }
                                    if (endDate == null || it > endDate) {
                                        endDate = it
                                    }
                                }

                                tagsStr?.split("|")?.map { it.trim() }?.let { allTags.addAll(it) }
                                meetingsCount++
                            }
                        } catch (e: Exception) {
                            issues.add("Error parsing row: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    isValidFormat = false
                    issues.add("Error reading CSV: ${e.message}")
                }
            }
        } ?: throw IOException("Could not open input stream for URI: $uri")

        val dateRange = if (startDate != null && endDate != null) {
            Pair(startDate!!, endDate!!)
        } else {
            null
        }

        return ImportPreview(
            meetingsCount = meetingsCount,
            hasPreferences = false,
            dateRange = dateRange,
            tags = allTags.toList(),
            format = ExportImportRepository.ExportFormat.CSV,
            isValidFormat = isValidFormat,
            potentialIssues = issues
        )
    }

    /**
     * Preview a Calendar import file.
     */
    private suspend fun previewCalendarFile(uri: Uri): ImportPreview {
        // This is a placeholder implementation for ICS calendar preview
        return ImportPreview(
            meetingsCount = 0,
            hasPreferences = false,
            format = ExportImportRepository.ExportFormat.CSV,
            isValidFormat = false,
            potentialIssues = listOf("Calendar import preview not currently implemented")
        )
    }

    /**
     * Escape special characters for CSV.
     */
    private fun escapeForCsv(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }

    /**
     * Get the filename from a URI.
     */
    private fun getFileName(uri: Uri): String {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex("_display_name")
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return uri.lastPathSegment ?: "unknown"
    }

    /**
     * Get the app version.
     */
    private fun getAppVersion(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName ?: "1.0"
    }

    /**
     * Creates a data repository interface extension to define PDF export options
     */
    data class PdfExportOptions(
        val enableCompression: Boolean = true,
        val compressionLevel: Int = 9, // 1-9, with 9 being maximum compression
        val includePageNumbers: Boolean = true,
        val pagesPerSheet: Int = 1,  // Options for printing multiple logical pages per physical page
        val fontSizeMultiplier: Float = 1.0f // For accessibility adjustments
    )
}
