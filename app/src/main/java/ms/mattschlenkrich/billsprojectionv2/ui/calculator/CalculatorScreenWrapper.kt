package ms.mattschlenkrich.billsprojectionv2.ui.calculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.common.functions.NumberFunctions
import ms.mattschlenkrich.billsprojectionv2.ui.MainActivity

@Composable
fun CalculatorScreenWrapper(
    mainActivity: MainActivity,
    navController: NavController
) {
    val mainViewModel = mainActivity.mainViewModel
    val nf = NumberFunctions()
    mainActivity.topMenuBar.setTitle(R.string.calculator)

    var displayValue by remember { mutableStateOf("0") }
    val formulaList = remember { mutableStateListOf("") }
    val operatorList = remember { mutableStateListOf("") }
    val prevNumberList = remember { mutableStateListOf(0.0) }
    val resultList = remember { mutableStateListOf(0.0) }
    var currentCounter by remember { mutableIntStateOf(0) }
    var transferResult by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        val transferNum = mainViewModel.getTransferNum()
        if (transferNum != null && !transferNum.isNaN()) {
            displayValue = nf.getNumberFromDouble(transferNum)
        }
    }

    fun performMath() {
        val curNumber = if (displayValue == "0" || displayValue == "-0") {
            0.0
        } else {
            nf.getDoubleFromDollars(displayValue)
        }

        if (operatorList.isEmpty() || operatorList[currentCounter] == "") {
            if (formulaList.isEmpty()) {
                formulaList.add(displayValue)
                resultList.add(curNumber)
                operatorList.add("")
                prevNumberList.add(0.0)
            } else {
                formulaList[currentCounter] = displayValue
                resultList[currentCounter] = curNumber
            }
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
        if (operatorList.isEmpty() || operatorList[currentCounter] == "") {
            if (operatorList.isEmpty()) {
                operatorList.add(operation)
                prevNumberList.add(0.0)
                formulaList.add("")
                resultList.add(0.0)
            } else {
                operatorList[currentCounter] = operation
            }
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
        if (currentCounter < prevNumberList.size) {
            prevNumberList[currentCounter] = 0.0
            operatorList[currentCounter] = ""
        }
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

    CalculatorScreen(
        displayValue = displayValue,
        formulaList = formulaList,
        transferResult = transferResult,
        onDigitClick = { addDigit(it) },
        onOperatorClick = { performOperatorAction(it) },
        onEqualClick = { performEqualAction() },
        onClearClick = { clear() },
        onClearAllClick = { clearAll() },
        onBackspaceClick = { performBackspace() },
        onTransferClick = {
            mainViewModel.setTransferNum(transferResult)
            navController.popBackStack()
        }
    )
}