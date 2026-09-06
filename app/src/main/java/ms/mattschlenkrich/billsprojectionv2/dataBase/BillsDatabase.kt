package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ms.mattschlenkrich.billsprojectionv2.common.DB_NAME
import ms.mattschlenkrich.billsprojectionv2.common.DB_VERSION
import ms.mattschlenkrich.billsprojectionv2.dataBase.dao.AccountDao
import ms.mattschlenkrich.billsprojectionv2.dataBase.dao.AccountTypeDao
import ms.mattschlenkrich.billsprojectionv2.dataBase.dao.BudgetItemDao
import ms.mattschlenkrich.billsprojectionv2.dataBase.dao.BudgetRuleDao
import ms.mattschlenkrich.billsprojectionv2.dataBase.dao.SyncHistoryDao
import ms.mattschlenkrich.billsprojectionv2.dataBase.dao.TransactionDao
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.Account
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountAndType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.account.AccountType
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetItem.BudgetItem
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.budgetRule.BudgetRule
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.transactions.Transactions

@Database(
    entities = [
        Account::class,
        AccountType::class,
        BudgetRule::class,
        Transactions::class,
        BudgetItem::class,
        SyncHistory::class,
    ], version = DB_VERSION,
    views = [AccountAndType::class]
)
abstract class BillsDatabase : RoomDatabase() {
    abstract fun getAccountTypesDao(): AccountTypeDao
    abstract fun getAccountDao(): AccountDao
    abstract fun getBudgetRuleDao(): BudgetRuleDao
    abstract fun getTransactionDao(): TransactionDao
    abstract fun getBudgetItemDao(): BudgetItemDao
    abstract fun getSyncHistoryDao(): SyncHistoryDao

    companion object {
        @Volatile
        private var instance: BillsDatabase? = null
        private val LOCK = Any()

        fun resetInstance() {
            instance?.close()
            instance = null
        }

        fun closeDatabase() {
            instance?.close()
            instance = null
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema was identical or handled by Room's automatic updates for these versions
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // View definition changed slightly (semicolon removed).
                // Room views are recreated automatically if they don't match, 
                // but we can force it here just in case.
                db.execSQL("DROP VIEW IF EXISTS `AccountAndType`")
                db.execSQL("CREATE VIEW `AccountAndType` AS SELECT accounts.*,accountTypes.* FROM accounts LEFT JOIN accountTypes on accounts.accountTypeId =accountTypes.typeId")
            }
        }

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP VIEW IF EXISTS `AccountAndType`")
                db.execSQL("CREATE VIEW `AccountAndType` AS SELECT accounts.*,accountTypes.* FROM accounts LEFT JOIN accountTypes on accounts.accountTypeId =accountTypes.typeId")
            }
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                .createFromAsset(DB_NAME)
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}