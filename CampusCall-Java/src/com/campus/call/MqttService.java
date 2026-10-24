package com.campus.call;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

/**
 * MQTT over WebSocket 服务（Paho 客户端）。
 * 两端输入相同房间号即订阅同一主题，自动配对。
 */
public class MqttService {

    public interface Listener {
        void onConnected();

        void onDisconnected();

        void onMessage(String topic, String payload);
    }

    public static final String DEFAULT_BROKER = "wss://broker-cn.emqx.io:8084/mqtt";

    private MqttClient client;
    private Listener listener;
    private String broker;
    private String room;

    /** 连接 Broker 并订阅房间主题 */
    public void connect(String broker, String room, Listener l) throws MqttException {
        this.broker = broker;
        this.room = room;
        this.listener = l;

        String clientId = "campuscall_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
        client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setConnectionTimeout(15);
        opts.setKeepAliveInterval(30);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                if (listener != null) {
                    listener.onDisconnected();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                if (listener != null) {
                    listener.onMessage(topic, new String(message.getPayload(), StandardCharsets.UTF_8));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        client.connect(opts);
        client.subscribe(topic(), 1);
        if (listener != null) {
            listener.onConnected();
        }
    }

    /** 房间主题：campus/call/{房间号} */
    public String topic() {
        return "campus/call/" + room;
    }

    /** 发布呼叫消息（JSON 文本） */
    public void publish(String payload) throws MqttException {
        if (client == null || !client.isConnected()) {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }
        MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        msg.setQos(1);
        client.publish(topic(), msg);
    }

    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public String getBroker() {
        return broker;
    }

    public String getRoom() {
        return room;
    }

    /** 构造呼叫消息 JSON */
    public static String buildCallPayload(String cls, String name, String text) {
        return "{\"type\":\"call\",\"cls\":\"" + esc(cls) + "\",\"name\":\"" + esc(name)
                + "\",\"text\":\"" + esc(text) + "\",\"ts\":" + System.currentTimeMillis() + "}";
    }

    public static String jsonField(String json, String key) {
        String marker = "\"" + key + "\":";
        int i = json.indexOf(marker);
        if (i < 0) {
            return "";
        }
        int start = i + marker.length();
        if (start >= json.length() || json.charAt(start) != '"') {
            return "";
        }
        int end = start + 1;
        while (end < json.length() && json.charAt(end) != '"') {
            end++;
        }
        return unesc(json.substring(start + 1, end));
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unesc(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
