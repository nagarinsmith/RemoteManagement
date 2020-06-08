import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.RaspiPin
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.script
import kotlinx.html.title
import java.util.concurrent.atomic.AtomicBoolean

fun main() {
    val gpio = GpioFactory.getInstance()
    val button = gpio.provisionDigitalMultipurposePin(RaspiPin.GPIO_00, PinMode.DIGITAL_INPUT)
    val isPowerButtonPressed = AtomicBoolean(false)

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        routing {
            get {
                call.respondHtml {
                    head {
                        title("Management")
                    }
                    body {
                        button {
                            id = POWER_BUTTON
                            +"POWER"
                        }
                        script(src = "/static/RemoteManagement.js") {}
                    }
                }
            }
            post("/$SHUTDOWN") {
                call.respond(HttpStatusCode.OK)
                launch {
                    if (isPowerButtonPressed.compareAndSet(false, true)) {
                        button.mode = PinMode.DIGITAL_OUTPUT
                        delay(1000)
                        button.mode = PinMode.DIGITAL_INPUT
                        isPowerButtonPressed.set(false)
                    }
                }
            }
            static("/static") {
                resource("RemoteManagement.js")
            }
        }
    }.start(wait = true)
}
