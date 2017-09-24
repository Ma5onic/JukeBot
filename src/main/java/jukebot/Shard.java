package jukebot;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import jukebot.utils.Bot;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

import static jukebot.utils.Bot.LOG;

public class Shard {

    public JDA jda;

    Shard(int shardId, int totalShards, String token) throws Exception {
        final JDABuilder jdab = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setAutoReconnect(true)
                .setReconnectQueue(Bot.reconnectQueue)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .addEventListener(new EventListener())
                .addEventListener(Bot.waiter)
                .useSharding(shardId, totalShards)
                .setGame(Game.of(Bot.defaultPrefix + "help | " + Bot.VERSION + " | [" + (shardId + 1) + "/" + totalShards + "]"));

        LOG.info("[" + (shardId + 1) + "/" + totalShards + "] Logging in...");
        this.jda = jdab.buildAsync();
    }
}
