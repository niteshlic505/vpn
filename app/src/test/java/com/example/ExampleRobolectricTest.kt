package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PrivacySettingsEntity
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import com.example.data.model.VpnSessionInfo
import com.example.data.model.VpnStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ShieldAI VPN", appName)
  }

  @Test
  fun `verify vpn protocol tunnel classification`() {
    assertTrue(VpnProtocol.WIREGUARD.isFullTunnel)
    assertTrue(VpnProtocol.OPENVPN.isFullTunnel)
    org.junit.Assert.assertFalse(VpnProtocol.SOCKS5.isFullTunnel)
  }

  @Test
  fun `verify vpn server model`() {
    val server = VpnServer(
      id = "test_node",
      countryName = "Iceland",
      countryCode = "IS",
      city = "Reykjavik",
      ipAddress = "192.0.2.1",
      pingMs = 30,
      loadPercent = 40,
      speedMbps = 1000,
      protocol = VpnProtocol.WIREGUARD,
      isPremium = false,
      reliabilityScore = 99,
      latitude = 64.1466,
      longitude = -21.9426,
      flagEmoji = "🇮🇸"
    )
    assertEquals("Iceland", server.countryName)
    assertEquals(VpnProtocol.WIREGUARD, server.protocol)
    assertTrue(server.isOnline)
  }

  @Test
  fun `verify haversine distance calculation`() {
    // Distance between New York (40.7128, -74.0060) and London (51.5074, -0.1278) is approx 5570 km
    val dist = com.example.data.util.LocationAndSpeedEngine.calculateDistanceKm(
      40.7128, -74.0060,
      51.5074, -0.1278
    )
    assertTrue(dist in 5500..5650)
  }

  @Test
  fun `verify recommendation scoring incorporates ping load and distance`() {
    val server = VpnServer(
      id = "local_node",
      countryName = "United States",
      countryCode = "US",
      city = "New York",
      ipAddress = "198.51.100.1",
      pingMs = 20,
      loadPercent = 30,
      speedMbps = 1000,
      protocol = VpnProtocol.WIREGUARD,
      isPremium = false,
      reliabilityScore = 99,
      latitude = 40.7128,
      longitude = -74.0060,
      flagEmoji = "🇺🇸"
    )
    val userLoc = com.example.data.model.UserLocation.DEFAULT
    val speedTest = com.example.data.model.SpeedTestMetric.DEFAULT

    val (score, distance) = com.example.data.util.LocationAndSpeedEngine.calculateSuitabilityScore(
      server = server,
      userLocation = userLoc,
      speedTest = speedTest,
      goal = com.example.data.model.RecommendationGoal.GAMING
    )

    assertTrue("Score should be high for local low-latency node", score >= 80)
    assertEquals(0, distance)
  }
}
