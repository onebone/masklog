package me.onebone.masklog

import com.squareup.moshi.Json
import java.util.*

data class RegisteredLocation (
	val location: String,
	val users: MutableList<Receiver>
)

data class Receiver (
	val userId: Int,
	val chatId: Long
)

const val STORE_TYPE_PHARMACY = "01"
const val STORE_TYPE_POST_OFFICE = "02"
const val STORE_TYPE_NONGHYUP = "03"

// https://app.swaggerhub.com/apis-docs/Promptech/public-mask-info/20200307-oas3#/Store
data class Store (
	val code: String,
	val name: String,
	val addr: String,
	val type: String,
	val lat: Float,
	val lng: Float,
	@Json(name="created_at") val createdAt: Date,
	@Json(name="remain_stat") val remainStat: Date,
	@Json(name="stock_at") val stockAt: Date
)
