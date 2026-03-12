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

    // Flag para detectar BrowserStack
    private boolean isBrowserStack = false;

    @BeforeSuite
    public void setupSuite() {
        // Detectar BrowserStack por la presencia del perfil
        isBrowserStack = "browserstack".equals(System.getProperty("execution.env"));

        String reportPath = ConfigReader.getProperty("report.path") + "TestReport.html";
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Orange HRM Test Report");
        sparkReporter.config().setReportName("Automation Test Results");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", System.getProperty("env", "QA"));
        extent.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
        extent.setSystemInfo("Execution Env", isBrowserStack ? "BrowserStack" : "Local");
        extent.setSystemInfo("OS", System.getProperty("os.name"));
    }

    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method) {
        test = extent.createTest(method.getName());
        test.info("Iniciando test: " + method.getName());

        try {
            // Inicializar driver según el entorno
            initializeDriver();

            // Verificar que driver NO es null
            if (driver == null) {
                throw new RuntimeException("FATAL: driver es null después de initializeDriver()");
            }

            test.info("Driver inicializado correctamente. Clase: " + driver.getClass().getSimpleName());

            // Configurar timeouts
            driver.manage().timeouts().implicitlyWait(
                    Duration.ofSeconds(ConfigReader.getIntProperty("app.timeout")));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

            // Maximizar solo si es local
            if (!isBrowserStack) {
                driver.manage().window().maximize();
            }

        } catch (Exception e) {
            test.fail("Error en setup: " + e.getMessage());
            throw e;
        }
    }

    private void initializeDriver() {
        if (isBrowserStack) {
            // ⚠️ IMPORTANTE: En BrowserStack NO creamos driver
            // El SDK de BrowserStack ya creó el driver y lo inyecta
            // Solo necesitamos obtener la referencia actual
            test.info("Ejecutando en BrowserStack - el SDK gestiona el driver");

            // Intentar obtener el driver actual de alguna manera
            // El SDK de BrowserStack suele inyectarlo automáticamente
            // Si no está disponible, esperamos un poco
            int attempts = 0;
            while (driver == null && attempts < 10) {
                try {
                    Thread.sleep(500);
                    attempts++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (driver == null) {
                throw new RuntimeException("No se pudo obtener el driver de BrowserStack después de 5 segundos");
            }
        } else {
            // Crear driver local
            createLocalDriver();
        }
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

    @AfterMethod(alwaysRun = true)
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

            // Cerrar driver SOLO si es local
            if (driver != null && !isBrowserStack) {
                test.info("Cerrando WebDriver local...");
                try {
                    driver.manage().deleteAllCookies();
                    driver.quit();
                    test.info("WebDriver local cerrado correctamente");
                } catch (Exception e) {
                    test.warning("Error al cerrar driver local: " + e.getMessage());
                }
            } else if (driver != null && isBrowserStack) {
                test.info("Driver de BrowserStack será cerrado por el SDK");
                // NO llamamos a driver.quit() aquí
            }

        } catch (Exception e) {
            test.warning("Error durante tearDown: " + e.getMessage());
        } finally {
            test = null;
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
            if (driver == null) {
                test.warning("No se puede tomar screenshot: driver null");
                return;
            }

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

    // Método helper para obtener el driver (útil para las páginas)
    public WebDriver getDriver() {
        if (driver == null) {
            throw new RuntimeException("Driver no inicializado. Llama a setupTest primero.");
        }
        return driver;
    }
}