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
import ms.mattschlenkrich.billsprojectionv2.common.CommonFunctions
import ms.mattschlenkrich.billsprojectionv2.common.FRAGMENT_CALC
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_ACCOUNT_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_ITEM_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_BUDGET_RULE_UPDATE
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_ADD
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_PERFORM
import ms.mattschlenkrich.billsprojectionv2.common.FRAG_TRANS_UPDATE
import ms.mattschlenkrich.billsprojectionv2.databinding.FragmentCalcBinding
import ms.mattschlenkrich.billsprojectionv2.viewModel.MainViewModel

private const val TAG = FRAGMENT_CALC

class CalcFragment : Fragment(R.layout.fragment_calc) {

    private lateinit var formula: ArrayList<String>
    private lateinit var operator: ArrayList<String>
    private lateinit var curNumber: ArrayList<Double>
    private lateinit var prevNumber: ArrayList<Double>
    private lateinit var result: ArrayList<Double>
    private var counter = 0
    private val cf = CommonFunctions()

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
        setNumberActions()
        setOperatorActions()
        if (!mainViewModel.getTransferNum()!!.isNaN()) {
            binding.tvDisplay.text =
                cf.getNumberFromDouble(mainViewModel.getTransferNum()!!.toDouble())
        }
        doMath()
        setTransferActions()
    }

    private fun setTransferActions() {
        binding.btnTransfer.setOnClickListener {
            mainViewModel.setTransferNum(result[counter])
            when (mainViewModel.getReturnTo()) {
                FRAG_TRANS_ADD -> {
                    mView.findNavController().navigate(
                        CalcFragmentDirections
                            .actionCalcFragmentToTransactionAddFragment()
                    )
                }

                FRAG_TRANS_PERFORM -> {
                    mView.findNavController().navigate(
                        CalcFragmentDirections
                            .actionCalcFragmentToTransactionPerformFragment()
                    )
                }

                FRAG_BUDGET_RULE_ADD -> {
                    mView.findNavController().navigate(
                        CalcFragmentDirections
                            .actionCalcFragmentToBudgetRuleAddFragment()
                    )
                }

                FRAG_BUDGET_ITEM_ADD -> {
                    //goto
                }

                FRAG_TRANS_UPDATE -> {
                    mView.findNavController().navigate(
                        CalcFragmentDirections
                            .actionCalcFragmentToTransactionUpdateFragment()
                    )
                }

                FRAG_ACCOUNT_UPDATE -> {
                    //goto
                }

                FRAG_BUDGET_ITEM_UPDATE -> {
                    //goto
                }

                FRAG_BUDGET_RULE_UPDATE -> {
                    mView.findNavController().navigate(
                        CalcFragmentDirections
                            .actionCalcFragmentToBudgetRuleUpdateFragment()
                    )
                }
            }
        }
    }

    private fun setOperatorActions() {
        binding.apply {
            btnPlus.setOnClickListener { doOperator("+") }
            btnMinus.setOnClickListener { doOperator("-") }
            btnTimes.setOnClickListener { doOperator("X") }
            btnDivide.setOnClickListener { doOperator("/") }
            btnBackspace.setOnClickListener { doBackspace() }
            btnClear.setOnClickListener { doClear() }
            btnClearAll.setOnClickListener { doClearAll() }
            btnEqual.setOnClickListener { doEqual() }
        }
    }

    private fun doEqual() {
        counter += 1
        binding.tvDisplay.text = cf.getNumberFromDouble(result[counter - 1])
        prevNumber.add(counter, 0.0)
        curNumber.add(counter, 0.0)
        operator.add(counter, "")
        formula.add(counter, "")
        result.add(counter, 0.0)
        doMath()
    }

    private fun doClearAll() {
        prevNumber[counter] = 0.0
        operator[counter] = ""
        binding.tvDisplay.text = "0"
        doMath()
    }

    private fun doClear() {
        binding.tvDisplay.text = "0"
        doMath()
    }

    private fun doBackspace() {
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
        doMath()
    }

    private fun setNumberActions() {
        binding.apply {
            btn0.setOnClickListener { doNumber("0") }
            btn1.setOnClickListener { doNumber("1") }
            btn2.setOnClickListener { doNumber("2") }
            btn3.setOnClickListener { doNumber("3") }
            btn4.setOnClickListener { doNumber("4") }
            btn5.setOnClickListener { doNumber("5") }
            btn6.setOnClickListener { doNumber("6") }
            btn7.setOnClickListener { doNumber("7") }
            btn8.setOnClickListener { doNumber("8") }
            btn9.setOnClickListener { doNumber("9") }
            btnPeriod.setOnClickListener { doNumber(".") }
            btnNegative.setOnClickListener { doNumber("-") }
        }
    }

    private fun doOperator(i: String) {
        binding.apply {
            if (operator[counter] == "") {
                operator[counter] = i
                prevNumber[counter] = curNumber[counter]
                tvDisplay.text = "0"
            } else {
                operator[counter] = i
            }
            doMath()
        }
    }

    private fun doNumber(i: String) {
        binding.apply {
            var prefix = ""
            if (tvDisplay.text.contains("-")) {
                prefix = "-"
            }
            var num = tvDisplay.text.toString().replace("-", "")
            @Suppress("KotlinConstantConditions")
            if (i == "0" && num != "0") {
                num += "0"
            } else if (num == "0") {
                num = i
            } else if (i != "0" && i != "." && i != "-") {
                num += i
            }
            if ((i == ".") && !num.contains(".")) {
                num += "."
            }
            @Suppress("KotlinConstantConditions")
            if ((i == "-") && prefix != "-") {
                prefix = "-"
            } else if (i == "-" && prefix == "-") {
                prefix = ""
            }
            val display = prefix + num
            tvDisplay.text = display
        }
        doMath()
    }

    private fun doMath() {
        binding.apply {
            curNumber[counter] =
                if (tvDisplay.text.toString() == "0" ||
                    tvDisplay.text.toString() == "-0"
                ) {
                    0.0
                } else {
                    tvDisplay.text.toString().toDouble()
                }
            if (operator[counter] == "") {
                formula[counter] = tvDisplay.text.toString()
                result[counter] = curNumber[counter]
            } else if (operator[counter] == "+") {
                result[counter] = prevNumber[counter] + curNumber[counter]
                formula[counter] = "${cf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${cf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${cf.getDollarsFromDouble(result[counter])}"
            } else if (operator[counter] == "-") {
                result[counter] = prevNumber[counter] - curNumber[counter]
                formula[counter] = "${cf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${cf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${cf.getDollarsFromDouble(result[counter])}"
            } else if (operator[counter] == "X") {
                result[counter] = prevNumber[counter] * curNumber[counter]
                formula[counter] = "${cf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${cf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${cf.getDollarsFromDouble(result[counter])}"
            } else if (operator[counter] == "/") {
                result[counter] = prevNumber[counter] / curNumber[counter]
                formula[counter] = "${cf.getNumberFromDouble(prevNumber[counter])} " +
                        "${operator[counter]} " +
                        "${cf.getNumberFromDouble(curNumber[counter])} " +
                        "= ${cf.getDollarsFromDouble(result[counter])}"
            }
            var display = ""
            for (n in 0..counter) {
                display += formula[n]
                if (n < counter) display += "\n"
            }
            tvResults.text = display
            display = "TRANSFER ${cf.getDollarsFromDouble(result[counter])}"
            btnTransfer.text = display
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}