package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.billsprojectionv2.common.DB_NAME
import ms.mattschlenkrich.billsprojectionv2.common.DB_VERSION
import ms.mattschlenkrich.billsprojectionv2.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.model.transactions.Transactions

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
                }
            }

        private fun createDataBase(context: Context): BillsDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BillsDatabase::class.java,
                DB_NAME
            )
                .createFromAsset("bills2.db")
                .build()
        }
    }
}