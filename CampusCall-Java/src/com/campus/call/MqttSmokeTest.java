package com.campus.call;

/**
 * MQTT 连通性冒烟测试：连接公共 Broker、订阅房间主题、发布并接收一条呼叫消息。
 * 用法：java -cp "out;lib/xxx.jar" com.campus.call.MqttSmokeTest
 */
public class MqttSmokeTest {

    public static void main(String[] args) throws Exception {
        MqttService mqtt = new MqttService();
        final boolean[] received = {false};

        mqtt.connect(MqttService.DEFAULT_BROKER, "smoketest-java", new MqttService.Listener() {
            @Override
            public void onConnected() {
                System.out.println("[OK] connected, topic=" + mqtt.topic());
            }

            @Override
            public void onDisconnected() {
                System.out.println("[WARN] disconnected");
            }

            @Override
            public void onMessage(String topic, String payload) {
                System.out.println("[RECV] " + payload);
                received[0] = true;
            }
        });

        Thread.sleep(2500);
        String payload = MqttService.buildCallPayload("高二(3)班", "李四", "请高二(3)班李四同学到办公室");
        mqtt.publish(payload);
        System.out.println("[SENT] " + payload);

        long deadline = System.currentTimeMillis() + 12000;
        while (!received[0] && System.currentTimeMillis() < deadline) {
            Thread.sleep(300);
        }
        mqtt.disconnect();
        System.out.println("[RESULT] " + (received[0] ? "PASS" : "FAIL: no message received"));
        System.exit(received[0] ? 0 : 1);
    }
}
