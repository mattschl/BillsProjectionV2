package ms.mattschlenkrich.billsprojectionv2.dataBase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ms.mattschlenkrich.billsprojectionv2.common.TABLE_SYNC_HISTORY
import ms.mattschlenkrich.billsprojectionv2.dataBase.model.sync.SyncHistory

@Dao
interface SyncHistoryDao {
    @Insert
    suspend fun insertSyncHistory(syncHistory: SyncHistory)

    @Query("SELECT * FROM $TABLE_SYNC_HISTORY ORDER BY syncTime DESC LIMIT 1")
    suspend fun getLastSyncHistory(): SyncHistory?

    @Query("SELECT * FROM $TABLE_SYNC_HISTORY ORDER BY syncTime DESC")
    suspend fun getAllSyncHistory(): List<SyncHistory>
}