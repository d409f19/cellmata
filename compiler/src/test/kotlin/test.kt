package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.compile
import dk.aau.cs.d409f19.cellumata.main
import org.junit.jupiter.api.Test

class CompilerTests {

    /**
     * Calls compile function in main
     */
    @Test
    fun `Run compiler`() {
        main(arrayOf("src/main/resources/stress.cell"))
    }
}