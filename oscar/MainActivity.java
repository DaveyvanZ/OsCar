package com.dmnn.oscar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

/**
 * MainActivity class
 * @author Davey van Zetten
 */
public class MainActivity extends AppCompatActivity
{

    /** MedewerkerID: het unieke ID van de huidige medewerker die ingelogd is. **/
    private int medewerkerID;

    private FragmentHome fragmentHome = new FragmentHome();
    private FragmentRoutes fragmentRoutes = new FragmentRoutes();
    private FragmentShop fragmentShop = new FragmentShop();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putInt("medewerkerID", medewerkerID);

            fragmentHome.setArguments(bundle);
            fragmentRoutes.setArguments(bundle);
            fragmentShop.setArguments(bundle);

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    // change to home fragment
                    fragmentTransaction.replace(R.id.frame_content, fragmentHome);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_route:
                    // change to route fragment
                    fragmentTransaction.replace(R.id.frame_content, fragmentRoutes);
                    fragmentTransaction.commit();
                    return true;
                case R.id.navigation_shop:
                    // change to shop fragment
                    fragmentTransaction.replace(R.id.frame_content, fragmentShop);
                    fragmentTransaction.commit();
                    return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        medewerkerID = bundle.getInt("medewerkerID");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putInt("medewerkerID", medewerkerID);
        fragmentHome.setArguments(fragmentBundle);

        fragmentTransaction.replace(R.id.frame_content, fragmentHome);
        fragmentTransaction.commit();
    }

}