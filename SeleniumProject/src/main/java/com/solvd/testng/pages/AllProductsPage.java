package com.solvd.testng.pages;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@Getter
public class AllProductsPage {

    private WebDriver driver;

    @FindBy(xpath = "//a[@href='prod.html?idp_=3' and @class='hrefch']")
    private WebElement nexusProduct;

    @FindBy(xpath = "//a[@class='hrefch']")
    private List<WebElement> productList;

    @FindBy(css = "#navbarExample > ul > li.nav-item.active > a")
    private WebElement Home;

    @FindBy(xpath = "//a[@id='cartur']")
    private WebElement cartButton;

    @FindBy(xpath = "//a[@id='logout2']")
    private WebElement logoutButton;

    public AllProductsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void selectProduct() {
        nexusProduct.click();
    }

    public String getProductName() {
        return nexusProduct.getText();
    }

    public WebElement getProductNameElement() {
        return nexusProduct;
    }

    public void goToHome() {
        Home.click();
    }

    /**
     * Fetches the price of a product currently displayed.
     * Assumes the product page is already open.
     *
     * @return Price of the product as a String.
     */
    private String fetchProductPrice() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement priceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[@class='price-container']")));
        return priceElement.getText().replaceAll("[^0-9]", "");
    }

    /**
     * I will add a new product using this method
     * Here we are working on the list of web elements which is more preferable way to work with elements
     * The element will be take by index and it will be added to the cart
     * Fetches the product's price before adding it to the cart.
     * After adding the product to the cart I will go back to the main page
     * @param index Index of the product in the product list.
     * @return Price of the product as a String.
     */
    public String addProductToCartByIndex(int index) {

        productList.get(index).click();

        String productPrice = fetchProductPrice();

        String dynamicXPath = String.format("//a[@onclick='addToCart(%d)']", index + 1);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(dynamicXPath)));
        addToCartButton.click();

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            System.out.println("Alert not found: " + e.getMessage());
        }

        goToHome();
        return productPrice;
    }

    public String getProductNameByIndex(int index) {
        return productList.get(index).getText();
    }

    public void goToCart() {
        cartButton.click();
    }

    public void logout() {
        logoutButton.click();
    }
}
