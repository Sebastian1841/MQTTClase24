package com.example.mqtt;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    static String MQTTHOST = "tcp://crudfirebase5.cloud.shiftr.io:1883";
    static String MQTTUSER = "crudfirebase5";
    static String MQTTPASS = "t8oSYavYbWhUSaAD";
    MqttAndroidClient client;
    MqttConnectOptions opciones;
    Boolean permisoPublicar;
    String clienteID = "";

    static String TOPIC = "LED";
    static String TOPIC_MSG_ON = "Encender";
    static String TOPIC_MSG_OFF = "Apagar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectBroker();

        Button btn_encendido = findViewById(R.id.btn_encender);
        btn_encendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje(TOPIC, TOPIC_MSG_ON);
            }
        });
        Button btn_apagado = findViewById(R.id.btn_apagar);
        btn_apagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje(TOPIC, TOPIC_MSG_OFF);
            }
        });

    }
    private void enviarMensaje(String topic, String msg){
        checkConnection();
        if(this.permisoPublicar){
            try {
                int qos = 0;
                this.client.publish(topic,msg.getBytes(),qos,false);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private void checkConnection(){
        if(this.client.isConnected()){
            this.permisoPublicar = true;
        }else {
            this.permisoPublicar = false;
            connectBroker();
        }
    }
    private void connectBroker() {
        this.client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, this.clienteID);
        this.opciones = new MqttConnectOptions();
        this.opciones.setUserName(MQTTUSER);
        this.opciones.setPassword(MQTTPASS.toCharArray());
        try {
            IMqttToken token = this.client.connect(opciones);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getBaseContext(), "Conectado", Toast.LENGTH_SHORT).show();
                    // Llama a mensajearTopic() después de la conexión exitosa.
                    mensajearTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getBaseContext(), "Conexion Fallida", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void mensajearTopic(){
        try{
            this.client.subscribe(TOPIC, 0);
        }catch (MqttException e){
            e.printStackTrace();
        }
        this.client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), "Se desconecto el servidor", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                TextView txtinfo = findViewById(R.id.txtInfo);
                if (topic.matches(TOPIC)){
                    String msg = new String(message.getPayload());
                    if (msg.matches(TOPIC_MSG_ON)){
                        txtinfo.setText(msg);
                        txtinfo.setBackgroundColor(GREEN);
                    }
                    if (msg.matches(TOPIC_MSG_OFF)){
                        txtinfo.setText(msg);
                        txtinfo.setBackgroundColor(RED);
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}