package org.seconf22;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.service.DriverCommandExecutor;
import org.openqa.selenium.support.ui.Select;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebAuthnTest {

    static WebDriver driver;

    @BeforeAll
    static void setup() {
        driver = WebDriverManager.chromedriver().create();
    }

    @Test
    @DisplayName("WebAuthn reg and auth flow should work")
    void sampleTest() throws Exception {

        // set up virtual authenticator for webauthn
        setupVirtualAuthenticator();

        // start registration
        driver.get("https://webauthn.io");
        driver.findElement(By.id("input-email")).sendKeys("seconf22");

        Select selectAttestationType = new Select(driver.findElement(By.id("select-attestation")));
        selectAttestationType.selectByVisibleText("Direct");

        Select selectAuthenticatorType = new Select(driver.findElement(By.id("select-authenticator")));
        selectAuthenticatorType.selectByVisibleText("Platform (TPM)");

        driver.findElement(By.id("register-button")).click();

        // registration should be successful
        Thread.sleep(3000);
        assertTrue(driver.findElement(By.xpath("//div[contains(text(),'Success!')]")).isDisplayed());

        // start authentication
        driver.findElement(By.id("login-button")).click();

        // authentication should be successful
        Thread.sleep(3000);
        assertTrue(driver.findElement(By.xpath("//h3[text()=\"You're logged in!\"]")).isDisplayed());
    }

    private void setupVirtualAuthenticator() throws Exception {
        // get the session id
        RemoteWebDriver remoteWebDriver = (RemoteWebDriver) driver;
        SessionId sessionId = remoteWebDriver.getSessionId();

        DriverCommandExecutor commandExecutor = (DriverCommandExecutor) remoteWebDriver.getCommandExecutor();
        CommandInfo commandInfo = new CommandInfo("/session/:sessionId/webauthn/authenticator", HttpMethod.POST);

        // using reflection to access protected method
        Method defineCommand = HttpCommandExecutor.class.getDeclaredMethod("defineCommand", String.class, CommandInfo.class);
        defineCommand.setAccessible(true);
        defineCommand.invoke(commandExecutor, "AddVirtualAuthenticator", commandInfo);

        // executing new 'add virtual authenticator' command
        Map<String, String> params = Map.of("protocol", "ctap2", "transport", "internal");
        Command addVirtualAuthCommand = new Command(sessionId, "AddVirtualAuthenticator", params);
        commandExecutor.execute(addVirtualAuthCommand);
    }

    @AfterAll
    static void cleanup() {
        driver.quit();
    }
}