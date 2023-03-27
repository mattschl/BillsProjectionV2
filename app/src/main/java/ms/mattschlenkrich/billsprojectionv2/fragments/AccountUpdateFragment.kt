package ms.mattschlenkrich.billsprojectionv2.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountUpdateBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AccountUpdateFragment : Fragment(R.layout.fragment_account_update) {

    private var _binding: FragmentAccountUpdateBinding? = null
    private val binding get() = _binding!!

    private var mView: View? = null
    private lateinit var accountsViewModel: AccountViewModel

    //since the update fragment contains arguments in nav_graph
    private val args: AccountUpdateFragmentArgs by navArgs()
    private lateinit var currentAccount: Account

    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)

    //    val dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountUpdateBinding.inflate(inflater, container, false)
        mView = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        currentAccount = args.account!!

        fillValues()

        binding.fabAccountUpdateDone.setOnClickListener {
            updateAccount()
        }
    }

    private fun updateAccount() {
        val accountName = binding.edAccountUpdateName.text.toString().trim()
        val accountHandle = binding.edAccountUpdateHandle.text.toString().trim()
        val accountTypeId = binding.drpAccountUpdateType.text.toString().toLong()
        val balance = binding.edAccountUpdateBalance.text.toString()
            .replace(",", "").replace("$", "").toDouble()
        val owing = binding.edAccountUpdateOwing.text.toString()
            .replace(",", "").replace("$", "").toDouble()
        val budgeted = binding.edAccountUpdateBudgeted.text.toString()
            .replace(",", "").replace("$", "").toDouble()
        val currTime = timeFormatter.format(Calendar.getInstance().time)
        val account = Account(
            currentAccount.accountId, accountName,
            accountHandle, accountTypeId, budgeted,
            balance, owing,
            false, currTime
        )

        if (accountName == currentAccount.accountName) {
            accountsViewModel.updateAccount(account)
            mView?.findNavController()
                ?.navigate(R.id.action_accountUpdateFragment_to_accountsFragment)
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
                    mView?.findNavController()?.navigate(
                        R.id.action_accountUpdateFragment_to_accountsFragment
                    )
                    /*if (!accountsViewModel.updateAccount(account).isCancelled) {
                        Toast.makeText(
                            context,
                            "Account was updated",
                            Toast.LENGTH_LONG
                        ).show()
                        mView?.findNavController() ?.navigate(
                                R.id.action_accountUpdateFragment_to_accountsFragment
                            )
                    } else {
                        Toast.makeText(
                            context,
                            "$accountName already exists!!\n" +
                                    "Please use edit that Account Type",
                            Toast.LENGTH_LONG
                        ).show()
                    }*/
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
        binding.edAccountUpdateName.setText(currentAccount.accountName)
        binding.edAccountUpdateHandle.setText(currentAccount.accountNumber)
        binding.drpAccountUpdateType.setText(currentAccount.accountTypeId.toString())
        binding.edAccountUpdateBalance.setText(
            dollarFormat.format(currentAccount.accountBalance)
        )
        binding.edAccountUpdateOwing.setText(
            dollarFormat.format(currentAccount.accountOwing)
        )
        binding.edAccountUpdateBudgeted.setText(
            dollarFormat.format(currentAccount.budgetAmount)
        )
        binding.txtAccountUpdateAccountId.text = currentAccount.accountId.toString()
    }

    private fun deleteAccount() {
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Account?")
            setMessage("Are you sure you want to delete this account? ")
            setPositiveButton("Delete") { _, _ ->
                accountsViewModel.deleteAccount(
                    currentAccount.accountId,
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