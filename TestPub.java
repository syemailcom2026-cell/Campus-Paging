import org.eclipse.paho.client.mqttv3.*;

public class TestPub {
    public static void main(String[] args) throws Exception {
        String broker = "wss://broker-cn.emqx.io:8084/mqtt";
        String topic = "campus/call/123456";
        MqttClient client = new MqttClient(broker, "testpub-" + System.currentTimeMillis());
        MqttConnectOptions opt = new MqttConnectOptions();
        opt.setCleanSession(true);
        opt.setConnectionTimeout(15);
        client.connect(opt);
        String id = args.length > 0 ? args[0] : "testcall-1";
        String payload = "{"
            + "\"type\":\"call\","
            + "\"id\":\"" + id + "\","
            + "\"cls\":\"三年级一班\","
            + "\"name\":\"张伟\","
            + "\"destination\":\"教务处\","
            + "\"remark\":\"\","
            + "\"text\":\"请三年级一班的张伟同学，听到广播后到教务处。\","
            + "\"params\":{\"repeat\":2,\"interval\":1,\"rate\":1.0,\"pitch\":1.0,\"volume\":100},"
            + "\"ts\":" + System.currentTimeMillis()
            + "}";
        client.publish(topic, payload.getBytes("UTF-8"), 1, false);
        System.out.println("published " + id);
        client.disconnect();
        client.close();
    }
}
