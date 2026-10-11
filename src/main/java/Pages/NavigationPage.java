package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Side menu and user-menu actions available on every authenticated page. */
public class NavigationPage extends BasePage {
    private final By anyMenuItem = By.cssSelector(".oxd-main-menu-item");
    private final By userDropdown = By.cssSelector(".oxd-userdropdown-tab");
    private final By logoutLink = By.xpath("//a[normalize-space()='Logout']");

    public NavigationPage(WebDriver driver) {
        super(driver);
    }

    private By menuItem(String name) {
        return By.xpath("//a[contains(@class,'oxd-main-menu-item')][normalize-space()='" + name + "']");
    }

    /** Waits for the side menu to render, then reports whether the named item exists. */
    public boolean isMenuVisible(String name) {
        waitVisible(anyMenuItem);
        return !driver.findElements(menuItem(name)).isEmpty();
    }

    public void openMenu(String name) {
        click(menuItem(name));
    }

    public LoginPage logout() {
        click(userDropdown);
        click(logoutLink);
        LoginPage loginPage = new LoginPage(driver);
        loginPage.isAt();
        return loginPage;
    }

    /** True if the browser URL stops containing the fragment within a short time (redirect/block). */
    public boolean isRedirectedAwayFrom(String urlFragment) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(d -> !d.getCurrentUrl().contains(urlFragment));
        } catch (TimeoutException e) {
            return false;
        }
    }
}
