package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class AdminUsersPage extends OrangeHrmPage {
    private static final String PATH = "/web/index.php/admin/viewSystemUsers";

    private final By username = inputByLabel("Username");

    public AdminUsersPage(WebDriver driver) {
        super(driver);
    }

    public AdminUsersPage open() {
        driver.get(origin() + PATH);
        waitVisible(username);
        waitForLoading();
        getRecordsSummary();
        return this;
    }

    public AdminUsersPage searchByUsername(String value) {
        type(username, value);
        click(SUBMIT);
        waitForLoading();
        getRecordsSummary();
        return this;
    }

    public List<String> getUsernames() {
        return getColumnValues("Username");
    }

    public List<String> getEmployeeNames() {
        return getColumnValues("Employee Name");
    }

    public List<String> getUserRoles() {
        return getColumnValues("User Role");
    }

    public List<String> getStatuses() {
        return getColumnValues("Status");
    }

    public boolean isAt() {
        return urlContains("/admin/");
    }
}
