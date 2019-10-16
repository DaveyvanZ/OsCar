package com.dmnn.oscar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * FragmentRit class
 * @author Nathan van Jole
 */
public class FragmentRit extends Fragment
{

    /**
     * MedewerkerID: het unieke ID van de huidige medewerker die ingelogd is.
     */
    private int medewerkerID;

    /**
     * Rit: de rit waarvan de gegevens worden weergegeven.
     */
    private Rit rit;

    private int ritDuurMinuten;

    private ImageView ritMapIv;
    private TextView bestuurderTv;
    private TextView vertrekdatumTv;
    private TextView vertrektijdTv;
    private TextView aankomsttijdTv;
    private TextView vertrekplaatsTv;
    private Button inuitschrijfBtn;
    private Button routeInfoBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        medewerkerID = bundle.getInt("medewerkerID");
        rit = (Rit) bundle.getSerializable("rit");

        return inflater.inflate(R.layout.fragment_rit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        calculateVertrektijd();

        ritMapIv = view.findViewById(R.id.ritMapIv);
        bestuurderTv = view.findViewById(R.id.bestuurderValueTv);
        vertrekdatumTv = view.findViewById(R.id.vertrekdatumValueTv);
        vertrektijdTv = view.findViewById(R.id.vertrektijdValueTv);
        aankomsttijdTv = view.findViewById(R.id.aankomsttijdValueTv);
        vertrekplaatsTv = view.findViewById(R.id.vertrekplaatsValueTv);
        inuitschrijfBtn = view.findViewById(R.id.inuitschrijfBtn);
        routeInfoBtn = view.findViewById(R.id.ritRouteBtn);

        Picasso.get().load(rit.getStaticMapImageUrl(17, true, "red")).into(ritMapIv);
        vertrekdatumTv.setText(rit.getVertrekdatumString());
        aankomsttijdTv.setText(rit.getAankomsttijdString());
        vertrekplaatsTv.setText(rit.getVertrekplaats());

        // Check if medewerker is ingeschreven voor deze rit
        HashMap data = new HashMap<String, String>();
        data.put("ritID", rit.getID());
        data.put("medewerkerID", Integer.toString(medewerkerID));

        DBConnection.getInstance().executeQuery(this.getActivity(), "GetSpecificInschrijving.php", data, true,
        output ->
        {
            RitInschrijving ritInschrijving = null;
            String bestuurder = null;
            try
            {
                JSONArray array = new JSONArray(output);
                if(array.length() > 0)
                {
                    JSONObject o = (JSONObject) array.get(0);
                    String ritID = (String) o.get("RitID");
                    int medewerkerID = Integer.parseInt((String)o.get("MedewerkerID"));
                    String rolString = (String) o.get("Rol");
                    bestuurder = (String) o.get("Bestuurder");
                    Rol rol = (rolString.equals("Hoofdbestuurder")) ? Rol.HOOFDBESTUURDER : ((rolString.equals("Reservebestuurder")) ? Rol.RESERVEBESTUURDER : Rol.MEERIJDER);
                    ritInschrijving = new RitInschrijving(medewerkerID, ritID, rol);
                }
            }
            catch (JSONException e)
            {
                bestuurder = output;
            }

            setup(ritInschrijving, bestuurder);
        });

    }

    /**
     * Veranderd de UI elements + event handlers gebaseerd op of de gebruiker ingeschreven is voor de route of niet.
     * @param ritInschrijving RitInschrijving ritInschrijving, null als gebruiker niet ingeschreven is
     * @param bestuurder String de naam en achternaam van de bestuurder van de rit
     */
    private void setup(RitInschrijving ritInschrijving, String bestuurder)
    {
        bestuurderTv.setText(bestuurder);
        if(ritInschrijving == null)
        {
            routeInfoBtn.setVisibility(View.GONE);
            inuitschrijfBtn.setText("Inschrijven");
            inuitschrijfBtn.setOnClickListener(v ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Bevestiging");
                builder.setMessage("Weet u zeker dat u zich wilt inschrijven voor deze rit?");
                builder.setPositiveButton("Ja",
                        (dialog, which) ->
                        {
                            schrijfMedewerkerIn(medewerkerID, rit.getID());
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> {});

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }
        else
        {
            routeInfoBtn.setVisibility(View.VISIBLE);
            routeInfoBtn.setOnClickListener(v ->
            {
                FragmentRitDetails fragmentRitDetails = new FragmentRitDetails();

                FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Bundle fragmentBundle = new Bundle();
                fragmentBundle.putInt("medewerkerID", medewerkerID);
                fragmentBundle.putSerializable("rit", rit);
                fragmentRitDetails.setArguments(fragmentBundle);

                fragmentTransaction.replace(R.id.frame_content, fragmentRitDetails);
                fragmentTransaction.commit();
            });

            inuitschrijfBtn.setText("Uitschrijven");
            inuitschrijfBtn.setOnClickListener(v ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Bevestiging");
                builder.setMessage("Weet u zeker dat u zich wilt uitschrijven voor deze rit?");
                builder.setPositiveButton("Ja",
                        (dialog, which) ->
                        {
                            schrijfMedewerkerUit(medewerkerID, rit.getID());
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> {});

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }
    }

    /**
     * Schrijft een medewerker in voor een specifieke rit en handelt foutmeldingen.
     * @param medewerkerID ID van de medewerker
     * @param ritID ID van de rit
     */
    private void schrijfMedewerkerIn(int medewerkerID, String ritID)
    {
        HashMap data = new HashMap<String, String>();
        data.put("medewerkerID", Integer.toString(medewerkerID));
        data.put("ritID", ritID);

        DBConnection.getInstance().executeQuery(this.getActivity(), "Inschrijving.php", data, true,
        output ->
        {
            if(output.equals("ERROR"))
            {
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                confirmBuilder.setCancelable(true);
                confirmBuilder.setTitle("Bevestiging");
                confirmBuilder.setMessage("U staat op het punt om zich in te schrijven als reservebestuurder voor deze rit. Dit betekend dat u zult moeten invallen wanneer de hoofdbestuurder zich afmeldt. Weet u zeker dat u door wilt gaan?");
                confirmBuilder.setPositiveButton("OK",
                        (dialog, which) ->
                        {
                            DBConnection.getInstance().executeQuery(this.getActivity(), "InschrijvingReservebestuurder.php", data, true,
                            result ->
                            {
                                AlertDialog.Builder successBuilder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                                successBuilder.setCancelable(true);
                                successBuilder.setTitle("Gelukt");
                                successBuilder.setMessage("Succesvol ingeschreven voor rit " + rit.getID() + ".");
                                successBuilder.setPositiveButton("OK", (d, w) ->
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
                            });
                        });
                confirmBuilder.setNegativeButton("Annuleer", (dialog, which) -> {});
                AlertDialog dialog = confirmBuilder.create();
                dialog.show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Gelukt");
                builder.setMessage("Succesvol ingeschreven voor rit " + rit.getID() + ".");
                builder.setPositiveButton("OK", (dialog, which) ->
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
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    /**
     * Schrijft een medewerker uit voor een rit en handelt foutmeldingen.
     * @param medewerkerID ID van de medewerker
     * @param ritID ID van de rit
     */
    private void schrijfMedewerkerUit(int medewerkerID, String ritID)
    {
        HashMap data = new HashMap<String, String>();
        data.put("medewerkerID", Integer.toString(medewerkerID));
        data.put("ritID", ritID);

        DBConnection.getInstance().executeQuery(this.getActivity(), "Uitschrijven.php", data, true,
        output ->
        {
            System.out.println(output);
            if(output.equals("TIME_LIMIT"))
            {
                // reservebestuurder probeert zich uit te schrijven... dit mag niet
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Oops");
                builder.setMessage("U kunt zich maximaal tot 1 dag voor de rit uitschrijven!");
                builder.setPositiveButton("OK",
                        (dialog, which) -> {});
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if(output.equals("ERROR"))
            {
                // reservebestuurder probeert zich uit te schrijven... dit mag niet
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Oops");
                builder.setMessage("U kunt zich niet uitschrijven als reservebestuurder.");
                builder.setPositiveButton("OK",
                        (dialog, which) -> {});
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Gelukt");
                builder.setMessage("Succesvol uitgeschreven voor rit " + rit.getID() + ".");
                builder.setPositiveButton("OK",
                        (dialog, which) ->
                        {
                            FragmentRoutes fragmentRoutes = new FragmentRoutes();

                            FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                            Bundle fragmentBundle = new Bundle();
                            fragmentBundle.putInt("medewerkerID", medewerkerID);
                            fragmentRoutes.setArguments(fragmentBundle);

                            fragmentTransaction.replace(R.id.frame_content, fragmentRoutes);
                            fragmentTransaction.commit();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                if(output.equals("UPDATE"))
                {
                    calculateVertrektijd();
                }
            }
        });
    }

    /**
     * Berekent de vertrektijd door te kijken hoelang het duurt om alle passagiers op te halen,
     * en dit vervolgens van de aankomsttijd af te trekken. Geeft vervolgens deze vertrektijd ook weer op het scherm.
     */
    private void calculateVertrektijd()
    {
        HashMap data = new HashMap<String, String>();
        data.put("ritID", rit.getID());

        DBConnection.getInstance().executeQuery(this.getActivity(), "GetRitDuur.php", data, true,
        output ->
        {
            Double d = Integer.parseInt(output) / 60.0;
            Long lRitDuurMinuten = Math.round(d);
            ritDuurMinuten = lRitDuurMinuten.intValue();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            Date aankomsttijd = rit.getAankomsttijd();
            Calendar cal = Calendar.getInstance();
            cal.setTime(aankomsttijd);
            cal.add(Calendar.MINUTE, -(ritDuurMinuten));
            Time ritVertrektijd = new Time(cal.getTime().getTime());
            String ritVertrektijdString = df.format(cal.getTime());
            vertrektijdTv.setText(ritVertrektijdString);
        });
    }
}
