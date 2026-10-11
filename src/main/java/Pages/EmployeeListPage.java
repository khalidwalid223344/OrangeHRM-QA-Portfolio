package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class EmployeeListPage extends OrangeHrmPage {
    private static final String PATH = "/web/index.php/pim/viewEmployeeList";
    private static final String PLACEHOLDER = "-- Select --";

    private final By employeeName = inputByLabel("Employee Name");
    private final By employeeId = inputByLabel("Employee Id");
    private final By resetButton = By.cssSelector("button[type='reset']");

    public EmployeeListPage(WebDriver driver) {
        super(driver);
    }

    public EmployeeListPage open() {
        driver.get(origin() + PATH);
        waitVisible(employeeId);
        waitForLoading();
        getRecordsSummary();
        return this;
    }

    public EmployeeListPage searchByEmployeeId(String id) {
        type(employeeId, id);
        clickSearch();
        return this;
    }

    public EmployeeListPage searchByName(String name) {
        enterName(name);
        clickSearch();
        return this;
    }

    public EmployeeListPage enterName(String name) {
        type(employeeName, name);
        waitVisible(employeeName).sendKeys(Keys.TAB);
        return this;
    }

    public EmployeeListPage filterByEmploymentStatus(String status) {
        chooseOption("Employment Status", status);
        return this;
    }

    public EmployeeListPage filterBySubUnit(String subUnit) {
        chooseOption("Sub Unit", subUnit);
        return this;
    }

    public void clickSearch() {
        click(SUBMIT);
        waitForLoading();
        getRecordsSummary();
    }

    public void clickReset() {
        click(resetButton);
        waitForLoading();
        getRecordsSummary();
    }

    public boolean isEmploymentStatusFilterCleared() {
        return selectedText("Employment Status").equals(PLACEHOLDER);
    }

    public List<String> getEmployeeIds() {
        return getColumnValues("Id");
    }

    public List<String> getFirstNames() {
        return getColumnValues("First (& Middle) Name");
    }

    public List<String> getLastNames() {
        return getColumnValues("Last Name");
    }

    public List<String> getEmploymentStatuses() {
        return getColumnValues("Employment Status");
    }

    public List<String> getSubUnits() {
        return getColumnValues("Sub Unit");
    }
}
