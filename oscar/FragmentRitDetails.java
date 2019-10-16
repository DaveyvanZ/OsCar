package com.dmnn.oscar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * FragmentRitDetails class
 * @author Nathan van Jole
 */
public class FragmentRitDetails extends Fragment
{

    /**
     * Rit: het unieke rit waarvan de route moet worden opgevraagd.
     */
    private Rit rit;

    /**
     * MedewerkerID: het unieke ID van de huidige medewerker die ingelogd is.
     */
    private int medewerkerID;

    private ArrayList<RitInschrijvingWAdres> inschrijvingen;
    private TableLayout tableLayout;
    private Button backToRitBtn;
    private Button routeBegeleidingBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        rit = (Rit) bundle.getSerializable("rit");
        medewerkerID = bundle.getInt("medewerkerID");

        return inflater.inflate(R.layout.fragment_ritdetails, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        inschrijvingen = new ArrayList<>();
        backToRitBtn = view.findViewById(R.id.backToRitBtn);
        routeBegeleidingBtn = view.findViewById(R.id.routebegeleidingBtn);
        tableLayout = view.findViewById(R.id.infoLayout);

        backToRitBtn.setOnClickListener(v ->
        {
            FragmentRit fragmentRit = new FragmentRit();

            FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle fragmentBundle = new Bundle();
            fragmentBundle.putInt("medewerkerID", medewerkerID);
            fragmentBundle.putSerializable("rit", rit);
            fragmentRit.setArguments(fragmentBundle);

            fragmentTransaction.replace(R.id.frame_content, fragmentRit);
            fragmentTransaction.commit();
        });

        HashMap data = new HashMap<>();
        data.put("ritID", rit.getID());
        DBConnection.getInstance().executeQuery(this.getActivity(), "GetRitInschrijvingenWAdres.php", data, true,
        output ->
        {
            try
            {
                JSONArray array = new JSONArray(output);

                for(int i = 0; i < array.length(); i++)
                {
                    JSONObject o =  (JSONObject) array.get(i);
                    int medewerkerID = Integer.parseInt((String)o.get("MedewerkerID"));
                    String ritID = (String) o.get("RitID");
                    String rolString = (String) o.get("Rol");
                    String adres = (String) o.get("Adres");
                    Rol rol = (rolString.equals("Hoofdbestuurder")) ? Rol.HOOFDBESTUURDER : ((rolString.equals("Reservebestuurder")) ? Rol.RESERVEBESTUURDER : Rol.MEERIJDER);
                    RitInschrijvingWAdres ritInschrijving = new RitInschrijvingWAdres(medewerkerID, ritID, rol, adres);
                    inschrijvingen.add(ritInschrijving);
                }

                routeBegeleidingBtn.setOnClickListener(v ->
                {
                    String url = createGoogleMapsNavigationUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                });
                buildTableLayout();

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        });

    }

    /**
     * Bouwt de lijst van alle passagiers met adressen in een TableView.
     */
    private void buildTableLayout()
    {
        final Context context = this.getContext();
        tableLayout.removeAllViews();
        for(int i = 0; i < inschrijvingen.size(); i++)
        {
            boolean isLast = (i == (inschrijvingen.size() - 1));
            RitInschrijving ri = inschrijvingen.get(i);
            HashMap data = new HashMap<>();
            data.put("medewerkerID", Integer.toString(ri.getMedewerkerID()));
            DBConnection.getInstance().executeQuery(this.getActivity(), "GetSpecificMedewerker.php", data, true,
            output ->
            {
                try
                {
                    JSONArray array = new JSONArray(output);

                    if (array.length() > 0)
                    {
                        JSONObject o = (JSONObject) array.get(0);
                        String naam = (String) o.get("Naam");
                        String achternaam = (String) o.get("Achternaam");
                        String adres = (String) o.get("Adres");

                        LayoutInflater inflater = LayoutInflater.from(context);
                        TableRow layout = (TableRow) inflater.inflate(R.layout.layout_ritdetails_item, null, false);
                        ImageView ivSideBar = layout.findViewById(R.id.ivSideBar);
                        TextView tvNaam = layout.findViewById(R.id.tvMedewerkerNaam);
                        TextView tvAdres = layout.findViewById(R.id.tvMedewerkerAdres);
                        tvNaam.setText(naam + " " + achternaam);
                        tvAdres.setText(adres);
                        if(isLast)
                        {
                            ivSideBar.setImageResource(R.drawable.rit_details_sidebar_b);
                        }
                        tableLayout.addView(layout);

                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            });
        }
    }

    /**
     * Stelt een Google Maps navigatie url samen op basis van de inschrijvingen.
     * @return String navigation url
     */
    private String createGoogleMapsNavigationUrl()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.google.com/maps/dir/?api=1&destination=Borchwerf,Roosendaal,Netherlands");

        for(int i = 0; i < inschrijvingen.size(); i++)
        {
            RitInschrijvingWAdres riwa = inschrijvingen.get(i);
            String adres = riwa.getMedewerkerAdres();
            String encodedAdres = adres;
            try
            {
                encodedAdres = URLEncoder.encode(adres, "UTF-8");
            }
            catch(UnsupportedEncodingException e)
            {
                // should never happen as UTF-8 is always supported
            }

            if(i == 0)
            {
                sb.append("&origin=" + encodedAdres + "&waypoints=");
            }
            else if(i == (inschrijvingen.size() - 1))
            {
                sb.append(encodedAdres);
            }
            else
            {
                sb.append(encodedAdres + "|");
            }
        }

        sb.append("&travelmode=driving&dir_action=navigate");
        return sb.toString();
    }

    /**
     * Private class die een RitInschrijving met adres van de medewerker die ingeschreven samenvoegt.
     */
    private class RitInschrijvingWAdres extends RitInschrijving
    {
        private String medewerkerAdres;

        public RitInschrijvingWAdres(int medewerkerID, String ritID, Rol rol, String medewerkerAdres)
        {
            super(medewerkerID, ritID, rol);
            this.medewerkerAdres = medewerkerAdres;
        }

        public String getMedewerkerAdres()
        {
            return medewerkerAdres;
        }
    }
}
