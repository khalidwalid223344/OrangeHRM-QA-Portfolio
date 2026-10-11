package Test;

import Pages.AddUserPage;
import Pages.AdminUsersPage;
import Pages.EmployeeListPage;
import Utils.DataFactory;
import Utils.Employee;
import Utils.SystemUser;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * REQ-011 System User Management (Admin).
 * Only the three documented manual cases are automated; edit/role/status cases do not exist in the manual sheet.
 */
@Epic("OrangeHRM")
@Feature("Admin")
public class SystemUserTests extends HrmBaseTest {
    private static final String ROLE = "ESS";

    @BeforeMethod
    public void login() {
        loginAsAdmin();
    }

    // TC-REQ-011-001 | REQ-011
    @Test(description = "TC-REQ-011-001: Create a system user linked to an existing employee")
    @Story("REQ-011") @TmsLink("TC-REQ-011-001")
    public void TC_REQ_011_001_verifyCreateSystemUserLinkedToExistingEmployee() {
        Employee employee = createEmployee(DataFactory.firstName(), DataFactory.lastName());
        SystemUser user = track(new SystemUser(DataFactory.username(), DataFactory.password(), ROLE, employee));

        AddUserPage addPage = fillUserForm(user, employee);
        addPage.clickSave();

        Assert.assertTrue(addPage.isSaved(), "System user should be created");
        AdminUsersPage users = new AdminUsersPage(driver).open().searchByUsername(user.username());
        Assert.assertEquals(users.getUsernames(), java.util.List.of(user.username()), "Created user listed");
        Assert.assertTrue(users.getEmployeeNames().get(0).contains(employee.firstName()),
                "User should be linked to the selected employee");
        Assert.assertEquals(users.getUserRoles().get(0), ROLE, "Role");
        Assert.assertEquals(users.getStatuses().get(0), "Enabled", "Status");
    }

    // TC-REQ-011-002 | REQ-011
    @Test(description = "TC-REQ-011-002: Duplicate username is rejected")
    @Story("REQ-011") @TmsLink("TC-REQ-011-002")
    public void TC_REQ_011_002_verifyDuplicateUsernameIsRejected() {
        SystemUser existing = createSystemUser(createEmployee(DataFactory.firstName(), DataFactory.lastName()), ROLE);
        Employee second = createEmployee(DataFactory.firstName(), DataFactory.lastName());
        SystemUser duplicate = new SystemUser(existing.username(), DataFactory.password(), ROLE, second);

        AddUserPage addPage = fillUserForm(duplicate, second);
        addPage.clickSave();

        Assert.assertFalse(addPage.getUsernameError().isBlank(), "A duplicate-username message should be displayed");
        Assert.assertTrue(addPage.isAt(), "The duplicate user must not be created");
        AdminUsersPage users = new AdminUsersPage(driver).open().searchByUsername(existing.username());
        Assert.assertEquals(users.getRecordCount(), 1, "Only the original user should exist");
    }

    // TC-REQ-011-003 | REQ-011
    @Test(description = "TC-REQ-011-003: Deleting a system user keeps the linked employee")
    @Story("REQ-011") @TmsLink("TC-REQ-011-003")
    public void TC_REQ_011_003_verifyDeletingSystemUserKeepsLinkedEmployee() {
        Employee employee = createEmployee(DataFactory.firstName(), DataFactory.lastName());
        SystemUser user = createSystemUser(employee, ROLE);

        AdminUsersPage users = new AdminUsersPage(driver).open().searchByUsername(user.username());
        users.deleteRow(user.username());

        users.searchByUsername(user.username());
        Assert.assertTrue(users.getRecordsSummary().toLowerCase().contains("no records found"),
                "System user should be deleted");
        EmployeeListPage list = new EmployeeListPage(driver).open().searchByEmployeeId(employee.employeeId());
        Assert.assertTrue(list.getEmployeeIds().contains(employee.employeeId()),
                "Linked employee record must still exist");
    }

    private AddUserPage fillUserForm(SystemUser user, Employee employee) {
        return new AddUserPage(driver).open()
                .selectUserRole(user.role())
                .selectEmployee(employee.firstName(), employee.firstName())
                .selectStatus("Enabled")
                .enterUsername(user.username())
                .enterPassword(user.password());
    }
}
