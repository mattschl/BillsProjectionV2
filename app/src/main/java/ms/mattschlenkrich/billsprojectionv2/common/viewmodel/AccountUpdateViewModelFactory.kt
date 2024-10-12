package ms.mattschlenkrich.billsprojectionv2.common.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.dataBase.viewModel.TransactionViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

class AccountUpdateViewModelFactory(
    val mainActivity: MainActivity,
    val transactionViewModel: TransactionViewModel,
    val accountViewModel: AccountViewModel,
    val app: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AccountUpdateViewModel(
            transactionViewModel,
            accountViewModel,
            app
        ) as T
    }
}