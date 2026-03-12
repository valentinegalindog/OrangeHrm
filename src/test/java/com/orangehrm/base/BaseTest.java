package com.orangehrm.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.orangehrm.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class BaseTest {

    public WebDriver driver;
    protected static ExtentReports extent;
    protected ExtentTest test;

    // ─── Detección de BrowserStack ────────────────────────────────────────────
    // El SDK setea "browserstack.sdk" cuando carga como javaagent.
    // Además comprobamos las variables de entorno para el caso de RemoteWebDriver directo.
    private static final boolean IS_BROWSERSTACK =
            System.getProperty("browserstack.sdk") != null
                    || (System.getenv("BROWSERSTACK_USERNAME") != null
                    && System.getenv("BROWSERSTACK_ACCESS_KEY") != null
                    && "browserstack".equalsIgnoreCase(System.getProperty("execution.env")));

    private static final String BS_HUB_URL = "https://hub.browserstack.com/wd/hub";

    @BeforeSuite
    public void setupSuite() {
        String reportPath = ConfigReader.getProperty("report.path") + "TestReport.html";
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Orange HRM Test Report");
        sparkReporter.config().setReportName("Automation Test Results");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", System.getProperty("env", "QA"));
        extent.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
        extent.setSystemInfo("Execution Env", IS_BROWSERSTACK ? "BrowserStack" : "Local");

        System.out.println("[CONFIG] Modo de ejecución: " + (IS_BROWSERSTACK ? "BrowserStack" : "Local"));
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method) {
        test = extent.createTest(method.getName());
        test.info("Iniciando test: " + method.getName());

        if (IS_BROWSERSTACK) {
            createBrowserStackDriver(method.getName());
        } else {
            createLocalDriver();
            driver.manage().window().maximize();
        }

        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getIntProperty("app.timeout")));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        test.info("WebDriver inicializado - Modo: " + (IS_BROWSERSTACK ? "BrowserStack" : "Local"));
    }

    // ─── Driver para BrowserStack (RemoteWebDriver directo) ──────────────────
    private void createBrowserStackDriver(String testName) {
        try {
            String username = System.getenv("BROWSERSTACK_USERNAME");
            String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

            if (username == null || accessKey == null) {
                throw new RuntimeException(
                        "Faltan credenciales BrowserStack. " +
                                "BROWSERSTACK_USERNAME y BROWSERSTACK_ACCESS_KEY deben estar definidos.");
            }

            MutableCapabilities capabilities = new MutableCapabilities();
            capabilities.setCapability("browserName", "Chrome");
            capabilities.setCapability("browserVersion", "120.0");

            HashMap<String, Object> bsOptions = new HashMap<>();
            bsOptions.put("os", "Windows");
            bsOptions.put("osVersion", "10");
            bsOptions.put("userName", username);
            bsOptions.put("accessKey", accessKey);
            bsOptions.put("projectName", "OrangeHrm Automation");
            bsOptions.put("buildName", "OrangeHRM Build " +
                    System.getenv().getOrDefault("GITHUB_RUN_NUMBER", "local"));
            bsOptions.put("sessionName", testName);
            bsOptions.put("debug", true);
            bsOptions.put("networkLogs", true);
            bsOptions.put("consoleLogs", "info");
            // Sin local tunnel (browserstackLocal: false) porque GitHub Actions
            // puede llegar a orangehrm.com directamente
            bsOptions.put("local", false);

            capabilities.setCapability("bstack:options", bsOptions);

            driver = new RemoteWebDriver(new URL(BS_HUB_URL), capabilities);
            System.out.println("[BROWSERSTACK] Sesión iniciada: " +
                    ((RemoteWebDriver) driver).getSessionId());

        } catch (Exception e) {
            throw new RuntimeException("Error al crear driver de BrowserStack: " + e.getMessage(), e);
        }
    }

    // ─── Driver local ─────────────────────────────────────────────────────────
    private void createLocalDriver() {
        String browser = ConfigReader.getProperty("browser");
        boolean headless = ConfigReader.getBooleanProperty("headless", false);

        // En CI siempre headless
        if ("true".equals(System.getenv("CI"))) {
            headless = true;
        }

        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();

                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                    chromeOptions.addArguments("--window-size=1920,1080");
                } else {
                    chromeOptions.addArguments("--start-maximized");
                }

                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");

                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;

            default:
                throw new IllegalArgumentException("Browser no soportado: " + browser);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownTest(ITestResult result) {
        String testName = result.getName();

        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                test.fail("Test falló: " + result.getThrowable());
                takeScreenshot(testName + "_failure");

                // Marcar sesión como failed en BrowserStack
                if (IS_BROWSERSTACK && driver != null) {
                    ((RemoteWebDriver) driver).executeScript(
                            "browserstack_executor: {\"action\": \"setSessionStatus\", " +
                                    "\"arguments\": {\"status\": \"failed\", \"reason\": \"" +
                                    result.getThrowable().getMessage().replace("\"", "'") + "\"}}");
                }

            } else if (result.getStatus() == ITestResult.SUCCESS) {
                test.pass("Test pasó exitosamente");

                // Marcar sesión como passed en BrowserStack
                if (IS_BROWSERSTACK && driver != null) {
                    ((RemoteWebDriver) driver).executeScript(
                            "browserstack_executor: {\"action\": \"setSessionStatus\", " +
                                    "\"arguments\": {\"status\": \"passed\", \"reason\": \"Test passed\"}}");
                }

            } else if (result.getStatus() == ITestResult.SKIP) {
                test.skip("Test saltado");
            }

        } catch (Exception e) {
            test.warning("Error al reportar resultado: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    test.info("WebDriver cerrado correctamente");
                } catch (Exception e) {
                    test.warning("Error al cerrar driver: " + e.getMessage());
                }
            }
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
            System.out.println("Reporte generado en: " +
                    ConfigReader.getProperty("report.path") + "TestReport.html");
        }
    }

    protected void takeScreenshot(String name) {
        try {
            if (driver == null) return;

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotDir = ConfigReader.getProperty("screenshot.path");
            Files.createDirectories(Paths.get(screenshotDir));

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String screenshotName = name + "_" + timestamp + ".png";
            Path screenshotPath = Paths.get(screenshotDir, screenshotName);

            Files.copy(screenshot.toPath(), screenshotPath);
            test.addScreenCaptureFromPath(screenshotPath.toString());
            test.info("Screenshot guardado: " + screenshotName);

        } catch (IOException e) {
            test.warning("No se pudo tomar screenshot: " + e.getMessage());
        }
    }
}