package me.tea.hangman.game.multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import me.tea.hangman.Controller;

import static com.esotericsoftware.minlog.Log.LEVEL_TRACE;

public class UDPClient implements Runnable {

    private Client client;
    private String host = "127.0.0.1";
    private Controller controller;

    public UDPClient(Controller controller, String host){
        this.controller = controller;
        Log.set(LEVEL_TRACE);

        if(host != null)
            this.host = host;
    }


    @Override
    public void run() {
        try{
            client = new Client();
            client.start();
            client.connect(5000, host, 8417, 8417);

            Kryo kryo = client.getKryo();
            kryo.register(SomeResponse.class);

            sendMessage("{\"getword\": \"true\"}");

            client.addListener(new Listener() {

                @Override
                public void received (Connection connection, Object object) {
                    if (object instanceof SomeResponse) {
                        SomeResponse response = (SomeResponse)object;
                        System.out.println("Response from server: " + response.text);

                        controller.mpAction(response.text);
                    }
                }
            });
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message){
        SomeResponse request = new SomeResponse();
        request.text = message;
        client.sendUDP(request);
        System.out.println("Sending to server: " + request.text);
    }
}
