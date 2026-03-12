package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.pages.DirectoryPage;
import com.orangehrm.pages.HomePage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.pages.PimPage;
import org.testng.annotations.Test;

public class AddEmployee extends BaseTest {

    private LoginPage loginPage;
    private HomePage homePage;
    private PimPage pimPage;
    private DirectoryPage directoryPage;
    String nameEmployee = "valentin";
    String lastNameEmployee = "galindo";
    String fullName = nameEmployee + " " + lastNameEmployee;
    String textUser = "Admin";
    String textPassword = "admin123";

    @Test(priority = 1, description = "Test Agregar empleado")
    public void testAddEmployee() throws InterruptedException {
        loginPage = new LoginPage(driver);
        homePage = new HomePage(driver);
        pimPage = new PimPage(driver);
        directoryPage = new DirectoryPage(driver);
        loginPage.open();
        test.info("Test: Abrir pagina principal Orange HRM");
        loginPage.loginOrangeHrm(textUser, textPassword);
        test.info("Test: Se hace login con usuario y contraseña");
        homePage.homeClickPim();
        test.info("Test: Se da click en el modulo PIM");
        pimPage.validateAddModulePim();
        test.info("Test: Se valida que estamos en el modulo PIM");
        pimPage.addEmployee(nameEmployee, lastNameEmployee);
        test.info("Test: Se da click en agregar empleado");
        test.info("Test: Se agrega empleado con Nombre y Apellido");
        pimPage.validateEmployeeCreation(fullName);
        test.info("Test: Se valida la creacion del empleado");
        pimPage.clickDirectory();
        test.info("Test: Se da click en el modulo Directory");
        directoryPage.searchEmployee(nameEmployee);
        test.info("Test: Se ingresa nombre de empleado a buscar y se da click en buscar");
        directoryPage.validateSearchEmployee(fullName);
        test.info("Test: Se valida la busqueda exitosa del empleado");
    }
}
