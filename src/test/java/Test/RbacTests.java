package Test;

import Pages.DashboardPage;
import Pages.LoginPage;
import Pages.NavigationPage;
import Pages.PersonalDetailsPage;
import Utils.Config;
import Utils.DataFactory;
import Utils.JsonReader;
import Utils.SystemUser;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REQ-010 Role-Based Access Control. ESS tests use a throw-away ESS user created through the Admin UI
 * (then removed), and log in through the real login page.
 */
@Epic("OrangeHRM")
@Feature("Access Control")
public class RbacTests extends HrmBaseTest {
    private static final JsonNode DATA = JsonReader.getTestData("rbac");

    // TC-REQ-010-001 | REQ-010
    @Test(description = "TC-REQ-010-001: ESS user cannot see Admin-only menus")
    @Story("REQ-010") @TmsLink("TC-REQ-010-001")
    public void TC_REQ_010_001_verifyEssUserCannotSeeAdminOnlyMenus() {
        loginAsEssUser();

        NavigationPage navigation = new NavigationPage(driver);
        for (JsonNode menu : DATA.get("essHiddenMenus")) {
            Assert.assertFalse(navigation.isMenuVisible(menu.asText()),
                    "Admin-only menu '" + menu.asText() + "' must not be visible to an ESS user");
        }
    }

    // TC-REQ-010-002 | REQ-010
    // Related Bug: BUG-007 (ESS user can open the Admin module by URL)
    @Test(description = "TC-REQ-010-002: ESS user cannot open the Admin module by direct URL", groups = "known-defect")
    @Story("REQ-010") @TmsLink("TC-REQ-010-002") @Issue("BUG-007")
    public void TC_REQ_010_002_verifyEssUserCannotAccessAdminModuleByDirectUrl() {
        loginAsEssUser();

        driver.get(Config.baseUrl() + DATA.get("adminUrlPath").asText());

        Assert.assertTrue(new NavigationPage(driver).isRedirectedAwayFrom("/admin/"),
                "Access to the Admin module should be blocked or redirected, but the URL is: "
                        + driver.getCurrentUrl());
    }

    // TC-REQ-010-003 | REQ-010
    @Test(description = "TC-REQ-010-003: ESS user can access an authorized feature")
    @Story("REQ-010") @TmsLink("TC-REQ-010-003")
    public void TC_REQ_010_003_verifyEssUserCanAccessAuthorizedFeature() {
        loginAsEssUser();

        new NavigationPage(driver).openMenu(DATA.get("essFeatureMenu").asText());

        PersonalDetailsPage details = new PersonalDetailsPage(driver);
        Assert.assertTrue(details.isLoaded(), "ESS user should reach their own Personal Details (My Info)");
        details.waitUntilLoaded();
    }

    // TC-REQ-010-004 | REQ-010
    @Test(description = "TC-REQ-010-004: Admin user can access Admin-only features")
    @Story("REQ-010") @TmsLink("TC-REQ-010-004")
    public void TC_REQ_010_004_verifyAdminUserCanAccessAdminOnlyFeatures() {
        loginAsAdmin();
        NavigationPage navigation = new NavigationPage(driver);
        String adminMenu = DATA.get("adminMenu").asText();

        Assert.assertTrue(navigation.isMenuVisible(adminMenu), "Admin menu should be visible to an Admin user");
        navigation.openMenu(adminMenu);

        Assert.assertTrue(new Pages.AdminUsersPage(driver).isAt(),
                "Admin module should open for an Admin user, URL: " + driver.getCurrentUrl());
    }

    /** Creates an ESS user as admin, logs out, and logs in as that ESS user. */
    private void loginAsEssUser() {
        loginAsAdmin();
        SystemUser ess = createSystemUser(createEmployee(DataFactory.firstName(), DataFactory.lastName()), "ESS");
        new NavigationPage(driver).logout().loginAs(ess.username(), ess.password());
        Assert.assertTrue(new DashboardPage(driver).isLoaded(), "ESS user should be able to log in");
    }
}
