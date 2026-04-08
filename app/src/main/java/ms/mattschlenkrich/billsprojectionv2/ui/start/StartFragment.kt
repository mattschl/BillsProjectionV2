package ms.mattschlenkrich.billsprojectionv2.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import ms.mattschlenkrich.billsprojectionv2.NavGraphDirections
import ms.mattschlenkrich.billsprojectionv2.R
import ms.mattschlenkrich.billsprojectionv2.ui.theme.BillsProjectionTheme


class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BillsProjectionTheme {
                    StartScreen()
                }
            }
        }
    }

    @Composable
    fun StartScreen() {
        LaunchedEffect(Unit) {
            delay(1500)
            gotoBudgetViewFragment()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_bills_projection_foreground),
                contentDescription = stringResource(id = R.string.start),
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }

    private fun gotoBudgetViewFragment() {
        val direction = NavGraphDirections.actionGlobalBudgetViewFragment()
        findNavController().navigate(direction)
    }
}