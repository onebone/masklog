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
) {
	fun hasStock(): Boolean {
		return remainStat != REMAIN_EMPTY
	}

	override fun toString(): String {
		val typeString = when (type) {
			STORE_TYPE_PHARMACY -> "약국"
			STORE_TYPE_POST_OFFICE -> "우체국"
			STORE_TYPE_NONGHYUP -> "농협"
			else -> "매장 종류 불명"
		}

		val remainString = when(remainStat) {
			REMAIN_EMPTY -> "재고 없음"
			REMAIN_FEW -> "조금 있음 (2~29개)"
			REMAIN_SOME -> "꽤 있음 (30~99개)"
			REMAIN_PLENTY -> "충분함 (100개~)"
			else -> "재고 불명"
		}

		return "[$typeString] $name ($addr): $remainString"
	}
}

data class JsonResponse (
	val address: String,
	val count: Int,
	val stores: List<Store>
)

data class Queue (
	val location: String,
	val callback: (stores: List<Store>) -> Unit
)
