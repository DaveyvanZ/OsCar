package com.dmnn.oscar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * UserLoginActivity class
 *
 * word gebruikt om de gebruiker gegevens te controlleren
 * en beslist of het de juiste gegevens zijn, dit word samen met de xml file:
 * activity_user_login.xml
 *
 * als het wel de juiste gegevens zijn word de gebruiker ingelogged
 * en word de gebruikerID als extra gestuurd naar de MainActivity scherm
 *
 * als niet word de grbuiker geinformeerd dat hij/zij de verkeerde gegevens in heeft getypt
 *
 * alle design nodes worden geinitializeerd in de on create:
 *
 * de knop krijgt in de on create een on click listener{
 *
 *     in de listener word de user input als een string opgenomen,
 *     en er word een database connectie gemaakt(DbConnectMedewerker()) om de gebruiker gegevens uit de database te halen
 * }
 *
 *
 * DbConnectMedewerker():
 * de DBConnection class word gebruikt om een query te executen.
 * met deze query is het mogelijk om alle gegevens van een gebruiker op te roepen waar
 * de inputID overeenkomstig is met de DB ID. De getMedewerker() functie word gebruikt om de output van de query op te vangen
 *
 * getMedewerker():
 * de output van de query word in de json array geconvert.
 * nu dat de database gegevens van een gebruiker in de code gebruikt kan worden
 * kunnen de input gegevens gechecked worden.
 *
 * als de gegevens overeenkomstig zijn word startMap() aangeroepen
 *
 * als niet word de user geinformeerd
 *
 * StartMap()
 *
 * word naar de main activity gestuurd samen met de id als extra
 *
 * @author: Nathaniel Veldkamp
 *
 */

public class UserLoginActivity extends AppCompatActivity {

    EditText userID, userPass;
    Button UserLoginBtn;

    String inputID, inputPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        userID = findViewById(R.id.UserIDtxt);
        userPass = findViewById(R.id.UserPassTxt);

        UserLoginBtn = findViewById(R.id.UserLogInBtn);
        UserLoginBtn.setOnClickListener(e -> {

            inputID = userID.getText().toString();
            inputPass = userPass.getText().toString();
            DbConnectMedewerker();

        });


    }

    private void DbConnectMedewerker(){

        HashMap<String, String> data = new HashMap<>();
        data.put("medewerkerID", inputID);
        DBConnection.getInstance().executeQuery(this, "GetSpecificMedewerker.php",
                data, false, output -> getMedewerker(output));

    }

    private void getMedewerker(String output) {

        try{

            JSONArray array = new JSONArray(output);

            for(int i = 0; i < array.length(); i++){

                JSONObject o = (JSONObject) array.get(i);

                String pass = (String) o.get("Wachtwoord");
                String name = (String) o.get("Naam");

                Log.d("MYTAG", "getMedewerker: name: " + name);
                Log.d("MYTAG", "getMedewerker: pass: " + pass);

                if(inputPass.equals(pass)){

                    startMap();

                }else{
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }

            }

        }catch(JSONException e){
            e.printStackTrace();
        }

    }


    private void startMap() {


        Intent myIntent = new Intent(UserLoginActivity.this, MainActivity.class);
        myIntent.putExtra("medewerkerID", Integer.parseInt(inputID));
        UserLoginActivity.this.startActivity(myIntent);

    }


}