package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ms.mattschlenkrich.billsprojectionv2.common.ACCT_UPDATE_TIME
import ms.mattschlenkrich.billsprojectionv2.common.BILLS_DATABASE
import ms.mattschlenkrich.billsprojectionv2.common.DB_NAME
import ms.mattschlenkrich.billsprojectionv2.common.DB_VERSION
import ms.mattschlenkrich.billsprojectionv2.common.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.Transactions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = BILLS_DATABASE

@Database(
    entities = [
        Account::class,
        AccountType::class,
        BudgetRule::class,
        Transactions::class,
        BudgetItem::class,
    ], version = DB_VERSION
)
abstract class BillsDatabase : RoomDatabase() {
    abstract fun getAccountTypesDao(): AccountTypeDao
    abstract fun getAccountDao(): AccountDao
    abstract fun getBudgetRuleDao(): BudgetRuleDao
    abstract fun getTransactionDao(): TransactionDao
    abstract fun getBudgetItemDao(): BudgetItemDao

    companion object {
        @Volatile
        private var instance: BillsDatabase? = null
        private val LOCK = Any()
        private var myCallback: MyCallback? = null

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: createDataBase(context).also {
                    instance = it
                    Log.d(TAG, "operator is invoked")
                    myCallback = MyCallback(instance)
                    testDb(context)
                }
            }

        private fun testDb(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            BillsDatabase::class.java,
            DB_NAME
        ).addCallback(myCallback!!).build()


        private fun createDataBase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                BillsDatabase::class.java,
                DB_NAME
            ).build()
    }


    class MyCallback(
        private val database: BillsDatabase?,

        ) : Callback() {

        val timeFormatter =
            SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            if (
                database != null
            ) {
//                val dao = database.getAccountTypesDao()

                val sql =
                    "CREATE TRIGGER IF NOT EXISTS updateToAccBalanceOnInsert " +
                            "AFTER INSERT " +
                            "ON transactions " +
                            "WHEN ( (" +
                            " SELECT count( * )  " +
                            "FROM accountTypes" +
                            "INNER JOIN" +
                            "accounts ON accounts.accountId = NEW.toAccountId " +
                            "WHERE accountTypes.isAsset = 1" +
                            ") " +
                            "= 1)  " +
                            "BEGIN " +
                            "UPDATE accounts " +
                            "SET accountBalance = accountBalance + NEW.amount," +
                            " $ACCT_UPDATE_TIME = " +
                            "${timeFormatter.format(Calendar.getInstance().time)} " +
                            "WHERE accountId = NEW.toAccountId; " +
                            "END;"
                try {
                    db.query(sql)
                } catch (e: SQLiteException) {
                    Log.e(TAG, "Exception is ${e.stackTrace}")
                }
            }
        }
    }
//       /*private val updateFromAccBalanceOnInsertSQL =
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