package org.listenbrainz.android

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockWebServer
import kotlin.Throws
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.listenbrainz.android.data.sources.api.MusicBrainzServiceGenerator
import org.listenbrainz.android.data.sources.api.LoginService
import java.io.IOException
import java.util.*

class LoginRepositoryTest {
    private var webServer: MockWebServer? = null
    var code = "Nlaa7v15QHm9g8rUOmT3dQ"
    var accessToken = """{
  "access_token":"8OC8as1VpATqkM79KfcdTw",
  "expires_in":3600,
  "token_type":"Bearer",
  "refresh_token":"Td5-okcuJvqgXLuq_YMXHA"
}"""

    private fun getErrorBody(reason: String): String {
        return String.format("{\"error\":\"%s\"}", reason)
    }

    @Before
    @Throws(IOException::class)
    fun setUp() {
        webServer = MockWebServer()
        webServer!!.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                var response: MockResponse? = null
                when (request.path) {
                    "/oauth2/authorize" -> response = MockResponse()
                        .setResponseCode(200)
                        .setStatus("OK")
                        .setBody(MusicBrainzServiceGenerator.OAUTH_REDIRECT_URI + "?code=" + code)
                    "/oauth2/token" -> {
                        val parameters: MutableMap<String, String> = HashMap()
                        val args = request.body.readUtf8().split("&").toTypedArray()
                        Arrays.stream(args).forEach { s: String ->
                            val parts = s.split("=").toTypedArray()
                            parameters[parts[0]] = parts[1]
                        }
                        var invalid = ""
                        if (!parameters.containsKey("grant_type")) invalid =
                            "Missing grant type" else if (!parameters.containsKey("client_id")) invalid =
                            "Missing Client id" else if (!parameters.containsKey("client_secret")) invalid =
                            "Missing Client Secret" else if (!parameters.containsKey("code")) invalid =
                            "Missing code" else if (!parameters.containsKey("redirect_uri")) invalid =
                            "Missing redirect URI" else if (!parameters["grant_type"].equals(
                                "authorization_code",
                                ignoreCase = true
                            )
                        ) invalid = "Invalid grant type" else if (!parameters["client_id"].equals(
                                MusicBrainzServiceGenerator.CLIENT_ID,
                                ignoreCase = true
                            )
                        ) invalid =
                            "Invalid Client ID" else if (!parameters["client_secret"].equals(
                                MusicBrainzServiceGenerator.CLIENT_SECRET,
                                ignoreCase = true
                            )
                        ) invalid = "Invalid Client Secret" else if (!parameters["code"].equals(
                                code,
                                ignoreCase = true
                            )
                        ) invalid = "Invalid authorization code" else {
                            response = MockResponse()
                                .setResponseCode(200)
                                .setBody(accessToken)
                        }
                        if (invalid.isNotEmpty()) response = MockResponse()
                            .setResponseCode(401)
                            .setBody(getErrorBody(invalid))
                    }
                    else -> response = MockResponse().setResponseCode(404)
                }
                return response!!
            }
        }
        webServer!!.start()
    }

    @Test
    @Throws(IOException::class)
    fun fetchAccessToken() {
        val service =
            RetrofitUtils.createTestService(LoginService::class.java, webServer!!.url("/"))
        val accessToken = service
            .getAccessToken(
                webServer!!.url("/oauth2/").toString() + "token",
                code,
                "authorization_code",
                MusicBrainzServiceGenerator.CLIENT_ID,
                MusicBrainzServiceGenerator.CLIENT_SECRET,
                MusicBrainzServiceGenerator.OAUTH_REDIRECT_URI
            )
            ?.execute()
            ?.body()!!
        Assert.assertEquals(accessToken.accessToken, "8OC8as1VpATqkM79KfcdTw")
    }

    @Test
    fun fetchUserInfo() {
    }
}