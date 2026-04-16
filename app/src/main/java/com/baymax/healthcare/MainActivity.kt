package com.baymax.healthcare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private var isListening = false
    private var isSpeaking = false

    // Medical database
    private val medicalResponses = mapOf(
        "headache" to MedicalResponse(
            "I understand you have a headache. On a scale of 1 to 10, how severe is it? Common causes include dehydration, stress, or eye strain. Try drinking water, resting in a dark room, and applying a cool compress. If the headache is severe or accompanied by vision changes, please see a doctor.",
            listOf("Hydration", "Rest in dark room", "Cool compress"),
            false
        ),
        "fever" to MedicalResponse(
            "You have a fever. Your body is fighting an infection. Rest is very important. Drink plenty of fluids like water or herbal tea. You can use a cool cloth on your forehead. If your fever goes above 103°F or 39.5°C or lasts more than 3 days, please consult a doctor.",
            listOf("Rest", "Hydration", "Cool compress"),
            false
        ),
        "cough" to MedicalResponse(
            "I hear you have a cough. Honey and warm tea can help soothe your throat. Stay hydrated and rest your voice. If you have difficulty breathing, chest pain, or coughing up blood, seek medical attention immediately.",
            listOf("Honey tea", "Rest", "Humidifier"),
            false
        ),
        "stomach pain" to MedicalResponse(
            "Your stomach is bothering you. Try the BRAT diet - bananas, rice, applesauce, toast. Sip clear liquids. Avoid spicy or fatty foods. If you have severe pain, blood in vomit or stool, or high fever, please see a doctor right away.",
            listOf("BRAT diet", "Clear liquids", "Rest"),
            false
        ),
        "cold" to MedicalResponse(
            "You have a cold. Rest and hydration are key. Drink warm fluids, use saline nasal spray, and get plenty of sleep. Over-the-counter cold medications may help with symptoms. If symptoms worsen or last more than 10 days, consult a doctor.",
            listOf("Rest", "Hydration", "Warm fluids"),
            false
        ),
        "flu" to MedicalResponse(
            "You may have the flu. This requires serious rest and hydration. Stay home to avoid spreading it. Drink lots of fluids, monitor your temperature, and consider antiviral medication if prescribed. Seek immediate care if you have difficulty breathing or chest pain.",
            listOf("Rest", "Hydration", "Isolation"),
            false
        ),
        "allergies" to MedicalResponse(
            "You're experiencing allergies. Try to identify and avoid triggers. Over-the-counter antihistamines may help. Keep windows closed during high pollen days, use air purifiers, and consider nasal saline rinses. See an allergist if symptoms are severe.",
            listOf("Avoid triggers", "Antihistamines", "Air purifier"),
            false
        ),
        "back pain" to MedicalResponse(
            "You have back pain. Gentle stretching and proper posture can help. Apply heat or cold packs, avoid heavy lifting, and consider over-the-counter pain relievers. If pain is severe, radiates down legs, or includes numbness, seek medical care.",
            listOf("Gentle stretching", "Heat/cold packs", "Proper posture"),
            false
        ),
        "sore throat" to MedicalResponse(
            "Your throat hurts. Gargle with warm salt water, drink warm tea with honey, and use throat lozenges. Rest your voice and stay hydrated. If you have difficulty swallowing, white spots on tonsils, or high fever, please see a doctor.",
            listOf("Salt water gargle", "Warm tea with honey", "Throat lozenges"),
            false
        ),
        "nausea" to MedicalResponse(
            "You feel nauseous. Try small sips of clear fluids, eat bland foods like crackers, and avoid strong smells. Ginger tea or peppermint may help. If you have severe vomiting, dehydration signs, or blood in vomit, seek emergency care.",
            listOf("Clear fluids", "Bland foods", "Ginger tea"),
            false
        ),
        "dizziness" to MedicalResponse(
            "You're feeling dizzy. Sit or lie down immediately to prevent falls. Stay hydrated and avoid sudden movements. If dizziness is severe, accompanied by chest pain, confusion, or fainting, seek emergency medical attention.",
            listOf("Sit down", "Hydration", "Avoid sudden movements"),
            false
        ),
        "fatigue" to MedicalResponse(
            "You're feeling tired. Prioritize sleep - aim for 7-9 hours nightly. Maintain a consistent sleep schedule, exercise regularly, and manage stress. If fatigue is persistent, severe, or accompanied by other symptoms, consult a doctor.",
            listOf("Adequate sleep", "Regular exercise", "Stress management"),
            false
        ),
        "chest pain" to MedicalResponse(
            "This could be serious. Chest pain can indicate a heart emergency. I strongly recommend calling emergency services immediately or going to the nearest emergency room. Do not drive yourself. Time is critical in heart emergencies.",
            listOf("Call emergency services", "Go to ER", "Do not delay"),
            true
        ),
        "shortness of breath" to MedicalResponse(
            "Difficulty breathing requires immediate attention. This could be a serious respiratory or cardiac emergency. Please call emergency services or go to the nearest emergency room right away. Do not wait to see if it improves.",
            listOf("Call emergency services", "Go to ER", "Sit upright"),
            true
        ),
        "rash" to MedicalResponse(
            "You have a skin rash. Keep the area clean and dry, avoid scratching, and consider over-the-counter hydrocortisone cream. If rash is spreading rapidly, painful, blistering, or accompanied by fever, seek medical care promptly.",
            listOf("Keep clean and dry", "Avoid scratching", "Hydrocortisone cream"),
            false
        ),
        "toothache" to DentalResponse(
            "Your tooth hurts. Rinse with warm salt water, use over-the-counter pain relievers, and avoid very hot or cold foods. Apply clove oil to the area if available. See a dentist as soon as possible - tooth infections can become serious.",
            listOf("Salt water rinse", "Pain relievers", "See dentist"),
            false
        ),
        "ear pain" to MedicalResponse(
            "Your ear hurts. Apply a warm compress, avoid inserting objects in your ear, and consider over-the-counter pain relievers. If pain is severe, accompanied by fever, hearing loss, or discharge, see a doctor promptly.",
            listOf("Warm compress", "Pain relievers", "No objects in ear"),
            false
        ),
        "eye strain" to MedicalResponse(
            "Your eyes are strained. Follow the 20-20-20 rule: every 20 minutes, look at something 20 feet away for 20 seconds. Adjust screen brightness, use proper lighting, and consider artificial tears. If vision changes persist, see an eye doctor.",
            listOf("20-20-20 rule", "Adjust screen brightness", "Artificial tears"),
            false
        ),
        "stress" to MedicalResponse(
            "You're experiencing stress. Practice deep breathing, regular exercise, and adequate sleep. Consider meditation, yoga, or talking with someone you trust. If stress becomes overwhelming or affects daily functioning, consider professional help.",
            listOf("Deep breathing", "Exercise", "Adequate sleep"),
            false
        ),
        "insomnia" to MedicalResponse(
            "You're having trouble sleeping. Maintain a consistent sleep schedule, avoid screens before bed, and create a comfortable sleep environment. Avoid caffeine late in the day. If insomnia persists or affects daily life, consult a doctor.",
            listOf("Consistent schedule", "No screens before bed", "Comfortable environment"),
            false
        )
    )

    data class MedicalResponse(
        val response: String,
        val remedies: List<String>,
        val isEmergency: Boolean
    )

    data class DentalResponse(
        val response: String,
        val remedies: List<String>,
        val isEmergency: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set immersive full screen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        // Hide status bar and navigation bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)

        // Check and request permissions
        checkPermissions()

        // Initialize speech recognition
        initializeSpeechRecognition()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
        )

        if (!hasAllPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 100)
        }
    }

    private fun hasAllPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun initializeSpeechRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    updateBaymaxFace(true)
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    updateBaymaxFace(false)
                }

                override fun onError(error: Int) {
                    isListening = false
                    updateBaymaxFace(false)
                    restartListening()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0].toLowerCase(Locale.getDefault())
                        processUserSpeech(spokenText)
                    }
                    restartListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun restartListening() {
        if (!isSpeaking && !isListening) {
            startListening()
        }
    }

    private fun startListening() {
        try {
            speechRecognizer.startListening(recognizerIntent)
        } catch (e: Exception) {
            // Handle error, restart listening after delay
            Handler().postDelayed({ startListening() }, 1000)
        }
    }

    private fun updateBaymaxFace(isListening: Boolean) {
        // Update eye animations based on listening state
        val leftEye = findViewById<BaymaxEye>(R.id.leftEye)
        val rightEye = findViewById<BaymaxEye>(R.id.rightEye)
        
        if (isListening) {
            leftEye.startListening()
            rightEye.startListening()
        } else {
            leftEye.stopListening()
            rightEye.stopListening()
        }
    }

    private fun processUserSpeech(spokenText: String) {
        val symptomType = detectSymptom(spokenText)
        val medicalData = medicalResponses[symptomType] ?: medicalResponses["default"] ?: return

        val response = buildString {
            append("Hello. I am Baymax, your personal healthcare companion. ")
            append(medicalData.response)
            
            if (medicalData.isEmergency) {
                append(" This is a medical emergency. Please act immediately.")
            } else {
                append(" I cannot deactivate until I know you are satisfied with your care.")
            }
            
            append(" Please tell me more about how you're feeling.")
        }

        speak(response)
    }

    private fun detectSymptom(text: String): String {
        return when {
            text.contains("head") || text.contains("headache") || text.contains("migraine") -> "headache"
            text.contains("fever") || text.contains("hot") || text.contains("temperature") -> "fever"
            text.contains("cough") || text.contains("throat") && text.contains("sore") -> "sore throat"
            text.contains("cough") -> "cough"
            text.contains("stomach") || text.contains("belly") || text.contains("nausea") -> if (text.contains("nausea")) "nausea" else "stomach pain"
            text.contains("cold") -> "cold"
            text.contains("flu") -> "flu"
            text.contains("allerg") -> "allergies"
            text.contains("back") -> "back pain"
            text.contains("dizzy") -> "dizziness"
            text.contains("tired") || text.contains("fatigue") -> "fatigue"
            text.contains("chest") && text.contains("pain") -> "chest pain"
            text.contains("breath") || text.contains("breathing") -> "shortness of breath"
            text.contains("rash") || text.contains("skin") -> "rash"
            text.contains("tooth") || text.contains("dental") -> "toothache"
            text.contains("ear") -> "ear pain"
            text.contains("eye") || text.contains("vision") -> "eye strain"
            text.contains("stress") || text.contains("anxious") -> "stress"
            text.contains("sleep") || text.contains("insomnia") -> "insomnia"
            else -> "headache" // Default fallback
        }
    }

    private fun speak(text: String) {
        isSpeaking = true
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "baymax_speech")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
            textToSpeech.setSpeechRate(0.9f)
            textToSpeech.setPitch(1.0f)
            
            // Set gentle voice if available
            val voices = textToSpeech.voices
            val gentleVoice = voices?.find { 
                it.name.contains("female", ignoreCase = true) || 
                it.name.contains("samantha", ignoreCase = true) ||
                it.name.contains("google", ignoreCase = true)
            }
            gentleVoice?.let { textToSpeech.voice = it }

            // Welcome message
            Handler().postDelayed({
                speak("Hello. I am Baymax, your personal healthcare companion. I am here to help you with your health concerns. Please tell me how you are feeling today.")
                startListening()
            }, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        speechRecognizer.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            }
        }
    }
}
