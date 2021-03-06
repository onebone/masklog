package me.onebone.masklog

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.io.FileOutputStream

fun main() {
	if(!File("config.json").exists()) {
		Object::class.java.getResourceAsStream("/config.json").use {input ->
			FileOutputStream(File("config.json")).use {
				input.copyTo(it)
			}
		}
	}

	val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
	val adapter = moshi.adapter(Config::class.java)
	val config = adapter.fromJson(File("config.json").bufferedReader().readText())!!

	ApiContextInitializer.init()

	val api = TelegramBotsApi()
	try{
		api.registerBot(MaskBot(config.token))
	}catch(e: TelegramApiException) {
		e.printStackTrace()
	}
}

data class Config (
	val token: String
)
