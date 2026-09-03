package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ms.mattschlenkrich.billsprojectionv2.dataBase.BillsDatabase
import org.junit.After
import org.junit.Before
import java.io.IOException

abstract class BaseDaoTest {
    protected lateinit var db: BillsDatabase
    protected lateinit var accountDao: AccountDao
    protected lateinit var accountTypeDao: AccountTypeDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, BillsDatabase::class.java
        ).build()
        accountDao = db.getAccountDao()
        accountTypeDao = db.getAccountTypesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}