package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.billsprojectionv2.DB_NAME
import ms.mattschlenkrich.billsprojectionv2.DB_VERSION
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.BudgetView
import ms.mattschlenkrich.billsprojectionv2.model.Transactions

@Database(
    entities = [
        Account::class,
        AccountType::class,
        BudgetRule::class,
        Transactions::class,
        BudgetView::class,
    ], version = DB_VERSION
)
abstract class BillsDatabase : RoomDatabase() {
    abstract fun getAccountTypesDao(): AccountTypeDao
    abstract fun getAccountDao(): AccountDao
    abstract fun getBudgetRuleDao(): BudgetRuleDao
    abstract fun getTransactionDao(): TransactionDao
    abstract fun getBudgetViewDao(): BudgetViewDao

    companion object {
        @Volatile
        private var instance: BillsDatabase? = null
        private val LOCK = Any()

        //        private var myCallback : MyCallback? = null
        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: createDataBase(context).also {
                    instance = it
                }
            }

        /*fun testDb(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            BillsDatabase::class.java,
            DB_NAME
        ).addCallback(myCallback!!).build()*/


        private fun createDataBase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                BillsDatabase::class.java,
                DB_NAME
            )
                .build()
    }


//    class MyCallback(
//        private val database: BillsDatabase?,
//
//        ) : Callback() {
//
//        private val timeFormatter: SimpleDateFormat =
//            SimpleDateFormat(SQLITE_TIME, Locale.CANADA)
//
//        override fun onOpen(db: SupportSQLiteDatabase) {
//            super.onOpen(db)
//            if (
//                database != null
//            ) {
//                val dao = database.getAccountTypesDao()
//
//                /*private val updateToAccBalanceOnInsertSQL =
//           "CREATE TRIGGER IF NOT EXISTS updateToAccBalanceOnInsert " +
//                   "AFTER INSERT " +
//                   "ON transactions " +
//                   "WHEN ( (" +
//                   " SELECT count( * )  " +
//                   "FROM accountTypes" +
//                   "INNER JOIN" +
//                   "accounts ON accounts.accountId = NEW.toAccountId " +
//                   "WHERE accountTypes.isAsset = 1" +
//                   ") " +
//                   "= 1)  " +
//                   "BEGIN " +
//                   "UPDATE accounts " +
//                   "SET accountBalance = accountBalance + NEW.amount " +
//                   "WHERE accountId = NEW.toAccountId; " +
//                   "END;"
//       private val updateFromAccBalanceOnInsertSQL =
//           "CREATE TRIGGER IF NOT EXISTS updateFromAccBalanceOnInsert " +
//                   "AFTER INSERT " +
//                   "ON transactions " +
//                   "WHEN ( (" +
//                   " SELECT count( * )  " +
//                   "FROM accountTypes" +
//                   "INNER JOIN" +
//                   "accounts ON accounts.accountId = NEW.fromAccountId " +
//                   "WHERE accountTypes.isAsset = 1" +
//                   ") " +
//                   "= 1)  " +
//                   "BEGIN " +
//                   "UPDATE accounts " +
//                   "SET accountBalance = accountBalance - NEW.amount " +
//                   "WHERE accountId = NEW.fromAccountId; " +
//                   "END;"*/
//
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    dao.insertAccountType(
//                        AccountType(
//                            1,
//                            "Bank Account in Budget",
//                            keepTotals = true,
//                            isAsset = true,
//                            tallyOwing = false,
//                            keepMileage = false,
//                            displayAsAsset = true,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                    dao.insertAccountType(
//                        AccountType(
//                            2,
//                            "Bank Account",
//                            keepTotals = true,
//                            isAsset = true,
//                            tallyOwing = false,
//                            keepMileage = false,
//                            displayAsAsset = false,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                    dao.insertAccountType(
//                        AccountType(
//                            3,
//                            "Income",
//                            keepTotals = true,
//                            isAsset = false,
//                            tallyOwing = false,
//                            keepMileage = false,
//                            displayAsAsset = false,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                    dao.insertAccountType(
//                        AccountType(
//                            4,
//                            "Credit Card in Budget",
//                            keepTotals = true,
//                            isAsset = false,
//                            tallyOwing = true,
//                            keepMileage = false,
//                            displayAsAsset = true,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                    dao.insertAccountType(
//                        AccountType(
//                            5,
//                            "Credit Card",
//                            keepTotals = true,
//                            isAsset = false,
//                            tallyOwing = true,
//                            keepMileage = false,
//                            displayAsAsset = false,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                    dao.insertAccountType(
//                        AccountType(
//                            6,
//                            "Expense",
//                            keepTotals = true,
//                            isAsset = false,
//                            tallyOwing = false,
//                            keepMileage = false,
//                            displayAsAsset = false,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                    dao.insertAccountType(
//                        AccountType(
//                            7,
//                            "Gas Expense",
//                            keepTotals = true,
//                            isAsset = false,
//                            tallyOwing = true,
//                            keepMileage = true,
//                            displayAsAsset = false,
//                            isDeleted = false,
//                            updateTime =
//                            timeFormatter.format(Calendar.getInstance().time)
//                        )
//                    )
//                }
//            }
//        }

//    }
}