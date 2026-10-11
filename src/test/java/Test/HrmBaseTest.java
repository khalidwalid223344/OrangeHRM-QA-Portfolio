package Test;

import Pages.AdminUsersPage;
import Pages.DashboardPage;
import Pages.EmployeeListPage;
import Pages.LoginPage;
import Pages.OrangeHrmPage;
import Utils.Config;
import Utils.Employee;
import Utils.Seeder;
import Utils.SystemUser;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for OrangeHRM tests: one fresh browser per test method so tests are isolated
 * on the shared public demo. Records created by a test are tracked and removed afterwards
 * through a separate admin session. (The legacy BaseTest is Jotform-specific and untouched.)
 */
public class HrmBaseTest {
    protected WebDriver driver;

    private final List<Employee> createdEmployees = new ArrayList<>();
    private final List<SystemUser> createdUsers = new ArrayList<>();

    private static WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1920,1080", "--disable-notifications");
        if (Config.headless()) {
            options.addArguments("--headless=new");
        }
        // Keep Chrome's password manager out of the way so tests are deterministic.
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        return new ChromeDriver(options);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = createDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (driver != null && result.getStatus() == ITestResult.FAILURE) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Screenshot on failure", "image/png",
                        new ByteArrayInputStream(screenshot), "png");
                attachFailureContext(result);
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
            cleanUpTestData();
        }
    }

    /** Attaches URL, tracked Employee IDs, toasts seen and validation messages. Contains no credentials. */
    private void attachFailureContext(ITestResult result) {
        StringBuilder sb = new StringBuilder("Test: ").append(result.getName()).append('\n');
        try {
            sb.append("URL: ").append(driver.getCurrentUrl()).append('\n');
            sb.append("Title: ").append(driver.getTitle()).append('\n');
            List<String> ids = new ArrayList<>();
            for (Employee e : createdEmployees) {
                ids.add(e.employeeId());
            }
            sb.append("Tracked Employee IDs: ").append(ids).append('\n');
            sb.append("Toasts seen (kind:text): ")
                    .append(OrangeHrmPage.readToastLog(driver).replace('\t', ':').replace("/", "")).append('\n');
            List<String> validation = new ArrayList<>();
            for (WebElement e : driver.findElements(By.cssSelector(".oxd-input-field-error-message"))) {
                validation.add(e.getText().trim());
            }
            sb.append("Validation messages: ").append(validation).append('\n');
        } catch (RuntimeException e) {
            sb.append("(context partially unavailable: ").append(e.getClass().getSimpleName()).append(")\n");
        }
        Allure.addAttachment("Failure context", sb.toString());
    }

    /** Logs in through the real login page as the configured admin and verifies the dashboard. */
    protected DashboardPage loginAsAdmin() {
        loginAsAdmin(driver);
        return new DashboardPage(driver);
    }

    private static void loginAsAdmin(WebDriver webDriver) {
        new LoginPage(webDriver).open(Config.baseUrl()).loginAs(Config.adminUsername(), Config.adminPassword());
        if (!new DashboardPage(webDriver).isLoaded()) {
            throw new IllegalStateException("Admin login failed; check ORANGEHRM_ADMIN_USER / ORANGEHRM_ADMIN_PASS");
        }
    }

    protected Employee track(Employee employee) {
        createdEmployees.add(employee);
        return employee;
    }

    protected SystemUser track(SystemUser user) {
        createdUsers.add(user);
        return user;
    }

    /** Creates (and tracks for cleanup) an employee as a test precondition. */
    protected Employee createEmployee(String firstName, String lastName) {
        return track(Seeder.createEmployee(driver, firstName, lastName));
    }

    /** Creates (and tracks for cleanup) a system user as a test precondition. */
    protected SystemUser createSystemUser(Employee employee, String role) {
        return track(Seeder.createSystemUser(driver, employee, role));
    }

    /** Deletes tracked users, then employees, in a separate admin session. Never fails the test. */
    private void cleanUpTestData() {
        if (createdEmployees.isEmpty() && createdUsers.isEmpty()) {
            return;
        }
        WebDriver cleaner = createDriver();
        try {
            loginAsAdmin(cleaner);
            for (SystemUser user : createdUsers) {
                AdminUsersPage users = new AdminUsersPage(cleaner).open().searchByUsername(user.username());
                if (users.getUsernames().contains(user.username())) {
                    users.deleteRow(user.username());
                }
            }
            for (Employee employee : createdEmployees) {
                EmployeeListPage list = new EmployeeListPage(cleaner).open().searchByEmployeeId(employee.employeeId());
                if (list.getEmployeeIds().contains(employee.employeeId())) {
                    list.deleteRow(employee.employeeId());
                }
            }
        } catch (RuntimeException e) {
            Allure.addAttachment("Cleanup warning", "Test data cleanup failed: " + e);
        } finally {
            cleaner.quit();
            createdUsers.clear();
            createdEmployees.clear();
        }
    }
}
