package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class MyLeavePage extends OrangeHrmPage {
    private static final String PATH = "/web/index.php/leave/viewMyLeaveList";

    private final By fromDate = inputByLabel("From Date");
    private final By toDate = inputByLabel("To Date");

    public MyLeavePage(WebDriver driver) {
        super(driver);
    }

    public MyLeavePage open() {
        driver.get(origin() + PATH);
        waitVisible(fromDate);
        waitForLoading();
        return this;
    }

    /**
     * Sets the date range and makes sure the given status is included in the status filter,
     * then searches. The default filter shows only some statuses and the current year, so a new
     * request would otherwise be hidden by the filter rather than by a defect.
     */
    public MyLeavePage search(String from, String to, String status) {
        typeDate(fromDate, from);
        typeDate(toDate, to);
        if (!selectedText("Show Leave with Status").contains(status)) {
            chooseOption("Show Leave with Status", status);
            closeDropdown();
        }
        click(SUBMIT);
        waitForLoading();
        return this;
    }

    public List<String> getDates() {
        return getColumnValues("Date");
    }

    public List<String> getLeaveTypes() {
        return getColumnValues("Leave Type");
    }

    public List<String> getStatuses() {
        return getColumnValues("Status");
    }
}
