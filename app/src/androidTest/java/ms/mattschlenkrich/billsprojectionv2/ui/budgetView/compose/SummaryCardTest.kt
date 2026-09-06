package ms.mattschlenkrich.billsprojectionv2.ui.budgetView.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ms.mattschlenkrich.billsprojectionv2.common.functions.LocalNumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountWithType
import org.junit.Rule
import org.junit.Test

class SummaryCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val nf = NumberFunctions()

    @Test
    fun summaryCard_showsRemainder_whenAssetAccountAndItemsSelected() {
        val assetAccount = AccountWithType(
            account = Account(
                accountId = 1L,
                accountName = "Checking",
                accountNumber = "123",
                accountTypeId = 1L,
                accBudgetedAmount = 0.0,
                accountBalance = 1000.0,
                accountOwing = 0.0,
                accountCreditLimit = 0.0,
                accIsDeleted = false,
                accUpdateTime = ""
            ),
            accountType = AccountType(
                typeId = 1L,
                accountType = "Asset",
                keepTotals = true,
                isAsset = true,
                tallyOwing = false,
                keepMileage = false,
                displayAsAsset = true,
                allowPending = true,
                acctIsDeleted = false,
                acctUpdateTime = ""
            )
        )

        val selectedSum = -250.0 // Representing a debit of 250
        val expectedRemainder = 750.0

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNumberFunctions provides nf) {
                SummaryCard(
                    assetList = listOf("Checking"),
                    selectedAsset = "Checking",
                    onAssetSelected = {},
                    payDayList = emptyList(),
                    selectedPayDay = "",
                    onPayDaySelected = {},
                    curAsset = assetAccount,
                    budgetTotals = BudgetTotals(0.0, 0.0, 0.0, 0.0),
                    pendingAmount = 0.0,
                    onAccountClick = {},
                    selectedSum = selectedSum,
                    showSelectedSum = true
                )
            }
        }

        // Check if "Selected: $250.00" is shown (abs value)
        composeTestRule.onNodeWithText("Selected: ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(nf.displayDollars(250.0), substring = true)
            .assertIsDisplayed()

        // Check if "Remainder: $750.00" is shown (1000 + (-250))
        composeTestRule.onNodeWithText("Remainder: ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(nf.displayDollars(expectedRemainder), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun summaryCard_showsIncreasedBalance_whenAssetAccountAndDepositSelected() {
        val assetAccount = AccountWithType(
            account = Account(1L, "Checking", "123", 1L, 0.0, 1000.0, 0.0, 0.0, false, ""),
            accountType = AccountType(1L, "Asset", true, true, false, false, true, true, false, "")
        )

        val selectedSum = 100.0 // Representing a credit of 100
        val expectedRemainder = 1100.0

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNumberFunctions provides nf) {
                SummaryCard(
                    assetList = listOf("Checking"),
                    selectedAsset = "Checking",
                    onAssetSelected = {},
                    payDayList = emptyList(),
                    selectedPayDay = "",
                    onPayDaySelected = {},
                    curAsset = assetAccount,
                    budgetTotals = BudgetTotals(0.0, 0.0, 0.0, 0.0),
                    pendingAmount = 0.0,
                    onAccountClick = {},
                    selectedSum = selectedSum,
                    showSelectedSum = true
                )
            }
        }

        // Check if "Remainder: $1,100.00" is shown
        composeTestRule.onNodeWithText("Remainder: ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(nf.displayDollars(expectedRemainder), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun summaryCard_showsUpdatedOwing_whenDebtAccountAndItemsSelected() {
        val debtAccount = AccountWithType(
            account = Account(
                accountId = 2L,
                accountName = "Visa",
                accountNumber = "456",
                accountTypeId = 2L,
                accBudgetedAmount = 0.0,
                accountBalance = 0.0,
                accountOwing = 500.0,
                accountCreditLimit = 5000.0,
                accIsDeleted = false,
                accUpdateTime = ""
            ),
            accountType = AccountType(
                typeId = 2L,
                accountType = "Credit Card",
                keepTotals = false,
                isAsset = false,
                tallyOwing = true,
                keepMileage = false,
                displayAsAsset = false,
                allowPending = true,
                acctIsDeleted = false,
                acctUpdateTime = ""
            )
        )

        val selectedSum = -150.0 // Representing a debit/expense of 150 from the credit card
        val expectedTotalOwing = 650.0

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNumberFunctions provides nf) {
                SummaryCard(
                    assetList = listOf("Visa"),
                    selectedAsset = "Visa",
                    onAssetSelected = {},
                    payDayList = emptyList(),
                    selectedPayDay = "",
                    onPayDaySelected = {},
                    curAsset = debtAccount,
                    budgetTotals = BudgetTotals(0.0, 0.0, 0.0, 0.0),
                    pendingAmount = 0.0,
                    onAccountClick = {},
                    selectedSum = selectedSum,
                    showSelectedSum = true
                )
            }
        }

        // Check if "Selected: $150.00" is shown
        composeTestRule.onNodeWithText("Selected: ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(nf.displayDollars(150.0), substring = true)
            .assertIsDisplayed()

        // Check if "Owing: $650.00" is shown (500 - (-150))
        composeTestRule.onNodeWithText("Owing: ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(nf.displayDollars(expectedTotalOwing), substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun summaryCard_hidesSelectionRow_whenNoItemsSelected() {
        val assetAccount = AccountWithType(
            account = Account(1L, "Checking", "123", 1L, 0.0, 1000.0, 0.0, 0.0, false, ""),
            accountType = AccountType(1L, "Asset", true, true, false, false, true, true, false, "")
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalNumberFunctions provides nf) {
                SummaryCard(
                    assetList = listOf("Checking"),
                    selectedAsset = "Checking",
                    onAssetSelected = {},
                    payDayList = emptyList(),
                    selectedPayDay = "",
                    onPayDaySelected = {},
                    curAsset = assetAccount,
                    budgetTotals = BudgetTotals(0.0, 0.0, 0.0, 0.0),
                    pendingAmount = 0.0,
                    onAccountClick = {},
                    selectedSum = 0.0,
                    showSelectedSum = false
                )
            }
        }

        // Verify "Selected: " is NOT shown
        composeTestRule.onNodeWithText("Selected: ", substring = true).assertDoesNotExist()
    }
}