package me.onebone.masklog.telegram

import me.onebone.masklog.MaskBot
import me.onebone.masklog.Queue
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

class RegisterCommand(
	private val bot: MaskBot
): ReplyableBotCommand(
	"register", "마스크 재고 입점이 생겼을 때 알림을 받도록 등록합니다."
) {
	override fun execute(sender: AbsSender, message: Message, args: Array<out String>): String? {
		if(args.isEmpty()) {
			return "명령어: /register <알림을 받을 지역>"
		}

		val location = args.joinToString(" ").trim()

		if(this.bot.hasLocation(message.chat.id, location)) {
			return "이미 해당 주소를 등록했습니다."
		}

		val replied = replyMessage(sender, message, "등록 대기 중입니다. 잠시만 기다려주세요.")

		this.bot.addQueue(Queue(location, callback={
			if(it.isNotEmpty()) {
				if(this.bot.registerLocation(location, message.from.id, message.chat.id)) {
					sender.execute(EditMessageText().apply {
						chatId = message.chat.id.toString()
						messageId = replied.messageId
						text = "주소(${location})가 등록되었습니다. 해당 지역에는 ${it.size}개의 공적 마스크 판매소가 있습니다." +
								" 마스크의 재고에 변화가 있을 경우 이곳에 메시지를 보냅니다."
					})
				}
			}else{
				sender.execute(EditMessageText().apply {
					chatId = message.chat.id.toString()
					messageId = replied.messageId
					text = "해당 주소는 올바르지 않습니다. 시 단위의 입력은 사용할 수 없습니다.\n올바른 사용 예시: 서울특별시 강남구"
				})
			}
		}, errorCallback={
			sender.execute(EditMessageText().apply {
				chatId = message.chat.id.toString()
				messageId = replied.messageId
				text = "처리 중 오류가 발생하여 결과를 가져올 수 없습니다. 다시 시도해주세요."
			})
		}))

		return null
	}
}
