package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PersonalDetailsPage extends OrangeHrmPage {
    private final By firstName = By.name("firstName");
    private final By employeeId = inputByLabel("Employee Id");

    public PersonalDetailsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return urlContains("/pim/viewPersonalDetails");
    }

    /** Waits until the form has been populated with the employee's data. */
    public PersonalDetailsPage waitUntilLoaded() {
        wait.until(d -> !d.findElement(firstName).getDomProperty("value").isBlank());
        return this;
    }

    public String getFirstName() {
        return waitVisible(firstName).getDomProperty("value");
    }

    public String getEmployeeId() {
        wait.until(d -> !d.findElement(employeeId).getDomProperty("value").isBlank());
        return driver.findElement(employeeId).getDomProperty("value");
    }
}
