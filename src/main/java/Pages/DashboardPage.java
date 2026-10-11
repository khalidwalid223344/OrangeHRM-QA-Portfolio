package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {
    private static final String DASHBOARD_URL_FRAGMENT = "/dashboard/index";

    private final By moduleTitle = By.cssSelector(".oxd-topbar-header-breadcrumb-module");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return urlContains(DASHBOARD_URL_FRAGMENT) && isVisible(moduleTitle);
    }

    public String getModuleTitle() {
        return getText(moduleTitle);
    }
}
