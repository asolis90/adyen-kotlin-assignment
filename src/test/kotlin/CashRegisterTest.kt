import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CashRegisterTest {
    @Test
    fun testTransactionExactPaymentReturnsNone() {
        val initialChange = Change()
            .add(Bill.TEN_EURO, 1)
            .add(Coin.ONE_EURO, 1)

        val register = CashRegister(initialChange)
        val amountPaid = Change().add(Bill.TEN_EURO, 1)

        val change = register.performTransaction(price = 1000, amountPaid = amountPaid)

        assertEquals(0, change.total)
    }

    @Test
    fun testTransactionAndReturnChange1() {
        val initialChange = Change()
            .add(Bill.TEN_EURO, 1)
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TWO_EURO, 2)
            .add(Coin.ONE_EURO, 2)
            .add(Coin.FIFTY_CENT, 3)
            .add(Coin.TWENTY_CENT, 5)
            .add(Coin.TEN_CENT, 10)
            .add(Coin.TWENTY_CENT, 5)
            .add(Coin.TEN_CENT, 10)

        val register = CashRegister(initialChange)
        val amountPaid = Change().add(Bill.TWENTY_EURO, 1)
        val change = register.performTransaction(price = 1760, amountPaid = amountPaid)
        assertEquals(240, change.total)
    }

    @Test
    fun testTransactionAndReturnChange2() {
        val initialChange = Change()
            .add(Bill.TEN_EURO, 1)
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TWO_EURO, 2)
            .add(Coin.ONE_EURO, 2)
            .add(Coin.FIFTY_CENT, 3)
            .add(Coin.TWENTY_CENT, 5)
            .add(Coin.TEN_CENT, 10)
            .add(Coin.TWENTY_CENT, 5)
            .add(Coin.TEN_CENT, 10)

        val register = CashRegister(initialChange)
        val amountPaid = Change().add(Bill.TWENTY_EURO, 1)

        val change = register.performTransaction(price = 1160, amountPaid = amountPaid)
        assertEquals(840, change.total)
    }


    @Test
    fun testTransactionAndThrowInsufficientPayment() {
        val initialChange = Change()
            .add(Bill.TEN_EURO, 1)
            .add(Bill.FIVE_EURO, 1)
            .add(Coin.TWO_EURO, 2)
            .add(Coin.ONE_EURO, 2)
            .add(Coin.FIFTY_CENT, 3)
            .add(Coin.TWENTY_CENT, 5)
            .add(Coin.TEN_CENT, 10)
            .add(Coin.TWENTY_CENT, 5)
            .add(Coin.TEN_CENT, 10)

        val register = CashRegister(initialChange)
        val amountPaid = Change().add(Bill.TEN_EURO, 1)

        assertFailsWith<CashRegister.TransactionException.InsufficientPayment> {
            register.performTransaction(price = 1160, amountPaid = amountPaid)
        }
    }

    @Test
    fun testTransactionFailedNotEnoughChange() {
        val initialChange = Change()
            .add(Bill.TEN_EURO, 1)
            .add(Coin.FIFTY_CENT, 1)
            .add(Coin.TEN_CENT, 1)

        val register = CashRegister(initialChange)
        val amountPaid = Change().add(Bill.TWENTY_EURO, 1)

        assertFailsWith<CashRegister.TransactionException.NotEnoughChange> {
            register.performTransaction(price = 1760, amountPaid = amountPaid)
        }
    }


    @Test
    fun testTransactionFailedInvalidPrice() {
        val initialChange = Change()
            .add(Bill.TEN_EURO, 1)
            .add(Coin.FIFTY_CENT, 1)
            .add(Coin.TEN_CENT, 1)

        val register = CashRegister(initialChange)
        val amountPaid = Change().add(Bill.TWENTY_EURO, 1)

        assertFailsWith<CashRegister.TransactionException.InvalidPrice> {
            register.performTransaction(price = 0, amountPaid = amountPaid)
        }
    }

    @Test
    fun testTransactionFailedWithZeroPayment() {
        val register = CashRegister(Change().add(Bill.TEN_EURO, 1))
        val amountPaid = Change()
        assertFailsWith<CashRegister.TransactionException.InsufficientPayment> {
            register.performTransaction(price = 1000, amountPaid = amountPaid)
        }
    }

    @Test
    fun testTransactionWithLargeValues() {
        val initialChange = Change().add(Bill.FIVE_HUNDRED_EURO, 1_000_000)
        val register = CashRegister(initialChange)

        val price = 400_000_000L * 100   // ✅ €4,000,000.00 in cents
        val amountPaid = Change().add(Bill.FIVE_HUNDRED_EURO, 2_000_000) // €1,000,000,000.00

        val change = register.performTransaction(price, amountPaid)

        assertTrue(change.total > 0)
    }
}
