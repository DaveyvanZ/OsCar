package com.dmnn.oscar;

/**
 * Medewerker class
 * @author Nathan van Jole
 */
public class Medewerker
{
    private int nummer;
    private int ID;
    private String naam;
    private String achternaam;
    private String adres;
    private String wachtwoord;
    private String bedrijf;

    public Medewerker(int nummer, int ID, String naam, String achternaam, String adres, String wachtwoord, String bedrijf)
    {
        this.nummer = nummer;
        this.ID = ID;
        this.naam = naam;
        this.achternaam = achternaam;
        this.adres = adres;
        this.wachtwoord = wachtwoord;
        this.bedrijf = bedrijf;
    }

    public int getNummer()
    {
        return nummer;
    }

    public int getID()
    {
        return ID;
    }

    public String getNaam()
    {
        return naam;
    }

    public String getAchternaam()
    {
        return achternaam;
    }

    public String getAdres()
    {
        return adres;
    }

    public String getWachtwoord()
    {
        return wachtwoord;
    }

    public String getBedrijf()
    {
        return bedrijf;
    }
}
