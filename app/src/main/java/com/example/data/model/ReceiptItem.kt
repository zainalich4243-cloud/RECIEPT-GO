package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class ReceiptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val unitPrice: Double = 0.0
) {
    val totalPrice: Double
        get() = quantity * unitPrice

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("quantity", quantity)
            put("unit", unit)
            put("unitPrice", unitPrice)
        }
    }

    companion object {
        fun fromJson(obj: JSONObject): ReceiptItem {
            return ReceiptItem(
                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                name = obj.optString("name", ""),
                quantity = obj.optDouble("quantity", 1.0),
                unit = obj.optString("unit", "Pcs"),
                unitPrice = obj.optDouble("unitPrice", 0.0)
            )
        }

        fun listToJsonString(items: List<ReceiptItem>): String {
            val jsonArray = JSONArray()
            items.forEach { jsonArray.put(it.toJson()) }
            return jsonArray.toString()
        }

        fun listFromJsonString(jsonStr: String): List<ReceiptItem> {
            val list = mutableListOf<ReceiptItem>()
            if (jsonStr.isBlank()) return list
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(fromJson(array.getJSONObject(i)))
                }
            } catch (_: Exception) {}
            return list
        }
    }
}
