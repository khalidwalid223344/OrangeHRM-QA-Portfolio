package Test;

import Pages.ApplyLeavePage;
import Pages.MyLeavePage;
import Utils.DataFactory;
import Utils.JsonReader;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;

/**
 * REQ-007 Apply for Leave, run as the configured admin (who is also an employee).
 * Needs a leave entitlement; otherwise the tests fail fast with a clear precondition message.
 * Dates are random future Mondays so reruns never overlap. Submitted leave cannot be removed through the UI,
 * so each submission consumes one day of balance on the (daily-reset) demo.
 * TC-REQ007-08 (leave listed with correct type/dates) is covered by TC-REQ007-01.
 */
@Epic("OrangeHRM")
@Feature("Leave")
public class ApplyLeaveTests extends HrmBaseTest {
    private static final JsonNode DATA = JsonReader.getTestData("leave");

    private ApplyLeavePage apply;
    private String dateFormat;

    @BeforeMethod
    public void openApplyLeave() {
        loginAsAdmin();
        apply = new ApplyLeavePage(driver).open();
        if (!apply.hasLeaveTypes()) {
            throw new IllegalStateException("Precondition not met: the user has no leave type with balance");
        }
        dateFormat = apply.getDateFormat();
    }

    // TC-REQ007-01 | REQ-007
    // Related Bug: BUG-005 (submitted leave not listed in My Leave)
    @Test(description = "TC-REQ007-01: Submit leave with valid data and find it in My Leave", groups = "known-defect")
    @Story("REQ-007") @TmsLink("TC-REQ007-01") @Issue("BUG-005")
    public void TC_REQ007_01_verifySubmittedLeaveAppearsInMyLeave() {
        LocalDate from = DataFactory.futureMonday();
        LocalDate to = from.plusDays(1);
        String leaveType = submit(from, to);

        MyLeavePage myLeave = new MyLeavePage(driver).open().search(fmt(from), fmt(to), pending());

        Assert.assertTrue(myLeave.getLeaveTypes().contains(leaveType),
                "Submitted leave type '" + leaveType + "' should be listed; My Leave shows: " + myLeave.getRecordsSummary());
        Assert.assertTrue(myLeave.getDates().stream().anyMatch(d -> d.contains(fmt(from))),
                "Submitted dates should be listed: " + myLeave.getDates());
    }

    // TC-REQ007-02 | REQ-007
    @Test(description = "TC-REQ007-02: Submit leave without Leave Type")
    @Story("REQ-007") @TmsLink("TC-REQ007-02")
    public void TC_REQ007_02_verifyLeaveTypeIsRequired() {
        LocalDate from = DataFactory.futureMonday();
        apply.enterFromDate(fmt(from)).enterToDate(fmt(from)).clickApply();

        assertRequired(apply.getLeaveTypeError(), "Leave Type");
        Assert.assertTrue(apply.isAt(), "Request must not be submitted");
    }

    // TC-REQ007-03 | REQ-007
    @Test(description = "TC-REQ007-03: Submit leave without From Date")
    @Story("REQ-007") @TmsLink("TC-REQ007-03")
    public void TC_REQ007_03_verifyFromDateIsRequired() {
        apply.selectFirstLeaveType();
        apply.enterFromDate("").enterToDate(fmt(DataFactory.futureMonday())).clickApply();

        assertRequired(apply.getFromDateError(), "From Date");
        Assert.assertTrue(apply.isAt(), "Request must not be submitted");
    }

    // TC-REQ007-04 | REQ-007
    @Test(description = "TC-REQ007-04: Submit leave without To Date")
    @Story("REQ-007") @TmsLink("TC-REQ007-04")
    public void TC_REQ007_04_verifyToDateIsRequired() {
        apply.selectFirstLeaveType();
        apply.enterFromDate(fmt(DataFactory.futureMonday())).enterToDate("").clickApply();

        assertRequired(apply.getToDateError(), "To Date");
        Assert.assertTrue(apply.isAt(), "Request must not be submitted");
    }

    // TC-REQ007-05 | REQ-007
    @Test(description = "TC-REQ007-05: Submit leave with all mandatory fields empty")
    @Story("REQ-007") @TmsLink("TC-REQ007-05")
    public void TC_REQ007_05_verifyAllMandatoryFieldsAreRequired() {
        apply.enterFromDate("").enterToDate("").clickApply();

        assertRequired(apply.getLeaveTypeError(), "Leave Type");
        assertRequired(apply.getFromDateError(), "From Date");
        assertRequired(apply.getToDateError(), "To Date");
        Assert.assertTrue(apply.isAt(), "Request must not be submitted");
    }

    // TC-REQ007-06 | REQ-007
    @Test(description = "TC-REQ007-06: Submit leave where To Date is earlier than From Date")
    @Story("REQ-007") @TmsLink("TC-REQ007-06")
    public void TC_REQ007_06_verifyToDateEarlierThanFromDateIsRejected() {
        LocalDate from = DataFactory.futureMonday();
        apply.selectFirstLeaveType();
        apply.enterFromDate(fmt(from)).enterToDate(fmt(from.minusDays(1))).clickApply();

        Assert.assertFalse(apply.getToDateError().isBlank(),
                "A validation message should state that To Date cannot be earlier than From Date");
        Assert.assertTrue(apply.isAt(), "Request must not be submitted");
    }

    // TC-REQ007-07 | REQ-007
    // Related Bug: BUG-005
    @Test(description = "TC-REQ007-07: Same-day leave is submitted and listed in My Leave", groups = "known-defect")
    @Story("REQ-007") @TmsLink("TC-REQ007-07") @Issue("BUG-005")
    public void TC_REQ007_07_verifySameDayLeaveAppearsInMyLeave() {
        LocalDate day = DataFactory.futureMonday();
        submit(day, day);

        MyLeavePage myLeave = new MyLeavePage(driver).open().search(fmt(day), fmt(day), pending());

        Assert.assertFalse(myLeave.getStatuses().isEmpty(),
                "Same-day leave should be listed; My Leave shows: " + myLeave.getRecordsSummary());
    }

    // TC-REQ007-09 | REQ-007
    // Related Bug: BUG-005
    @Test(description = "TC-REQ007-09: Newly submitted leave has status Pending Approval", groups = "known-defect")
    @Story("REQ-007") @TmsLink("TC-REQ007-09") @Issue("BUG-005")
    public void TC_REQ007_09_verifyNewLeaveStatusIsPendingApproval() {
        LocalDate day = DataFactory.futureMonday();
        submit(day, day);

        MyLeavePage myLeave = new MyLeavePage(driver).open().search(fmt(day), fmt(day), pending());

        Assert.assertTrue(myLeave.getStatuses().stream().anyMatch(s -> s.startsWith(pending())),
                "Status 'Pending Approval' expected; My Leave shows: " + myLeave.getStatuses());
    }

    // TC-REQ007-10 | REQ-007
    // Related Bug: BUG-005 (filters are re-applied after the reload because the page resets them)
    @Test(description = "TC-REQ007-10: Submitted leave persists after page refresh", groups = "known-defect")
    @Story("REQ-007") @TmsLink("TC-REQ007-10") @Issue("BUG-005")
    public void TC_REQ007_10_verifySubmittedLeavePersistsAfterRefresh() {
        LocalDate day = DataFactory.futureMonday();
        submit(day, day);
        MyLeavePage myLeave = new MyLeavePage(driver).open().search(fmt(day), fmt(day), pending());
        Assert.assertFalse(myLeave.getStatuses().isEmpty(),
                "Leave should be listed before refresh; My Leave shows: " + myLeave.getRecordsSummary());

        driver.navigate().refresh();
        myLeave = new MyLeavePage(driver);
        myLeave.open().search(fmt(day), fmt(day), pending());

        Assert.assertTrue(myLeave.getStatuses().stream().anyMatch(s -> s.startsWith(pending())),
                "Leave should remain listed as Pending Approval after refresh: " + myLeave.getStatuses());
    }

    /** Submits a request with the first available leave type; returns that leave type. */
    private String submit(LocalDate from, LocalDate to) {
        String leaveType = apply.selectFirstLeaveType();
        apply.enterFromDate(fmt(from)).enterToDate(fmt(to)).clickApply();
        Assert.assertTrue(apply.hasSuccessToast(),
                "Leave should be submitted successfully but feedback was: '" + apply.getFeedbackText()
                        + "'. " + apply.describeState());
        return leaveType;
    }

    private String fmt(LocalDate date) {
        return DataFactory.format(date, dateFormat);
    }

    private String pending() {
        return DATA.get("pendingStatus").asText();
    }

    private void assertRequired(String actual, String field) {
        Assert.assertTrue(actual.toLowerCase().contains("required"),
                "A required-field message should be shown for " + field + " but was: '" + actual + "'");
    }
}
