package com.dmnn.oscar;

/**
 * RitInschrijving class
 * @author Nathan van Jole
 */
public class RitInschrijving
{

    private int medewerkerID;
    private String ritID;
    private Rol rol;

    /**
     * Constructs een RitInschrijving
     * @param medewerkerID ID van de medewerker
     * @param ritID ID van de rit
     * @param rol Rol rol van de medewerker
     */
    public RitInschrijving(int medewerkerID, String ritID, Rol rol)
    {
        this.medewerkerID = medewerkerID;
        this.ritID = ritID;
        this.rol = rol;
    }

    public int getMedewerkerID()
    {
        return medewerkerID;
    }

    public String getRitID()
    {
        return ritID;
    }

    public Rol getRol()
    {
        return rol;
    }
}
