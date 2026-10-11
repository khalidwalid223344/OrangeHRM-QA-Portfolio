package Test;

import Pages.EmployeeListPage;
import Utils.DataFactory;
import Utils.Employee;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * REQ-006 Delete Employee. Every test deletes only an employee it created itself.
 * Not automated: TC-EMP-DEL-009 (needs a simulated backend failure).
 * TC-EMP-DEL-008 (delete from search results) is covered by the search-then-delete flow of TC-EMP-DEL-003/007.
 */
@Epic("OrangeHRM")
@Feature("PIM")
public class DeleteEmployeeTests extends HrmBaseTest {
    private EmployeeListPage list;
    private Employee employee;

    @BeforeMethod
    public void createEmployeeAndOpenList() {
        loginAsAdmin();
        employee = createEmployee(DataFactory.firstName(), DataFactory.lastName());
        list = new EmployeeListPage(driver).open().searchByEmployeeId(employee.employeeId());
        Assert.assertTrue(list.getEmployeeIds().contains(employee.employeeId()),
                "Precondition: employee to delete must be listed");
    }

    // TC-EMP-DEL-001 | REQ-006
    @Test(description = "TC-EMP-DEL-001: Deleting an employee asks for confirmation")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-001")
    public void TC_EMP_DEL_001_verifyDeleteShowsConfirmationDialog() {
        list.clickDeleteForRow(employee.employeeId());

        Assert.assertTrue(list.isDeleteDialogDisplayed(), "A confirmation dialog should be displayed");
    }

    // TC-EMP-DEL-002 | REQ-006
    @Test(description = "TC-EMP-DEL-002: Cancelling deletion keeps the employee")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-002")
    public void TC_EMP_DEL_002_verifyCancelDeletionKeepsEmployee() {
        list.clickDeleteForRow(employee.employeeId());
        list.cancelDelete();

        Assert.assertTrue(list.isDeleteDialogClosed(), "Dialog should close");
        Assert.assertTrue(list.getEmployeeIds().contains(employee.employeeId()), "Employee should remain listed");
    }

    // TC-EMP-DEL-003 | REQ-006
    @Test(description = "TC-EMP-DEL-003: Confirming deletion removes the employee")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-003")
    public void TC_EMP_DEL_003_verifyConfirmDeletionRemovesEmployee() {
        list.deleteRow(employee.employeeId());

        list.searchByEmployeeId(employee.employeeId());
        Assert.assertFalse(list.getEmployeeIds().contains(employee.employeeId()), "Employee should be removed");
    }

    // TC-EMP-DEL-004 | REQ-006
    @Test(description = "TC-EMP-DEL-004: Deleted employee stays absent after refresh")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-004")
    public void TC_EMP_DEL_004_verifyDeletedEmployeeAbsentAfterRefresh() {
        list.deleteRow(employee.employeeId());

        list.open().searchByEmployeeId(employee.employeeId());
        Assert.assertFalse(list.getEmployeeIds().contains(employee.employeeId()),
                "Deleted employee should remain absent after reload");
    }

    // TC-EMP-DEL-005 | REQ-006
    @Test(description = "TC-EMP-DEL-005: Deleted employee cannot be restored through the UI")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-005")
    public void TC_EMP_DEL_005_verifyDeletedEmployeeCannotBeRestored() {
        list.deleteRow(employee.employeeId());

        list.searchByName(employee.fullName());
        Assert.assertTrue(list.getRecordsSummary().toLowerCase().contains("no records found"),
                "Deleted employee should not be found");
        Assert.assertTrue(driver.findElements(org.openqa.selenium.By.xpath(
                "//*[self::button or self::a][contains(translate(normalize-space(),'RESTORE','restore'),'restore')]"))
                .isEmpty(), "No restore option should be available");
    }

    // TC-EMP-DEL-006 | REQ-006
    @Test(description = "TC-EMP-DEL-006: Closing the dialog does not delete the employee")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-006")
    public void TC_EMP_DEL_006_verifyClosingDialogDoesNotDeleteEmployee() {
        list.clickDeleteForRow(employee.employeeId());
        list.closeDeleteDialog();

        Assert.assertTrue(list.isDeleteDialogClosed(), "Dialog should close");
        Assert.assertTrue(list.getEmployeeIds().contains(employee.employeeId()), "Employee should not be deleted");
    }

    // TC-EMP-DEL-007 | REQ-006

    @Test(description = "TC-EMP-DEL-007: Only the selected employee is deleted")
    @Story("REQ-006")
    @TmsLink("TC-EMP-DEL-007")
    public void TC_EMP_DEL_007_verifyOnlySelectedEmployeeIsDeleted() {
        Employee other = createEmployee(
                DataFactory.firstName(),
                employee.lastName()
        );

        list = new EmployeeListPage(driver)
                .open()
                .searchByName(employee.lastName());

        Assert.assertEquals(
                list.getRecordCount(),
                2,
                "Precondition failed: both employees should exist before deletion"
        );

        String deletedId = employee.employeeId();
        String remainingId = other.employeeId();

        list.deleteRow(deletedId);

        // Search again after deletion
        list.open().searchByName(employee.lastName());

        // Verify only the other employee remains
        Assert.assertEquals(
                list.getEmployeeIds(),
                java.util.List.of(remainingId),
                "Only the selected employee should be deleted; the other employee must remain"
        );
    }

    // TC-EMP-DEL-010 | REQ-006
    // UI-level check only: the number of backend requests cannot be observed from the UI.
    @Test(description = "TC-EMP-DEL-010: Repeated quick confirm clicks stay stable")
    @Story("REQ-006") @TmsLink("TC-EMP-DEL-010")
    public void TC_EMP_DEL_010_verifyRepeatedConfirmClicksAreHandledGracefully() {
        list.clickDeleteForRow(employee.employeeId());
        list.confirmDeleteWithRapidClicks();

        Assert.assertTrue(list.isDeleteDialogClosed(), "Dialog should close");
        Assert.assertFalse(list.isErrorToastShown(), "No error should be shown for repeated clicks");
        list.open().searchByEmployeeId(employee.employeeId());
        Assert.assertFalse(list.getEmployeeIds().contains(employee.employeeId()), "Employee should be deleted once");
    }
}
