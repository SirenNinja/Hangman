package me.tea.hangman.game.multiplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import me.tea.hangman.Controller;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;

import static com.esotericsoftware.minlog.Log.LEVEL_TRACE;

public class UDPServer implements Runnable{

    private Server server;

    private Controller controller;

    public UDPServer(Controller controller){
        this.controller = controller;
        Log.set(LEVEL_TRACE);
    }

    @Override
    public void run() {
        try{
            server = new Server();
            server.start();
            server.bind(8417, 8417);

            Kryo kryo = server.getKryo();
            kryo.register(SomeResponse.class);
            kryo.register(ArrayList.class);
            kryo.register(int.class);

            controller.mpAction("{\"isturn\": \"true\"}");

            server.addListener(new Listener() {

                public void connected(Connection connection){
                    System.out.println("Player connected. Client: " + connection.getID());
                }

                public void disconnected (Connection connection){
                    System.out.println("Player disconnected.");
                }

                public void received (Connection connection, Object object) {
                    if (object instanceof SomeResponse) {
                        SomeResponse response = (SomeResponse)object;
                        System.out.println("Response from client (" + connection.getID() + "): " + response.text);

                        try{
                            JSONParser parser = new JSONParser();
                            JSONObject jsonObject = (JSONObject) parser.parse(response.text);

                            boolean isGettingWord = Boolean.valueOf(String.valueOf(jsonObject.get("getword")));

                            if(isGettingWord){
                                SomeResponse request = new SomeResponse();
                                request.text = "{\"gameword\": \"" + controller.getGame().getWord() + "\"}";
                                connection.sendUDP(request);

                                return;
                            }

                            String letter = String.valueOf(jsonObject.get("letter"));

                            if(!jsonObject.containsKey("letter"))
                                return;

                            int lastID = connection.getID();
                            Connection[] connections = server.getConnections();

                            sendGlobalMessage(lastID, letter, (lastID >= Arrays.asList(connections).size()));
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
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
        server.sendToAllUDP(request);

        System.out.println("Sending all clients: " + request.text);
    }

    public void sendGlobalMessage(int lastID, String letter, boolean serverTurn){
        for(Connection connection : server.getConnections()){
            boolean isNext = false;
            SomeResponse request = new SomeResponse();

            if(connection.getID() == (lastID+1))
                isNext = true;

            request.text = "{\"isturn\": \"" + isNext + "\", \"letter\": \"" + letter + "\"}";
            //connection.sendUDP(request);

            server.sendToUDP(connection.getID(), request);

            System.out.println("Client: " + connection.getID());

            System.out.println("Sending client " + connection.getID() + ": " + request.text);
        }

        controller.mpAction("{\"isturn\": \"" + serverTurn + "\", \"letter\": \"" + letter + "\"}");

        System.out.println("LastID: " + lastID);
    }

    private void sendMessage(int id, String message){
        SomeResponse request = new SomeResponse();
        request.text = message;
        server.sendToUDP(id, request);
        controller.mpAction(message);
        System.out.println("Sent client (" + id + "): " + message);
    }
}
