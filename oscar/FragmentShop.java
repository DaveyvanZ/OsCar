package com.dmnn.oscar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * FragmentShop class
 * @author Davey van Zetten
 */
public class FragmentShop extends Fragment
{

    /** MedewerkerID: het unieke nummer van de huidige medewerker die ingelogd is. **/
    private Button geschiedenisBTN, koopBTN;
    private int medewerkerID, prijs, saldo;
    private FragmentHistory fragmentHistory = new FragmentHistory();
    ArrayList beloning;
    private String gekozenBeloning;
    PopupWindow beloningINFO;
    private String message, naam, info, kosten, gekozenInfo;
    TextView saldoTXT;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        medewerkerID = bundle.getInt("medewerkerID");
        fragmentHistory.setArguments(bundle);

        //popup window en beloning lijst aanmaken
        beloningINFO = new PopupWindow(getActivity());
        beloning = new ArrayList<String>();
        return inflater.inflate(R.layout.fragment_shop, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ListView shopList = getView().findViewById(R.id.shopList);
        saldoTXT = getView().findViewById(R.id.saldoTXT);

        // een querie naar de database die alle beschikbare beloningen ophaalt
        HashMap h = new HashMap();
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetAlleBeloningen.php", h, true, output ->
        {
            try
            {
                JSONArray array = new JSONArray(output);

                for(int i = 0; i < array.length(); i++)
                {
                    JSONObject o = (JSONObject) array.get(i);
                    naam = (String) o.get("BeloningID");
                    beloning.add(naam);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, beloning);

                shopList.setAdapter(adapter);
            }
            catch (JSONException e)
            {

            }
        });

        geschiedenisBTN = getView().findViewById(R.id.historyBTN);
        koopBTN = getView().findViewById(R.id.buyBTN);
        UpdateSaldo();

        // Wanneer er op een item gedrukt wordt van de lijst dan wordt er gekeken welke item het is en wordt er een querie gestuurd
        // die extra informatie over de geselecteerde item terug geeft.
        shopList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                beloningINFO.dismiss();
                gekozenBeloning = shopList.getItemAtPosition(i).toString();
                TextView infoNaam = new TextView(getActivity());
                HashMap<String,String> hashMapInfo = new HashMap<>();
                hashMapInfo.put("beloningID", gekozenBeloning);
                DBConnection.getInstance().executeQuery(getActivity(), "GetSpecificBeloning.php", hashMapInfo, true, output ->
                {
                    try
                    {
                        JSONArray array = new JSONArray(output);
                        JSONObject o = (JSONObject) array.get(0);
                        info = (String) o.get("Beschrijving");
                        kosten = (String) o.get("Prijs");
                        gekozenInfo = info + " prijs: " + kosten;
                        infoNaam.setText(gekozenInfo);
                    }
                    catch (JSONException e)
                    {

                    }
                });

                // maakt de popup window
                beloningINFO.setContentView(infoNaam);

                beloningINFO.setWidth(500);
                beloningINFO.setHeight(500);
                beloningINFO.showAtLocation(getView(),0,300,500);

                // removes de popup window wanneer er op gedrukt wordt.
                beloningINFO.getContentView().setOnClickListener(e ->
                {
                    beloningINFO.dismiss();
                });

            }
        });

        // gaat naar de history fragment
        geschiedenisBTN.setOnClickListener(e ->
        {
            beloningINFO.dismiss();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_content, fragmentHistory);
            transaction.commit();
        });

        // koopt een beloning wanneer er een geselecteerd is en wanneer de gebruiker voldoende saldo heeft.
        // stuurt een querie die de transactie gegevens toevoegt aan de database.
        koopBTN.setOnClickListener(e ->
        {
            beloningINFO.dismiss();
            TextView info = new TextView(getActivity());
            beloningINFO.setWidth(500);
            beloningINFO.setHeight(500);
            beloningINFO.setContentView(info);
            beloningINFO.showAtLocation(getView(), 0, 300, 500);

            if (gekozenBeloning != null)
            {
                if(saldo >= Integer.parseInt(kosten))
                {
                    HashMap<String,String> hashMapKoop = new HashMap<>();
                    hashMapKoop.put("beloningID", gekozenBeloning);
                    hashMapKoop.put("medewerkerID", Integer.toString(medewerkerID));
                    hashMapKoop.put("prijs", kosten);
                    DBConnection.getInstance().executeQuery(getActivity(), "AddTransactie.php", hashMapKoop, true, output -> { });
                    message = gekozenBeloning + " has been succesfully purchased!";
                    info.setText(message);
                    UpdateSaldo();
                }
                else
                {
                    message = "You don't have enough points to buy this reward!";
                    info.setText(message);
                }
            }
            else
            {
                message = "Please select a reward";
                info.setText(message);
            }

            beloningINFO.getContentView().setOnClickListener(a ->
            {
                beloningINFO.dismiss();
            });
        });
    }

    // haalt de saldo op van de ingelogde gebruiker
    public void UpdateSaldo()
    {
        HashMap<String, String> hashMapPunten = new HashMap<>();
        hashMapPunten.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetPuntenSaldo.php", hashMapPunten, true, output ->
        {
            try {
                JSONArray array = new JSONArray(output);
                JSONObject o = (JSONObject) array.get(0);
                saldo = Integer.parseInt((String) o.get("Saldo"));
                saldoTXT.setText(Integer.toString(saldo));
            } catch (JSONException e) {

            }
        });
    }

}
