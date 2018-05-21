package me.tea.hangman.game;

import javafx.application.Platform;
import me.tea.hangman.Controller;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Game {

    private Controller controller;

    private String word;

    //private String key = "a2a73e7b926c924fad7001ca3111acd55af2ffabf50eb4ae5";
	private String key = "5dbc1436426ded857160908bdf106a2448ee7aee4a72104bd";

    /** LINK SETTINGS. **/
    private boolean hasDictionaryDef;
    private String includePartOfSpeech;

    private int minCorpusCount;
    private int maxCorpusCount;

    private int minDictionaryCount;
    private int maxDictionaryCount;

    private int minLength;
    private int maxLength;
    /** LINK SETTINGS. **/

    private int currentTries;
    private int triesLeft;

    private boolean wasSuccessful;

    public Game(Controller controller){
        this.controller = controller;
        defaultSettings();

        setWord();
    }

    public Game(Controller controller, String word){
        this.controller = controller;
        defaultSettings();

        setWord(word);
    }

    private void defaultSettings(){
        this.currentTries = 10;
        this.triesLeft = 10;

        this.hasDictionaryDef = true;
        this.includePartOfSpeech = "noun";
        this.minCorpusCount = 8000;
        this.maxCorpusCount = -1;
        this.minDictionaryCount = 3;
        this.maxDictionaryCount = -1;
        this.minLength = 6;
        this.maxLength = 12;
    }

    public void setWord(String word){
        this.word = word.toUpperCase();

        Platform.runLater(() -> controller.changeCurrentWord(word.toUpperCase()));
    }

    private void setWord(){

        try{
            URL url = new URL("http://api.wordnik.com:80/v4/words.json/randomWord?hasDictionaryDef=" + hasDictionaryDef + "&includePartOfSpeech=" + includePartOfSpeech + "&minCorpusCount=" + minCorpusCount + "&maxCorpusCount=" + maxCorpusCount + "&minDictionaryCount=" + minDictionaryCount + "&maxDictionaryCount=" + maxDictionaryCount + "&minLength=" + minLength + "&maxLength=" + maxLength + "&api_key=" + this.key);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "");

            Reader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

            StringBuilder builder = new StringBuilder();
            for(int line; (line = in.read()) >= 0;)
                builder.append((char)line);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(builder.toString());

            word = String.valueOf(jsonObject.get("word")).toUpperCase();

            controller.changeCurrentWord(String.valueOf(jsonObject.get("word")).toUpperCase());
            wasSuccessful = true;
        }catch(Exception e){
            wasSuccessful = false;
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getWord() {
        return this.word;
    }

    public int getCurrentTries() {
        return this.currentTries;
    }

    public int getTriesLeft() {
        return this.triesLeft;
    }

    public void setTriesLeft(){
        this.currentTries = (this.currentTries - 1);
    }

    public boolean wasSuccessful() {
        return this.wasSuccessful;
    }
}
