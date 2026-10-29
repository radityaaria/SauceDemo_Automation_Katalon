import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType

// TC-05: Add multiple products to the cart dynamically (Gherkin @TC-05)
List<Integer> counts = [2, 4, 6]

for (int count : counts) {
    WebUI.comment("=== Skenario: tambah ${count} produk ===")

    // Browser baru per iterasi supaya cart selalu mulai dari kosong
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

        // Conditional: pastikan cart benar-benar kosong sebelum mulai
        if (WebUI.verifyElementPresent(findTestObject('Pages/Products/lbl_cartBadge'), 2, FailureHandling.OPTIONAL)) {
            WebUI.comment('Peringatan: cart sudah berisi sebelum test dimulai')
        } else {
            WebUI.comment('Cart kosong di awal (sesuai harapan)')
        }

        // Conditional: jangan minta lebih banyak dari produk yang tersedia
        int totalProducts = WebUI.findWebElements(findTestObject('Pages/Products/list_products'), 10).size()
        int toAdd = Math.min(count, totalProducts)

        // Looping: tombol add pertama yang tersisa selalu produk berikutnya,
        // karena tombol yang sudah diklik berubah menjadi "Remove"
        for (int i = 1; i <= toAdd; i++) {
            WebUI.waitForElementClickable(findTestObject('Pages/Products/btn_addToCart_next'), 10)
            WebUI.click(findTestObject('Pages/Products/btn_addToCart_next'))

			// Dynamic wait: tunggu sampai badge menampilkan angka i (tahan terhadap re-render)
			TestObject badgeNow = new TestObject('badge_' + i)
			badgeNow.addProperty('xpath', ConditionType.EQUALS,
				("//span[@class='shopping_cart_badge' and normalize-space(text())='" + i + "']").toString())
			WebUI.waitForElementVisible(badgeNow, 10)
        }

        // Verifikasi badge akhir
        WebUI.verifyElementText(findTestObject('Pages/Products/lbl_cartBadge'), String.valueOf(toAdd))

        // Verifikasi halaman Cart
        WebUI.click(findTestObject('Pages/Products/lnk_cart'))
        WebUI.waitForElementVisible(findTestObject('Pages/Cart/list_cartItems'), 10)
        int itemsInCart = WebUI.findWebElements(findTestObject('Pages/Cart/list_cartItems'), 10).size()
        WebUI.verifyEqual(itemsInCart, toAdd)

        WebUI.comment("OK: ${toAdd} produk tercatat di badge dan di halaman Cart")
    } finally {
        WebUI.closeBrowser()
    }
}