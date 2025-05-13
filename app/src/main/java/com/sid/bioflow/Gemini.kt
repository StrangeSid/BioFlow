package com.sid.bioflow
import com.google.ai.client.generativeai.GenerativeModel

fun isNumeric(input: String): Boolean {
    if (input.isEmpty()) {
        return false
    }
    return input.toDoubleOrNull() != null
}

suspend fun gemini(input:String): String? {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
        val rawResponse = generativeModel.generateContent(input)
        val response = rawResponse.text
        return response?:"No response"
    }

suspend fun getScore(input:String): Double {
    val score = gemini(
        input = "I will provide some data in the following format: \n" +

                "{entry_timestamp=yyyy-mm-ddThh:mm:ss.ms+timeZone, height_m=<height_meter_int>, " +
                "hydration_ml=<water_drank_int_milliliter>, " +
                "sleep_duration_hours=<hours_slept_hours_float>, " +
                "weight_kg=<human_weight_kilograms>, " +
                "note=<string_extra_logging_note>}\n\n" +

                "What you will do:\n" +
                "1. If the data is not given, then do not take is into account, not all the data points will always be provided.\n" +
                "2. You must only return a value that is a floating point number which is satisfies 0 ≤ x ≤ 100 where the returned number is x\n" +
                "3. The number that you return is a health 'score' based on the provided data, where 0 is bad for health and 100 is the best\n" +
                "4. You must only return/reply with the number. NOTHING ELSE.\n" +
                "5. Do not respond with any other type of data. I only require the score, the given response WILL ONLY BE A POSITIVE NUMBER LESSER THAN OF EQUAL TO 100\n" +
                "\n If you understand all of this, then respond now with the number from the given data:\n\n" +
                input
    )?:"No response"

    println("bro the score is: '$score' btw")
    while(!isNumeric(score)) {
        getScore("$input \n\n ONLY GIVE THE SCORE AND NOTHING ELSE")
        println("bro gemini got beef\nthis is what is got\nScore: $score")
    }
    println("bro it worked perfectly!!!\nScore: $score")
    return score.toDouble().coerceIn(0.0, 100.0)
}

