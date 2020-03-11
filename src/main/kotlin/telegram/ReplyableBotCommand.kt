package me.onebone.masklog.telegram

import org.telegram.telegrambots.extensions.bots.commandbot.commands.DefaultBotCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

abstract class ReplyableBotCommand(
	commandIdentifier: String, description: String
): DefaultBotCommand(commandIdentifier, description) {
	override fun processMessage(sender: AbsSender, message: Message, arguments: Array<out String>) {
		val reply = execute(sender, message, arguments) ?: return

		sender.execute(SendMessage(message.chat.id, reply).apply {
			replyToMessageId = message.messageId
		})
	}

	final override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, messageId: Int?, arguments: Array<out String>?) {
	}

	abstract fun execute(sender: AbsSender, message: Message, args: Array<out String>): String?
}
