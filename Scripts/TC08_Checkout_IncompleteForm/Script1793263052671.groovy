import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType

// TC-08: Checkout fails when the form is incomplete (Gherkin @TC-08)
def data = findTestData('Data Files/CheckoutNegative')
String checkoutUrl = 'https://www.saucedemo.com/checkout-step-one.html'

def cssObject = { String name, String css ->
    TestObject to = new TestObject(name)
    to.addProperty('css', ConditionType.EQUALS, css)
    return to
}

TestObject addBtn = cssObject('add_backpack', '[data-test="add-to-cart-sauce-labs-backpack"]')
TestObject removeBtn = cssObject('remove_backpack', '[data-test="remove-sauce-labs-backpack"]')

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
    // Login
    WebUI.navigateToUrl('https://www.saucedemo.com')
    WebUI.waitForElementVisible(findTestObject('Pages/Login/input_username'), 10)
    WebUI.setText(findTestObject('Pages/Login/input_username'), 'standard_user')
    WebUI.setText(findTestObject('Pages/Login/input_password'), 'secret_sauce')
    WebUI.waitForElementClickable(findTestObject('Pages/Login/btn_login'), 10)
    WebUI.click(findTestObject('Pages/Login/btn_login'))
    WebUI.waitForElementVisible(findTestObject('Pages/Products/lbl_title'), 15)

    // Tambah 1 produk, dengan retry kondisional bila klik tidak tercatat
    WebUI.waitForElementClickable(addBtn, 10, FailureHandling.STOP_ON_FAILURE)
    WebUI.click(addBtn)
    if (!WebUI.waitForElementVisible(removeBtn, 5, FailureHandling.OPTIONAL)) {
        WebUI.comment('PERINGATAN: klik add to cart tidak tercatat, mencoba ulang')
        WebUI.click(addBtn)
        WebUI.waitForElementVisible(removeBtn, 10, FailureHandling.STOP_ON_FAILURE)
    }

    // Looping: satu baris data = satu skenario form tidak lengkap
    for (int row = 1; row <= data.getRowNumbers(); row++) {
        String firstName = data.getValue('first_name', row) ?: ''
        String lastName = data.getValue('last_name', row) ?: ''
        String postalCode = data.getValue('postal_code', row) ?: ''
        String expectedError = data.getValue('expected_error', row)

        WebUI.comment("Row ${row}: first='${firstName}', last='${lastName}', postal='${postalCode}'")

        // Mulai dari form yang bersih di setiap iterasi (sesi login dan cart tetap)
        WebUI.navigateToUrl(checkoutUrl)
        WebUI.waitForElementVisible(findTestObject('Pages/Checkout/input_firstName'), 10)

        WebUI.setText(findTestObject('Pages/Checkout/input_firstName'), firstName)
        WebUI.setText(findTestObject('Pages/Checkout/input_lastName'), lastName)
        WebUI.setText(findTestObject('Pages/Checkout/input_postalCode'), postalCode)

        WebUI.waitForElementClickable(findTestObject('Pages/Checkout/btn_continue'), 10)
        WebUI.click(findTestObject('Pages/Checkout/btn_continue'))

        // Pesan error muncul setelah submit
        WebUI.waitForElementVisible(findTestObject('Pages/Checkout/lbl_error'), 10)
        WebUI.verifyElementText(findTestObject('Pages/Checkout/lbl_error'), expectedError)

        // User harus tetap di halaman Checkout Information, tidak lanjut ke Overview
        WebUI.verifyMatch(WebUI.getUrl(), '.*checkout-step-one.*', true)
        WebUI.verifyElementNotPresent(findTestObject('Pages/Checkout/lbl_total'), 2)
    }
} finally {
    WebUI.closeBrowser()
}