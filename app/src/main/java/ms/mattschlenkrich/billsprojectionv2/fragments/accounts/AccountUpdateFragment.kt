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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

    private var mView: View? = null
    private lateinit var accountsViewModel: AccountViewModel

    //since the update fragment contains arguments in nav_graph
    private val args: AccountUpdateFragmentArgs by navArgs()
    private var curBudgetRuleDetailed: BudgetRuleDetailed? = null
    private var curAccount: Account? = null
    private var newAccountType: AccountType? = null
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
        mView = binding.root
        return mView as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel =
            (activity as MainActivity).accountViewModel
        curBudgetRuleDetailed = args.budgetRuleDetailed!!
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
        val accountName =
            binding.edAccountUpdateName.text.toString().trim()
        val accountHandle =
            binding.edAccountUpdateHandle.text.toString().trim()
        val accountTypeId =
            curAccount!!.accountTypeId
        val balance =
            binding.edAccountUpdateBalance.text.toString()
                .replace(",", "").replace("$", "").toDouble()
        val owing =
            binding.edAccountUpdateOwing.text.toString()
                .replace(",", "").replace("$", "").toDouble()
        val budgeted =
            binding.edAccountUpdateBudgeted.text.toString()
                .replace(",", "").replace("$", "").toDouble()
        val currTime =
            timeFormatter.format(Calendar.getInstance().time)
        val account = Account(
            curAccount!!.accountId, accountName,
            accountHandle, accountTypeId, budgeted,
            balance, owing,
            false, currTime
        )
        val direction = AccountUpdateFragmentDirections
            .actionAccountUpdateFragmentToAccountTypesFragment(
                args.budgetRuleDetailed,
                account,
                args.requestedAccount,
                TAG
            )
        this.findNavController().navigate(direction)
    }

    private fun updateAccount() {
        Log.d(TAG, "updateAccount entered")
        val accountName =
            binding.edAccountUpdateName.text.toString().trim()
        val accountHandle =
            binding.edAccountUpdateHandle.text.toString().trim()
        val accountTypeId =
            newAccountType!!.typeId
        val balance =
            binding.edAccountUpdateBalance.text.toString()
                .replace(",", "").replace("$", "").toDouble()
        val owing =
            binding.edAccountUpdateOwing.text.toString()
                .replace(",", "").replace("$", "").toDouble()
        val budgeted =
            binding.edAccountUpdateBudgeted.text.toString()
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
            mView?.findNavController()?.navigate(
                R.id.action_accountUpdateFragment_to_accountsFragment
            )
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
                            args.budgetRuleDetailed,
                            args.requestedAccount,
                            args.callingFragment
                        )
                    mView?.findNavController()?.navigate(direction)
                }
                setNegativeButton("Cancel", null)
            }.create().show()

        } else {
            Toast.makeText(
                context,
                "Enter a name for this account",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun fillValues() {
        binding.edAccountUpdateName.setText(
            curAccount!!.accountName
        )
        binding.edAccountUpdateHandle.setText(
            curAccount!!.accountNumber
        )
        if (args.accountType != null) {
            binding.drpAccountUpdateType.text = args.accountType!!.accountType
        }
        binding.edAccountUpdateBalance.setText(
            dollarFormat.format(curAccount!!.accountBalance)
        )
        binding.edAccountUpdateOwing.setText(
            dollarFormat.format(curAccount!!.accountOwing)
        )
        binding.edAccountUpdateBudgeted.setText(
            dollarFormat.format(curAccount!!.budgetAmount)
        )
        binding.txtAccountUpdateAccountId.text =
            curAccount!!.accountId.toString()
    }

    private fun deleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account?")
            setMessage("Are you sure you want to delete this account? ")
            setPositiveButton("Delete") { _, _ ->
                accountsViewModel.deleteAccount(
                    args.account!!.accountId,
                    "no date"
                )
                mView?.findNavController()?.navigate(
                    R.id.action_accountUpdateFragment_to_accountsFragment
                )
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
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