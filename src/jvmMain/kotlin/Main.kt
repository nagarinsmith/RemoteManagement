import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.RaspiPin
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.css.CSSBuilder
import kotlinx.css.LinearDimension
import kotlinx.css.Position
import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.link
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
                        link(rel = "stylesheet", href = "/styles.css", type = "text/css")
                    }
                    body {
                        img {
                            id = POWER_BUTTON
                            src =
                                "https://upload.wikimedia.org/wikipedia/commons/5/59/Simpleicons_Interface_power-button-symbol.svg"
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
            get("/styles.css") {
                call.respondCss {
                    rule("#$POWER_BUTTON") {
                        position = Position.fixed
                        top = LinearDimension("50%")
                        left = LinearDimension("50%")
                        width = LinearDimension("100pt")
                        height = LinearDimension("100pt")
                        marginTop = LinearDimension("-50pt")
                        marginLeft = LinearDimension("-50pt")
                    }
                    rule("#$POWER_BUTTON:active") {
                        filter =
                            "invert(20%) sepia(96%) saturate(7421%) hue-rotate(4deg) brightness(99%) contrast(119%)"
                    }
                }
            }
            static("/static") {
                resource("RemoteManagement.js")
            }
        }
    }.start(wait = true)
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
