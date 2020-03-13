package me.onebone.masklog.telegram

import me.onebone.masklog.MaskBot
import me.onebone.masklog.Queue
import me.onebone.masklog.REMAIN_BREAK
import me.onebone.masklog.REMAIN_EMPTY
import me.onebone.masklog.REMAIN_FEW
import me.onebone.masklog.REMAIN_PLENTY
import me.onebone.masklog.REMAIN_SOME
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.bots.AbsSender

class StocksCommand(private val bot: MaskBot): ReplyableBotCommand(
	"stocks", "한 지역 마스크 매장의 재고를 바로 검색합니다."
) {
	override fun execute(sender: AbsSender, message: Message, args: Array<out String>): String? {
		if(args.isEmpty()) {
			return "명령어: /stocks <지역>"
		}

		val location = args.joinToString(" ")

		val replied = replyMessage(sender, message, "${location}를 대상으로 재고가 있는 매장을 검색하고 있습니다. 잠시만 기다려주세요...")
		this.bot.addQueue(Queue(location, callback={
			if(it.isEmpty()) {
				sender.execute(EditMessageText().apply {
					chatId = message.chat.id.toString()
					messageId = replied.messageId
					text = "주소가 잘못되었습니다. 지역의 입력은 시 단위로 입력될 수 없습니다.\n올바른 예시: 서울특별시 강남구"
				})
			}else{
				val availableStores = it.filter{ store -> store.hasStock() }
				if(availableStores.isEmpty()) {
					sender.execute(EditMessageText().apply {
						chatId = message.chat.id.toString()
						messageId = replied.messageId
						text = "${location}에는 마스크 재고가 있는 매장이 없습니다. 전체 매장: ${it.size}"
					})
				}else{
					sender.execute(EditMessageText().apply {
						chatId = message.chat.id.toString()
						messageId = replied.messageId

						val plenty = availableStores.filter { store -> store.remainStat == REMAIN_PLENTY }.size
						val some = availableStores.filter { store -> store.remainStat == REMAIN_SOME }.size
						val few = availableStores.filter { store -> store.remainStat == REMAIN_FEW }.size
						val empty = it.filter { store -> store.remainStat == REMAIN_EMPTY }.size
						val ceased = it.filter { store -> store.remainStat == REMAIN_BREAK }.size

						text = """${location}의 공적 마스크 매장 현황(전체 ${it.size}곳):
								충분함(100개~): ${plenty}곳
								꽤 있음(30~99개): ${some}곳
								조금 있음(2~29개): ${few}곳
								없음(0~1개): ${empty}곳
								
								판매 중지: ${ceased}곳
							""".trimIndent()
					})
				}
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
