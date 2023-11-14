package at.sirebi;

import com.google.common.base.Strings;
import org.eclipse.collections.api.factory.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerThread implements Runnable {

    public static final String url = "https://www.google.at/maps/search/";
    private static final String query = "Restaurant";

    private List<Integer> plzList = Lists.mutable.empty();
    private int id;

    public WorkerThread(List<Integer> plzList, int id) {
        this.plzList = plzList;
        this.id = id;
    }

    @Override
    public void run() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.images", 2);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        boolean first = true;

        for (int plz : plzList) {
            // search for query in plz region
            driver.get(url + plz + "+" + query);

            // skip the cookie page
            if (first) {
                removeCookieBanner(driver);
                first = false;
            }

            // scroll to the bottom to list all elements (max 120)
            scrollToBottom(driver);

            // extract all urls from the html code and add them to the thread queue
            List<String> urlList = Lists.mutable.empty();
            for (WebElement webElement : driver.findElements(By.className("hfpxzc"))) {
                String url = webElement.getAttribute("href").replace("https://www.google.at/maps/place/", "https://www.google.at/maps/search/");
                urlList.add(url);
            }

            // go through all urls
            for (String url : urlList) {
                driver.get(url);

                try {
                    List<WebElement> subElements = driver.findElements(By.className("CsEnBe"));
                    String website = null;
                    String phoneNumber = null;
                    for (WebElement s : subElements) {
                        if (checkIfAttributeExists(s, "data-tooltip")) {
                            // check if it is the website element
                            if ("Open website".equals(s.getAttribute("data-tooltip"))) {
                                website = s.getAttribute("href");
                                System.out.println("Website: " + website);
                            }
                            // check if it is the phone-number element
                            else if ("Telefonnummer kopieren".equals(s.getAttribute("data-tooltip"))) {
                                phoneNumber = s.getAttribute("aria-label");
                                System.out.println("Phone-number: " + phoneNumber);
                            }
                        }
                    }
                    // get name
                    String name = driver.findElement(By.className("DUwDvf")).getText();
                    System.out.println("Name: " + name);
                    // get rating
                    String rating = driver.findElement(By.xpath("//*[@id=\"QA0Szd\"]/div/div/div[1]/div[2]/div/div[1]/div/div/div[2]/div/div[1]/div[2]/div/div[1]/div[2]/span[1]/span[2]")).getAttribute("aria-label");
                    System.out.println("Rating: " + rating);
                    // get amount of reviews
                    String reviews = driver.findElement(By.xpath("//*[@id=\"QA0Szd\"]/div/div/div[1]/div[2]/div/div[1]/div/div/div[2]/div/div[1]/div[2]/div/div[1]/div[2]/span[2]/span/span")).getAttribute("aria-label");
                    System.out.println("Reviews: " + reviews);

                    appendLine("URL: " + url);
                    appendLine("Name: " + name);
                    appendLine("Website: " + website);
                    appendLine("PhoneNumber: " + phoneNumber);
                    appendLine("Rating: " + rating);
                    appendLine("Reviews: " + reviews);
                    appendLine(Strings.repeat("-",100));
                } catch (Exception e) {}
            }
        }
        driver.quit();
    }

    private static void tryToSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeCookieBanner(WebDriver driver) {
        driver.findElement(By.xpath("/html/body/c-wiz/div/div/div/div[2]/div[1]/div[3]/div[1]/div[1]/form[1]/div/div/button")).click();
    }

    private static boolean checkIfAttributeExists(WebElement webElement, String name) {
        try {
            webElement.getAttribute(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void appendLine(String line) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/websites" + id +".txt", true))) {
            writer.append(line + "\n");
            writer.flush();
        }
    }

    private static void scrollToBottom(WebDriver driver) {
        try {
            while (tryToGetElementByClassname(driver, "HlvSq") == null) {
                WebElement scrollTo = driver.findElement(By.className("lXJj5c"));
                new Actions(driver).moveToElement(scrollTo).perform();
            }
        } catch (Exception ignore) {}
    }

    private static WebElement tryToGetElementByClassname(WebDriver driver, String name) {
        try {
            return driver.findElement(By.className(name));
        } catch (Exception exception) {
            return null;
        }
    }

}
