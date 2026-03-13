OrangeHRM Test Automation
Proyecto de automatización de pruebas para OrangeHRM usando Selenium, TestNG y Java. Soporta ejecución local y en BrowserStack.

Tecnologias:
*Java 11
*Selenium 4
*TestNG
*WebDriverManager
*ExtentReports
*BrowserStack

Requisitos:
*Java 11
*Maven
*Chrome o Firefox (para ejecución local)
*Credenciales de BrowserStack (para ejecución remota)

Configuracion
Las credenciales de BrowserStack se configuran como variables de entorno:

BROWSERSTACK_USERNAME=tu_usuario
BROWSERSTACK_ACCESS_KEY=tu_access_key

Ejecucion

Local: mvn clean test -Plocal

BrowserStack: mvn clean test \
-Dexecution.env=browserstack \
-DBROWSERSTACK_USERNAME=tu_usuario \
-DBROWSERSTACK_ACCESS_KEY=tu_access_key

Pipeline CI/CD:

El pipeline de GitHub Actions se ejecuta automaticamente en cada push a main o develop y corre los tests en BrowserStack.
Para ejecutar manualmente desde GitHub Actions, ir a Actions, seleccionar el workflow y elegir el entorno de ejecucion (browserstack o local).
Los secrets BROWSERSTACK_USERNAME y BROWSERSTACK_ACCESS_KEY deben estar configurados en Settings > Secrets and variables > Actions del repositorio.

Estructura del proyecto:

src/
test/
java/
com/orangehrm/
base/        # BaseTest y configuracion del driver
pages/       # Page Objects
tests/       # Clases de test
utils/       # ConfigReader y utilidades
resources/
images/        # Imagenes usadas en los tests
testng.xml     # Suite de TestNG
config.properties
screenshots/         # Capturas de pantalla generadas
test-output/         # Reportes de ExtentReports

Reportes: Al finalizar la ejecucion se genera un reporte HTML en test-output/TestReport.html. Las capturas de pantalla de los tests fallidos se guardan en screenshots/.