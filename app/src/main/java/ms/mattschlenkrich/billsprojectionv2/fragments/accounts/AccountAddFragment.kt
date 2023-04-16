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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel =
            (activity as MainActivity).accountViewModel
        fillValues()

        binding.tvAccAddType.setOnClickListener {
            gotoAccountTypes()
        }
        mView = view
    }

    private fun fillValues() {
        if (args.account != null) {
            binding.editAccAddName.setText(args.account!!.accountName)
            binding.editAccAddHandle.setText(args.account!!.accountNumber)
            binding.editAccAddBalance.setText(
                dollarFormat.format(args.account!!.accountBalance)
            )
            binding.editAccAddOwing.setText(
                dollarFormat.format(args.account!!.accountOwing)
            )
            binding.editAccAddBudgeted.setText(
                dollarFormat.format(args.account!!.budgetAmount)
            )
        }
        if (args.accountType != null) {
            binding.tvAccAddType.text = args.accountType!!.accountType
            var display =
                if (args.accountType!!.keepTotals) "Transactions will be calculated\n" else ""
            display += if (args.accountType!!.isAsset) "This is an asset \n" else ""
            display += if (args.accountType!!.displayAsAsset) "This will be used for the budget \n" else ""
            display += if (args.accountType!!.tallyOwing) "Balance owing will be calculated " else ""
            if (display.isEmpty()) {
                display = "This is a dummy account transactions will not effect any other accounts"
            }
            binding.tvTypeDetails.text = display
        }
    }

    private fun gotoAccountTypes() {
        val accountName =
            binding.editAccAddName.text.toString().trim()
        val accountHandle =
            binding.editAccAddHandle.text.toString().trim()
        val accountBalance =
            binding.editAccAddBalance.text.toString().trim()
                .replace(",", "")
                .replace("$", "")
                .toDouble()
        val accountOwing =
            binding.editAccAddOwing.text.toString().trim()
                .replace(",", "")
                .replace("$", "")
                .toDouble()
        val accountBudgeted =
            binding.editAccAddBudgeted.text.toString().trim()
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
        val direction = AccountAddFragmentDirections
            .actionAccountAddFragmentToAccountTypesFragment(account, TAG)
        mView.findNavController().navigate(direction)
    }

    private fun saveAccount(view: View) {
        Log.d(TAG, "saveAccount entered")
        val accountName =
            binding.editAccAddName.text.toString().trim()
        val accountHandle =
            binding.editAccAddHandle.text.toString().trim()
        val accountBalance =
            binding.editAccAddBalance.text.toString().trim()
                .replace(",", "")
                .replace("$", "")
                .toDouble()
        val accountOwing =
            binding.editAccAddOwing.text.toString().trim()
                .replace(",", "")
                .replace("$", "")
                .toDouble()
        val accountBudgeted =
            binding.editAccAddBudgeted.text.toString().trim()
                .replace(",", "")
                .replace("$", "")
                .toDouble()
        val currTime =
            timeFormatter.format(Calendar.getInstance().time)
        if (binding.tvAccAddType.toString() != getString(R.string.choose_account_type)
            && args.accountType != null
        ) {
            val accountTypeId =
                args.accountType!!.typeId
            if (accountName.isNotEmpty()) {
                val account = Account(
                    0, accountName, accountHandle,
                    accountTypeId, accountBudgeted, accountBalance,
                    accountOwing, false,
                    currTime
                )
                accountsViewModel.addAccount(account)
                view.findNavController().navigate(
                    R.id.action_accountAddFragment_to_accountsFragment
                )

            } else {
                Toast.makeText(
                    mView.context,
                    "Enter a unique Name for this Account",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                mView.context,
                "This account must have a type!\n" +
                        "Please click on ${getString(R.string.choose_account_type)} " +
                        "to find one",
                Toast.LENGTH_LONG
            ).show()
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