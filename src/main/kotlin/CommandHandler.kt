package me.onebone.masklog

import me.onebone.masklog.telegram.RegisterCommand
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class CommandHandler(
	private val token: String
): TelegramLongPollingCommandBot() {
	init {
		//println("Starting bot with username: @${this.me.userName}")

		register(RegisterCommand())

		registerDefaultAction { sender, message ->
			sender.execute(SendMessage(message.chatId, "Use /register command").apply {
				replyToMessageId = message.messageId
			})
		}
	}

	override fun processNonCommandUpdate(update: Update?) {
	}

	override fun getBotToken(): String = token

	override fun getBotUsername(): String {
		return "kttest"
	}
}
