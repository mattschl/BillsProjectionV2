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
import ms.mattschlenkrich.billsprojectionv2.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.DateFunctions
import ms.mattschlenkrich.billsprojectionv2.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel

private const val TAG = FRAG_ACCOUNT_ADD

class AccountAddFragment :
    Fragment(R.layout.fragment_account_add) {

    private var _binding: FragmentAccountAddBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View
    private val args: AccountAddFragmentArgs by navArgs()
    private var accountNameList: List<String>? = null
    private val cf = CommonFunctions()
    private val df = DateFunctions()

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
        mainActivity = (activity as MainActivity)
        Log.d(TAG, "$TAG is entered")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            if (args.account != null) {
                etAccAddName.setText(args.account!!.accountName)
                etAccAddHandle.setText(args.account!!.accountNumber)
                etAccAddBalance.setText(
                    cf.displayDollars(args.account!!.accountBalance)
                )
                etAccAddOwing.setText(
                    cf.displayDollars(args.account!!.accountOwing)
                )
                etAccAddBudgeted.setText(
                    cf.displayDollars(args.account!!.accBudgetedAmount)
                )
            }
            if (args.accountType != null) {
                tvAccAddType.text = args.accountType!!.accountType
                var display =
                    if (args.accountType!!.keepTotals) "Transactions will be calculated\n" else ""
                display += if (args.accountType!!.isAsset) "This is an asset \n" else ""
                display += if (args.accountType!!.displayAsAsset) "This will be used for the budget \n" else ""
                display += if (args.accountType!!.tallyOwing) "Balance owing will be calculated " else ""
                display += if (args.accountType!!.allowPending) "Transactions may be delayed " else ""
                if (display.isEmpty()) {
                    display =
                        "This is a dummy account transactions will not effect any other accounts"
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
                0,
                cf.getDoubleFromDollars(etAccAddBudgeted.text.toString()),
                cf.getDoubleFromDollars(etAccAddBalance.text.toString()),
                cf.getDoubleFromDollars(etAccAddOwing.text.toString()),
                cf.getDoubleFromDollars(editAccAddLimit.text.toString()),
                false,
                df.getCurrentTimeAsString()
            )
        }
    }

    private fun gotoAccountTypes() {
        val fragmentChain = "${args.callingFragments}, $TAG"
        val direction = AccountAddFragmentDirections
            .actionAccountAddFragmentToAccountTypesFragment(
                args.budgetItem,
                args.transaction,
                args.budgetRuleDetailed,
                getCurrentAccount(),
                args.requestedAccount,
                fragmentChain
            )
        mView.findNavController().navigate(direction)

    }

    private fun saveAccount(view: View) {
        val mes = checkAccount()
        if (mes == "Ok") {
            val fragmentChain =
                args.callingFragments!!
                    .replace(", $FRAG_ACCOUNT_ADD", "")
            accountsViewModel.addAccount(getCurrentAccount())
            val direction = AccountAddFragmentDirections
                .actionAccountAddFragmentToAccountsFragment(
                    args.budgetItem,
                    args.transaction,
                    args.budgetRuleDetailed,
                    args.requestedAccount,
                    fragmentChain
                )
            Log.d(
                TAG, "fragment chain is\n" +
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
//        menu.clear()
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