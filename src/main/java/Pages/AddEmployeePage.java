package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class AddEmployeePage extends OrangeHrmPage {
    private static final String PATH = "/web/index.php/pim/addEmployee";

    private final By firstName = By.name("firstName");
    private final By middleName = By.name("middleName");
    private final By lastName = By.name("lastName");
    private final By employeeId = inputByLabel("Employee Id");

    public AddEmployeePage(WebDriver driver) {
        super(driver);
    }

    public AddEmployeePage open() {
        driver.get(origin() + PATH);
        waitVisible(firstName);
        try {
            // the Employee Id field is pre-filled asynchronously with the next generated id
            wait.until(d -> !d.findElement(employeeId).getDomProperty("value").isBlank());
        } catch (TimeoutException ignored) {
            // manual-entry configuration: field stays empty
        }
        installFeedbackCapture();
        return this;
    }

    public AddEmployeePage enterFirstName(String value) {
        type(firstName, value);
        return this;
    }

    public AddEmployeePage enterMiddleName(String value) {
        type(middleName, value);
        return this;
    }

    public AddEmployeePage enterLastName(String value) {
        type(lastName, value);
        return this;
    }

    /**
     * Replaces the (pre-filled) Employee Id. Select-all + delete is used because WebElement.clear() does
     * not reliably reset OrangeHRM's Vue-bound inputs, which left the generated id in front of the typed
     * one. The typed value is verified in the DOM and the field is blurred so the app's own validation runs.
     */
    public AddEmployeePage enterEmployeeId(String value) {
        WebElement field = waitVisible(employeeId);
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        field.sendKeys(value);
        try {
            wait.until(d -> value.equals(d.findElement(employeeId).getDomProperty("value")));
        } catch (TimeoutException e) {
            throw new IllegalStateException("Employee Id field holds '" + currentEmployeeIdValue()
                    + "' after typing '" + value + "'", e);
        }
        field.sendKeys(Keys.TAB);
        return this;
    }

    public String getEmployeeId() {
        return waitVisible(employeeId).getDomProperty("value");
    }

    public void clickSave() {
        click(SUBMIT);
    }

    /** True once the app has redirected to the new employee's Personal Details page. */
    public boolean isSaved() {
        return urlContains("/pim/viewPersonalDetails");
    }

    public boolean isAt() {
        return urlContains("/pim/addEmployee");
    }

    public String getFirstNameError() {
        return fieldErrorByInputName("firstName");
    }

    public String getLastNameError() {
        return fieldErrorByInputName("lastName");
    }

    public String getEmployeeIdError() {
        return fieldError("Employee Id");
    }

    /** Generic page state plus the current Employee Id field value. */
    @Override
    public String describeState() {
        return super.describeState() + "; employeeIdField='" + currentEmployeeIdValue() + "'";
    }

    private String currentEmployeeIdValue() {
        try {
            return driver.findElement(employeeId).getDomProperty("value");
        } catch (RuntimeException e) {
            return "<unavailable>";
        }
    }
}
