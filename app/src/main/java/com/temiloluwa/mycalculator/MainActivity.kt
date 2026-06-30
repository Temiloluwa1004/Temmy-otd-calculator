package com.temiloluwa.mycalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.temiloluwa.mycalculator.ui.theme.MyCalculatorTheme
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyCalculatorTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212),
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Calculator", "Matrix")

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1A1A1A),
            contentColor = Color.White,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> CalculatorScreen()
            1 -> MatrixScreen()
        }
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = display,
                fontSize = 36.sp,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 42.sp
            )
        }

        val buttons = listOf(
            listOf("sinh", "cosh", "tanh", "ln", "abs"),
            listOf("nPr", "nCr", "!", "^", "e"),
            listOf("(", ")", "%", "AC", "C"),
            listOf("sin", "cos", "tan", "log", "/"),
            listOf("√", "∛", "π", "7", "8"),
            listOf("9", "*", "4", "5", "6"),
            listOf("-", "1", "2", "3", "+"),
            listOf("0", ".", "=", " ", " ")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { label ->
                    if (label.isNotBlank()) {
                        CalcButton(
                            text = label,
                            modifier = Modifier.weight(1f)
                        ) {
                            when (label) {
                                "AC" -> display = "0"
                                "C" -> {
                                    display = if (display.length > 1) {
                                        display.substring(0, display.length - 1)
                                    } else {
                                        "0"
                                    }
                                }
                                "=" -> {
                                    display = try {
                                        evaluate(display).toString()
                                    } catch (_: Exception) {
                                        "Error"
                                    }
                                }
                                "nPr" -> display = append(display, "P")
                                "nCr" -> display = append(display, "C")
                                else -> display = append(display, label)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixScreen() {
    var matrixSize by remember { mutableIntStateOf(2) }
    var matrixA by remember { mutableStateOf(List(4) { List(4) { "" } }) }
    var matrixB by remember { mutableStateOf(List(4) { List(4) { "" } }) }
    var resultMatrix by remember { mutableStateOf<List<List<Double>>?>(null) }
    var operation by remember { mutableStateOf("+") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Matrix Size: $matrixSize x $matrixSize", color = Color.White, fontSize = 20.sp)
        Slider(
            value = matrixSize.toFloat(),
            onValueChange = { matrixSize = it.toInt() },
            valueRange = 2f..4f,
            steps = 1,
            colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800))
        )

        Text("Matrix A", color = Color.White, fontWeight = FontWeight.Bold)
        MatrixInputGrid(matrixSize, matrixA) { r, c, v ->
            val newList = matrixA.mapIndexed { ri, row ->
                if (ri == r) row.mapIndexed { ci, col -> if (ci == c) v else col } else row
            }
            matrixA = newList
        }

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("+", "-", "*").forEach { op ->
                Button(
                    onClick = { operation = op },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (operation == op) Color(0xFFFF9800) else Color(0xFF333333)
                    )
                ) {
                    Text(op)
                }
            }
        }

        Text("Matrix B", color = Color.White, fontWeight = FontWeight.Bold)
        MatrixInputGrid(matrixSize, matrixB) { r, c, v ->
            val newList = matrixB.mapIndexed { ri, row ->
                if (ri == r) row.mapIndexed { ci, col -> if (ci == c) v else col } else row
            }
            matrixB = newList
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                resultMatrix = try { calculateMatrix(matrixA, matrixB, matrixSize, operation) } catch(_: Exception) { null }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("Calculate", color = Color.Black)
        }

        if (resultMatrix != null) {
            Text("Result", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            resultMatrix?.let { res ->
                val locale = LocalConfiguration.current.locales[0]
                Column(
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    for (i in 0 until matrixSize) {
                        Row {
                            for (j in 0 until matrixSize) {
                                Text(
                                    text = "%.2f".format(locale, res[i][j]),
                                    color = Color.White,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixInputGrid(size: Int, data: List<List<String>>, onValueChange: (Int, Int, String) -> Unit) {
    Column {
        for (i in 0 until size) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (j in 0 until size) {
                    TextField(
                        value = data[i][j],
                        onValueChange = { onValueChange(i, j, it) },
                        modifier = Modifier.width(65.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 12.sp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedContainerColor = Color(0xFF333333),
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = text in listOf("/", "*", "-", "+", "=", "^", "%", "P", "C")
    val isFunction = text in listOf("sin", "cos", "tan", "log", "C", "AC", "√", "∛", "(", ")", "π", "sinh", "cosh", "tanh", "e", "!", "nPr", "nCr", "ln", "abs")

    val containerColor = when {
        isOperator -> Color(0xFFFF9800)
        isFunction -> Color(0xFF333333)
        else -> Color(0xFF1A1A1A)
    }

    val contentColor = when {
        isOperator -> Color.Black
        text == "AC" -> Color(0xFFFF5252)
        else -> Color.White
    }

    Button(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.2f),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (text.length > 2) 10.sp else 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun append(current: String, value: String): String {
    if ((value == ".") && current.contains(".")) {
        val lastPart = current.split(Regex("[+\\-*/^()%PC]")).last()
        if (lastPart.contains(".")) return current
    }
    if (value == "π") return if (current == "0") "π" else current + "π"
    if (value == "e") return if (current == "0") "e" else current + "e"
    
    return if (current == "0") {
        if (value == "." || value == "(") "0$value" else value
    } else {
        current + value
    }
}

fun factorial(n: Double): Double {
    if ((n !in 0.0..170.0)) return Double.NaN
    if (n == 0.0) return 1.0
    var res = 1.0
    for (i in 1..n.toInt()) res *= i
    return res
}

fun evaluate(expression: String): Double {
    val processedExpr = expression
        .replace("π", Math.PI.toString())
        .replace("e", Math.E.toString())
        .replace("√", "sqrt")
        .replace("∛", "cbrt")

    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < processedExpr.length) processedExpr[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < processedExpr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parsePermComb()
            while (true) {
                if (eat('*'.code)) x *= parsePermComb()
                else if (eat('/'.code)) x /= parsePermComb()
                else if (eat('%'.code)) {
                    val next = parsePermComb()
                    x %= next
                } else return x
            }
        }

        fun parsePermComb(): Double {
            var x = parseFactor()
            while (true) {
                x = if (eat('P'.code)) {
                    val r = parseFactor()
                    if (x >= r && x >= 0 && r >= 0) factorial(x) / factorial(x - r) else Double.NaN
                } else if (eat('C'.code)) {
                    val r = parseFactor()
                    if (x >= r && x >= 0 && r >= 0) factorial(x) / (factorial(r) * factorial(x - r)) else Double.NaN
                } else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = processedExpr.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = processedExpr.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    "abs" -> abs(x)
                    "cbrt" -> Math.cbrt(x)
                    "sinh" -> sinh(x)
                    "cosh" -> cosh(x)
                    "tanh" -> tanh(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())
            if (eat('!'.code)) x = factorial(x)

            return x
        }
    }.parse()
}

fun calculateMatrix(a: List<List<String>>, b: List<List<String>>, size: Int, op: String): List<List<Double>> {
    val matA = a.map { row -> row.map { it.toDoubleOrNull() ?: 0.0 } }
    val matB = b.map { row -> row.map { it.toDoubleOrNull() ?: 0.0 } }
    
    return when (op) {
        "+" -> List(size) { i -> List(size) { j -> matA[i][j] + matB[i][j] } }
        "-" -> List(size) { i -> List(size) { j -> matA[i][j] - matB[i][j] } }
        "*" -> {
            val res = MutableList(size) { MutableList(size) { 0.0 } }
            for (i in 0 until size) {
                for (j in 0 until size) {
                    for (k in 0 until size) {
                        res[i][j] += matA[i][k] * matB[k][j]
                    }
                }
            }
            res
        }
        else -> matA
    }
}
