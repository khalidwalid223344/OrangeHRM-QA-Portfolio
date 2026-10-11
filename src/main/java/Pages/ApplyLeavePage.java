package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ApplyLeavePage extends OrangeHrmPage {
    private static final String PATH = "/web/index.php/leave/applyLeave";

    private final By fromDate = inputByLabel("From Date");
    private final By toDate = inputByLabel("To Date");
    private final By noLeaveTypes = By.xpath("//*[normalize-space()='No Leave Types with Leave Balance']");

    public ApplyLeavePage(WebDriver driver) {
        super(driver);
    }

    public ApplyLeavePage open() {
        driver.get(origin() + PATH);
        wait.until(d -> !d.findElements(selectByLabel("Leave Type")).isEmpty()
                || !d.findElements(noLeaveTypes).isEmpty());
        return this;
    }

    /** False when the user has no leave entitlement, so the apply form is not offered. */
    public boolean hasLeaveTypes() {
        return !driver.findElements(selectByLabel("Leave Type")).isEmpty();
    }

    public String selectFirstLeaveType() {
        return chooseFirstOption("Leave Type");
    }

    /** Date format configured in OrangeHRM, read from the input placeholder (e.g. yyyy-mm-dd). */
    public String getDateFormat() {
        return waitVisible(fromDate).getDomAttribute("placeholder");
    }

    public ApplyLeavePage enterFromDate(String value) {
        typeDate(fromDate, value);
        return this;
    }

    public ApplyLeavePage enterToDate(String value) {
        typeDate(toDate, value);
        return this;
    }

    public void clickApply() {
        click(SUBMIT);
    }

    public String getLeaveTypeError() {
        return fieldError("Leave Type");
    }

    public String getFromDateError() {
        return fieldError("From Date");
    }

    public String getToDateError() {
        return fieldError("To Date");
    }

    public boolean isAt() {
        return urlContains("/leave/applyLeave");
    }
}
