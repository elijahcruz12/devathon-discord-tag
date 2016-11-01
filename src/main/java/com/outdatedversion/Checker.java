package com.outdatedversion;

import org.jsoup.Jsoup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * OutdatedVersion
 * Oct/31/2016 (12:23 AM)
 */

public class Checker
{

    /** asynchronously run requests */
    private final ExecutorService executor;

    /**
     * in charge of handling requests
     * to the Devathon site.
     */
    public Checker()
    {
        executor = Executors.newCachedThreadPool();

        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    /**
     * check if there's account bound to the
     * provided username.
     *
     * @param username the username we're checking
     * @param exists whether or not an account exists
     *               for the provided name.
     */
    public void check(String username, Consumer<Boolean> exists)
    {
        executor.submit(() ->
        {
            try
            {
                exists.accept(!Jsoup.connect("https://devathon.org/user/" + username)
                                    .userAgent("Mozilla/5.0 (Discord Role Checker)")
                                    .get().location().contains("error"));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        });
    }

}
