/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */
class CashRegister(private val change: Change) {
    /**
     * Performs a transaction for a product/products with a certain price and a given amount.
     *
     * @param price The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return The change for the transaction.
     *
     * @throws TransactionException If the transaction cannot be performed.
     */
    fun performTransaction(price: Long, amountPaid: Change): Change {
        if(price == 0L) {
            throw TransactionException("Transaction failed: Invalid Price.")
        }
        // Check if total amount paid is less than the price
        if (amountPaid.total < price) {
            throw TransactionException("Transaction failed: Insufficient payment.")
        }

        val changeDue = amountPaid.total - price

        // if there is no amount due, add the amount paid to the register and return nothing
        if (changeDue == 0L) {
            addChange(amountPaid)
            return Change.none()
        }

        // Combine existing change with the paid amount
        val available = Change()
        for (element in change.getElements()) {
            available.add(element, change.getCount(element))
        }
        for (element in amountPaid.getElements()) {
            available.add(element, amountPaid.getCount(element))
        }

        // Calculate change to return
        val changeToReturn = calculateChange(changeDue, available)

        // Update the register change
        addChange(amountPaid)

        // Remove the change from the register
        for (element in changeToReturn.getElements()) {
            change.remove(element, changeToReturn.getCount(element))
        }

        return changeToReturn
    }

    /**
     * Adds the provided payment to the register.
     * @param amountPaid The amount paid by the shopper, represented as a {@link Change} object.
     */
    private fun addChange(amountPaid: Change) {
        for (element in amountPaid.getElements()) {
            change.add(element, amountPaid.getCount(element))
        }
    }

    /**
     * Calculates the exact change to return for a given amount due using available funds.
     *
     * @param amountDue The amount of change to be returned.
     * @param availableChange The combined available funds (register + paid amount).
     *
     * @return The change to be returned.
     *
     * @throws TransactionException If the exact change cannot be provided.
     */
    private fun calculateChange(amountDue: Long, availableChange: Change): Change {
        var remaining = amountDue
        val result = Change()

        // Add all the elements in descending order of value
        val allElements: List<MonetaryElement> =
            (Bill.values().toList() + Coin.values().toList())
                .sortedByDescending { it.minorValue }

        // Add the elements to the result until we reach the exact change or the amount is zero
        for (element in allElements) {
            val available = availableChange.getCount(element)
            val needed = (remaining / element.minorValue).toInt().coerceAtMost(available)

            if (needed > 0) {
                result.add(element, needed)
                remaining -= element.minorValue.toLong() * needed
            }

            if (remaining == 0L) break
        }

        // if the remaining change is not zero, we cannot provide exact change
        if (remaining > 0) {
            throw TransactionException("Transaction failed: Cannot provide exact change.")
        }

        return result
    }

    class TransactionException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
