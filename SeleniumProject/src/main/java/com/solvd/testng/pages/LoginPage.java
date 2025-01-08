package com.solvd.testng.pages;

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Getter
public class LoginPage {

    private WebDriver driver;

    @FindBy(xpath = "//a[@id='login2']")
    private WebElement loginButton;

    @FindBy(xpath = "//input[@id='loginusername']")
    private WebElement usernameField;

    @FindBy(xpath = "//input[@id='loginpassword']")
    private WebElement passwordField;

    @FindBy(xpath = "//a[@id='nameofuser']")
    private WebElement welcomeText;

    @FindBy(xpath = "//button[@onclick='logIn()']")
    private WebElement submitButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void openLoginModal() {
        loginButton.click();
    }

    public void login(String username, String password) {
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        submitButton.click();
    }

    public String getWelcomeText() {
        return welcomeText.getText();
    }

    public WebElement getWelcomeTextElement() {
        return welcomeText;
    }

    public void open() {
        driver.get("https://www.demoblaze.com/index.html");
    }
}
