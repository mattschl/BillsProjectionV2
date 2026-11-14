package ms.mattschlenkrich.billsprojectionv2.common.functions

import android.graphics.Color
import java.util.Random

class VisualsFunctions {
    private val random = Random()

    fun getRandomColorInt(): Int {
        return Color.argb(
            120, random.nextInt(256), random.nextInt(256), random.nextInt(256)
        )
    }
}