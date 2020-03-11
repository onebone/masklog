package me.onebone.masklog

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.onebone.masklog.telegram.RegisterCommand
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File

class MaskBot (
	private val token: String
): TelegramLongPollingCommandBot() {
	private val data: MutableList<RegisteredLocation>

	init {
		println("Starting bot with username: @${this.me.userName}")

		val dataFile = File("data.json")
		if(!dataFile.exists()) {
			this.data = mutableListOf()
		}else{
			val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
			val type = Types.newParameterizedType(MutableList::class.java, RegisteredLocation::class.java)
			val adapter = moshi.adapter<MutableList<RegisteredLocation>>(type)

			this.data = adapter.fromJson(dataFile.readText())!!
		}

		register(RegisterCommand(this))
		registerDefaultAction { sender, message ->
			sender.execute(SendMessage(message.chatId, "Use /register command").apply {
				replyToMessageId = message.messageId
			})
		}
	}

	fun registerLocation(location: String, userId: Int, chatId: Long): Boolean {
		val d = data.find { it.location == location }
		if(d == null) {
			this.data.add(RegisteredLocation(location, mutableListOf(Receiver(userId, chatId))))
			this.save()
			return true
		}

		if(d.users.find { it.chatId == chatId } == null) {
			d.users.add(Receiver(userId, chatId))
			this.save()
			return true
		}

		return false
	}

	private fun save() {
		val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
		val type = Types.newParameterizedType(MutableList::class.java, RegisteredLocation::class.java)
		val adapter = moshi.adapter<MutableList<RegisteredLocation>>(type)

		val json = adapter.toJson(this.data)
		val dataFile = File("data.json")
		dataFile.writeText(json)
	}

	override fun processNonCommandUpdate(update: Update?) {
	}

	override fun getBotToken(): String = token

	override fun getBotUsername(): String {
		return "kttest"
	}
}
