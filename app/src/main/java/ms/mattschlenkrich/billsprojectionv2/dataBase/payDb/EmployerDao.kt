package ms.mattschlenkrich.billsprojectionv2.dataBase.payDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import ms.mattschlenkrich.billsprojectionv2.model.Employers

@Dao
interface EmployerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployer(employers: Employers)
}