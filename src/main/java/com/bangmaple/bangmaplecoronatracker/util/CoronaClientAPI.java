/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bangmaple.bangmaplecoronatracker.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author bangmaple
 */
public class CoronaClientAPI {

    private static final String BASE_URI = "https://corona-api.com/countries";

    public static final String getJSON() {
        try {
            URL url = new URL(BASE_URI);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
            connection.connect();
            String response;
            try (BufferedReader serverResponse = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                response = serverResponse.readLine();
            }
            return response;
        } catch (IOException e) {
        }
        return null;
    }
}
