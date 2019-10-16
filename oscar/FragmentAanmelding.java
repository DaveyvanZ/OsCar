package com.dmnn.oscar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

/**
 * FragmentAanmelding Class
 * @author Nathan van Jole
 */
public class FragmentAanmelding extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{

    /**
     * MedewerkerID: het unieke ID van de huidige medewerker die ingelogd is.
     */
    private int medewerkerID;

    private Button btnVertrekdatum;
    private Button btnAankomsttijd;
    private Button btnAantalPicker;
    private Button btnMinAantal;
    private Button btnPlusAantal;
    private Button btnBevestig;
    private Button btnAnnuleer;

    private final int minVrijeplaatsen = 1;
    private final int maxVrijeplaatsen = 25;
    private int numVrijeplaatsen = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Bundle bundle = this.getArguments();
        medewerkerID = bundle.getInt("medewerkerID");

        return inflater.inflate(R.layout.fragment_aanmelding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        btnVertrekdatum = view.findViewById(R.id.btnVertrekdatum);
        btnAankomsttijd = view.findViewById(R.id.btnAankomsttijd);
        btnBevestig = view.findViewById(R.id.btnBevestig);
        btnAnnuleer = view.findViewById(R.id.btnAnnuleer);
        btnAantalPicker = view.findViewById(R.id.btnAantalPicker);
        btnAantalPicker.setText(Integer.toString(numVrijeplaatsen));
        btnAantalPicker.setEnabled(false);
        btnMinAantal = view.findViewById(R.id.btnMinAantal);
        btnPlusAantal = view.findViewById(R.id.btnPlusAantal);

        btnVertrekdatum.setOnClickListener(v ->
        {
            showDatePickerDialog();
        });

        btnAankomsttijd.setOnClickListener(v ->
        {
            showTimePickerDialog();
        });

        btnBevestig.setOnClickListener(v ->
        {
            bevestigAanmelding();
        });

        btnAnnuleer.setOnClickListener(v ->
        {
            annuleerAanmelding();
        });

        btnPlusAantal.setOnClickListener(v ->
        {
            if(numVrijeplaatsen < maxVrijeplaatsen)
            {
                numVrijeplaatsen++;
                btnAantalPicker.setText(Integer.toString(numVrijeplaatsen));
            }
        });

        btnMinAantal.setOnClickListener(v ->
        {
            if(numVrijeplaatsen > minVrijeplaatsen)
            {
                numVrijeplaatsen--;
                btnAantalPicker.setText(Integer.toString(numVrijeplaatsen));
            }
        });

    }

    /**
     * Bevestigt de aanmelding, en voegt de Rit toe aan de database.
     */
    private void bevestigAanmelding()
    {
        String[] datum = (btnVertrekdatum.getText().toString()).split("-");
        int day = Integer.parseInt(datum[0]);
        int month = Integer.parseInt(datum[1]);
        int year = Integer.parseInt(datum[2]);
        String vertrekdatum = (year + "-" + month + "-" + day);
        String aankomsttijd = (btnAankomsttijd.getText().toString());
        int vrijePlaatsen = numVrijeplaatsen;

        HashMap data = new HashMap<>();
        data.put("vertrekdatum", vertrekdatum);
        data.put("aankomsttijd", aankomsttijd);
        data.put("vrijePlaatsen", Integer.toString(vrijePlaatsen));
        data.put("medewerkerID", Integer.toString(medewerkerID));

        DBConnection.getInstance().executeQuery(this.getActivity(), "Aanmelding.php", data, true,
        output ->
        {
            if(output == "ERROR")
            {
                Toast.makeText(this.getActivity(), "Er ging iets mis!", Toast.LENGTH_LONG).show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.AlertDialogTheme);
                builder.setCancelable(true);
                builder.setTitle("Geslaagd");
                builder.setMessage("De aanmelding is geslaagd!");
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
            }
        });
    }

    /**
     * Annuleer de aanmelding en stuurt de gebruiker terug naar het home-fragment.
     */
    private void annuleerAanmelding()
    {
        FragmentHome fragmentHome = new FragmentHome();

        FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putInt("medewerkerID", medewerkerID);
        fragmentHome.setArguments(fragmentBundle);

        fragmentTransaction.replace(R.id.frame_content, fragmentHome);
        fragmentTransaction.commit();
    }

    /**
     * Laat een DatePicker zien, waarin de gebruiker de Vertrekdatum kan invullen.
     */
    private void showDatePickerDialog()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this.getContext(),
                R.style.DialogTheme,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * Laat een TimePicker zien, waarin de gebruiker een aankomsttijd kan invullen.
     */
    private void showTimePickerDialog()
    {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this.getContext(),
                R.style.DialogTheme,
                this,
                Calendar.getInstance().get(Calendar.HOUR),
                Calendar.getInstance().get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        String date = (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "-" + (month + 1 < 10 ? ("0" + (month + 1)) : month  + 1) + "-" + year;
        btnVertrekdatum.setText(date);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        String time = (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute);
        btnAankomsttijd.setText(time);

    }
}
