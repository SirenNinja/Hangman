package me.tea.hangman;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import me.tea.hangman.game.Game;
import me.tea.hangman.game.User;
import me.tea.hangman.game.multiplayer.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Arrays;

public class Controller {

    @FXML
    Pane gamePane, failPane, titlescreen, multiplayerPane;

    @FXML
    Label winsLabel, currentWord, infoText, triesLeft, clientText, notCurrentTurnText;

    @FXML
    JFXProgressBar triesLeftProgressbar;

    @FXML
    JFXButton play, playagain, joinServer, createServer;

    @FXML
    JFXTextField connectionIP;

    @FXML
    Rectangle notCurrentTurn;

    private UDPServer server;
    private UDPClient client;

    private boolean isMPGameRunning = false;
    private boolean isServer = false;

    private User user = new User();
    private Game game = null;

    public Game getGame(){
        return game;
    }

    private void changeTurn(boolean isCurrentTurn){
        Platform.runLater(() -> {
                    notCurrentTurn.setVisible(!isCurrentTurn);
                    notCurrentTurnText.setVisible(!isCurrentTurn);
                }
        );
    }

    public void multiplayerScreen(){
        titlescreen.setVisible(false);
        multiplayerPane.setVisible(true);
    }

    public void joinServer(){
        client = new UDPClient(this, connectionIP.getText());
        Thread tClient = new Thread(this.client);
        tClient.start();

        Platform.runLater(() -> Main.getStage().setTitle("Hangman - TheEvilArchitect [Connected to Server]"));

        isMPGameRunning = true;
        changeTurn(false);
    }

    public void mpAction(String reader){
        String response = String.valueOf(reader);

        System.out.println("Response: " + response);

        try{
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(response);

            if(jsonObject.containsKey("gameword")){
                String word = String.valueOf(jsonObject.get("gameword"));

                onPlay(word);
                System.out.println("Word changed to the hosts word.");
                return;
            }

            if(jsonObject.containsKey("isturn")){
                boolean isTurn = Boolean.valueOf(String.valueOf(jsonObject.get("isturn")));

                changeTurn(isTurn);
                System.out.println("Your turn: " + isTurn);
            }



            if(jsonObject.containsKey("letter")){
                String letter = String.valueOf(jsonObject.get("letter"));

                for(Node component : gamePane.getChildren()){
                    if(component instanceof JFXButton){
                        if(((JFXButton) component).getText().equals(letter.toUpperCase())){
                            if(!(component.isDisable()))
                                decide((JFXButton)component);
                        }
                    }
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void createServer(){
        onPlay(null);

        server = new UDPServer(this);
        Thread tServer = new Thread(server);
        tServer.start();

        Platform.runLater(() -> Main.getStage().setTitle("Hangman - TheEvilArchitect [Hosting Server]"));

        isServer = true;
        isMPGameRunning = true;
    }

    public void onPlay(){
        onPlay(null);
    }

    private void onPlay(String presetWord){
        Platform.runLater(
                () -> {
                    if(presetWord == null)
                        game = new Game(this);
                    else
                        game = new Game(this, presetWord);

                    if(isMPGameRunning && isServer)
                        server.sendMessage(game.getWord());

                    if(!isMPGameRunning)
                        changeTurn(true);

                    titlescreen.setVisible(false);
                    failPane.setVisible(false);
                    multiplayerPane.setVisible(false);

                    for(Node component : gamePane.getChildren()){
                        if(component instanceof JFXButton){
                            ((JFXButton) component).setTextFill(Paint.valueOf("white"));
                            component.setDisable(false);
                        }
                    }

                    triesLeft.setText("Tries left: " + game.getCurrentTries() + " / " + game.getTriesLeft());

                    double value = (double)game.getCurrentTries()/(double)game.getTriesLeft();
                    triesLeftProgressbar.setProgress(value);

                    gamePane.setVisible(true);
                }
        );
    }

    private void onSuccess(JFXButton button){
        Platform.runLater(
                () -> {
                    button.setTextFill(Paint.valueOf("green"));
                    button.setDisable(true);

                    if(currentWord.getText().equalsIgnoreCase(game.getWord())){
                        infoText.setText("You Win!");
                        infoText.setTextFill(Paint.valueOf("green"));

                        if(isMPGameRunning && !isServer){
                            playagain.setVisible(false);
                            clientText.setTextFill(Paint.valueOf("red"));
                            clientText.setVisible(true);
                        }else{
                            playagain.setVisible(true);
                            clientText.setVisible(false);
                        }

                        gamePane.setVisible(false);
                        failPane.setVisible(true);

                        user.addWin();
                        user.addGame();
                        winsLabel.setText("Wins: " + user.getWins() + " / " + user.getGames());
                    }
                }
        );
    }

    private void onFail(JFXButton button){
        game.setTriesLeft();

        Platform.runLater(
                () -> {
                    if(game.getCurrentTries() <= 0){
                        infoText.setText("You Lose!");
                        infoText.setTextFill(Paint.valueOf("red"));

                        if(isMPGameRunning && !isServer){
                            playagain.setVisible(false);
                            clientText.setTextFill(Paint.valueOf("red"));
                            clientText.setVisible(true);
                        }else{
                            playagain.setVisible(true);
                            clientText.setVisible(false);
                        }

                        gamePane.setVisible(false);
                        failPane.setVisible(true);

                        user.addLose();
                        user.addGame();
                        winsLabel.setText("Wins: " + user.getWins() + " / " + user.getGames());
                        return;
                    }

                    double value = (double)game.getCurrentTries()/(double)game.getTriesLeft();

                    triesLeftProgressbar.setProgress(value);

                    button.setTextFill(Paint.valueOf("red"));
                    button.setDisable(true);

                    triesLeft.setText("Tries left: " + game.getCurrentTries() + " / " + game.getTriesLeft());
                }
        );
    }

    private void decide(JFXButton button){
        Platform.runLater(
                () -> {
                    if(checkLetter(button.getText().charAt(0)))
                        onSuccess(button);
                    else
                        onFail(button);
                }
        );
    }

    public void onClick(MouseEvent event){
        JFXButton button = (JFXButton)event.getSource();

        System.out.println("Text: " + button.getText());

        System.out.println("Word: " + currentWord.getText());
        System.out.println("Game: " + game.getWord());

        if(isMPGameRunning) {
            changeTurn(false);
            if (isServer) {
                server.sendGlobalMessage(1, button.getText(), true);
                decide(button);
            }else
                client.sendMessage("{\"getword\": false, \"letter\": \"" + button.getText() + "\"}");
        }else
            decide(button);
    }

    private boolean checkLetter(char letter){
        boolean value = false;

        for(int i = 0; i < game.getWord().length(); i++){
            //System.out.println("Char at " + i + ": [" + game.getWord().charAt(i) + ", " + currentWord.getText().charAt(i) + "] Equals: " + (game.getWord().charAt(i) == letter));

            if(Character.toUpperCase(game.getWord().charAt(i)) == Character.toUpperCase(letter)){
                StringBuilder temp = new StringBuilder(currentWord.getText());
                temp.setCharAt(i, game.getWord().charAt(i));

                currentWord.setText(temp.toString());

                value = true;
            }
        }

        return value;
    }

    public void changeCurrentWord(String word){
        String s = "";
        Character[] whitelisted = {' ', '-', '_'};

        for(int i = 0; i <= word.length()-1; i++) {
            if(Arrays.asList(whitelisted).contains(word.charAt(i)))
                s += word.charAt(i);
            else
                s += "*";
        }

        currentWord.setText(s);
    }
}
