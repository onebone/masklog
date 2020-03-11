package me.onebone.masklog.telegram

import me.onebone.masklog.MaskBot
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

class RegisterCommand(
	private val bot: MaskBot
): ReplyableBotCommand(
	"register", "마스크 재고 입점이 생겼을 때 알림을 받도록 등록합니다."
) {
	override fun execute(sender: AbsSender, message: Message, args: Array<out String>): String? {
		if(args.isEmpty()) {
			return "명령어: /register <알림을 받을 주소>"
		}

		val location = args.joinToString(" ").trim()
		return if(this.bot.registerLocation(location, message.from.id, message.chat.id)) {
			"주소(${location})가 등록되었습니다. 마스크의 재고에 변화가 있을 경우 이곳에 메시지를 보냅니다."
		}else{
			"이미 해당 주소를 등록했습니다."
		}
	}
}
