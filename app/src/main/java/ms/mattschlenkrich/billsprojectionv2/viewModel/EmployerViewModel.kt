package ms.mattschlenkrich.billsprojectionv2.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.model.Employers
import ms.mattschlenkrich.billsprojectionv2.repository.EmployerRepository

class EmployerViewModel(
    app: Application,
    private val employerRepository: EmployerRepository
) : AndroidViewModel(app) {

    fun insertEmployer(employers: Employers) =
        viewModelScope.launch {
            employerRepository.insertEmployer(employers)
        }
}