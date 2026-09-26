package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.AssetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("IT Help", appName)
  }

  @Test
  fun `test Room database asset inventory insert, search, and update`() = runBlocking {
    val assetDao = database.assetDao()

    val testAsset = AssetEntity(
      assetTag = "AST-TEST-01",
      hardwareModel = "MacBook Pro 16\" Apple M3 Max",
      category = "Laptop",
      serialNumber = "C02TEST889",
      assignedUser = "Alex Rivera (Lead Architect)",
      department = "Engineering",
      location = "Floor 4 Desk 12",
      status = "In Use",
      ipAddress = "10.0.20.114",
      macAddress = "F4:D4:88:99:AA:02",
      warrantyExpiry = "2027-02-14",
      specifications = "64GB RAM, 1TB SSD"
    )

    val id = assetDao.insertAsset(testAsset)
    assertTrue(id > 0)

    val assets = assetDao.getAllAssets().first()
    assertEquals(1, assets.size)
    assertEquals("MacBook Pro 16\" Apple M3 Max", assets[0].hardwareModel)
    assertEquals("C02TEST889", assets[0].serialNumber)
    assertEquals("Alex Rivera (Lead Architect)", assets[0].assignedUser)

    // Test search query by hardware model
    val searchResults = assetDao.searchAssets("MacBook").first()
    assertEquals(1, searchResults.size)

    // Test search query by serial number
    val searchBySerial = assetDao.searchAssets("C02TEST").first()
    assertEquals(1, searchBySerial.size)

    // Test search query by assigned user
    val searchByUser = assetDao.searchAssets("Alex Rivera").first()
    assertEquals(1, searchByUser.size)

    // Test status update
    assetDao.updateAssetStatus(id, "Spare Inventory")
    val updated = assetDao.getAssetById(id)
    assertNotNull(updated)
    assertEquals("Spare Inventory", updated?.status)
  }
}
