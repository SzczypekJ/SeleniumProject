package com.solvd.testng.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CartPage {

    private WebDriver driver;

    @FindBy(css = "#tbodyid > tr > td:nth-child(2)")
    private List<WebElement> productNamesInCart;

    @FindBy(css = "#tbodyid > tr > td:nth-child(3)")
    private List<WebElement> productPricesInCart;

    @FindBy(xpath = "//button[@data-target='#orderModal']")
    private WebElement placeOrderButton;

    @FindBy(css = "#totalp")
    private WebElement totalPrice;

    @FindBy(xpath = "//a[@id='logout2']")
    private WebElement logoutButton;

    @FindBy(xpath = "//a[contains(@onclick, 'deleteItem')]")
    private List<WebElement> deleteButtons;

    @FindBy(id = "tbodyid")
    private WebElement cartTable;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public List<String> getProductNamesInCart() {
        List<String> productNames = new ArrayList<>();

        for (WebElement productName : productNamesInCart) {
            productNames.add(productName.getText());
        }
        return productNames;
    }

    public List<WebElement> getProductNamesInCartElement() {
        return productNamesInCart;
    }

    public List<String> getProductPricesInCart() {
        List<String> productPrices = new ArrayList<>();
        for (WebElement productPrice : productPricesInCart) {
            String rawPrice = productPrice.getText();
            productPrices.add(rawPrice.replaceAll("[^0-9]", ""));
        }
        return productPrices;
    }

    public List<WebElement> getProductPricesInCartElement() {
        return productPricesInCart;
    }

    public void placeOrder() {
        placeOrderButton.click();
    }

    public WebElement getProductNameInTheCartElement() {
        return productNamesInCart.get(0);
    }

    public WebElement getProductPriceInTheCartElement() {
        return productPricesInCart.get(0);
    }

    public String getTotalPrice() {
        return totalPrice.getText();
    }

    public void logout() {
        logoutButton.click();
        System.out.println("Wylogowano użytkownika.");
    }

    public void deleteAllItems() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        for (WebElement deleteButton : deleteButtons) {
            deleteButton.click();
            wait.until(ExpectedConditions.stalenessOf(deleteButton));
        }

        boolean isEmpty = cartTable.findElements(By.xpath("./tr")).isEmpty();
        if (!isEmpty) {
            throw new AssertionError("Cart is not empty after deleting items!");
        }
    }

    public List<WebElement> getDeleteButtons() {
        return deleteButtons;
    }

}
