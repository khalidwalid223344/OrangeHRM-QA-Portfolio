package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AddUserPage extends OrangeHrmPage {
    private static final String PATH = "/web/index.php/admin/saveSystemUser";

    private final By employeeName = inputByLabel("Employee Name");
    private final By username = inputByLabel("Username");
    private final By password = inputByLabel("Password");
    private final By confirmPassword = inputByLabel("Confirm Password");

    public AddUserPage(WebDriver driver) {
        super(driver);
    }

    public AddUserPage open() {
        driver.get(origin() + PATH);
        waitVisible(username);
        return this;
    }

    public AddUserPage selectUserRole(String role) {
        chooseOption("User Role", role);
        return this;
    }

    public AddUserPage selectStatus(String status) {
        chooseOption("Status", status);
        return this;
    }

    /** Types into the Employee Name autocomplete and picks the suggestion containing {@code match}. */
    public AddUserPage selectEmployee(String typed, String match) {
        type(employeeName, typed);
        click(By.xpath("//div[@role='listbox']//span[contains(normalize-space(),'" + match + "')]"));
        return this;
    }

    public AddUserPage enterUsername(String value) {
        type(username, value);
        return this;
    }

    public AddUserPage enterPassword(String value) {
        type(password, value);
        type(confirmPassword, value);
        return this;
    }

    public void clickSave() {
        click(SUBMIT);
    }

    public boolean isSaved() {
        return urlContains("/admin/viewSystemUsers");
    }

    public boolean isAt() {
        return urlContains("/admin/saveSystemUser");
    }

    public String getUsernameError() {
        return fieldError("Username");
    }
}
