package ms.mattschlenkrich.billsprojectionv2.fragments.accounts

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
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = FRAG_ACCOUNT_ADD

class AccountAddFragment :
    Fragment(R.layout.fragment_account_add) {

    private var _binding: FragmentAccountAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private val args: AccountAddFragmentArgs by navArgs()
    private var accountNameList: List<String>? = null

    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)

    //    private val dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
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
        // Inflate the layout for this fragment
        _binding = FragmentAccountAddBinding.inflate(
            inflater, container, false
        )
        Log.d(TAG, "$TAG is entered")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel =
            (activity as MainActivity).accountViewModel
        CoroutineScope(Dispatchers.IO).launch {
            accountNameList =
                accountsViewModel.getAccountNameList()
        }
        fillValues()

        binding.tvAccAddType.setOnClickListener {
            gotoAccountTypes()
        }
        mView = view
    }

    private fun fillValues() {
        binding.apply {
            if (args.account != null) {
                editAccAddName.setText(args.account!!.accountName)
                editAccAddHandle.setText(args.account!!.accountNumber)
                editAccAddBalance.setText(
                    dollarFormat.format(args.account!!.accountBalance)
                )
                editAccAddOwing.setText(
                    dollarFormat.format(args.account!!.accountOwing)
                )
                editAccAddBudgeted.setText(
                    dollarFormat.format(args.account!!.budgetAmount)
                )
            }
            if (args.accountType != null) {
                tvAccAddType.text = args.accountType!!.accountType
                var display =
                    if (args.accountType!!.keepTotals) "Transactions will be calculated\n" else ""
                display += if (args.accountType!!.isAsset) "This is an asset \n" else ""
                display += if (args.accountType!!.displayAsAsset) "This will be used for the budget \n" else ""
                display += if (args.accountType!!.tallyOwing) "Balance owing will be calculated " else ""
                if (display.isEmpty()) {
                    display =
                        "This is a dummy account transactions will not effect any other accounts"
                }
                tvTypeDetails.text = display
            }
        }
    }

    private fun gotoAccountTypes() {
        binding.apply {
            val accountName =
                editAccAddName.text.toString().trim()
            val accountHandle =
                editAccAddHandle.text.toString().trim()
            val accountBalance =
                editAccAddBalance.text.toString().trim()
                    .replace(",", "")
                    .replace("$", "")
                    .toDouble()
            val accountOwing =
                editAccAddOwing.text.toString().trim()
                    .replace(",", "")
                    .replace("$", "")
                    .toDouble()
            val accountBudgeted =
                editAccAddBudgeted.text.toString().trim()
                    .replace(",", "")
                    .replace("$", "")
                    .toDouble()
            val currTime =
                timeFormatter.format(Calendar.getInstance().time)
            val account = Account(
                0, accountName, accountHandle,
                0, accountBudgeted, accountBalance,
                accountOwing, false,
                currTime
            )
            val fragmentChain = "${args.callingFragments}, $TAG"
            val direction = AccountAddFragmentDirections
                .actionAccountAddFragmentToAccountTypesFragment(
                    args.budgetRuleDetailed,
                    account,
                    args.requestedAccount,
                    fragmentChain
                )
            mView.findNavController().navigate(direction)
        }
    }

    private fun saveAccount(view: View) {
        val mes = checkAccount()
        binding.apply {
            if (mes == "Ok") {
                val accountName =
                    editAccAddName.text.toString().trim()
                val accountHandle =
                    editAccAddHandle.text.toString().trim()
                val accountBalance =
                    editAccAddBalance.text.toString().trim()
                        .replace(",", "")
                        .replace("$", "")
                        .toDouble()
                val accountOwing =
                    editAccAddOwing.text.toString().trim()
                        .replace(",", "")
                        .replace("$", "")
                        .toDouble()
                val accountBudgeted =
                    editAccAddBudgeted.text.toString().trim()
                        .replace(",", "")
                        .replace("$", "")
                        .toDouble()
                val updateTime =
                    timeFormatter.format(Calendar.getInstance().time)
                val accountTypeId = args.accountType!!.typeId
                val account = Account(
                    0, accountName, accountHandle,
                    accountTypeId, accountBudgeted, accountBalance,
                    accountOwing, false,
                    updateTime
                )
                val fragmentChain =
                    args.callingFragments!!
                        .replace(", $FRAG_ACCOUNT_ADD", "")
                accountsViewModel.addAccount(account)
                val direction = AccountAddFragmentDirections
                    .actionAccountAddFragmentToAccountsFragment(
                        args.budgetRuleDetailed,
                        args.requestedAccount,
                        fragmentChain
                    )
                view.findNavController().navigate(direction)

            } else {
                Toast.makeText(
                    mView.context,
                    mes,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkAccount(): String {
        binding.apply {
            val nameIsBlank =
                editAccAddName.text.isNullOrEmpty()
            var nameFound = false
            if (accountNameList!!.isNotEmpty() && !nameIsBlank) {
                for (i in 0 until accountNameList!!.size) {
                    if (accountNameList!![i] ==
                        editAccAddName.text.toString().trim()
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
            } else if (args.accountType == null
            ) {
                "     Error!!\n" +
                        "This account must have an account Type."
            } else {
                "Ok"
            }
            return errorMess
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveAccount(mView)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}