package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ms.mattschlenkrich.billsprojectionv2.common.BILLS_DATABASE
import ms.mattschlenkrich.billsprojectionv2.common.BI_LOCKED
import ms.mattschlenkrich.billsprojectionv2.common.DB_NAME
import ms.mattschlenkrich.billsprojectionv2.common.DB_VERSION
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_BUDGET_ITEMS
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.Transactions

private const val TAG = BILLS_DATABASE

@Database(
    entities = [
        Account::class,
        AccountType::class,
        BudgetRule::class,
        Transactions::class,
        BudgetItem::class,
    ], version = DB_VERSION,
    views = [AccountAndType::class]
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

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: createDataBase(context).also {
                    instance = it
                    Log.d(TAG, "operator is invoked")
                }
            }

        private fun createDataBase(context: Context): BillsDatabase {
//            val addAccountTypes = AddAccountTypeCallBack()
//            val addAccounts = AddAccountsCallBack()
//            val addBudgetRules = AddBudgetRuleCallBack()
            val upgrade1to2 = Upgrade1to2()
            return Room.databaseBuilder(
                context.applicationContext,
                BillsDatabase::class.java,
                DB_NAME
            )
//                .addCallback(addAccountTypes)
//                .addCallback(addAccounts)
                .addMigrations(upgrade1to2)
                .build()
        }
    }

    class Upgrade1to2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val sql = "ALTER TABLE $TABLE_BUDGET_ITEMS " +
                    "ADD COLUMN $BI_LOCKED BOOLEAN;"
            database.execSQL(sql)
        }
    }

//    class AddBudgetRuleCallBack : Callback() {
//        val df = DateFunctions()
//        override fun onCreate(db: SupportSQLiteDatabase) {
//            super.onCreate(db)
//            var sql = "INSERT INTO $TABLE_BUDGET_RULES " +
//                    "($RULE_ID, $BUDGET_RULE_NAME, " +
//                    "$BUD_TO_ACCOUNT_ID, $BUD_FROM_ACCOUNT_ID, " +
//                    "$BUDGET_AMOUNT, $BUD_FIXED_AMOUNT, " +
//                    "$BUD_IS_PAY_DAY, $BUD_IS_AUTO_PAY, " +
//                    "$BUD_START_DATE, $BUD_END_DATE, " +
//                    "$BUD_DAY_OF_WEEK_ID, $BUD_FREQUENCY_TYPE_ID, " +
//                    "$BUD_FREQUENCY_COUNT, $BUD_LEAD_DAYS, " +
//                    "$BUD_IS_DELETED, $BUD_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "1, 'Pay Check', 1, 5, 2000, 0, " +
//                    "1, 1, " +
//                    "'2023-09-01', '2033-01-01', 4, 1, 2, 0, " +
//                    "0, ${df.getCurrentTimeAsString()});"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_BUDGET_RULES " +
//                    "($RULE_ID, $BUDGET_RULE_NAME, " +
//                    "$BUD_TO_ACCOUNT_ID, $BUD_FROM_ACCOUNT_ID, " +
//                    "$BUDGET_AMOUNT, $BUD_FIXED_AMOUNT, " +
//                    "$BUD_IS_PAY_DAY, $BUD_IS_AUTO_PAY, " +
//                    "$BUD_START_DATE, $BUD_END_DATE, " +
//                    "$BUD_DAY_OF_WEEK_ID, $BUD_FREQUENCY_TYPE_ID, " +
//                    "$BUD_FREQUENCY_COUNT, $BUD_LEAD_DAYS, " +
//                    "$BUD_IS_DELETED, $BUD_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "2, 'Rent or Mortgage Payment', 10, 1, 1500, 1, " +
//                    "0, 0, " +
//                    "'2023-09-01', '2033-01-01', 4, 1, 2, 0, " +
//                    "0, ${df.getCurrentTimeAsString()});"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//        }
//    }
//
//    class AddAccountsCallBack : Callback() {
//        val df = DateFunctions()
//        override fun onCreate(db: SupportSQLiteDatabase) {
//            super.onCreate(db)
//            var sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "1, 'Checking Account', '1234', " +
//                    "1, 0, 100, 0, 0, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "2, 'Savings Account', '1234', " +
//                    "2, 0, 50, 0, 0, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "3, 'Credit Card 1', '1234', " +
//                    "3, 0, 0, 100, 1000, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "4, 'Credit Card 2', '0000', " +
//                    "4, 0, 0, 20, 1000, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "5, 'Employer 1', 'employee #', " +
//                    "5, 0, 0, 0, 0, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "6, 'Groceries', 'weekly', " +
//                    "6, 0, 0, 0, 0, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "10, 'Rent or Mortgage', 'monthly', " +
//                    "6, 1200, 0, 0, 0, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "7, 'Gas', 'monthly', " +
//                    "7, 0, 0, 0, 0, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNTS " +
//                    "($ACCOUNT_ID, $ACCOUNT_NAME, $ACCOUNT_NUMBER, " +
//                    "$ACCOUNT_TYPE_ID, $ACCOUNT_BUDGETED_AMOUNT, " +
//                    "$ACCOUNT_BALANCE, $ACCOUNT_OWING, " +
//                    "$ACCOUNT_CREDIT_LIMIT, $ACCOUNT_IS_DELETED, " +
//                    "$ACCOUNT_UPDATE_TIME)" +
//                    "VALUES (" +
//                    "9, 'Line of Credit', '22222', " +
//                    "9, 0, 300, 0, 5000, 0, " +
//                    "${df.getCurrentTimeAsString()};"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.e(TAG, "Exception is ${e.stackTrace}")
//            }
//        }
//    }
//
//    class AddAccountTypeCallBack : Callback() {
//        val df = DateFunctions()
//        override fun onCreate(db: SupportSQLiteDatabase) {
//            Log.d(TAG, "entering AddAccountTypeCallback")
//            super.onCreate(db)
//            var sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (1, " +
//                    "'Bank Account in Budget', " +
//                    "1, 1, 0, 0, 1, 1, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, $KEEP_BALANCE," +
//                    "$IS_ASSET, $TALLY_OWING, $KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, $ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, $ACCT_UPDATE_TIME) " +
//                    "VALUES (2, " +
//                    "'Bank Account', " +
//                    "1, 1, 0, 0, 0, 0, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (3, " +
//                    "'Credit Card in Budget', " +
//                    "0, 0, 1, 0, 1, 1, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (4, " +
//                    "'Credit Card', " +
//                    "0, 0, 1, 0, 0, 1, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (5, " +
//                    "'Income', " +
//                    "0, 0, 0, 0, 0, 0, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (6, " +
//                    "'Expense', " +
//                    "0, 0, 0, 0, 0, 0, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (7, " +
//                    "'Gas Expense', " +
//                    "0, 0, 0, 1, 0, 0, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (8, " +
//                    "'Loan', " +
//                    "0, 0, 1, 0, 0, 0, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//            sql = "INSERT INTO $TABLE_ACCOUNT_TYPES " +
//                    "($TYPE_ID, $ACCOUNT_TYPE, " +
//                    "$KEEP_BALANCE, " +
//                    "$IS_ASSET, " +
//                    "$TALLY_OWING, " +
//                    "$KEEP_MILEAGE, " +
//                    "$ACCT_DISPLAY_AS_ASSET, " +
//                    "$ALLOW_PENDING, " +
//                    "$ACCT_IS_DELETED, " +
//                    "$ACCT_UPDATE_TIME) " +
//                    "VALUES (9, " +
//                    "'Line of Credit', " +
//                    "0, 0, 1, 0, 0, 0, 0, " +
//                    "'${df.getCurrentTimeAsString()}');"
//            try {
//                db.query(sql)
//            } catch (e: SQLiteException) {
//                Log.d(TAG, "Exception is ${e.stackTrace}")
//            }
//        }
//    }
}