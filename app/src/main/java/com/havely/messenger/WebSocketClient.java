package com.havely.messenger;

import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class WebSocketClient {
    private static final String TAG = "HavelyWebSocket";
    private WebSocket webSocket;
    private MessageListener messageListener;
    
    // ТВОЙ IP
    private String serverUrl = "ws://100.84.189.163:8080";
    
    public interface MessageListener {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    public void connect(String username, MessageListener listener) {
        this.messageListener = listener;
        
        OkHttpClient client = new OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build();
            
        Request request = new Request.Builder()
            .url(serverUrl)
            .build();
            
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "✅ Connected to Havely server");
                messageListener.onConnected();
                
                try {
                    JSONObject joinMsg = new JSONObject();
                    joinMsg.put("type", "join");
                    joinMsg.put("username", username);
                    webSocket.send(joinMsg.toString());
                    Log.d(TAG, "📨 Sent join request for: " + username);
                } catch (Exception e) {
                    Log.e(TAG, "Error sending join message", e);
                }
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "📩 Server: " + text);
                messageListener.onMessageReceived(text);
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "❌ Disconnected from server");
                messageListener.onDisconnected();
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "💥 Connection failed: " + t.getMessage());
                messageListener.onError(t.getMessage());
            }
        });
    }
    
    public void sendMessage(String message) {
        if (webSocket != null) {
            try {
                // ОБЯЗАТЕЛЬНО отправляем как JSON с полем "content"
                JSONObject msg = new JSONObject();
                msg.put("type", "message");
                msg.put("content", message);  // ЭТО ВАЖНО!
                webSocket.send(msg.toString());
                Log.d(TAG, "📤 Sent JSON message: " + msg.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
            }
        } else {
            Log.e(TAG, "❌ Cannot send - WebSocket is null");
        }
    }
    
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
        }
    }
}
