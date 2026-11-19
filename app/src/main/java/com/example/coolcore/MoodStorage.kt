package com.example.coolcore

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CoolRecord(val timestamp: Long, val emotionTag: String, val heatBefore: Int, val heatAfter: Int, val note: String) {
    fun formattedTime(): String = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

class MoodStorage(context: Context) {
    private val prefs = context.getSharedPreferences("coolcore_local_db", Context.MODE_PRIVATE)
    private val key = "records_json"

    fun loadRecords(): List<CoolRecord> {
        val list = mutableListOf<CoolRecord>()
        try {
            val arr = JSONArray(prefs.getString(key, "[]") ?: "[]")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(CoolRecord(o.getLong("t"), o.getString("tag"), o.getInt("before"), o.getInt("after"), o.optString("note", "")))
            }
        } catch (_: Exception) {}
        return list.sortedByDescending { it.timestamp }
    }

    fun saveRecord(record: CoolRecord) {
        val list = loadRecords().toMutableList().apply { add(0, record) }.take(50)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("t", it.timestamp); put("tag", it.emotionTag)
                put("before", it.heatBefore); put("after", it.heatAfter); put("note", it.note)
            })
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
