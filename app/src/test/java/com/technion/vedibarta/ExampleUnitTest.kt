package com.technion.vedibarta

import org.junit.Test
import org.junit.Assert.*
import io.mockk.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun subtract_isCorrect()
    {
        val l1: MutableList<Int> = arrayListOf(0,1,2,3,4,5,6,7,8,9)
        val l2 : List<Int> = listOf(0,1,2,3,4,5,6,8,9) // 7 is missing

        l1.removeAt(3)
        l1.add(0,3)
        print(l1.subtract(l2))
    }
}
