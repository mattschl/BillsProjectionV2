package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.model.AccountType
import ms.mattschlenkrich.billsprojectionv2.model.BudgetRuleDetailed
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = FRAG_ACCOUNT_UPDATE

@Suppress("DEPRECATION")
class AccountUpdateFragment :
    Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private var mView: View? = null
    private lateinit var accountsViewModel: AccountViewModel

    //since the update fragment contains arguments in nav_graph
    private val args: AccountUpdateFragmentArgs by navArgs()
    private var curBudgetRuleDetailed: BudgetRuleDetailed? = null
    private var curAccount: Account? = null
    private var newAccountType: AccountType? = null
    private var accountNameList: List<String>? = null
//    private var currAccountType: AccountType? = null

    private val dollarFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.CANADA)

    //    val dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter: SimpleDateFormat =
        SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUpdateBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "$TAG is entered")
        mainActivity = (activity as MainActivity)
        mView = binding.root
        return mView as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel =
            (activity as MainActivity).accountViewModel
        CoroutineScope(Dispatchers.IO).launch {
            accountNameList = accountsViewModel.getAccountNameList()
        }
        mainActivity.title = "Update Account"
        curBudgetRuleDetailed = args.budgetRuleDetailed
        curAccount = args.account!!
        newAccountType = args.accountType!!
        fillValues()
        binding.drpAccountUpdateType.setOnClickListener {
            gotoAccountTypes()
        }
        binding.fabAccountUpdateDone.setOnClickListener {
            updateAccount()
        }
    }

    private fun gotoAccountTypes() {
        binding.apply {
            val accountName =
                edAccountUpdateName.text.toString().trim()
            val accountHandle =
                edAccountUpdateHandle.text.toString().trim()
            val accountTypeId =
                curAccount!!.accountTypeId
            val balance =
                edAccountUpdateBalance.text.toString()
                    .replace(",", "").replace("$", "").toDouble()
            val owing =
                edAccountUpdateOwing.text.toString()
                    .replace(",", "").replace("$", "").toDouble()
            val budgeted =
                edAccountUpdateBudgeted.text.toString()
                    .replace(",", "").replace("$", "").toDouble()
            val currTime =
                timeFormatter.format(Calendar.getInstance().time)
            val account = Account(
                curAccount!!.accountId, accountName,
                accountHandle, accountTypeId, budgeted,
                balance, owing,
                false, currTime
            )
            val fragmentChain = "${args.callingFragments}, $TAG"
            val direction = AccountUpdateFragmentDirections
                .actionAccountUpdateFragmentToAccountTypesFragment(
                    args.budgetItem,
                    args.transaction,
                    args.budgetRuleDetailed,
                    account,
                    args.requestedAccount,
                    fragmentChain
                )
            mView?.findNavController()?.navigate(direction)
        }
    }

    private fun checkAccount(): String {
        binding.apply {
            val nameIsBlank =
                edAccountUpdateName.text.isNullOrBlank()
            var nameFound = false
            if (accountNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList!!.size) {
                    if (accountNameList!![i] ==
                        edAccountUpdateName.text.toString() &&
                        accountNameList!![i] !=
                        args.account!!.accountName
                    ) {
                        nameFound = true
                        break
                    }
                }
            }
            val errorMes = if (nameIsBlank) {
                "     Error!!\n" +
                        "Please enter a name"
            } else if (nameFound) {
                "     Error!!\n" +
                        "This budget rule already exists."
            } else if (drpAccountUpdateType.text.isNullOrBlank()) {
                "     Error!!\n" +
                        "Please choose an account Type"
            } else {
                "Ok"
            }
            return errorMes
        }
    }

    private fun updateAccount() {
        val mess = checkAccount()
        binding.apply {
            if (mess == "Ok") {
                val accountName =
                    edAccountUpdateName.text.toString().trim()
                val accountHandle =
                    edAccountUpdateHandle.text.toString().trim()
                val accountTypeId =
                    newAccountType!!.typeId
                val balance =
                    edAccountUpdateBalance.text.toString()
                        .replace(",", "").replace("$", "").toDouble()
                val owing =
                    edAccountUpdateOwing.text.toString()
                        .replace(",", "").replace("$", "").toDouble()
                val budgeted =
                    edAccountUpdateBudgeted.text.toString()
                        .replace(",", "").replace("$", "").toDouble()
                val currTime =
                    timeFormatter.format(Calendar.getInstance().time)
                val account = Account(
                    curAccount!!.accountId, accountName,
                    accountHandle, accountTypeId, budgeted,
                    balance, owing,
                    false, currTime
                )

                if (accountName == curAccount!!.accountName) {
                    accountsViewModel.updateAccount(account)
                    val direction = AccountUpdateFragmentDirections
                        .actionAccountUpdateFragmentToAccountsFragment(
                            args.budgetItem,
                            args.transaction,
                            args.budgetRuleDetailed,
                            args.requestedAccount,
                            args.callingFragments
                        )
                    mView?.findNavController()?.navigate(direction)
                } else if (accountName.isNotBlank()) {
                    AlertDialog.Builder(activity).apply {
                        setTitle("Rename Account?")
                        setMessage(
                            "Are you sure you want to rename this Account?\n " +
                                    "      NOTE:\n" +
                                    "This will NOT replace an existing Account"
                        )
                        setPositiveButton("Update Account") { _, _ ->
                            accountsViewModel.updateAccount(account)
                            val direction = AccountUpdateFragmentDirections
                                .actionAccountUpdateFragmentToAccountsFragment(
                                    args.budgetItem,
                                    args.transaction,
                                    args.budgetRuleDetailed,
                                    args.requestedAccount,
                                    args.callingFragments
                                )
                            mView?.findNavController()?.navigate(direction)
                        }
                        setNegativeButton("Cancel", null)
                    }.create().show()
                }
            } else {
                Toast.makeText(
                    mView!!.context,
                    mess,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fillValues() {
        binding.apply {
            edAccountUpdateName.setText(
                curAccount!!.accountName
            )
            edAccountUpdateHandle.setText(
                curAccount!!.accountNumber
            )
            if (args.accountType != null) {
                drpAccountUpdateType.text = args.accountType!!.accountType
            }
            edAccountUpdateBalance.setText(
                dollarFormat.format(curAccount!!.accountBalance)
            )
            edAccountUpdateOwing.setText(
                dollarFormat.format(curAccount!!.accountOwing)
            )
            edAccountUpdateBudgeted.setText(
                dollarFormat.format(curAccount!!.accBudgetedAmount)
            )
            txtAccountUpdateAccountId.text =
                curAccount!!.accountId.toString()
        }
    }

    private fun deleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account?")
            setMessage("Are you sure you want to delete this account? ")
            setPositiveButton("Delete") { _, _ ->
                doDelete()
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    private fun doDelete() {
        val updateTime =
            timeFormatter.format(Calendar.getInstance().time)
        accountsViewModel.deleteAccount(
            args.account!!.accountId,
            updateTime
        )
        val fragmentChain =
            args.callingFragments!!
                .replace(", $FRAG_ACCOUNT_UPDATE", "")
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountsFragment(
                args.budgetItem,
                args.transaction,
                args.budgetRuleDetailed,
                args.requestedAccount,
                fragmentChain,
            )
        mView?.findNavController()?.navigate(direction)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        menu.clear()
        inflater.inflate(R.menu.delete_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete -> {
                deleteAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}