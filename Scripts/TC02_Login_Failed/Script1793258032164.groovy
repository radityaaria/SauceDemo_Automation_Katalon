import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

// TC-02: Failed login with invalid credentials (Gherkin @TC-02)
def data = findTestData('Data Files/LoginNegative')
String loginUrl = 'https://www.saucedemo.com/'

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
    // Looping: satu baris data = satu skenario negatif
    for (int row = 1; row <= data.getRowNumbers(); row++) {
        String username = data.getValue('username', row) ?: ''
        String password = data.getValue('password', row) ?: ''
        String expectedError = data.getValue('expected_error', row)

        WebUI.comment("Row ${row}: username='${username}', password='${password}'")

        // Mulai dari halaman login yang bersih di setiap iterasi
        WebUI.navigateToUrl(loginUrl)
        WebUI.waitForElementVisible(findTestObject('Pages/Login/input_username'), 10)

        WebUI.setText(findTestObject('Pages/Login/input_username'), username)
        WebUI.setText(findTestObject('Pages/Login/input_password'), password)

        WebUI.waitForElementClickable(findTestObject('Pages/Login/btn_login'), 10)
        WebUI.click(findTestObject('Pages/Login/btn_login'))

        // Pesan error muncul setelah submit, jadi tunggu sampai terlihat
        WebUI.waitForElementVisible(findTestObject('Pages/Login/lbl_error'), 10)
        WebUI.verifyElementText(findTestObject('Pages/Login/lbl_error'), expectedError)

        // Conditional: pastikan user tetap di halaman login dan tidak masuk ke Products
        if (WebUI.getUrl() == loginUrl) {
            WebUI.comment("Row ${row}: tetap di halaman login (sesuai harapan)")
        } else {
            WebUI.verifyEqual(WebUI.getUrl(), loginUrl)   // sengaja dibuat gagal agar terlihat di report
        }
        WebUI.verifyElementNotPresent(findTestObject('Pages/Products/lbl_title'), 2)
    }
} finally {
    WebUI.closeBrowser()
}