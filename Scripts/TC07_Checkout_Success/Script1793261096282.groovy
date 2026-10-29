import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import org.openqa.selenium.WebElement

// TC-07: Successful end-to-end checkout (Gherkin @TC-07)

// Helper: buat Test Object dinamis dari selector CSS
def cssObject = { String name, String css ->
    TestObject to = new TestObject(name)
    to.addProperty('css', ConditionType.EQUALS, css)
    return to
}
// Helper: ubah teks seperti "Item total: $45.98" menjadi angka 45.98
def parseMoney = { String text ->
    return new BigDecimal(text.replaceAll('[^0-9.]', ''))
}

List<String> products = ['sauce-labs-backpack', 'sauce-labs-bolt-t-shirt']

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
    // 1. Login
    WebUI.navigateToUrl('https://www.saucedemo.com')
    WebUI.waitForElementVisible(findTestObject('Pages/Login/input_username'), 10)
    WebUI.setText(findTestObject('Pages/Login/input_username'), 'standard_user')
    WebUI.setText(findTestObject('Pages/Login/input_password'), 'secret_sauce')
    WebUI.waitForElementClickable(findTestObject('Pages/Login/btn_login'), 10)
    WebUI.click(findTestObject('Pages/Login/btn_login'))
    WebUI.waitForElementVisible(findTestObject('Pages/Products/lbl_title'), 15)

    // 2. Tambah produk secara dinamis, dengan retry kondisional bila klik tidak tercatat
	for (String p : products) {
		TestObject addBtn = cssObject('add_' + p, '[data-test="add-to-cart-' + p + '"]')
		TestObject removeBtn = cssObject('remove_' + p, '[data-test="remove-' + p + '"]')

		WebUI.waitForElementClickable(addBtn, 10, FailureHandling.STOP_ON_FAILURE)
		WebUI.click(addBtn)

	    // Conditional: bila tombol Remove tidak muncul, catat dan coba sekali lagi
	    if (!WebUI.waitForElementVisible(removeBtn, 5, FailureHandling.OPTIONAL)) {
	        WebUI.comment("PERINGATAN: klik pertama pada ${p} tidak tercatat, mencoba ulang")
	        WebUI.takeScreenshot()
	        WebUI.click(addBtn)
	        WebUI.waitForElementVisible(removeBtn, 10, FailureHandling.STOP_ON_FAILURE)
    }
}

    // 3. Ke halaman Cart, tunggu SETIAP produk muncul, baru hitung
	WebUI.click(findTestObject('Pages/Products/lnk_cart'))
	WebUI.waitForElementVisible(findTestObject('Pages/Cart/list_cartItems'), 10)

	for (String p : products) {
		TestObject itemInCart = cssObject('cart_' + p, '.cart_item [data-test="remove-' + p + '"]')
		WebUI.waitForElementVisible(itemInCart, 10)
	}

	TestObject cartRows = cssObject('cartRows', '.cart_item')
	List<WebElement> rows = WebUI.findWebElements(cartRows, 10)
	List<WebElement> names = WebUI.findWebElements(cssObject('cartNames', '.cart_item .inventory_item_name'), 10)
	WebUI.comment("Item di Cart: ${rows.size()}, nama: " + names.collect { it.getText() })
	WebUI.verifyEqual(rows.size(), products.size())

    // 4. Checkout: isi form
    WebUI.waitForElementClickable(findTestObject('Pages/Cart/btn_checkout'), 10)
    WebUI.click(findTestObject('Pages/Cart/btn_checkout'))
    WebUI.waitForElementVisible(findTestObject('Pages/Checkout/input_firstName'), 10)
    WebUI.setText(findTestObject('Pages/Checkout/input_firstName'), 'Raditya')
    WebUI.setText(findTestObject('Pages/Checkout/input_lastName'), 'Ramadhani')
    WebUI.setText(findTestObject('Pages/Checkout/input_postalCode'), '55281')
    WebUI.click(findTestObject('Pages/Checkout/btn_continue'))

    // 5. Overview: tunggu halaman termuat (elemen dinamis)
    // Conditional: kalau form ditolak, gagal dengan pesan yang jelas
    if (WebUI.verifyElementPresent(findTestObject('Pages/Checkout/lbl_error'), 2, FailureHandling.OPTIONAL)) {
        WebUI.comment('Form ditolak: ' + WebUI.getText(findTestObject('Pages/Checkout/lbl_error')))
    }
    WebUI.waitForElementVisible(findTestObject('Pages/Checkout/lbl_total'), 10)

    int itemsInOverview = WebUI.findWebElements(findTestObject('Pages/Checkout/list_overviewItems'), 10).size()
    WebUI.verifyEqual(itemsInOverview, products.size())

    // 6. Verifikasi perhitungan: item total = jumlah harga item, total = item total + tax
    BigDecimal sumPrices = 0
    List<WebElement> priceElements = WebUI.findWebElements(findTestObject('Pages/Checkout/lbl_itemPrice'), 10)
    for (WebElement el : priceElements) {
        sumPrices += parseMoney(el.getText())
    }
    BigDecimal itemTotal = parseMoney(WebUI.getText(findTestObject('Pages/Checkout/lbl_itemTotal')))
    BigDecimal tax = parseMoney(WebUI.getText(findTestObject('Pages/Checkout/lbl_tax')))
    BigDecimal total = parseMoney(WebUI.getText(findTestObject('Pages/Checkout/lbl_total')))

    WebUI.comment("Jumlah harga item=${sumPrices}, item total=${itemTotal}, tax=${tax}, total=${total}")
    WebUI.verifyEqual(itemTotal, sumPrices)
    WebUI.verifyEqual(total, itemTotal + tax)

    // 7. Selesaikan order dan verifikasi konfirmasi
    WebUI.waitForElementClickable(findTestObject('Pages/Checkout/btn_finish'), 10)
    WebUI.click(findTestObject('Pages/Checkout/btn_finish'))
    WebUI.waitForElementVisible(findTestObject('Pages/Checkout/lbl_completeHeader'), 10)
    WebUI.verifyElementText(findTestObject('Pages/Checkout/lbl_completeHeader'), 'Thank you for your order!')

    // 8. Cart ter-reset: badge tidak boleh ada lagi
    WebUI.verifyElementNotPresent(findTestObject('Pages/Products/lbl_cartBadge'), 3)

    // 9. Kembali ke Products
    WebUI.click(findTestObject('Pages/Checkout/btn_backHome'))
    WebUI.waitForElementVisible(findTestObject('Pages/Products/lbl_title'), 10)
    WebUI.verifyElementNotPresent(findTestObject('Pages/Products/lbl_cartBadge'), 3)
} finally {
    WebUI.closeBrowser()
}