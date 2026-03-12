package com.orangehrm.pages;

import com.orangehrm.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = "input[name='username']")
    private WebElement txtUsername;
    @FindBy(css = "input[name='password']")
    private WebElement txtPassword;
    @FindBy(css = "button[type='submit']")
    private WebElement btnLogin;

    public void open() {
        driver.get(ConfigReader.getProperty("app.url"));
    }

    public void loginOrangeHrm(String user, String password) {
        waitForElementVisible(txtPassword, 5000);
        sendKeys(txtUsername, user);
        sendKeys(txtPassword, password);
        takeScreenshot("Datos_Login");
        click(btnLogin);
    }

}
