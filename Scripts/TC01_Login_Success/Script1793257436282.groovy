import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

// TC-01: Successful login with valid users (Gherkin @TC-01)
String password = 'secret_sauce'
List<String> users = ['standard_user', 'performance_glitch_user']

// Looping: satu test case menguji beberapa user
for (String user : users) {
    try {
        WebUI.openBrowser('')
        WebUI.maximizeWindow()
        WebUI.navigateToUrl('https://www.saucedemo.com')

        WebUI.waitForElementVisible(findTestObject('Pages/Login/input_username'), 10)
        WebUI.setText(findTestObject('Pages/Login/input_username'), user)
        WebUI.setText(findTestObject('Pages/Login/input_password'), password)

        long start = System.currentTimeMillis()
        WebUI.waitForElementClickable(findTestObject('Pages/Login/btn_login'), 10)
        WebUI.click(findTestObject('Pages/Login/btn_login'))

        // Dynamic wait: timeout 15 detik karena performance_glitch_user login-nya lambat
        WebUI.waitForElementVisible(findTestObject('Pages/Products/lbl_title'), 15)
        long elapsed = System.currentTimeMillis() - start

        WebUI.verifyElementText(findTestObject('Pages/Products/lbl_title'), 'Products')

        int totalProducts = WebUI.findWebElements(findTestObject('Pages/Products/list_products'), 10).size()
        WebUI.verifyEqual(totalProducts, 6)

        // Conditional: catat waktu muat khusus untuk user yang lambat
        if (user == 'performance_glitch_user') {
            WebUI.comment("Login ${user} butuh ${elapsed} ms (dynamic wait berhasil)")
        } else {
            WebUI.comment("Login ${user} selesai dalam ${elapsed} ms")
        }
    } finally {
        WebUI.closeBrowser()
    }
}