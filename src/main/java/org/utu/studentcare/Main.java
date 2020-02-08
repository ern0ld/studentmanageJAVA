package org.utu.studentcare;

import org.utu.studentcare.applogic.DBApp;

/**
 * Ohjelman pääluokka (main(String[])).
 * Tuotu erilliseksi, jotta ohjelma voidaan käynnistää
 * IDEA:sta Play-napilla ilman Javan modulepath-säätöjä.

 * Ei tarvitse muokata.
 */
//Kytkin tähän hidastuksen, jos komentoriviparametrina on mitä tahansa, eli pituus on yli 0, asetetaan hidastus päälle
public class Main {
    public static void main(String[] args) {

        if (args.length >0) {
            try {

                    DBApp.setSlowMode(true);

            } catch (Exception ex) {
                System.err.println("Error reading arguments");

            }
        }
        else {DBApp.setSlowMode(false);}

        MainApp.launch(MainApp.class, args);
    }
}