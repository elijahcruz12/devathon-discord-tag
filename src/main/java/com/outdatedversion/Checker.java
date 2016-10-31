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

    private final ExecutorService executorService;

    public Checker()
    {
        executorService = Executors.newCachedThreadPool();

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    public void check(String username, Consumer<Boolean> exists)
    {
        executorService.submit(() ->
        {
            try
            {
                exists.accept(!Jsoup.connect("https://devathon.org/user/" + username)
                                    .userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0")
                                    .get().location().contains("error"));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        });
    }

}
