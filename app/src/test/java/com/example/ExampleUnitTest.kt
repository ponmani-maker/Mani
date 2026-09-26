package com.example

import com.example.data.HelpDeskRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSubnetCalculation() {
    // Test pure logic calculation without context
    val prefix = 24
    val parts = "192.168.1.100".split(".").map { it.toInt() }
    var ipNum = 0L
    for (oct in parts) {
      ipNum = (ipNum shl 8) or (oct.toLong() and 0xFFL)
    }
    val maskNum = (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
    val wildcardNum = maskNum.inv() and 0xFFFFFFFFL
    val netNum = ipNum and maskNum
    val bcastNum = netNum or wildcardNum

    fun numToIp(num: Long): String {
      return "${(num shr 24) and 0xFF}.${(num shr 16) and 0xFF}.${(num shr 8) and 0xFF}.${num and 0xFF}"
    }

    assertEquals("192.168.1.0", numToIp(netNum))
    assertEquals("255.255.255.0", numToIp(maskNum))
    assertEquals("192.168.1.255", numToIp(bcastNum))
    assertEquals("192.168.1.1", numToIp(netNum + 1))
    assertEquals("192.168.1.254", numToIp(bcastNum - 1))
  }

  @Test
  fun testMemoryUtilizationCalculation() {
    val totalRamMb = 8192L
    val availRamMb = 4096L
    val usedRamMb = totalRamMb - availRamMb
    val usedPct = ((usedRamMb.toDouble() / totalRamMb) * 100).toInt()

    assertEquals(4096L, usedRamMb)
    assertEquals(50, usedPct)
    assertTrue("Should be optimal under 75%", usedPct < 75)
  }
}

