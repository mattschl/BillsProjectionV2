package ms.mattschlenkrich.billsprojectionv2.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ms.mattschlenkrich.billsprojectionv2.common.PAY_DB_NAME
import ms.mattschlenkrich.billsprojectionv2.common.PAY_DB_VERSION
import ms.mattschlenkrich.billsprojectionv2.dataBase.payDb.EmployerDao
import ms.mattschlenkrich.billsprojectionv2.model.EmployerTaxRules
import ms.mattschlenkrich.billsprojectionv2.model.Employers
import ms.mattschlenkrich.billsprojectionv2.model.WorkDates
import ms.mattschlenkrich.billsprojectionv2.model.WorkDatesExtras
import ms.mattschlenkrich.billsprojectionv2.model.WorkExtraFrequencies
import ms.mattschlenkrich.billsprojectionv2.model.WorkExtrasDefinitions
import ms.mattschlenkrich.billsprojectionv2.model.WorkPayPeriodExtras
import ms.mattschlenkrich.billsprojectionv2.model.WorkPayPeriodTax
import ms.mattschlenkrich.billsprojectionv2.model.WorkPayPeriods
import ms.mattschlenkrich.billsprojectionv2.model.WorkTaxRules

@Database(
    entities = [
        Employers::class,
        WorkPayPeriods::class,
        WorkDates::class,
        WorkDatesExtras::class,
        WorkPayPeriodExtras::class,
        WorkPayPeriodTax::class,
        WorkExtrasDefinitions::class,
        WorkExtraFrequencies::class,
        WorkTaxRules::class,
        EmployerTaxRules::class,
    ],
    version = PAY_DB_VERSION,
)
abstract class PayDatabase : RoomDatabase() {

    abstract fun getEmployerDao(): EmployerDao

    companion object {
        @Volatile
        private var instance: PayDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) =
            instance ?: synchronized(LOCK) {
                instance ?: creatDatabase(context).also {
                    instance = it
                }
            }

        private fun creatDatabase(context: Context): PayDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PayDatabase::class.java,
                PAY_DB_NAME
            )
                .build()
        }
    }
}