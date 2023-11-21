package ms.mattschlenkrich.billsprojectionv2.repository

import ms.mattschlenkrich.billsprojectionv2.dataBase.PayDatabase
import ms.mattschlenkrich.billsprojectionv2.model.Employers

class EmployerRepository(private val db: PayDatabase) {

    suspend fun insertEmployer(employers: Employers) =
        db.getEmployerDao().insertEmployer(employers)
}