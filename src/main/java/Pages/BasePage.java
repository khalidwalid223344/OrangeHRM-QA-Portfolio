package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;

/**
 * Shared base for all page objects. Provides explicit-wait helpers only
 * (no implicit waits, no Thread.sleep).
 */
public class BasePage {
    protected static final Duration TIMEOUT = Duration.ofSeconds(15);

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {

        this.driver = driver;
        this.wait = new WebDriverWait(driver, TIMEOUT);

    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitVisible(locator).getText().trim();
    }

    /** True if the element becomes visible within the timeout, false otherwise. */
    protected boolean isVisible(By locator) {
        try {
            waitVisible(locator);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** True if the URL contains the fragment within the timeout, false otherwise. */
    protected boolean urlContains(String fragment) {
        try {
            return wait.until(ExpectedConditions.urlContains(fragment));
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Scheme and host of the page currently open, e.g. https://opensource-demo.orangehrmlive.com */
    protected String origin() {
        URI uri = URI.create(driver.getCurrentUrl());
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    /** True if the element is gone/hidden within the timeout, false otherwise. */
    protected boolean waitInvisible(By locator) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}
