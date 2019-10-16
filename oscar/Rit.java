package com.dmnn.oscar;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Rit class
 * @author Nathan van Jole
 */
public class Rit implements Serializable
{

    private final String GOOGLE_MAPS_API_KEY = "AIzaSyDCSYkNKByN1FhNC-EST63Wlbjg9RRgLzw";

    public enum Status {BEVESTIGD, ONBEVESTIGD};
    public enum HinderStatus {VERHINDERD, ONVERHINDERD};

    private String ID;
    private Date vertrekdatum;
    private Time aankomsttijd;
    private String vertrekplaats;
    private Status status;
    private HinderStatus hinderStatus;
    private int aantalVrijePlaatsen;

    /**
     * Constructs een Rit
     * @param ID ID van de rit
     * @param vertrekdatum Vertrekdatum van de rit
     * @param aankomsttijd Aankomsttijd van de rit
     * @param vertrekplaats Vertrekplaats van de rit
     * @param status Status van de rit
     * @param hinderStatus Hinderstatus van de rit
     * @param aantalVrijePlaatsen Aantal vrije plaatsen
     */
    public Rit(String ID, Date vertrekdatum, Time aankomsttijd, String vertrekplaats, String status, String hinderStatus, int aantalVrijePlaatsen)
    {
        this.ID = ID;
        this.vertrekdatum = vertrekdatum;
        this.aankomsttijd = aankomsttijd;
        this.vertrekplaats = vertrekplaats;
        this.status = (status.toLowerCase() == "bevestigd" ? Status.BEVESTIGD : Status.ONBEVESTIGD);
        this.hinderStatus = (hinderStatus.toLowerCase() == "verhinderd" ? HinderStatus.VERHINDERD : HinderStatus.ONVERHINDERD);;
        this.aantalVrijePlaatsen = aantalVrijePlaatsen;
    }

    public String getID()
    {
        return ID;
    }

    public Date getVertrekdatum()
    {
        return vertrekdatum;
    }

    public String getVertrekdatumString()
    {
        Date vdtm = this.getVertrekdatum();
        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM, yyyy");
        return dateFormat.format(vdtm);
    }

    public Time getAankomsttijd()
    {
        return aankomsttijd;
    }

    public String getAankomsttijdString()
    {
        Time vtijd = this.getAankomsttijd();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(vtijd);
    }

    public String getVertrekplaats()
    {
        return vertrekplaats;
    }

    public Status getStatus()
    {
        return status;
    }

    public HinderStatus getHinderStatus()
    {
        return hinderStatus;
    }

    public int getAantalVrijePlaatsen()
    {
        return aantalVrijePlaatsen;
    }

    public String getStaticMapImageUrl(int zoom, boolean withMarker, String color)
    {
        return getStaticMapImageUrl(zoom, withMarker, color, null);
    }

    public String getStaticMapImageUrl(int zoom, boolean withMarker, String color, String label)
    {
        if (!withMarker)
        {
            return "https://maps.googleapis.com/maps/api/staticmap?center=" + this.getVertrekplaats() + "&zoom=" + zoom + "&size=600x300&maptype=roadmap&key=" + GOOGLE_MAPS_API_KEY;
        }
        else
        {
            return "https://maps.googleapis.com/maps/api/staticmap?center=" + this.getVertrekplaats() + "&zoom=" + zoom + "&size=600x300&maptype=roadmap&markers=color:" + color + "%7Clabel:" + label + "%7C" + this.getVertrekplaats() + "&key=" + GOOGLE_MAPS_API_KEY;
        }
    }

}