package me.onebone.masklog

import com.squareup.moshi.Json
import java.util.Date

data class RegisteredLocation (
	val location: String,
	val users: MutableList<Receiver>
)

data class Receiver (
	val userId: Int,
	val chatId: Long
)

const val API_URI = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByAddr/json"

const val STORE_TYPE_PHARMACY = "01"
const val STORE_TYPE_POST_OFFICE = "02"
const val STORE_TYPE_NONGHYUP = "03"

// (green) 100 <= x
const val REMAIN_PLENTY = "plenty"
// (yellow) 30 <= x < 100
const val REMAIN_SOME = "some"
// (red) 2 <= x < 30
const val REMAIN_FEW = "few"
// (gray) 0 <= x < 2
const val REMAIN_EMPTY = "empty"

// https://app.swaggerhub.com/apis-docs/Promptech/public-mask-info/20200307-oas3#/Store
data class Store (
	val code: String,
	val name: String,
	val addr: String,
	val type: String,
	val lat: Float?,
	val lng: Float?,
	@Json(name="created_at") val createdAt: String?,
	@Json(name="remain_stat") val remainStat: String?,
	@Json(name="stock_at") val stockAt: String?
)

data class JsonResponse (
	val address: String,
	val count: Int,
	val stores: List<Store>
)

data class Queue (
	val location: String,
	val callback: (stores: List<Store>) -> Unit
)
