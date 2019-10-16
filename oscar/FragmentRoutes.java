package com.dmnn.oscar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * FragmentRoutes class
 * @author Nathan van Jole
 */
public class FragmentRoutes extends Fragment
{

    /**
     * MedewerkerID: het unieke ID van de huidige medewerker die ingelogd is.
     */
    private int medewerkerID;

    /**
     * De activity view
     */
    private View view;

    /**
     * ArrayList waarin beschikbare ritten worden opgeslagen
     * om door te sturen naar de RecyclerView
     */
    private ArrayList<Rit> availableRitten = new ArrayList<>();

    /**
     * ArrayLists waarin geplande ritten wordt opgeslagen
     * om door te sturen naar de RecyclerView
     */
    private ArrayList<Rit> plannedRitten = new ArrayList<>();

    private Button availableRoutesBtn;
    private Button plannedRoutesBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        medewerkerID = bundle.getInt("medewerkerID");

        return inflater.inflate(R.layout.fragment_routes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        getAvailableRoutes();

        availableRoutesBtn = view.findViewById(R.id.availableRoutesBtn);
        plannedRoutesBtn = view.findViewById(R.id.plannedRoutesBtn);

        availableRoutesBtn.setOnClickListener(v ->
                {
                    availableRoutesBtn.setTextSize(23);
                    plannedRoutesBtn.setTextSize(20);
                    getAvailableRoutes();
                });
        plannedRoutesBtn.setOnClickListener(v ->
                {
                    plannedRoutesBtn.setTextSize(23);
                    availableRoutesBtn.setTextSize(20);
                    getPlannedRoutes();
                });

    }

    /**
     * Verkrijg de beschikbare routes van de database en initializeer de RecyclerView met
     * de verkregen data.
     */
    private void getAvailableRoutes()
    {
        availableRitten.clear();

        HashMap data = new HashMap<String, String>();
        data.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetAlleRittenOrderedByDay.php", data, true,
        output ->
        {
            try
            {
                JSONArray array = new JSONArray(output);

                for(int i = 0; i < array.length(); i++)
                {
                    JSONObject o =  (JSONObject) array.get(i);

                    String ID = (String) o.get("ID");
                    String vertrekdatumString = (String) o.get("Vertrekdatum");
                    Date vertrekdatum = dateStringToSQLDate(vertrekdatumString);
                    String vertrektijdString = (String) o.get("Aankomsttijd");
                    Time vertrektijd = timeStringToSQLTime(vertrektijdString);
                    String vertrekplaats = (String) o.get("Vertrekplaats");

                    String status = (String) o.get("Status");
                    String hinderStatus = (String) o.get("HinderStatus");
                    int vrijePlaatsen = Integer.parseInt((String) o.get("VrijePlaatsen"));

                    Rit rit = new Rit(ID, vertrekdatum, vertrektijd, vertrekplaats, status, hinderStatus, vrijePlaatsen);
                    availableRitten.add(rit);
                }

                initRecyclerView(availableRitten, false);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        });
    }

    /**
     * Verkrijg de geplande route van de database en initializeer de RecyclerView met
     * de verkregen data.
     */
    private void getPlannedRoutes()
    {
        plannedRitten.clear();

        HashMap data = new HashMap<String, String>();
        data.put("medewerkerID", Integer.toString(medewerkerID));
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetPlannedRitten.php", data, true,
        output ->
        {
            try
            {
                JSONArray array = new JSONArray(output);

                for(int i = 0; i < array.length(); i++)
                {
                    JSONObject o =  (JSONObject) array.get(i);

                    String ritID = (String) o.get("RitID");
                    String vertrekdatumString = (String) o.get("Vertrekdatum");
                    Date vertrekdatum = dateStringToSQLDate(vertrekdatumString);
                    String aankomsttijdString = (String) o.get("Aankomsttijd");
                    Time aankomsttijd = timeStringToSQLTime(aankomsttijdString);
                    String vertrekplaats = (String) o.get("Vertrekplaats");

                    String status = (String) o.get("Status");
                    String hinderStatus = (String) o.get("HinderStatus");
                    int vrijePlaatsen = Integer.parseInt((String) o.get("VrijePlaatsen"));

                    Rit rit = new Rit(ritID, vertrekdatum, aankomsttijd, vertrekplaats, status, hinderStatus, vrijePlaatsen);
                    plannedRitten.add(rit);
                }

                initRecyclerView(plannedRitten, true);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        });
    }

    /**
     * Initializeert de RecyclerView die de ritten laat zien
     * @param ritten ArrayList ritten
     */
    private void initRecyclerView(ArrayList<Rit> ritten, boolean plannedRitten)
    {
        RecyclerView recyclerView = view.findViewById(R.id.routesRv);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(view.getContext(), medewerkerID, ritten, plannedRitten);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    /**
     * Converteert een datum string naar een sql.Date object
     * @param date String date
     * @return Date date
     */
    public static Date dateStringToSQLDate(String date)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date utilDate;
        Date sqlDate = null;

        try
        {
            utilDate = format.parse(date);
            sqlDate = new Date(utilDate.getTime());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return sqlDate;
    }

    /**
     * Converteert een time string naar een sql.Time object
     * @param time String time
     * @return Time time
     */
    public static Time timeStringToSQLTime(String time)
    {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        Time sqlTime = null;

        try
        {
            long ms = format.parse(time).getTime();
            sqlTime = new Time(ms);

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return sqlTime;
    }
}
