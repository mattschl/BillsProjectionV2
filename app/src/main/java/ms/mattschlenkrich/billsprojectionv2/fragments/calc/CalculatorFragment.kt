package ms.mattschlenkrich.billsprojectionv2.fragments.calc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import ms.mattschlenkrich.billsprojectionv2.MainActivity
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
import ms.mattschlenkrich.billsprojectionv2.common.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentCalcBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.supportActionBar?.setTitle(R.string.calculator)
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
        setDigitActions()
        setOperatorActions()
        if (!mainViewModel.getTransferNum()!!.isNaN()) {
            binding.tvDisplay.text =
                nf.getNumberFromDouble(mainViewModel.getTransferNum()!!.toDouble())
        }
        performMath()
        setTransferActions()
    }

    private fun setTransferActions() {
        binding.btnTransfer.setOnClickListener {
            mainViewModel.setTransferNum(result[counter])
            when (mainViewModel.getReturnTo()) {

                FRAG_TRANSACTION_SPLIT -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToTransactionSplitFragment()
                    )
                }

                FRAG_TRANS_ADD -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToTransactionAddFragment()
                    )
                }

                FRAG_TRANS_PERFORM -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToTransactionPerformFragment()
                    )
                }

                FRAG_BUDGET_RULE_ADD -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToBudgetRuleAddFragment()
                    )
                }

                FRAG_BUDGET_ITEM_ADD -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToBudgetItemAddFragment()
                    )
                }

                FRAG_TRANS_UPDATE -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToTransactionUpdateFragment()
                    )
                }

                FRAG_BUDGET_ITEM_UPDATE -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToBudgetItemUpdateFragment()
                    )
                }

                FRAG_BUDGET_RULE_UPDATE -> {
                    mView.findNavController().navigate(
                        CalculatorFragmentDirections
                            .actionCalcFragmentToBudgetRuleUpdateFragment()
                    )
                }

                else -> {
                    if (mainViewModel.getReturnTo()!!.contains(FRAG_ACCOUNT_UPDATE)) {
                        mView.findNavController().navigate(
                            CalculatorFragmentDirections
                                .actionCalcFragmentToAccountUpdateFragment()
                        )
                    } else if (mainViewModel.getReturnTo()!!.contains(FRAG_ACCOUNT_ADD)) {

                        mView.findNavController().navigate(
                            CalculatorFragmentDirections
                                .actionCalcFragmentToAccountAddFragment()
                        )
                    }
                }
            }
        }
    }

    private fun setOperatorActions() {
        binding.apply {
            btnPlus.setOnClickListener { operatorAction("+") }
            btnMinus.setOnClickListener { operatorAction("-") }
            btnTimes.setOnClickListener { operatorAction("X") }
            btnDivide.setOnClickListener { operatorAction("/") }
            btnBackspace.setOnClickListener { backspace() }
            btnClear.setOnClickListener { clear() }
            btnClearAll.setOnClickListener { clearAll() }
            btnEqual.setOnClickListener { equalAction() }
        }
    }

    private fun equalAction() {
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

    private fun backspace() {
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

    private fun setDigitActions() {
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

    private fun operatorAction(operation: String) {
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
            if (operator[counter] == "") {
                formula[counter] = tvDisplay.text.toString()
                result[counter] = curNumber[counter]
            } else if (operator[counter] == "+") {
                result[counter] = prevNumber[counter] + curNumber[counter]
                formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${nf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${nf.getDollarsFromDouble(result[counter])}"
            } else if (operator[counter] == "-") {
                result[counter] = prevNumber[counter] - curNumber[counter]
                formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${nf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${nf.getDollarsFromDouble(result[counter])}"
            } else if (operator[counter] == "X") {
                result[counter] = prevNumber[counter] * curNumber[counter]
                formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${nf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${nf.getDollarsFromDouble(result[counter])}"
            } else if (operator[counter] == "/") {
                result[counter] = prevNumber[counter] / curNumber[counter]
                formula[counter] = "${nf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${nf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${nf.getDollarsFromDouble(result[counter])}"
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}