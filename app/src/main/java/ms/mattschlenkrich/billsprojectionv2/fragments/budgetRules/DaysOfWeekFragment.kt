package ms.mattschlenkrich.billsprojectionv2.fragments.budgetRules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ms.mattschlenkrich.billsprojectionv2.FRAG_DAYS_OF_WEEK
import ms.mattschlenkrich.billsprojectionv2.R

private const val TAG = FRAG_DAYS_OF_WEEK

class DaysOfWeekFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_days_of_week, container, false)
    }
}