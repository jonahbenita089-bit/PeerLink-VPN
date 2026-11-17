package com.example

import com.example.data.AppState
import com.example.data.HostIdGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testHostIdGeneration() {
    val id = HostIdGenerator.generate()
    assertNotNull(id)
    assertEquals(11, id.length) // "XXX-XXX-XXX"
    assertTrue(HostIdGenerator.isValid(id))
  }

  @Test
  fun testHostIdValidationAndCleaning() {
    assertTrue(HostIdGenerator.isValid("X7Y-9PL-2QZ"))
    assertTrue(HostIdGenerator.isValid("X7Y9PL2QZ"))
    assertFalse(HostIdGenerator.isValid("SHORT"))

    val cleaned = HostIdGenerator.cleanInput("x7y9pl2qz")
    assertEquals("X7Y-9PL-2QZ", cleaned)
  }

  @Test
  fun testAppStateDefaultValues() {
    val appState = AppState(
      role = "idle",
      isConnected = false,
      hostId = "ABC-123-XYZ",
      connectionTimeSeconds = 0,
      dataTransferredMB = 0f
    )
    assertEquals("idle", appState.role)
    assertFalse(appState.isConnected)
    assertEquals("ABC-123-XYZ", appState.hostId)
    assertEquals(0, appState.connectionTimeSeconds)
    assertEquals(0f, appState.dataTransferredMB, 0.001f)
  }
}
