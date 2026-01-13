package uk.co.zlurgg.thedayto.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.TimeUnit

object HttpClientFactory {

    private const val NETWORK_TIMEOUT_SECONDS = 30L

    fun create(enableLogging: Boolean = false): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = if (enableLogging) LogLevel.INFO else LogLevel.NONE
            }
        }
    }
}
