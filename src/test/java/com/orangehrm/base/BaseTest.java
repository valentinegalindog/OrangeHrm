package com.orangehrm.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.orangehrm.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseTest {

    public WebDriver driver;
    protected static ExtentReports extent;
    protected ExtentTest test;

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

        // Detectar si estamos en BrowserStack por la presencia del SDK
        boolean isBrowserStack = System.getProperty("browserstack.sdk") != null;
        extent.setSystemInfo("Execution Env", isBrowserStack ? "BrowserStack" : "Local");
    }

    @BeforeMethod(alwaysRun = true)  // Importante: alwaysRun=true
    public void setupTest(Method method) {
        test = extent.createTest(method.getName());
        test.info("Iniciando test: " + method.getName());

        // Crear driver local (BrowserStack lo reemplazará automáticamente si es necesario)
        createLocalDriver();

        // Configurar timeouts
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getIntProperty("app.timeout")));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));

        // Maximizar solo si no estamos en BrowserStack (aunque BrowserStack lo maneja)
        if (System.getProperty("browserstack.sdk") == null) {
            driver.manage().window().maximize();
        }

        test.info("WebDriver inicializado");
    }

    private void createLocalDriver() {
        String browser = ConfigReader.getProperty("browser");
        boolean headless = ConfigReader.getBooleanProperty("headless", false);

        // Detectar CI
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

    @AfterMethod(alwaysRun = true)  // Importante: alwaysRun=true
    public void tearDownTest(ITestResult result) {
        String testName = result.getName();

        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                test.fail("Test falló: " + result.getThrowable());
                takeScreenshot(testName + "_failure");
            } else if (result.getStatus() == ITestResult.SUCCESS) {
                test.pass("Test pasó exitosamente");
            } else if (result.getStatus() == ITestResult.SKIP) {
                test.skip("Test saltado");
            }

            // Cerrar driver
            if (driver != null) {
                driver.manage().deleteAllCookies();
                driver.quit();
                test.info("WebDriver cerrado correctamente");
            }

        } catch (Exception e) {
            test.warning("Error durante tearDown: " + e.getMessage());
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
            String reportPath = ConfigReader.getProperty("report.path") + "TestReport.html";
            System.out.println("Reporte generado en: " + reportPath);
        }
    }

    protected void takeScreenshot(String name) {
        try {
            if (driver == null) return;

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotDir = ConfigReader.getProperty("screenshot.path");
            Files.createDirectories(Paths.get(screenshotDir));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
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