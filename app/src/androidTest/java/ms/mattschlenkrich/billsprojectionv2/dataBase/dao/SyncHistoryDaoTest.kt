package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncHistoryDaoTest : BaseDaoTest() {

    private lateinit var syncHistoryDao: SyncHistoryDao

    @Before
    fun setup() {
        syncHistoryDao = db.getSyncHistoryDao()
    }

    @Test
    fun insertAndGetSyncHistory() = runBlocking {
        val sync = SyncHistory(1L, "2023-01-01 12:00:00", "Drive", 100L, "Success", "10")
        syncHistoryDao.insertSyncHistory(sync)

        val retrieved = syncHistoryDao.getSyncHistory(1L)
        assertNotNull(retrieved)
        assertEquals("Drive", retrieved?.syncSourceName)
    }

    @Test
    fun getLastSyncTime() = runBlocking {
        syncHistoryDao.insertSyncHistory(
            SyncHistory(
                1L,
                "2023-01-01 10:00:00",
                "S1",
                100L,
                "Success",
                ""
            )
        )
        syncHistoryDao.insertSyncHistory(
            SyncHistory(
                2L,
                "2023-01-01 11:00:00",
                "S2",
                100L,
                "Failed",
                ""
            )
        )
        syncHistoryDao.insertSyncHistory(
            SyncHistory(
                3L,
                "2023-01-01 12:00:00",
                "S3",
                100L,
                "Success",
                ""
            )
        )

        val lastTime = syncHistoryDao.getLastSyncTime(100L)
        assertEquals("2023-01-01 12:00:00", lastTime)
    }

    @Test
    fun purgeOldSyncHistory() = runBlocking {
        syncHistoryDao.insertSyncHistory(SyncHistory(1L, "2023-01-01", "", 0, "Success", ""))
        syncHistoryDao.insertSyncHistory(SyncHistory(2L, "2023-02-01", "", 0, "Success", ""))
        syncHistoryDao.insertSyncHistory(SyncHistory(3L, "2023-03-01", "", 0, "Success", ""))

        syncHistoryDao.purgeOldSyncHistory("2023-02-15")

        val remaining = syncHistoryDao.getAllSuccessfulSyncHistory()
        assertEquals(1, remaining.size)
        assertEquals(3L, remaining[0].syncId)
    }
}