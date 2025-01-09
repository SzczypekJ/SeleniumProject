package com.solvd.pageobjectpattern;

import com.solvd.testng.pages.*;
import com.solvd.testng.utils.DriverPool;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProductStoreTest {

    @Test
    @Parameters({"browser"})
    public void testLogin(@Optional("chrome") String browser) {
        WebDriver driver = DriverPool.getDriver(browser);
        try {
            login(driver, "jakubszczypek", "1234");
        } finally {
            DriverPool.quitDriver();
        }
    }

    @Test
    @Parameters({"browser"})
    public void testAddProductToCart(@Optional("chrome") String browser) {
        WebDriver driver = DriverPool.getDriver(browser);
        try {
            // Log in - the helpful method to log in - the reuse of code
            login(driver, "jakubszczypek2", "1234");

            // Adds product to the cart - helpful method which reduce amount of code
            addProductToCart(driver);

            CartPage cartPage = new CartPage(driver);
            cartPage.deleteAllItems();
            Assert.assertTrue(cartPage.getDeleteButtons().isEmpty(),
                    "Cart is not empty after removing all items");

        } finally {
            DriverPool.quitDriver();
        }
    }

    @Test
    @Parameters({"browser"})
    public void testAddSingleProductPurchase(@Optional("chrome") String browser) {
        WebDriver driver = DriverPool.getDriver(browser);

        try {
            login(driver, "jakubszczypek3", "1234");

            String price = addProductToCart(driver);
            testPurchaseProduct(driver, price);

        } finally {
            DriverPool.quitDriver();
        }
    }


    @Test
    @Parameters({"browser"})
    public void testPurchaseProductWithList(@Optional("chrome") String browser) {
        WebDriver driver = DriverPool.getDriver(browser);

        try {
            login(driver, "jakubszczypek4", "1234");

            AllProductsPage allProductsPage = new AllProductsPage(driver);
            waitForConditionWithMessage(driver,
                    ExpectedConditions.visibilityOfAllElements(allProductsPage.getProductList()),
                    "No products found on the page!");

            List<String> namesOfProducts = new ArrayList<>();
            List<String> pricesOfProducts = new ArrayList<>();

            // Product 1
            namesOfProducts.add(allProductsPage.getProductNameByIndex(0));
            pricesOfProducts.add(allProductsPage.addProductToCartByIndex(0));

            // Product 2
            namesOfProducts.add(allProductsPage.getProductNameByIndex(1));
            pricesOfProducts.add(allProductsPage.addProductToCartByIndex(1));

            // Product 3
            namesOfProducts.add(allProductsPage.getProductNameByIndex(5));
            pricesOfProducts.add(allProductsPage.addProductToCartByIndex(5));

            allProductsPage.goToCart();

            CartPage cartPage = new CartPage(driver);
            waitForConditionWithMessage(driver,
                    ExpectedConditions.visibilityOfAllElements(cartPage.getProductNamesInCartElement()),
                    "The elements Product names in the cart were not found in 10 seconds!"
            );

            waitForConditionWithMessage(driver,
                    ExpectedConditions.visibilityOfAllElements(cartPage.getProductPricesInCartElement()),
                    "The elements Product prices in the cart were not found in 10 seconds!");

            List<String> cartProductNames = cartPage.getProductNamesInCart();
            List<String> cartProductPrices = cartPage.getProductPricesInCart();

            // Sort lists using stream
            namesOfProducts = namesOfProducts.stream().sorted().toList();
            cartProductNames = cartProductNames.stream().sorted().toList();
            pricesOfProducts = pricesOfProducts.stream().sorted().toList();
            cartProductPrices = cartProductPrices.stream().sorted().toList();

            Assert.assertEquals(cartProductNames, namesOfProducts,
                    "Product names in the cart do not match the selected products!");
            Assert.assertEquals(cartProductPrices, pricesOfProducts,
                    "Product prices in the cart do not match the selected products!");

            String totalPrice = cartPage.getTotalPrice();
            int actualTotalPrice = Integer.parseInt(totalPrice);

            int expectedTotalPrice = cartProductPrices.stream()
                    .mapToInt(Integer::parseInt)
                    .sum();

            Assert.assertEquals(actualTotalPrice, expectedTotalPrice, "Total price mismatch!");

            testPurchaseProduct(driver, totalPrice);
        } finally {
            DriverPool.quitDriver();
        }
    }


    public void testPurchaseProduct(WebDriver driver, String totalPrice) {
        CheckoutPage checkoutPage= new CheckoutPage(driver);

        CartPage cartPage = new CartPage(driver);
        cartPage.placeOrder();

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getNameField()),
                "The element Name field was not found in 10 seconds!");

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getCountryField()),
                "The element Country field was not found in 10 seconds!");

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getCityField()),
                "The element City field was not found in 10 seconds!");

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getCardField()),
                "The element Card field was not found in 10 seconds!");

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getMonthField()),
                "The element Month field was not found in 10 seconds!");

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getYearField()),
                "The element Year field was not found in 10 seconds!");

        // Filling the purchase details
        String name = "Jakub";
        String country = "Poland";
        String city = "Cracow";
        String creditCard = "4111111111111111";
        String month = "January";
        String year = "2024";

        checkoutPage.fillCheckoutDetails(name, country, city, creditCard, month, year);

        waitForConditionWithMessage(driver,
                ExpectedConditions.elementToBeClickable(checkoutPage.getPurchaseButton()),
                "The element Purchase button was not found in 10 seconds!");

        checkoutPage.completePurchase();
        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(checkoutPage.getPurchaseDetailsElement()),
                "The element Purchase details was not found in 10 seconds!");

        // Validate the purchase summary details
        String purchaseDetails = checkoutPage.getPurchaseDetails();
        Assert.assertTrue(purchaseDetails.contains("Id:"), "Purchase details missing ID!");
        Assert.assertTrue(purchaseDetails.contains("Amount: " + totalPrice), "Purchase amount mismatch!");
        Assert.assertTrue(purchaseDetails.contains("Card Number: " + creditCard), "Credit card mismatch!");
        Assert.assertTrue(purchaseDetails.contains("Name: " + name), "Name mismatch in purchase details!");

        System.out.println("Test passed: Purchase details are correct!");

        checkoutPage.confirmPurchase();
    }

    public static void waitForConditionWithMessage(WebDriver driver, ExpectedCondition<?> condition, String message) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(condition);
        } catch (TimeoutException e) {
            throw new AssertionError(message, e);
        }
    }

    public static void login(WebDriver driver, String username, String password) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.openLoginModal();
        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(loginPage.getUsernameField()),
                "The element Username field was not found in 10 seconds!");

        loginPage.login(username, password);
        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(loginPage.getWelcomeTextElement()),
                "The welcome text was not found in 10 seconds!");

        Assert.assertEquals(loginPage.getWelcomeText(), "Welcome " + username,
                "Login failed or welcome message is incorrect!");
    }

    public static String addProductToCart(WebDriver driver) {
        AllProductsPage allProductsPage = new AllProductsPage(driver);

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(allProductsPage.getProductNameElement()),
                "The product name element was not found in 10 seconds!");

        ProductPage productPage = new ProductPage(driver);
        String productName = allProductsPage.getProductName();
        allProductsPage.selectProduct();

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(productPage.getProductPriceElement()),
                "The product price element was not found in 10 seconds!");
        String price = productPage.getProductPrice();
        productPage.addToCart();
        productPage.goToCart();

        CartPage cartPage = new CartPage(driver);
        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(cartPage.getProductNameInTheCartElement()),
                "The product name in the cart element was not found in 10 seconds!");

        waitForConditionWithMessage(driver,
                ExpectedConditions.visibilityOf(cartPage.getProductPriceInTheCartElement()),
                "The product price in the cart element was not found in 10 seconds!");

        Assert.assertEquals(cartPage.getProductNamesInCart().get(0), productName,
                "Product name in the cart does not match the selected product!");
        Assert.assertTrue(price.contains(cartPage.getProductPricesInCart().get(0)),
                "Product price in the cart does not match the selected product!");

        return price;

    }
}
