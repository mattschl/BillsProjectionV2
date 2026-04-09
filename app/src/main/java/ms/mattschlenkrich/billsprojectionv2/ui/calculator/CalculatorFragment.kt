package ms.mattschlenkrich.billsprojectionv2.ui.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.FRAGMENT_CALC
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.common.interfaces.RefreshableFragment
import ms.mattschlenkrich.billsprojectionv2.common.viewmodel.MainViewModel
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme

private const val TAG = FRAGMENT_CALC

class CalculatorFragment : Fragment(), RefreshableFragment {

    private val nf = NumberFunctions()
    private lateinit var mainActivity: MainActivity
    private lateinit var mainViewModel: MainViewModel

    private val refreshKey = mutableStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainActivity = (activity as MainActivity)
        mainViewModel = mainActivity.mainViewModel

        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    if (refreshKey.value >= 0) {
                        CalculatorScreen()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshData()
    }

    override fun refreshData() {
        mainActivity.topMenuBar.setTitle(R.string.calculator)
        refreshKey.value++
    }

    @Composable
    fun CalculatorScreen() {
        var displayValue by remember { mutableStateOf("0") }
        val formulaList = remember { mutableStateListOf("") }
        val operatorList = remember { mutableStateListOf("") }
        val prevNumberList = remember { mutableStateListOf(0.0) }
        val resultList = remember { mutableStateListOf(0.0) }
        var currentCounter by remember { mutableIntStateOf(0) }
        var transferResult by remember { mutableDoubleStateOf(0.0) }

        LaunchedEffect(Unit) {
            if (!mainViewModel.getTransferNum()!!.isNaN()) {
                displayValue = nf.getNumberFromDouble(mainViewModel.getTransferNum()!!.toDouble())
            }
        }

        fun performMath() {
            val curNumber = if (displayValue == "0" || displayValue == "-0") {
                0.0
            } else {
                nf.getDoubleFromDollars(displayValue)
            }

            if (operatorList[currentCounter] == "") {
                formulaList[currentCounter] = displayValue
                resultList[currentCounter] = curNumber
            } else {
                val prev = prevNumberList[currentCounter]
                val result = when (operatorList[currentCounter]) {
                    "+" -> prev + curNumber
                    "-" -> prev - curNumber
                    "X" -> prev * curNumber
                    "/" -> prev / curNumber
                    else -> curNumber
                }
                resultList[currentCounter] = result
                formulaList[currentCounter] =
                    "${nf.getNumberFromDouble(prev)} ${operatorList[currentCounter]} " +
                            "${nf.getNumberFromDouble(curNumber)} = ${nf.getDollarsFromDouble(result)}"
            }
            transferResult = resultList[currentCounter]
        }

        fun addDigit(digit: String) {
            var prefix = if (displayValue.contains("-")) "-" else ""
            var number = displayValue.replace("-", "")

            when {
                digit == "0" && number != "0" -> number += "0"
                number == "0" -> number = digit
                digit != "0" && digit != "." && digit != "-" -> number += digit
                digit == "." && !number.contains(".") -> number += "."
                digit == "-" -> prefix = if (prefix == "-") "" else "-"
            }
            displayValue = prefix + number
            performMath()
        }

        fun performOperatorAction(operation: String) {
            if (operatorList[currentCounter] == "") {
                operatorList[currentCounter] = operation
                prevNumberList[currentCounter] = if (displayValue == "0" || displayValue == "-0") {
                    0.0
                } else {
                    nf.getDoubleFromDollars(displayValue)
                }
                displayValue = "0"
            } else {
                operatorList[currentCounter] = operation
            }
            performMath()
        }

        fun performEqualAction() {
            currentCounter += 1
            displayValue = nf.getNumberFromDouble(resultList[currentCounter - 1])
            prevNumberList.add(0.0)
            operatorList.add("")
            formulaList.add("")
            resultList.add(0.0)
            performMath()
        }

        fun clearAll() {
            prevNumberList[currentCounter] = 0.0
            operatorList[currentCounter] = ""
            displayValue = "0"
            performMath()
        }

        fun clear() {
            displayValue = "0"
            performMath()
        }

        fun performBackspace() {
            var prefix = if (displayValue.contains("-")) "-" else ""
            var num = displayValue.replace("-", "")
            num = if (num.length > 1) {
                num.substring(0, num.length - 1)
            } else {
                "0"
            }
            displayValue = prefix + num
            performMath()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                val listState = rememberLazyListState()
                LaunchedEffect(formulaList.size) {
                    if (formulaList.isNotEmpty()) {
                        listState.animateScrollToItem(formulaList.size - 1)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    state = listState
                ) {
                    items(formulaList) { formula ->
                        Text(
                            text = formula,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val buttonModifier = Modifier
                        .size(64.dp)
                    val redButtonColors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB00020)
                    )
                    val whiteButtonColors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton("7", whiteButtonColors, buttonModifier) { addDigit("7") }
                        CalcButton("8", whiteButtonColors, buttonModifier) { addDigit("8") }
                        CalcButton("9", whiteButtonColors, buttonModifier) { addDigit("9") }
                        CalcButton(
                            "/",
                            redButtonColors,
                            buttonModifier
                        ) { performOperatorAction("/") }
                        CalcButton("<-", redButtonColors, buttonModifier) { performBackspace() }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton("4", whiteButtonColors, buttonModifier) { addDigit("4") }
                        CalcButton("5", whiteButtonColors, buttonModifier) { addDigit("5") }
                        CalcButton("6", whiteButtonColors, buttonModifier) { addDigit("6") }
                        CalcButton(
                            "X",
                            redButtonColors,
                            buttonModifier
                        ) { performOperatorAction("X") }
                        CalcButton("C", redButtonColors, buttonModifier) { clear() }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton("1", whiteButtonColors, buttonModifier) { addDigit("1") }
                        CalcButton("2", whiteButtonColors, buttonModifier) { addDigit("2") }
                        CalcButton("3", whiteButtonColors, buttonModifier) { addDigit("3") }
                        CalcButton(
                            "-",
                            redButtonColors,
                            buttonModifier
                        ) { performOperatorAction("-") }
                        CalcButton("CA", redButtonColors, buttonModifier) { clearAll() }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalcButton(".", whiteButtonColors, buttonModifier) { addDigit(".") }
                        CalcButton("0", whiteButtonColors, buttonModifier) { addDigit("0") }
                        CalcButton("+/-", whiteButtonColors, buttonModifier) { addDigit("-") }
                        CalcButton(
                            "+",
                            redButtonColors,
                            buttonModifier
                        ) { performOperatorAction("+") }
                        CalcButton("=", redButtonColors, buttonModifier) { performEqualAction() }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        mainViewModel.setTransferNum(transferResult)
                        returnValueToCallingFragment()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB00020)
                    )
                ) {
                    Text(
                        text = "TRANSFER ${nf.getDollarsFromDouble(transferResult)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    fun CalcButton(
        text: String,
        colors: androidx.compose.material3.ButtonColors,
        modifier: Modifier,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = colors,
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }

    private fun returnValueToCallingFragment() {
        findNavController().popBackStack()
    }

}