package fr.raksrinana.channelpointsminer.miner.irc;

import fr.raksrinana.channelpointsminer.miner.api.passport.TwitchLogin;
import fr.raksrinana.channelpointsminer.miner.database.IDatabase;
import fr.raksrinana.channelpointsminer.miner.irc.listeners.TwitchIrcConnectionEventListener;
import fr.raksrinana.channelpointsminer.miner.irc.listeners.TwitchIrcMessageEventListener;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.twitch.TwitchSupport;
import static org.kitteh.irc.client.library.Client.Builder.Server.SecurityType.SECURE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TwitchIrcFactory{
	private static final String TWITCH_IRC_HOST = "irc.chat.twitch.tv";
    
    @NotNull
    public static TwitchIrcClientPrototype createPrototype(){
        return new TwitchIrcClientPrototype();
    }
	
	@NotNull
	public static TwitchIrcClient create(TwitchIrcClientPrototype prototype, TwitchLogin twitchLogin){
		return new TwitchIrcClient(prototype, twitchLogin);
	}
	
	@NotNull
	public static Client createClient(@NotNull TwitchLogin twitchLogin){
		return createClient(twitchLogin.getUsername(), "oauth:%s".formatted(twitchLogin.getAccessToken()));
	}
	
	@NotNull
	private static Client createClient(@NotNull String username, @Nullable String password){
		var client = Client.builder()
				.server()
				.host(TWITCH_IRC_HOST).port(443, SECURE)
				.password(password).then()
				.nick(username)
				.build();
		TwitchSupport.addSupport(client);
		return client;
	}
	
	@NotNull
	public static TwitchIrcConnectionEventListener createConnectionListener(@NotNull String accountName){
		return new TwitchIrcConnectionEventListener(accountName);
	}
	
	@NotNull
    public static TwitchIrcMessageEventListener createMessageListener(@NotNull String accountName, IDatabase database){
	    return new TwitchIrcMessageEventListener(database, accountName);
    }
	
}
