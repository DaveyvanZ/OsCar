package com.dmnn.oscar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * FragmentHistory class
 * @author Davey van Zetten
 */
public class FragmentHistory extends Fragment
{
    private int medewerkerID;
    ArrayList history;
    private String naam, kosten, nummer, transactie;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        medewerkerID = bundle.getInt("medewerkerID");
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        history = new ArrayList<String>();
        ListView geschiedenisList = getView().findViewById(R.id.geschiedenisList);


        //stuurt een querie die de lijst van transacties van de inglogde gebruiker ophaalt.
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(getActivity(), "GetAlleTransacties.php", hashMap, true, output ->
        {
            try
            {
                JSONArray array = new JSONArray(output);

                for(int i = 0; i < array.length(); i++)
                {
                    JSONObject o = (JSONObject) array.get(i);
                    naam = (String) o.get("ID");
                    nummer = (String) o.get("Nummer");
                    kosten = (String) o.get("Prijs");
                    transactie = nummer + " - " + naam + " - " + kosten;
                    history.add(transactie);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, history);

                geschiedenisList.setAdapter(adapter);

            }
            catch (JSONException e)
            {

            }
        });
    }
}
