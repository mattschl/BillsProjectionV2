package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.billsprojectionv2.dataModel.Account
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountCategory
import ms.mattschlenkrich.billsprojectionv2.dataModel.AccountType

@Database(
    entities = [
        Account::class, AccountType::class, AccountCategory::class
    ], version = 1
)
abstract class BillsDatabase : RoomDatabase() {

    abstract fun getAccountTypesDao(): AccountTypeDao
    abstract fun getAccountDao(): AccountDao
    abstract fun getAccountCategoriesDao(): AccountCategoriesDao

    companion object {
        @Volatile
        private var instance: BillsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDataBase(context).also {
                instance = it
            }
        }

        private fun createDataBase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                BillsDatabase::class.java,
                "bills_db"
            ).build()
    }
}