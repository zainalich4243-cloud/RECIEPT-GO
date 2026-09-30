package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ReceiptItem
import com.example.viewmodel.ReceiptDraft
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Receipt Go", appName)
  }

  @Test
  fun `verify receipt draft subtotal and calculations`() {
    val items = listOf(
      ReceiptItem(name = "Sugar", quantity = 2.0, unit = "KG", unitPrice = 160.0), // 320
      ReceiptItem(name = "Milk", quantity = 1.0, unit = "L", unitPrice = 180.0)    // 180
    )
    val draft = ReceiptDraft(
      items = items,
      discountType = "AMOUNT",
      discountValue = 50.0,
      taxType = "PERCENT",
      taxValue = 10.0 // 10% of (500 - 50 = 450) = 45
    )

    assertEquals(500.0, draft.subtotal, 0.01)
    assertEquals(50.0, draft.discountCalculated, 0.01)
    assertEquals(45.0, draft.taxCalculated, 0.01)
    assertEquals(495.0, draft.totalAmount, 0.01)
  }
}
