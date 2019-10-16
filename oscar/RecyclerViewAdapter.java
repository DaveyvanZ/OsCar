package com.dmnn.oscar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * RecyclerViewAdapter class voor de RecyclerView in FragmentRoutes
 * @author Nathan van Jole
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{

    private int medewerkerID;
    private ArrayList<Rit> ritten;
    private Context context;
    private boolean isPlannedRitten;

    public RecyclerViewAdapter(Context context, int medewerkerID,
                               ArrayList<Rit> ritten, boolean plannedRitten)
    {
        this.medewerkerID = medewerkerID;
        this.ritten = ritten;
        this.context = context;
        this.isPlannedRitten = plannedRitten;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem_route, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        Rit currentRit = ritten.get(position);

        String staticMapImageUrl = currentRit.getStaticMapImageUrl(17, true, "red");
        Picasso.get().load(staticMapImageUrl).into(holder.routeMap);
        holder.routeDate.setText(currentRit.getVertrekdatumString() + ", " + currentRit.getAankomsttijdString());

        if(isPlannedRitten)
        {
            HashMap data = new HashMap<>();
            data.put("medewerkerID", Integer.toString(medewerkerID));
            data.put("ritID", currentRit.getID());
            DBConnection.getInstance().executeQuery(holder.routeMap.getContext(), "GetSpecificInschrijving.php", data, true,
            output ->
            {
                Rol rol = null;
                try
                {
                    JSONArray array = new JSONArray(output);

                    if(array.length() > 0)
                    {
                        JSONObject o = (JSONObject) array.get(0);
                        String rolString = (String) o.get("Rol");
                        rol = (rolString.equals("Hoofdbestuurder")) ? Rol.HOOFDBESTUURDER : ((rolString.equals("Reservebestuurder")) ? Rol.RESERVEBESTUURDER : Rol.MEERIJDER);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                int drawableResource = getDrawableID(rol);
                holder.detailImage.setImageResource(drawableResource);
            });
        }
        else
        {
            holder.detailImage.setVisibility(View.INVISIBLE);
        }

        holder.parentLayout.setOnClickListener(view ->
        {
            openFragmentRit(currentRit);
        });
    }

    /**
     * Stuurt de ID van de drawable terug die bij de rol past.
     * @param rol De rol van de medewerker
     * @return int drawable resource ID
     */
    private int getDrawableID(Rol rol)
    {
        if(rol == Rol.HOOFDBESTUURDER)
        {
            return R.drawable.driver_icon;
        }
        else if(rol == Rol.RESERVEBESTUURDER)
        {
            return R.drawable.backup_driver_icon;
        }
        else
        {
            return R.drawable.passenger_icon;
        }
    }

    /**
     * Opent rit Fragment
     * @param rit De rit die wordt doorgegeven aan FragmentRit
     */
    private void openFragmentRit(Rit rit)
    {
        FragmentRit fragmentRit = new FragmentRit();

        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putSerializable("rit", rit);
        fragmentBundle.putInt("medewerkerID", medewerkerID);
        fragmentRit.setArguments(fragmentBundle);

        fragmentTransaction.replace(R.id.frame_content, fragmentRit);
        fragmentTransaction.commit();
    }

    /**
     * Get de hoeveelheid items
     * @return int hoeveelheid items
     */
    @Override
    public int getItemCount()
    {
        return ritten.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView routeMap;
        ImageView detailImage;
        TextView routeDate;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView)
        {
            super(itemView);
            routeMap = itemView.findViewById(R.id.ivMap);
            detailImage = itemView.findViewById(R.id.ivExtraDetail);
            routeDate = itemView.findViewById(R.id.btnListRoute);
            parentLayout = itemView.findViewById(R.id.rlRouteListItem);
        }
    }

}