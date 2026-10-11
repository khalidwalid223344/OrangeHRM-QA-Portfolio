package Test;

import Pages.EmployeeListPage;
import Utils.DataFactory;
import Utils.Employee;
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

import java.util.List;

/** REQ-005 Search and Filter Employee List. Filter data comes from TestData.json ("search"). */
@Epic("OrangeHRM")
@Feature("PIM")
public class EmployeeSearchFilterTests extends HrmBaseTest {
    private static final JsonNode DATA = JsonReader.getTestData("search");

    private EmployeeListPage list;

    @BeforeMethod
    public void openEmployeeList() {
        loginAsAdmin();
        list = new EmployeeListPage(driver).open();
    }

    // TC-REQ005-001 | REQ-005
    @Test(description = "TC-REQ005-001: Search by name returns matching records only")
    @Story("REQ-005") @TmsLink("TC-REQ005-001")
    public void TC_REQ005_001_verifySearchByNameReturnsMatchingRecordsOnly() {
        Employee employee = createEmployee(DataFactory.firstName(), DataFactory.lastName());
        list.open().searchByName(employee.fullName());

        Assert.assertFalse(list.getEmployeeIds().isEmpty(), "At least the matching employee should be listed");
        assertAllContain(list.getFirstNames(), employee.firstName(), "first name");
        assertAllContain(list.getLastNames(), employee.lastName(), "last name");
    }

    // TC-REQ005-002 | REQ-005
    @Test(description = "TC-REQ005-002: Search returns all employees sharing the same name")
    @Story("REQ-005") @TmsLink("TC-REQ005-002")
    public void TC_REQ005_002_verifySearchReturnsAllEmployeesWithSameName() {
        String first = DataFactory.firstName();
        String last = DataFactory.lastName();
        Employee employee = createEmployee(first, last);
        createEmployee(first, last);
        list.open().searchByName(employee.fullName());

        Assert.assertEquals(list.getRecordCount(), 2, "Both employees with the shared name should be returned");
        assertAllContain(list.getFirstNames(), first, "first name");
        assertAllContain(list.getLastNames(), last, "last name");
    }

    // TC-REQ005-003 | REQ-005
    @Test(description = "TC-REQ005-003: Search with partial name returns matching records")
    @Story("REQ-005") @TmsLink("TC-REQ005-003")
    public void TC_REQ005_003_verifySearchWithPartialNameReturnsMatchingRecords() {
        Employee employee = createEmployee(DataFactory.firstName(), DataFactory.lastName());
        String partial = employee.firstName().substring(0, 6);
        list.open().searchByName(partial);

        Assert.assertTrue(list.getEmployeeIds().contains(employee.employeeId()),
                "Employee should be found by a partial name");
        assertAllContain(list.getFirstNames(), partial, "first name");
    }

    // TC-REQ005-004 | REQ-005
    @Test(description = "TC-REQ005-004: Search with a non-existing name shows No Records Found")
    @Story("REQ-005") @TmsLink("TC-REQ005-004")
    public void TC_REQ005_004_verifySearchWithNonExistingNameShowsNoRecordsFound() {
        list.searchByName("Zz" + DataFactory.lastName());

        Assert.assertTrue(list.getRecordsSummary().toLowerCase().contains("no records found"),
                "'No Records Found' state expected but was: " + list.getRecordsSummary());
        Assert.assertFalse(list.isErrorToastShown(), "No error should be shown");
    }

    // TC-REQ005-005 | REQ-005
    @Test(description = "TC-REQ005-005: Filter by Employment Status")
    @Story("REQ-005") @TmsLink("TC-REQ005-005")
    public void TC_REQ005_005_verifyFilterByEmploymentStatus() {
        String status = DATA.get("employmentStatus").asText();
        list.filterByEmploymentStatus(status);
        list.clickSearch();

        List<String> statuses = list.getEmploymentStatuses();
        Assert.assertFalse(statuses.isEmpty(), "Precondition: employees with status '" + status + "' must exist");
        assertAllEqual(statuses, status, "Employment Status");
    }

    // TC-REQ005-006 | REQ-005
    // Related Bug: BUG-004 (Sub-unit filter also returns other sub-units)
    @Test(description = "TC-REQ005-006: Filter by Sub-unit", groups = "known-defect")
    @Story("REQ-005") @TmsLink("TC-REQ005-006") @Issue("BUG-004")
    public void TC_REQ005_006_verifyFilterBySubUnit() {
        String subUnit = DATA.get("subUnit").asText();
        list.filterBySubUnit(subUnit);
        list.clickSearch();

        List<String> subUnits = list.getSubUnits();
        Assert.assertFalse(subUnits.isEmpty(), "Precondition: employees in sub-unit '" + subUnit + "' must exist");
        assertAllEqual(subUnits, subUnit, "Sub Unit");
    }

    // TC-REQ005-007 | REQ-005
    // Related Bug: BUG-004
    @Test(description = "TC-REQ005-007: Filter by Employment Status and Sub-unit together", groups = "known-defect")
    @Story("REQ-005") @TmsLink("TC-REQ005-007") @Issue("BUG-004")
    public void TC_REQ005_007_verifyFilterByEmploymentStatusAndSubUnitTogether() {
        String status = DATA.get("employmentStatus").asText();
        String subUnit = DATA.get("subUnit").asText();
        list.filterByEmploymentStatus(status).filterBySubUnit(subUnit);
        list.clickSearch();

        List<String> subUnits = list.getSubUnits();
        Assert.assertFalse(subUnits.isEmpty(), "Precondition: matching employees must exist");
        assertAllEqual(list.getEmploymentStatuses(), status, "Employment Status");
        assertAllEqual(subUnits, subUnit, "Sub Unit");
    }

    // TC-REQ005-008 | REQ-005
    @Test(description = "TC-REQ005-008: Clear applied filters and restore the employee list")
    @Story("REQ-005") @TmsLink("TC-REQ005-008")
    public void TC_REQ005_008_verifyClearFiltersRestoresEmployeeList() {
        int unfiltered = list.getRecordCount();
        list.filterByEmploymentStatus(DATA.get("employmentStatus").asText());
        list.clickSearch();

        list.clickReset();

        Assert.assertEquals(list.getRecordCount(), unfiltered, "Unfiltered results should be restored");
        Assert.assertTrue(list.isEmploymentStatusFilterCleared(), "Filter control should be cleared");
    }

    // TC-REQ005-009 | REQ-005
    @Test(description = "TC-REQ005-009: Search by name and filter by Employment Status together")
    @Story("REQ-005") @TmsLink("TC-REQ005-009")
    public void TC_REQ005_009_verifySearchAndFilterTogether() {
        // The first page of the unfiltered list on the shared demo may hold only employees without an
        // Employment Status, so the known employee is taken from the status-filtered result instead.
        String status = DATA.get("employmentStatus").asText();
        list.filterByEmploymentStatus(status);
        list.clickSearch();
        List<String> names = list.getFirstNames();
        Assert.assertFalse(names.isEmpty(), "Precondition: an employee with Employment Status '" + status
                + "' and a name must exist; list shows: " + list.getRecordsSummary());
        String firstName = names.get(0).split(" ")[0];
        Assert.assertFalse(firstName.isBlank(), "Precondition: the employee must have a first name");

        list.enterName(firstName);
        list.clickSearch();

        Assert.assertFalse(list.getEmployeeIds().isEmpty(),
                "The known employee '" + firstName + "' should match both criteria");
        assertAllEqual(list.getEmploymentStatuses(), status, "Employment Status");
        assertAllContain(list.getFirstNames(), firstName, "first name");
    }

    // TC-REQ005-010 | REQ-005
    @Test(description = "TC-REQ005-010: Search with an empty field shows the default employee list")
    @Story("REQ-005") @TmsLink("TC-REQ005-010")
    public void TC_REQ005_010_verifyEmptySearchShowsDefaultEmployeeList() {
        int defaultCount = list.getRecordCount();

        list.clickSearch();

        Assert.assertEquals(list.getRecordCount(), defaultCount, "Default list should be unchanged");
        Assert.assertFalse(list.isErrorToastShown(), "No error should be shown");
    }

    private void assertAllContain(List<String> values, String expected, String column) {
        for (String value : values) {
            Assert.assertTrue(value.toLowerCase().contains(expected.toLowerCase()),
                    "Row with " + column + " '" + value + "' does not match '" + expected + "'");
        }
    }

    private void assertAllEqual(List<String> values, String expected, String column) {
        for (String value : values) {
            Assert.assertEquals(value, expected, column + " of every listed employee; distinct values listed: "
                    + new java.util.LinkedHashSet<>(values));
        }
    }
}
