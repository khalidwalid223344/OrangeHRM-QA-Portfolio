# OrangeHRM QA Automation

Selenium + TestNG automation of the manual test cases for the OrangeHRM open-source demo
(`https://opensource-demo.orangehrmlive.com`), built from a PRD (REQ-001..011, NFR-001..005),
97 manual test cases and a bug report.

## Tech stack
Java 21 | Selenium WebDriver 4.45 | TestNG 7.12 | Maven | Allure 2.35 | Jackson (test data) | Page Object Model | Chrome

## Coverage
| Module | Requirement | Test class | Automated test cases |
|---|---|---|---|
| Authentication | REQ-001 | `LoginTests` | TC-LOGIN-001..007, 009 |
| Forgot Password | REQ-002 | `ForgotPasswordTests` | TC-FP-001, 002, 003, and **TC-FP-004 (new: empty username)** |
| PIM - add employee | REQ-003 | `AddEmployeeTests` | TC-PIM-001..009, 0011, 0012 |
| PIM - search/filter | REQ-005 | `EmployeeSearchFilterTests` | TC-REQ005-001..010 |
| PIM - delete | REQ-006 | `DeleteEmployeeTests` | TC-EMP-DEL-001..007, 010 |
| Admin - system users | REQ-011 | `SystemUserTests` | TC-REQ-011-001..003 |
| Access control | REQ-010 | `RbacTests` | TC-REQ-010-001..004 |
| Leave - apply | REQ-007 | `ApplyLeaveTests` | TC-REQ007-01..07, 09, 10 |

57 automated tests. Full status of all 97 cases (including what is not automated and why):
[docs/AUTOMATION_MAPPING.md](docs/AUTOMATION_MAPPING.md).

### Not automated
| Test case | Reason |
|---|---|
| TC-LOGIN-008 | Chrome password-manager autofill; a clean Selenium profile has no saved credentials, so a test would pass without proving anything |
| TC-EMP-DEL-009 | Needs a simulated backend failure; blocked manually |
| TC-NFR-002, 003, 004 | Cross-browser / browser-version cases; suite is Chrome-only |
| TC-NFR-005 | Session-timeout length unknown; never executed manually |
| TC-008-01..10 (REQ-008) | Cannot be executed right now (out of scope for this round) |
| TC-PIM-0010, 0013; TC-REQ007-08; TC-EMP-DEL-008 | Same steps as an automated case (009, 07-01, 003/007); covered, not duplicated |

### Remaining candidates for future automation
REQ-004 (edit details, profile picture upload: 10 cases), REQ-009 (recruitment: 5 cases), NFR-001, 006..010
(page load time, password not in page source/console, resolutions), and REQ-008 once it can be executed manually.

## Structure and design decisions
```
src/main/java/Pages     Page objects (locators + actions, no assertions)
  OrangeHrmPage         shared base: label-based fields, dropdowns, result tables, delete dialog, feedback
  *Page                 one class per screen (Login, Dashboard, AddEmployee, EmployeeList, AdminUsers, ...)
src/test/java/Test      Test classes (assertions only); HrmBaseTest = browser lifecycle, login, cleanup
src/test/java/Utils     Config, JsonReader, DataFactory (unique data), Seeder (preconditions), Employee/SystemUser
src/test/resources      TestData.json, config.properties
testng.xml              Suite executed by Maven
```
- **Isolation:** a fresh Chrome per test. Preconditions are created through the UI (`Seeder`).
- **Unique data:** every employee, Employee ID, username and leave date is generated per run (`DataFactory`),
  so reruns never collide with records left on the shared demo.
- **Cleanup:** records created by a test are tracked and deleted afterwards in a separate admin session
  (users first, then employees). Cleanup problems are attached to Allure and never fail a test.
  Submitted leave cannot be removed through the UI and consumes one day of balance per submission.
- **Locators:** controls are found through their visible label (stable) rather than DOM position; no absolute XPath,
  no `Thread.sleep`, explicit waits only.
- **Secrets:** no password is stored. Admin credentials come from environment variables; test-user passwords are
  generated at runtime.
- **Known defects:** tests linked to an open bug keep the documented expected result, are tagged with the Allure
  `@Issue` and the TestNG group `known-defect`, and are expected to fail until the defect is fixed.

## Prerequisites
JDK 21+, Maven, Chrome. The admin account of the demo (shown on its login page) must be exposed as:
```
set ORANGEHRM_ADMIN_USER=<username>     (Windows)   |   export ORANGEHRM_ADMIN_USER=<username>
set ORANGEHRM_ADMIN_PASS=<password>     (Windows)   |   export ORANGEHRM_ADMIN_PASS=<password>
```
In IntelliJ, add them to the run configuration's *Environment variables*. Optional: `-Dheadless=true`, `-DbaseUrl=...`.
The leave tests need the admin to have a leave type with balance (otherwise they fail with a clear precondition message).

## Run
```
mvn clean test                                                   # full suite (testng.xml)
mvn clean test -Dgroups=known-defect                             # only tests tied to open bugs
mvn clean test -Dtest=AddEmployeeTests                           # one class (bypasses testng.xml)
mvn clean test -Dtest=AddEmployeeTests#TC_PIM_001_verifyAddEmployeeWithValidFirstAndLastName   # one test
```
In IntelliJ you can also run any class or method with the gutter icon.

## Allure report
Results are written to `target/allure-results`.
```
mvn allure:serve        # build and open the report
mvn allure:report       # generate to target/site/allure-maven-plugin
```
Stories show the requirement, TMS links show the test case ID and issues show the bug ID; failures include a screenshot.

## Known defects
| Bug | Summary | Test cases |
|---|---|---|
| BUG-001 | Credentials auto-filled after failed login (may be browser autofill) | TC-LOGIN-008 (manual only) |
| BUG-002 | Forgot Password returns 504 | TC-FP-001 |
| BUG-003 | No duplicate-name warning | TC-PIM-009 (also 0010, 0013) |
| BUG-004 | Sub-unit filter returns other sub-units (may be by design: child sub-units) | TC-REQ005-006, 007 |
| BUG-005 | Submitted leave missing from My Leave (see note) | TC-REQ007-01, 07, 09, 10 (also 08) |
| BUG-006 | Candidate saved without Vacancy | TC-REQ-009-004 (planned) |
| BUG-007 | ESS user can open Admin module by URL | TC-REQ-010-002 |

**Note on BUG-005:** the leave tests open My Leave, set a date range covering the request and include the
"Pending Approval" status before searching. If the default My Leave filter (current year and/or status selection) was
hiding the request during manual testing, these tests will pass and BUG-005 should be re-checked. If they still fail, the defect is confirmed.
