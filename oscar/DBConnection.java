package com.dmnn.oscar;

import android.content.Context;
import com.kosalgeek.asynctask.PostResponseAsyncTask;
import com.kosalgeek.asynctask.AsyncResponse;
import java.util.HashMap;

/**
 * DBConnection class
 * @author Nathan van Jole
 */
public class DBConnection
{

    private static final String BASE_URL = "http://dev.nathanvj.com/";
    private static final String DIRECTORY = "OsCar";

    private static final String URL = BASE_URL + DIRECTORY;
    private static DBConnection instance = null;

    /**
     * Private constructor to prevent instantiation.
     */
    private DBConnection() {}

    /**
     * Singleton pattern
     * @return DBConnection
     */
    public static DBConnection getInstance()
    {
        if(instance == null)
        {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Execute a query
     * @param context The context from where the query is executed (use ClassName.this, ex. MainActivity.this)
     *                When using a fragment use this.getActivity().
     * @param page The server side page (ex. GetUserData.php)
     * @param postData A HashMap containing the expected parameters
     * @param showLoadingMessage Set true to show loading message until callback is called
     * @param callback Function that executes after request is completed, commonly a lambda
     */
    public void executeQuery(Context context, String page, HashMap postData, boolean showLoadingMessage, AsyncResponse callback)
    {
        PostResponseAsyncTask insertTask = new PostResponseAsyncTask(context, postData,
                showLoadingMessage, callback);

        insertTask.execute(URL + "/" + page);
    }

}