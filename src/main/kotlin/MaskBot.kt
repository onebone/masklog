package me.onebone.masklog

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.onebone.masklog.telegram.StocksCommand
import me.onebone.masklog.telegram.RegisterCommand
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.coroutines.CoroutineContext

class MaskBot (
	private val token: String
): TelegramLongPollingCommandBot(), CoroutineScope {
	private val data: MutableList<RegisteredLocation>
	private val lastStoreData: MutableList<Store>
	private val queues: MutableList<Queue> = mutableListOf()

	private val job = Job()
	override val coroutineContext: CoroutineContext
		get() = Dispatchers.Default + job

	init {
		println("Starting bot with username: @${this.me.userName}")

		val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

		val dataFile = File("data.json")
		if(!dataFile.exists()) {
			this.data = mutableListOf()
		}else{
			val type = Types.newParameterizedType(MutableList::class.java, RegisteredLocation::class.java)
			val adapter = moshi.adapter<MutableList<RegisteredLocation>>(type)

			this.data = adapter.fromJson(dataFile.readText())!!
		}

		val saveFolder = File("saves")
		if(!saveFolder.exists()) {
			saveFolder.mkdir()
		}

		val lastStoresFolder = File("stores.json")
		if(!lastStoresFolder.exists()) {
			this.lastStoreData = mutableListOf()
		}else{
			val adapter = moshi.adapter<MutableList<Store>>(Types.newParameterizedType(MutableList::class.java, Store::class.java))
			this.lastStoreData = adapter.fromJson(lastStoresFolder.readText())!!
		}

		register(RegisterCommand(this))
		register(StocksCommand(this))
		registerDefaultAction { sender, message ->
			sender.execute(SendMessage(message.chatId, "Use /register command").apply {
				replyToMessageId = message.messageId
			})
		}

		Timer().apply {
			scheduleAtFixedRate(timerTask {
				launch {
					fetchApi()
				}
			}, 0, 1000 * 60 * 60) // 60 min
		}

		Timer().apply {
			scheduleAtFixedRate(timerTask {
				launch {
					processQueues()
				}
			}, 1000 * 5, 1000 * 5)
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

	fun hasLocation(chatId: Long, location: String): Boolean {
		data.forEach {
			if(it.location == location) {
				return it.users.find { user -> user.chatId == chatId } != null
			}
		}

		return false
	}

	private fun save() {
		val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
		val type = Types.newParameterizedType(List::class.java, RegisteredLocation::class.java)
		val adapter = moshi.adapter<List<RegisteredLocation>>(type)

		val json = adapter.toJson(this.data.toList())
		val dataFile = File("data.json")
		dataFile.writeText(json)
	}

	private fun saveStores() {
		val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
		val adapter = moshi.adapter<List<Store>>(Types.newParameterizedType(List::class.java, Store::class.java))

		val dataFile = File("stores.json")
		dataFile.writeText(adapter.toJson(this.lastStoreData.toList()))
	}

	fun addQueue(queue: Queue) {
		this.queues.add(queue)
	}

	private suspend fun processQueues() = coroutineScope {
		this@MaskBot.queues.forEach {
			launch {
				try {
					val list = this@MaskBot.fetchLocation(it.location)
					it.callback.invoke(list)
				}catch(e: Exception) {
					e.printStackTrace()

					it.errorCallback?.invoke(e)
				}
			}
		}

		this@MaskBot.queues.clear()
	}

	private suspend fun fetchApi(save: Boolean = true) = coroutineScope {
		this@MaskBot.data.forEach { loc ->
			launch {
				try{
					val stores = mutableListOf<Store>()
					this@MaskBot.fetchLocation(loc.location).forEach {
						 stores.add(it)
					}

					if(stores.size > 0) {
						data class UpdatedStore(val last: Store, val current: Store)

						val updated = mutableListOf<UpdatedStore>()
						lastStoreData.forEach { last ->
							stores.find {it.code == last.code }?.let { store ->
								if (store.remainStat != last.remainStat) {
									updated.add(UpdatedStore(last, store))
								}
							}
						}

						if(updated.isNotEmpty()) {
							val plenty = stores.filter { it.remainStat == REMAIN_PLENTY }.size
							val some = stores.filter { it.remainStat == REMAIN_SOME }.size
							val few = stores.filter { it.remainStat == REMAIN_FEW }.size
							val empty = stores.filter { it.remainStat == REMAIN_EMPTY }.size
							val ceased = stores.filter { it.remainStat == REMAIN_BREAK }.size
							val updatedEmpty = updated.filter { it.current.remainStat == REMAIN_EMPTY && it.last.remainStat != REMAIN_EMPTY }.size

							val message = """${loc.location}의 판매소 재고량 변화(${updated.size} 곳 변경):
								충분함(100개~): ${plenty}곳
								꽤 있음(30~99개): ${some}곳
								조금 있음(2~29개): ${few}곳
								없음(0~1개): ${empty}곳
								판매 중지: ${ceased}곳
								
								최근에 재고가 없어진 곳: ${updatedEmpty}곳
							""".trimIndent()

							loc.users.forEach {
								this@MaskBot.execute(SendMessage(it.chatId, message))
							}
						}
					}

					stores.forEach(this@MaskBot::updateLastStoreData)
					this@MaskBot.saveStores()
				}catch(e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	private fun updateLastStoreData(store: Store) {
		val index = lastStoreData.indexOfFirst { store.code == it.code }
		if(index == -1) {
			lastStoreData.add(store)
		}else{
			lastStoreData[index] = store
		}
	}

	private  fun fetchLocation(query: String): List<Store> {
		val text = URL("$API_URI?address=${URLEncoder.encode(query, "UTF-8")}").readText()
		val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
		val response = moshi.adapter<JsonResponse>(Types.newParameterizedType(JsonResponse::class.java, List::class.java, Store::class.java))
			.fromJson(text) ?: return listOf()

		return response.stores
	}

	override fun processNonCommandUpdate(update: Update?) {
	}

	override fun getBotToken(): String = token

	override fun getBotUsername(): String {
		return "kttest"
	}
}
