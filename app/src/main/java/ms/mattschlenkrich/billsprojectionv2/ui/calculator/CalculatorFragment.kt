package ms.mattschlenkrich.billsprojectionv2.ui.calculator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAGMENT_CALC
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANSACTION_SPLIT
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentCalcBinding
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

private const val TAG = FRAGMENT_CALC

class CalculatorFragment : Fragment(R.layout.fragment_calc) {

    private lateinit var formula: ArrayList<String>
    private lateinit var operator: ArrayList<String>
    private lateinit var curNumber: ArrayList<Double>
    private lateinit var prevNumber: ArrayList<Double>
    private lateinit var result: ArrayList<Double>
    private var counter = 0
    private val nf = NumberFunctions()

    private var _binding: FragmentCalcBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalcBinding.inflate(
            inflater, container, false
        )
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel
        mView = binding.root
        Log.d(TAG, "creating $TAG")
        mainActivity.setTitle(R.string.calculator)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        createDigitActions()
        createOperatorActions()
        initializeStartValue()
        performMath()
        createTransferAction()
    }

    private fun initializeStartValue() {
        if (!mainViewModel.getTransferNum()!!.isNaN()) {
            binding.tvDisplay.text =
                nf.getNumberFromDouble(mainViewModel.getTransferNum()!!.toDouble())
        }
    }

    private fun initializeVariables() {
        formula = ArrayList()
        formula.add("")
        operator = ArrayList()
        operator.add("")
        curNumber = ArrayList()
        curNumber.add(0.0)
        prevNumber = ArrayList()
        prevNumber.add(0.0)
        result = ArrayList()
        result.add(0.0)
    }

    private fun createTransferAction() {
        binding.btnTransfer.setOnClickListener {
            returnValueToCallingFragment()
        }
    }

    private fun returnValueToCallingFragment() {
        mainViewModel.setTransferNum(result[counter])
        when (mainViewModel.getReturnTo()) {

            FRAG_TRANSACTION_SPLIT -> {
                gotoTransactionSplitFragment()
            }

            FRAG_TRANS_ADD -> {
                gotoTransactionAddFragment()
            }

            FRAG_TRANS_PERFORM -> {
                gotoTransactionPerformFragment()
            }

            FRAG_BUDGET_RULE_ADD -> {
                gotoBudgetRuleAddFragment()
            }

            FRAG_BUDGET_ITEM_ADD -> {
                gotoBudgetItemAddFragment()
            }

            FRAG_TRANS_UPDATE -> {
                gotoTransactionUpdateFragment()
            }

            FRAG_BUDGET_ITEM_UPDATE -> {
                gotoBudgetItemUpdateFragment()
            }

            FRAG_BUDGET_RULE_UPDATE -> {
                gotoBudgetRuleUpdateFragment()
            }

            else -> {
                if (mainViewModel.getReturnTo()!!.contains(FRAG_ACCOUNT_UPDATE)) {
                    gotoAccountUpdateFragment()
                } else if (mainViewModel.getReturnTo()!!.contains(FRAG_ACCOUNT_ADD)) {
                    gotoAccountAddFragment()
                }
            }
        }
    }

    private fun gotoTransactionSplitFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToTransactionSplitFragment()
        )
    }

    private fun gotoTransactionAddFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToTransactionAddFragment()
        )
    }

    private fun gotoTransactionPerformFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToTransactionPerformFragment()
        )
    }

    private fun gotoBudgetRuleAddFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToBudgetRuleAddFragment()
        )
    }

    private fun gotoBudgetItemAddFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToBudgetItemAddFragment()
        )
    }

    private fun gotoTransactionUpdateFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToTransactionUpdateFragment()
        )
    }

    private fun gotoBudgetItemUpdateFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToBudgetItemUpdateFragment()
        )
    }

    private fun gotoBudgetRuleUpdateFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToBudgetRuleUpdateFragment()
        )
    }

    private fun gotoAccountUpdateFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToAccountUpdateFragment()
        )
    }

    private fun gotoAccountAddFragment() {
        mView.findNavController().navigate(
            CalculatorFragmentDirections
                .actionCalcFragmentToAccountAddFragment()
        )
    }

    private fun createOperatorActions() {
        binding.apply {
            btnPlus.setOnClickListener { performOperatorAction("+") }
            btnMinus.setOnClickListener { performOperatorAction("-") }
            btnTimes.setOnClickListener { performOperatorAction("X") }
            btnDivide.setOnClickListener { performOperatorAction("/") }
            btnBackspace.setOnClickListener { performBackspace() }
            btnClear.setOnClickListener { clear() }
            btnClearAll.setOnClickListener { clearAll() }
            btnEqual.setOnClickListener { performEqualAction() }
        }
    }

    private fun performEqualAction() {
        counter += 1
        binding.tvDisplay.text = nf.getNumberFromDouble(result[counter - 1])
        prevNumber.add(counter, 0.0)
        curNumber.add(counter, 0.0)
        operator.add(counter, "")
        formula.add(counter, "")
        result.add(counter, 0.0)
        performMath()
    }

    private fun clearAll() {
        prevNumber[counter] = 0.0
        operator[counter] = ""
        binding.tvDisplay.text = "0"
        performMath()
    }

    private fun clear() {
        binding.tvDisplay.text = "0"
        performMath()
    }

    private fun performBackspace() {
        binding.apply {
            var prefix = ""
            if (tvDisplay.text.contains("-")) {
                prefix = "-"
            }
            var num = tvDisplay.text.toString().replace("-", "")
            num = if (num.length > 1) {
                num.substring(0, num.length - 1)
            } else {
                "0"
            }
            val display = prefix + num
            tvDisplay.text = display
        }
        performMath()
    }

    private fun createDigitActions() {
        binding.apply {
            btn0.setOnClickListener { addDigit("0") }
            btn1.setOnClickListener { addDigit("1") }
            btn2.setOnClickListener { addDigit("2") }
            btn3.setOnClickListener { addDigit("3") }
            btn4.setOnClickListener { addDigit("4") }
            btn5.setOnClickListener { addDigit("5") }
            btn6.setOnClickListener { addDigit("6") }
            btn7.setOnClickListener { addDigit("7") }
            btn8.setOnClickListener { addDigit("8") }
            btn9.setOnClickListener { addDigit("9") }
            btnPeriod.setOnClickListener { addDigit(".") }
            btnNegative.setOnClickListener { addDigit("-") }
        }
    }

    private fun performOperatorAction(operation: String) {
        binding.apply {
            if (operator[counter] == "") {
                operator[counter] = operation
                prevNumber[counter] = curNumber[counter]
                tvDisplay.text = "0"
            } else {
                operator[counter] = operation
            }
            performMath()
        }
    }

    private fun addDigit(digit: String) {
        binding.apply {
            var prefix = ""
            if (tvDisplay.text.contains("-")) {
                prefix = "-"
            }
            var number = tvDisplay.text.toString().replace("-", "")
            @Suppress("KotlinConstantConditions")
            if (digit == "0" && number != "0") {
                number += "0"
            } else if (number == "0") {
                number = digit
            } else if (digit != "0" && digit != "." && digit != "-") {
                number += digit
            }
            if ((digit == ".") && !number.contains(".")) {
                number += "."
            }
            @Suppress("KotlinConstantConditions")
            if ((digit == "-") && prefix != "-") {
                prefix = "-"
            } else if (digit == "-" && prefix == "-") {
                prefix = ""
            }
            val display = prefix + number
            tvDisplay.text = display
        }
        performMath()
    }

    private fun performMath() {
        binding.apply {
            curNumber[counter] =
                if (tvDisplay.text.toString() == "0" ||
                    tvDisplay.text.toString() == "-0"
                ) {
                    0.0
                } else {
                    nf.getDoubleFromDollars(tvDisplay.text.toString())
                }
            determineOperatorAction()
            var display = ""
            for (i in 0..counter) {
                display += formula[i]
                if (i < counter) display += "\n"
            }
            tvResults.text = display
            display = "TRANSFER ${nf.getDollarsFromDouble(result[counter])}"
            btnTransfer.text = display
        }
    }

    private fun determineOperatorAction() {
        binding.apply {
            if (operator[counter] == "") {
                performEmptyOperation()
            } else if (operator[counter] == "+") {
                performAddition()
            } else if (operator[counter] == "-") {
                performSubtraction()
            } else if (operator[counter] == "X") {
                performMultiplication()
            } else if (operator[counter] == "/") {
                performDivision()
            }
        }
    }

    private fun FragmentCalcBinding.performEmptyOperation() {
        formula[counter] = tvDisplay.text.toString()
        result[counter] = curNumber[counter]
    }

    private fun performAddition() {
        result[counter] = prevNumber[counter] + curNumber[counter]
        formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                "${operator[counter]} " +
                "${nf.getNumberFromDouble(curNumber[counter])} " +
                "= ${nf.getDollarsFromDouble(result[counter])}"
    }

    private fun performSubtraction() {
        result[counter] = prevNumber[counter] - curNumber[counter]
        formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                "${operator[counter]} " +
                "${nf.getNumberFromDouble(curNumber[counter])} " +
                "= ${nf.getDollarsFromDouble(result[counter])}"
    }

    private fun performMultiplication() {
        result[counter] = prevNumber[counter] * curNumber[counter]
        formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                "${operator[counter]} " +
                "${nf.getNumberFromDouble(curNumber[counter])} " +
                "= ${nf.getDollarsFromDouble(result[counter])}"
    }

    private fun performDivision() {
        result[counter] = prevNumber[counter] / curNumber[counter]
        formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                "${operator[counter]} " +
                "${nf.getNumberFromDouble(curNumber[counter])} " +
                "= ${nf.getDollarsFromDouble(result[counter])}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}