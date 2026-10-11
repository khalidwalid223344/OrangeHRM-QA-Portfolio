package Test;

import Pages.DashboardPage;
import Pages.LoginPage;
import Utils.Config;
import Utils.JsonReader;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * REQ-001 User Login.
 * Not automated: TC-LOGIN-008 (browser password-manager autofill, see BUG-001 and docs/AUTOMATION_MAPPING.md).
 */
@Epic("OrangeHRM")
@Feature("Authentication")
public class LoginTests extends HrmBaseTest {
    private static final JsonNode DATA = JsonReader.getTestData("login");

    private LoginPage loginPage;

    @BeforeMethod
    public void openLoginPage() {
        loginPage = new LoginPage(driver).open(Config.baseUrl());
    }

    // TC-LOGIN-001 | REQ-001
    @Test(description = "TC-LOGIN-001: User login with valid username and valid password")
    @Story("REQ-001") @TmsLink("TC-LOGIN-001")
    public void TC_LOGIN_001_verifyLoginWithValidCredentials() {
        loginPage.loginAs(Config.adminUsername(), Config.adminPassword());

        DashboardPage dashboard = new DashboardPage(driver);
        Assert.assertTrue(dashboard.isLoaded(), "User should land on the Dashboard after a valid login");
        Assert.assertEquals(dashboard.getModuleTitle(), "Dashboard", "Dashboard heading");
    }

    // TC-LOGIN-002 | REQ-001
    @Test(description = "TC-LOGIN-002: User login with valid username and invalid password")
    @Story("REQ-001") @TmsLink("TC-LOGIN-002")
    public void TC_LOGIN_002_verifyLoginWithValidUsernameAndInvalidPassword() {
        loginPage.loginAs(Config.adminUsername(), DATA.get("invalidPassword").asText());

        assertLoginRejectedWithErrorMessage();
    }

    // TC-LOGIN-003 | REQ-001
    @Test(description = "TC-LOGIN-003: User login with empty username")
    @Story("REQ-001") @TmsLink("TC-LOGIN-003")
    public void TC_LOGIN_003_verifyLoginWithEmptyUsername() {
        loginPage.enterPassword(Config.adminPassword()).clickLogin();

        assertRequired(loginPage.getUsernameFieldError(), "username");
        Assert.assertTrue(loginPage.isAt(), "User should remain on the Login page");
    }

    // TC-LOGIN-004 | REQ-001
    @Test(description = "TC-LOGIN-004: User login with empty password")
    @Story("REQ-001") @TmsLink("TC-LOGIN-004")
    public void TC_LOGIN_004_verifyLoginWithEmptyPassword() {
        loginPage.enterUsername(Config.adminUsername()).clickLogin();

        assertRequired(loginPage.getPasswordFieldError(), "password");
        Assert.assertTrue(loginPage.isAt(), "User should remain on the Login page");
    }

    // TC-LOGIN-005 | REQ-001
    @Test(description = "TC-LOGIN-005: Verify password masking")
    @Story("REQ-001") @TmsLink("TC-LOGIN-005")
    public void TC_LOGIN_005_verifyPasswordIsMasked() {
        loginPage.enterPassword(DATA.get("invalidPassword").asText());

        Assert.assertEquals(loginPage.getPasswordFieldType(), "password",
                "Password characters must be masked (input type=password)");
    }

    // TC-LOGIN-006 | REQ-001
    @Test(description = "TC-LOGIN-006: User login with invalid username and valid password")
    @Story("REQ-001") @TmsLink("TC-LOGIN-006")
    public void TC_LOGIN_006_verifyLoginWithInvalidUsernameAndValidPassword() {
        loginPage.loginAs(DATA.get("invalidUsername").asText(), Config.adminPassword());

        assertLoginRejectedWithErrorMessage();
    }

    // TC-LOGIN-007 | REQ-001
    @Test(description = "TC-LOGIN-007: User login with invalid username and invalid password")
    @Story("REQ-001") @TmsLink("TC-LOGIN-007")
    public void TC_LOGIN_007_verifyLoginWithInvalidUsernameAndInvalidPassword() {
        loginPage.loginAs(DATA.get("invalidUsername").asText(), DATA.get("invalidPassword").asText());

        assertLoginRejectedWithErrorMessage();
    }

    // TC-LOGIN-009 | REQ-001
    @Test(description = "TC-LOGIN-009: User login with empty username and empty password")
    @Story("REQ-001") @TmsLink("TC-LOGIN-009")
    public void TC_LOGIN_009_verifyLoginWithEmptyUsernameAndPassword() {
        loginPage.clickLogin();

        Assert.assertEquals(loginPage.getFieldErrorCount(), 2,
                "Required-field validation should be shown for both username and password");
        assertRequired(loginPage.getUsernameFieldError(), "username");
        assertRequired(loginPage.getPasswordFieldError(), "password");
    }

    private void assertLoginRejectedWithErrorMessage() {
        String message = loginPage.getAlertMessage();
        Assert.assertFalse(message.isBlank(), "An error message should be displayed for rejected credentials");
        Assert.assertTrue(loginPage.isAt(), "User should remain on the Login page");
    }

    private void assertRequired(String actualMessage, String fieldName) {
        Assert.assertTrue(actualMessage.toLowerCase().contains(DATA.get("requiredMessage").asText()),
                "A required-field validation message should be shown for " + fieldName
                        + " but was: '" + actualMessage + "'");
    }
}
