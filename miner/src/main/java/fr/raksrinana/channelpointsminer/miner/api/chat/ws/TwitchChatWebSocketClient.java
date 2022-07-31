package fr.raksrinana.channelpointsminer.miner.api.chat.ws;

import fr.raksrinana.channelpointsminer.miner.api.chat.ITwitchChatClient;
import fr.raksrinana.channelpointsminer.miner.api.passport.TwitchLogin;
import fr.raksrinana.channelpointsminer.miner.factory.TimeFactory;
import fr.raksrinana.channelpointsminer.miner.log.LogContext;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log4j2
public class TwitchChatWebSocketClient extends WebSocketClient implements ITwitchChatClient{
	@Getter
	private final Set<String> channels;
	private final Collection<ITwitchChatWebSocketListener> listeners;
	@Getter
	private final String uuid;
	private final TwitchLogin twitchLogin;
	
	@Getter
	private Instant lastHeartbeat;
	
	public TwitchChatWebSocketClient(@NotNull URI uri, @NotNull TwitchLogin twitchLogin){
		super(uri);
		this.twitchLogin = twitchLogin;
		uuid = UUID.randomUUID().toString();
		
		setConnectionLostTimeout(0);
		channels = new HashSet<>();
		listeners = new ConcurrentLinkedQueue<>();
		lastHeartbeat = Instant.EPOCH;
	}
	
	@Override
	public void onOpen(ServerHandshake serverHandshake){
		try(var ignored = LogContext.empty().withSocketId(uuid)){
			log.info("Chat WebSocket opened");
			onHeartbeat();
			sendMessage("CAP REQ :twitch.tv/tags twitch.tv/commands");
			send("PASS oauth:%s".formatted(twitchLogin.getAccessToken()));
			sendMessage("NICK %s".formatted(twitchLogin.getUsername().toLowerCase()));
		}
	}
	
	@Override
	public void onMessage(String messageStr){
		try(var logContext = LogContext.empty().withSocketId(uuid)){
			log.trace("Received Chat Websocket message: {}", messageStr.strip());
		}
		catch(Exception e){
			log.error("Failed to handle Chat WebSocket message {}", messageStr, e);
		}
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote){
		try(var ignored = LogContext.empty().withSocketId(uuid)){
			log.info("Chat WebSocket closed with code {}, from host {}, reason {}", code, remote, reason);
			listeners.forEach(l -> l.onWebSocketClosed(this, code, reason, remote));
		}
	}
	
	@Override
	public void onError(Exception e){
		log.error("Error from Chat WebSocket", e);
	}
	
	private void onHeartbeat(){
		lastHeartbeat = TimeFactory.now();
		log.debug("Received WS Chat heartbeat");
	}
	
	private void sendMessage(@NotNull String message){
		log.trace("Sending Chat message {}", message);
		send(message);
	}
	
	@Override
	public void onWebsocketPing(WebSocket conn, Framedata f){
		onHeartbeat();
		sendMessage("PONG");
	}
	
	@Override
	public void onWebsocketPong(WebSocket conn, Framedata f){
		onHeartbeat();
	}
	
	@Override
	public void join(@NotNull String channel){
		try(var ignored = LogContext.empty().withSocketId(uuid)){
			if(channels.add(channel)){
				log.info("Joining Chat channel {}", channel);
				sendMessage("JOIN #" + channel);
			}
		}
	}
	
	@Override
	public void joinPending(){
	}
	
	@Override
	public void leave(@NotNull String channel){
		try(var ignored = LogContext.empty().withSocketId(uuid)){
			log.info("Leaving Chat channel {}", channel);
			sendMessage("PART #" + channel);
			channels.remove(channel);
		}
	}
	
	@Override
	public void ping(){
		send("PING");
	}
	
	public boolean isChannelJoined(@NotNull String channel){
		return channels.contains(channel);
	}
	
	public void addListener(ITwitchChatWebSocketListener listener){
		listeners.add(listener);
	}
	
	public long getChannelCount(){
		return getChannels().size();
	}
}