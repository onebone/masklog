package me.onebone.masklog.telegram

import org.telegram.telegrambots.extensions.bots.commandbot.commands.DefaultBotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

class RegisterCommand: DefaultBotCommand(
	"register", "Registers notifier for mask entry"
) {
	override fun execute(sender: AbsSender, user: User, chat: Chat, messageId: Int, args: Array<out String>) {
		val reply = SendMessage(chat.id.toString(), "Hello World").apply {
			replyToMessageId = messageId
		}

		sender.execute(reply)
	}
}
