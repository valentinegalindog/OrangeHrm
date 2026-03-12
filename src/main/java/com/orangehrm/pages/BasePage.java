package com.orangehrm.pages;

import com.orangehrm.utils.ConfigReader;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Random;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.getIntProperty("app.timeout")));
        PageFactory.initElements(driver, this);
    }

    public void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        scrollToElement(element);
        element.click();
    }
    public void sendKeys(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    public void takeScreenshot(String testName) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String screenshotPath = ConfigReader.getProperty("screenshot.path") +
                    testName + "_" + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(screenshot, new File(screenshotPath));
            System.out.println("Screenshot guardado: " + screenshotPath);
        } catch (IOException e) {
            System.err.println("Error al tomar screenshot: " + e.getMessage());
        }
    }

    private void scrollToElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    public void assertEqualsText(WebElement element, String expected) {
        String actual = element.getText();
        String message = "Validacion asser del elemento: " + element + " fallida";
        Assert.assertEquals(actual, expected, message);
    }

    public void waitForElementVisible(WebElement element, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        customWait.until(ExpectedConditions.visibilityOf(element));
    }
    public void waitForElementClickable(WebElement element, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        customWait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void cargarFotoEmpleado(String rutaRelativa) {
        File archivo = new File(rutaRelativa);

        if (!archivo.exists()) {
            throw new RuntimeException("Archivo no encontrado en: " + archivo.getAbsolutePath());
        }

        // IMPORTANTE: setear ANTES de buscar el elemento
        if (driver instanceof RemoteWebDriver) {
            ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        }

        WebElement fileInput = driver.findElement(By.cssSelector("input[type='file'].oxd-file-input"));
        fileInput.sendKeys(archivo.getAbsolutePath());
    }


    public String generarNumeroAleatorio4Cifras() {
        Random random = new Random();
        int numero = 1000 + random.nextInt(9000);
        return String.valueOf(numero);
    }

}
