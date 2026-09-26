package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var isRecording = false

    fun startRecording(): Boolean {
        try {
            stopRecording() // ensure any previous recorder is cleaned up

            val outputDir = context.cacheDir
            currentOutputFile = File.createTempFile("it_voice_note_", ".m4a", outputDir)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(currentOutputFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording: ${e.message}")
            isRecording = false
            mediaRecorder?.release()
            mediaRecorder = null
            return false
        }
    }

    fun stopRecording(): ByteArray? {
        if (!isRecording && mediaRecorder == null) {
            return null
        }
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping recorder: ${e.message}")
        } finally {
            mediaRecorder = null
            isRecording = false
        }

        val file = currentOutputFile
        return if (file != null && file.exists() && file.length() > 0) {
            val bytes = file.readBytes()
            file.delete()
            currentOutputFile = null
            bytes
        } else {
            null
        }
    }

    fun isRecording(): Boolean = isRecording

    /**
     * Fallback demo audio: creates a valid, lightweight WAV audio file containing an audio sine wave
     * to test audio transcription in environments where physical mic hardware is unavailable.
     */
    fun createSampleWavAudio(durationSeconds: Int = 2): ByteArray {
        val sampleRate = 16000
        val numSamples = sampleRate * durationSeconds
        val pcmData = ShortArray(numSamples)
        val freq = 440.0 // A4 note

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / freq)
            pcmData[i] = (Math.sin(angle) * Short.MAX_VALUE * 0.5).toInt().toShort()
        }

        val byteRate = sampleRate * 2
        val dataSize = numSamples * 2
        val totalSize = 36 + dataSize

        val header = ByteArray(44)
        // RIFF header
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalSize and 0xff).toByte()
        header[5] = ((totalSize shr 8) and 0xff).toByte()
        header[6] = ((totalSize shr 16) and 0xff).toByte()
        header[7] = ((totalSize shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        // fmt chunk
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // 16 for PCM
        header[20] = 1; header[21] = 0 // format 1 = PCM
        header[22] = 1; header[23] = 0 // 1 channel
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // block align = 2 bytes
        header[34] = 16; header[35] = 0 // bits per sample = 16
        // data chunk
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (dataSize and 0xff).toByte()
        header[41] = ((dataSize shr 8) and 0xff).toByte()
        header[42] = ((dataSize shr 16) and 0xff).toByte()
        header[43] = ((dataSize shr 24) and 0xff).toByte()

        val pcmBytes = ByteArray(dataSize)
        for (i in 0 until numSamples) {
            val sample = pcmData[i]
            pcmBytes[i * 2] = (sample.toInt() and 0xff).toByte()
            pcmBytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xff).toByte()
        }

        return header + pcmBytes
    }
}
