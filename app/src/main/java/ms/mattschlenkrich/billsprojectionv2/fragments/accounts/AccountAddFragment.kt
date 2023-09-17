package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountWithType
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAG_ACCOUNT_ADD

class AccountAddFragment :
    Fragment(R.layout.fragment_account_add) {

    private var _binding: FragmentAccountAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private var accountNameList: List<String>? = null
    private val cf = CommonFunctions()
    private val df = DateFunctions()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountAddBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_save -> {
                        saveAccount(mView)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        accountsViewModel =
            mainActivity.accountViewModel
        CoroutineScope(Dispatchers.IO).launch {
            accountNameList =
                accountsViewModel.getAccountNameList()
        }
        mainActivity.title = "Add a new Account"
        fillValues()

        binding.tvAccAddType.setOnClickListener {
            gotoAccountTypes()
        }
        mView = view
    }

    private fun fillValues() {
        binding.apply {
            if (mainViewModel.getAccountWithType() != null) {
                etAccAddName.setText(
                    mainViewModel.getAccountWithType()!!.account.accountName
                )
                etAccAddHandle.setText(
                    mainViewModel.getAccountWithType()!!.account.accountNumber
                )
                etAccAddBalance.setText(
                    cf.displayDollars(
                        mainViewModel.getAccountWithType()!!.account.accountBalance
                    )
                )
                etAccAddOwing.setText(
                    cf.displayDollars(
                        mainViewModel.getAccountWithType()!!.account.accountOwing
                    )
                )
                etAccAddBudgeted.setText(
                    cf.displayDollars(
                        mainViewModel.getAccountWithType()!!.account.accBudgetedAmount
                    )
                )
                etAccAddLimit.setText(
                    cf.displayDollars(
                        mainViewModel.getAccountWithType()!!.account.accountCreditLimit
                    )
                )
            }
            if (mainViewModel.getAccountWithType() != null) {
                tvAccAddType.text =
                    mainViewModel.getAccountWithType()!!.accountType.accountType
                var display =
                    if (
                        mainViewModel.getAccountWithType()!!.accountType.keepTotals
                    ) "Transactions will be calculated\n" else ""
                display += if (
                    mainViewModel.getAccountWithType()!!.accountType.isAsset
                ) "This is an asset \n" else ""
                display += if (
                    mainViewModel.getAccountWithType()!!.accountType.displayAsAsset
                ) "This will be used for the budget \n" else ""
                display += if (
                    mainViewModel.getAccountWithType()!!.accountType.tallyOwing)
                    "Balance owing will be calculated " else ""
                display += if (
                    mainViewModel.getAccountWithType()!!.accountType.allowPending)
                    "Transactions may be delayed " else ""
                if (display.isEmpty()) {
                    display =
                        "This account does not keep a balance/owing amount"
                }
                tvTypeDetails.text = display
            }
        }
    }

    private fun getCurrentAccount(): Account {
        binding.apply {
            return Account(
                cf.generateId(),
                etAccAddName.text.toString().trim(),
                etAccAddHandle.text.toString().trim(),
                mainViewModel.getAccountWithType()?.accountType?.typeId ?: 0L,
                cf.getDoubleFromDollars(etAccAddBudgeted.text.toString()),
                cf.getDoubleFromDollars(etAccAddBalance.text.toString()),
                cf.getDoubleFromDollars(etAccAddOwing.text.toString()),
                cf.getDoubleFromDollars(etAccAddLimit.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoAccountTypes() {
        mainViewModel.setCallingFragments(
            mainViewModel.getCallingFragments() + ", " + TAG
        )
        val direction = AccountAddFragmentDirections
            .actionAccountAddFragmentToAccountTypesFragment()
        mView.findNavController().navigate(direction)

    }

    private fun saveAccount(view: View) {
        val mes = checkAccount()
        if (mes == "Ok") {
            mainViewModel.setCallingFragments(
                mainViewModel.getCallingFragments()!!
                    .replace(", $FRAG_ACCOUNT_ADD", "")
            )
            val curAccount = getCurrentAccount()
            accountsViewModel.addAccount(curAccount)
            mainViewModel.setAccountWithType(
                AccountWithType(
                    curAccount,
                    mainViewModel.getAccountWithType()?.accountType!!
                )
            )
            val direction = AccountAddFragmentDirections
                .actionAccountAddFragmentToAccountsFragment()
            view.findNavController().navigate(direction)

        } else {
            Toast.makeText(
                mView.context,
                mes,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkAccount(): String {
        binding.apply {
            val nameIsBlank =
                etAccAddName.text.isNullOrEmpty()
            var nameFound = false
            if (accountNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList!!.size) {
                    if (accountNameList!![i] ==
                        etAccAddName.text.toString().trim()
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMess = if (nameIsBlank) {
                "     Error!!\n" +
                        "Please enter a name"
            } else if (nameFound) {
                "     Error!!\n" +
                        "This account rule already exists."
            } else if (mainViewModel.getAccountWithType()?.accountType != null
            ) {
                "     Error!!\n" +
                        "This account must have an account Type."
            } else {
                "Ok"
            }
            return errorMess
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}