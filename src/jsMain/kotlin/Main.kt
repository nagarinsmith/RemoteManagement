import org.w3c.xhr.XMLHttpRequest
import kotlin.browser.document

fun main() {
    document.getElementById(POWER_BUTTON)?.addEventListener("click", {
        XMLHttpRequest().run {
            open("POST", SHUTDOWN, true)
            send()
        }
    })
}