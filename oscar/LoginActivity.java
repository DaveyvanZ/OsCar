package com.dmnn.oscar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity
{
    /**
     * LoginActivity class
     * @author Davey van Zetten
     */
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginBtn);
        loginButton.setOnClickListener(v ->
        {

            Intent intent = new Intent(LoginActivity.this, UserLoginActivity.class);
            startActivity(intent);


        });

    }

}