package ms.mattschlenkrich.billsprojectionv2.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.MainActivity
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.SQLITE_DATE
import ms.mattschlenkrich.billsprojectionv2.SQLITE_TIME
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentAccountAddBinding
import ms.mattschlenkrich.billsprojectionv2.model.Account
import ms.mattschlenkrich.billsprojectionv2.viewModel.AccountViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class AccountAddFragment : Fragment(R.layout.fragment_account_add) {

    private var _binding: FragmentAccountAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsViewModel: AccountViewModel
    private lateinit var mView: View

    private val dollarFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA)
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_DATE, Locale.CANADA)
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat(SQLITE_TIME, Locale.CANADA)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountAddBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountsViewModel = (activity as MainActivity).accountViewModel
        mView = view
    }

    private fun saveAccount(view: View) {
        val accountName = binding.editAccAddName.text.toString().trim()
        val accountHandle = binding.editAccAddHandle.text.toString().trim()
        val accountType = binding.dropAccAddType.text.toString().toLong()
        val accountBalance = binding.editAccAddBalance.text.toString().toDouble()
        val accountOwing = binding.editAccAddOwing.text.toString().toDouble()
        val accountBudgeted = binding.editAccAddBudgeted.text.toString().toDouble()
        val currTime = timeFormatter.format(Calendar.getInstance().time)

        if (accountName.isNotEmpty()) {
            val account = Account(
                0, accountName, accountHandle,
                accountType, accountBudgeted, accountBalance,
                accountOwing, false,
                currTime
            )

            if (accountsViewModel.addAccount(account).isCompleted) {

                Toast.makeText(
                    mView.context,
                    "Account was saved successfully",
                    Toast.LENGTH_LONG
                ).show()
                view.findNavController()
                    .navigate(R.id.action_accountAddFragment_to_accountsFragment)
            } else {
                Toast.makeText(
                    mView.context,
                    "$accountName already exists!!\n" +
                            "Please use another name",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                mView.context,
                "Enter a unique Name for this Account",
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