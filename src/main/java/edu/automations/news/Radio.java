package edu.automations.news;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;

public class Radio {

    private final static List<Integer> HOURS = Arrays.asList(1,3,5,7,8,9,11,13,15,17,19,21,23);

    public static void main(String[] args) throws Exception {

        System.setProperty("webdriver.chrome.driver", "c:\\repos\\selenium\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        System.out.println("Attaching to Chrome, this might take couple of seconds..");
        ChromeDriver driver = new ChromeDriver(options);
        String expectedTitle = "כרטיסייה חדשה";
        expectedTitle = "New tab";
        if (! Objects.requireNonNull(driver.getTitle()).contains(expectedTitle)) {
            throw new RuntimeException("Invalid Chrome's window's title: " + driver.getTitle());
        }

        // long initialSleep = 3_600_000L - System.currentTimeMillis() % 3_600_000L;
        // System.out.println("Waiting for the next hour... Going to sleep " + initialSleep / 60_000L + " minutes " + (initialSleep % 60_000L) / 1000 + " seconds");
        // Thread.sleep(initialSleep);

        while (true) {

            System.out.print("\rCurrent time: " + new Date());
            if (! HOURS.contains(new Date().getHours())) {
                ; // System.out.println("Skipping this hour");
            }
            else if (new Date().getMinutes() <= 1) {

                // System.out.println("Sleeping extra 30 sec because of the delay");
                // Thread.sleep(30 * 1_000L);

                driver = new ChromeDriver(options);
                driver.get("https://glzwizzlv.bynetcdn.com/glglz_mp3?awCollectionId=misc&awEpisodeId=glglz");
                driver.navigate().refresh();
                int duration = 6;
                System.out.println("\nListen for " + duration + " minutes..");
                Thread.sleep((duration - new Date().getMinutes()) * 60 * 1_000L);
                driver.navigate().back();
            }
            else {
                ; // System.out.println("Missed the beginning, will skip that hour");
            }

//            initialSleep = 3_600_000L - System.currentTimeMillis() % 3_600_000L; // - (60 - minutesInHours) * 300_000L;
//            System.out.println("Waiting for the next hour... Going to sleep " + initialSleep / 60_000L + " minutes " + (initialSleep % 60_000L) / 1000 + " seconds");
//            Thread.sleep(initialSleep);

//            System.out.println("Waiting for the next hour");
            Thread.sleep(60_000L);
        }
    }

}