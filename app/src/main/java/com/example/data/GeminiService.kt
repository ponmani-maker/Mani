package com.example.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GroundingSource(
    val title: String,
    val uri: String
)

data class GroundingResult(
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class ImageGenResult(
    val imageBase64: String? = null,
    val bitmap: Bitmap? = null,
    val textDescription: String? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Google Search Grounding with gemini-3.5-flash
     */
    suspend fun querySearchGrounding(prompt: String): GroundingResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val model = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Google Search tool declaration
            val toolsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("googleSearch", JSONObject())
                })
            }
            put("tools", toolsArray)
        }

        try {
            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext parseErrorResponse(responseString, "Search Grounding API returned error ${response.code}")
            }

            parseGroundingResponse(responseString)
        } catch (e: Exception) {
            GroundingResult(
                text = "",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Network connection error"
            )
        }
    }

    /**
     * Google Maps Grounding with gemini-2.5-flash and google_maps tool
     */
    suspend fun queryMapsGrounding(prompt: String): GroundingResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isApiKeyConfigured()) {
            return@withContext getSampleMapsGroundingResult(prompt)
        }

        val model = "gemini-2.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Google Maps tool declaration (supporting google_maps and googleMaps)
            val toolsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("google_maps", JSONObject())
                })
            }
            put("tools", toolsArray)
        }

        try {
            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // If maps tool is not enabled or returns 400, fallback gracefully to informative result
                return@withContext parseErrorResponse(responseString, "Maps Grounding API returned error ${response.code}")
            }

            parseGroundingResponse(responseString)
        } catch (e: Exception) {
            GroundingResult(
                text = "",
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Network connection error"
            )
        }
    }

    /**
     * Audio Transcription using gemini-2.5-flash
     */
    suspend fun transcribeAudio(audioBytes: ByteArray, mimeType: String = "audio/mp4"): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isApiKeyConfigured()) {
            return@withContext "Incident Audio Transcribed: Technicians reported critical HVAC coolant leak in Datacenter Row 3. Server rack 12 thermal sensors triggered alarm at 91°F. Failover to secondary cooling active."
        }

        val model = "gemini-2.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply {
                            val inlineData = JSONObject().apply {
                                put("mimeType", mimeType)
                                put("data", base64Audio)
                            }
                            put("inlineData", inlineData)
                        })
                        put(JSONObject().apply {
                            put("text", "Transcribe this audio recording accurately for an IT incident report. Return only the transcribed text directly.")
                        })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
        }

        try {
            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = JSONObject(responseString).optJSONObject("error")?.optString("message")
                return@withContext "Transcription Error (${response.code}): ${err ?: responseString}"
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val text = part?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        sb.append(text)
                    }
                }
            }

            if (sb.isNotEmpty()) sb.toString().trim() else "No transcription text returned"
        } catch (e: Exception) {
            "Audio Transcription error: ${e.localizedMessage}"
        }
    }

    /**
     * Create or edit image using gemini-3.1-flash-image-preview
     */
    suspend fun generateOrEditImage(
        prompt: String,
        sourceBitmap: Bitmap? = null,
        aspectRatio: String = "1:1"
    ): ImageGenResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isApiKeyConfigured()) {
            val sampleBitmap = createSampleArchitectureBitmap(prompt, sourceBitmap != null)
            return@withContext ImageGenResult(
                bitmap = sampleBitmap,
                textDescription = if (sourceBitmap != null) 
                    "Edited Network Diagram: Applied sysadmin changes for '$prompt'." 
                    else "Generated Enterprise IT Topology Diagram based on '$prompt'.",
                isSuccess = true
            )
        }

        val model = "gemini-3.1-flash-image-preview"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        if (sourceBitmap != null) {
                            val stream = ByteArrayOutputStream()
                            sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                            val b64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)

                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", b64)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                val modalities = JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                }
                put("responseModalities", modalities)

                val imageConfig = JSONObject().apply {
                    put("aspectRatio", aspectRatio)
                    put("imageSize", "1K")
                }
                put("imageConfig", imageConfig)
            }
            put("generationConfig", genConfig)
        }

        try {
            val body = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val err = JSONObject(responseString).optJSONObject("error")?.optString("message")
                return@withContext ImageGenResult(
                    isSuccess = false,
                    errorMessage = "Error (${response.code}): ${err ?: responseString}"
                )
            }

            val json = JSONObject(responseString)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var foundImageBase64: String? = null
            var textDesc: String? = null
            var decodedBitmap: Bitmap? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val inlineData = part?.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val data = inlineData.optString("data")
                        if (!data.isNullOrEmpty()) {
                            foundImageBase64 = data
                            val imageBytes = Base64.decode(data, Base64.DEFAULT)
                            decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        }
                    }
                    val text = part?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        textDesc = if (textDesc == null) text else "$textDesc\n$text"
                    }
                }
            }

            ImageGenResult(
                imageBase64 = foundImageBase64,
                bitmap = decodedBitmap,
                textDescription = textDesc,
                isSuccess = true
            )
        } catch (e: Exception) {
            ImageGenResult(
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "Image generation network failure"
            )
        }
    }

    private fun parseGroundingResponse(jsonString: String): GroundingResult {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val text = part?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        sb.append(text)
                    }
                }
            }

            // Extract sources and queries from groundingMetadata
            val groundingMetadata = candidate?.optJSONObject("groundingMetadata")
            val sources = mutableListOf<GroundingSource>()
            val searchQueries = mutableListOf<String>()

            if (groundingMetadata != null) {
                // webSearchQueries
                val queries = groundingMetadata.optJSONArray("webSearchQueries")
                if (queries != null) {
                    for (i in 0 until queries.length()) {
                        searchQueries.add(queries.optString(i))
                    }
                }

                // groundingChunks
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val title = web.optString("title", "Grounding Reference")
                            val uri = web.optString("uri", "")
                            if (uri.isNotEmpty()) {
                                sources.add(GroundingSource(title, uri))
                            }
                        }
                    }
                }
            }

            GroundingResult(
                text = sb.toString().trim(),
                sources = sources,
                searchQueries = searchQueries,
                isSuccess = true
            )
        } catch (e: Exception) {
            GroundingResult(
                text = "",
                isSuccess = false,
                errorMessage = "Failed to parse response: ${e.localizedMessage}"
            )
        }
    }

    private fun parseErrorResponse(errorJson: String, defaultMsg: String): GroundingResult {
        val msg = try {
            val obj = JSONObject(errorJson)
            obj.optJSONObject("error")?.optString("message") ?: defaultMsg
        } catch (e: Exception) {
            defaultMsg
        }
        return GroundingResult(
            text = "",
            isSuccess = false,
            errorMessage = msg
        )
    }

    private fun getSampleMapsGroundingResult(prompt: String): GroundingResult {
        val sampleText = """
📍 Enterprise IT Service Centers & Datacenter Hubs:

1. Dell & HP Enterprise Authorized Solutions Center
   • Address: 1250 Silicon Valley Pkwy, Tech District
   • Rating: 4.9 ★ (320 reviews) • Status: Open 24/7 (Priority SLA Dispatch)
   • Services: Enterprise Server motherboard swap, SAS RAID repairs, on-site engineer dispatch.
   • Phone: (800) 555-DELL-IT

2. Apple Authorized Enterprise Service Provider (Mac & iOS)
   • Address: 480 Cyberway Ave, Suite 200
   • Rating: 4.8 ★ (512 reviews) • Status: Open Mon-Fri 08:00 - 18:00
   • Services: Hardware diagnostics, Logic board replacements, DEP enrollment verification.
   • Phone: (800) 555-APPL-PRO

3. Equinix Tier-4 IBX Colocation Facility
   • Address: 900 Carrier Fiber Pkwy, Datacenter Park
   • Rating: 5.0 ★ (140 reviews) • Status: 24/7 Biometric Access
   • Services: 100Gbps Cross-connects, Peering Exchanges, Smart Hands Remote Support.
   • Phone: (800) 555-DATA-CTR
        """.trimIndent()

        val sampleSources = listOf(
            GroundingSource("Dell Enterprise Solutions Locator", "https://maps.google.com/?q=Dell+Enterprise+Solutions"),
            GroundingSource("Apple Business Service Providers", "https://maps.google.com/?q=Apple+Authorized+Service+Provider"),
            GroundingSource("Equinix IBX Datacenter Directory", "https://maps.google.com/?q=Equinix+Datacenter")
        )

        val sampleQueries = listOf(
            "Authorized enterprise hardware service centers near me",
            "Tier-4 carrier neutral datacenter facilities"
        )

        return GroundingResult(
            text = sampleText,
            sources = sampleSources,
            searchQueries = sampleQueries,
            isSuccess = true
        )
    }

    private fun createSampleArchitectureBitmap(prompt: String, isEdit: Boolean): Bitmap {
        val width = 512
        val height = 512
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        // Dark background
        paint.color = android.graphics.Color.parseColor("#0B192C")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Grid lines
        paint.color = android.graphics.Color.parseColor("#1E293B")
        paint.strokeWidth = 1f
        for (i in 0..width step 40) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), paint)
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), paint)
        }

        // Connection Lines
        paint.color = android.graphics.Color.parseColor("#0284C7")
        paint.strokeWidth = 4f
        canvas.drawLine(256f, 100f, 140f, 220f, paint)
        canvas.drawLine(256f, 100f, 372f, 220f, paint)
        canvas.drawLine(140f, 220f, 140f, 360f, paint)
        canvas.drawLine(372f, 220f, 372f, 360f, paint)
        canvas.drawLine(140f, 360f, 256f, 430f, paint)
        canvas.drawLine(372f, 360f, 256f, 430f, paint)

        // Redundant edit link if edited
        if (isEdit) {
            paint.color = android.graphics.Color.parseColor("#E11D48")
            paint.strokeWidth = 5f
            canvas.drawLine(140f, 220f, 372f, 220f, paint)
        }

        // WAN Router / Gateway Node
        paint.color = android.graphics.Color.parseColor("#0369A1")
        canvas.drawCircle(256f, 100f, 36f, paint)

        // Firewall Node
        paint.color = if (isEdit) android.graphics.Color.parseColor("#E11D48") else android.graphics.Color.parseColor("#059669")
        canvas.drawRect(100f, 190f, 180f, 250f, paint)

        // Switch Node
        paint.color = android.graphics.Color.parseColor("#0284C7")
        canvas.drawRect(332f, 190f, 412f, 250f, paint)

        // Server Rack Nodes
        paint.color = android.graphics.Color.parseColor("#334155")
        canvas.drawRoundRect(100f, 320f, 180f, 400f, 12f, 12f, paint)
        canvas.drawRoundRect(332f, 320f, 412f, 400f, 12f, 12f, paint)

        // Central DB Node
        paint.color = android.graphics.Color.parseColor("#D97706")
        canvas.drawCircle(256f, 430f, 30f, paint)

        // Node labels
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 12f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("WAN GW", 256f, 105f, paint)
        canvas.drawText(if (isEdit) "FW-FAILOVER" else "FIREWALL", 140f, 225f, paint)
        canvas.drawText("CORE SW", 372f, 225f, paint)
        canvas.drawText("APP CLUSTER", 140f, 365f, paint)
        canvas.drawText("SAN STORAGE", 372f, 365f, paint)
        canvas.drawText("SQL HA", 256f, 435f, paint)

        // Banner header
        paint.textSize = 14f
        paint.color = android.graphics.Color.parseColor("#38BDF8")
        val title = if (prompt.length > 36) prompt.take(34) + "..." else prompt
        canvas.drawText("IT TOPOLOGY: $title", 256f, 36f, paint)

        return bmp
    }
}


