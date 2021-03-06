package jukebot.utils


import jukebot.Database
import jukebot.JukeBot
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.future.await
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.coroutines.experimental.EmptyCoroutineContext

class Helpers {

    companion object {

        private val executor = Executors.newSingleThreadExecutor { Thread(it, "JukeBot-Helper") }
        public val monitor = Executors.newSingleThreadScheduledExecutor { Thread(it, "JukeBot-Pledge-Monitor") }
        private val timer = Executors.newSingleThreadScheduledExecutor { Thread(it, "JukeBot-Timer") }

        fun parseNumber(num: String?, def: Int): Int {
            return if (num == null) {
                def
            } else {
                try {
                    num.toInt()
                } catch (e: NumberFormatException) {
                    def
                }
            }
        }

        public fun schedule(task: Runnable, delay: Int, unit: TimeUnit) {
            timer.schedule(task, delay.toLong(), unit)
        }

        public fun schedule(task: () -> Unit, delay: Int, unit: TimeUnit) {
            timer.schedule(task, delay.toLong(), unit)
        }

        fun readFile(path: String, def: String): String {
            try {
                FileReader(path).use { file -> BufferedReader(file).use { reader -> return reader.lines().collect(Collectors.joining("\n")) } }
            } catch (e: IOException) {
                return def
            }
        }

        fun monitorPledges() {
            CoroutineScope(EmptyCoroutineContext).async {
                JukeBot.LOG.info("Checking pledges...")

                val future = CompletableFuture<List<PatreonUser>>()
                JukeBot.patreonApi.fetchPledgesOfCampaign("750822", future)

                val users = future.await()

                if (users.isEmpty()) {
                    return@async JukeBot.LOG.warn("Scheduled pledge clean failed: No users to check")
                }

                Database.getDonorIds().forEach { id ->
                    val pledge = users.firstOrNull { it.discordId != null && it.discordId.toLong() == id }

                    if (pledge == null || pledge.isDeclined) {
                        Database.setTier(id, 0)
                        JukeBot.LOG.info("Removed $id from donors")
                    }
                }
            }
        }

    }

}
