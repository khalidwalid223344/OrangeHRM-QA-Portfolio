package Utils;

import Pages.AddEmployeePage;
import Pages.AddUserPage;
import Pages.PersonalDetailsPage;
import org.openqa.selenium.WebDriver;

/**
 * Creates prerequisite records through the same UI pages the tests exercise.
 * Used only for preconditions, never for the behaviour a test is verifying.
 */
public final class Seeder {
    private Seeder() {
    }

    public static Employee createEmployee(WebDriver driver, String firstName, String lastName) {
        AddEmployeePage page = new AddEmployeePage(driver).open();
        String id = page.getEmployeeId();
        if (id == null || id.isBlank()) {
            id = DataFactory.employeeId();
            page.enterEmployeeId(id);
        }
        page.enterFirstName(firstName).enterLastName(lastName).clickSave();
        if (!page.isSaved()) {
            throw new IllegalStateException("Setup failed: employee '" + firstName + " " + lastName
                    + "' was not saved. Feedback: " + page.getFeedbackText());
        }
        // Verify the precondition: read the id back from the saved record instead of trusting the form value.
        String savedId = new PersonalDetailsPage(driver).waitUntilLoaded().getEmployeeId();
        if (!savedId.equals(id)) {
            throw new IllegalStateException("Setup failed: employee saved with Employee Id '" + savedId
                    + "' but '" + id + "' was entered");
        }
        return new Employee(firstName, lastName, savedId);
    }

    public static SystemUser createSystemUser(WebDriver driver, Employee employee, String role) {
        SystemUser user = new SystemUser(DataFactory.username(), DataFactory.password(), role, employee);
        AddUserPage page = new AddUserPage(driver).open();
        page.selectUserRole(role)
                .selectEmployee(employee.firstName(), employee.firstName())
                .selectStatus("Enabled")
                .enterUsername(user.username())
                .enterPassword(user.password());
        page.clickSave();
        if (!page.isSaved()) {
            throw new IllegalStateException("Setup failed: system user '" + user.username()
                    + "' was not saved. Feedback: " + page.getFeedbackText());
        }
        return user;
    }
}
