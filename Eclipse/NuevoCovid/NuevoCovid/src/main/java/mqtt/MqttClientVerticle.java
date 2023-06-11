package mqtt;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import Tablas.Zona;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class MqttClientVerticle extends AbstractVerticle {
	
	Gson gson;

	public void start(Promise<Void> startFuture) {
		gson = new Gson();
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "192.168.1.48", s -> {
			if (s.succeeded()) {

			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					System.out.println("Suscripción " + mqttClient.clientId());
				}
			});

			}
			mqttClient.publishHandler(handler -> {
				System.out.println("Mensaje recibido:");
				System.out.println("    Topic: " + handler.topicName().toString());
				System.out.println("    Id del mensaje: " + handler.messageId());
				System.out.println("    Contenido: " + handler.payload().toString());
				try {
				Zona sc = gson.fromJson(handler.payload().toString(), Zona.class);
				System.out.println("    Zona: " + sc.toString());
				}catch (JsonSyntaxException e) {
					System.out.println("    No es una SimpleClass. ");
				}
			});
			mqttClient.publish("topic_1", Buffer.buffer("0"), MqttQoS.AT_LEAST_ONCE, false, false);
		});

	}

}
