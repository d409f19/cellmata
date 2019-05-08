package dk.aau.cs.d409f19

import dk.aau.cs.d409f19.cellumata.ast.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class ASTTest {

    /**
     * Test values of world declaration nodes on AST from passed values on parameterised tests
     */
    @TestInstance(PER_CLASS)
    @DisplayName("World declaration tests")
    class WorldDeclTests {

        /**
         * Returns a list of integers from 10 to 1000 with step-size of 100 for testing dimension sizes
         */
        fun worldDeclDimensionData(): List<String> {
            return IntRange(10, 1000).step(100).map { it.toString() }
        }

        /**
         * Tests one dimension size is equal to passed value
         */
        @ParameterizedTest
        @MethodSource("worldDeclDimensionData")
        fun worldDeclOneDimensionTest(dimSize: String) {

            // Get compiler data of boilerplate program with only world declaration and dimOneSize as dimSize
            val compilerData = compileTestProgramParserASTInsecure(getWorldDeclString(dimSize))

            // Casting ast to RootNode
            val rootNode = compilerData.ast as RootNode

            // Assert dimension size
            assertEquals(
                dimSize.toInt(),
                rootNode.world.dimensions[0].size,
                "Dimension one size was not passed value of: $dimSize"
            )

        }

        /**
         * Tests two dimension size is equal to passed value
         */
        @ParameterizedTest
        @MethodSource("worldDeclDimensionData")
        fun worldDeclTwoDimensionTest(dimSize: String) {
            // Get compiler data of boilerplate program with only world declaration and both dimensions of dimSize,
            // setting twoDimensional to true as this is required by getWorldDeclString
            val compilerData =
                compileTestProgramParserASTInsecure(
                    getWorldDeclString(
                        dimOneSize = dimSize,
                        dimTwoSize = dimSize,
                        twoDimensional = true
                    )
                )
            // Casting ast to RootNode
            val rootNode = compilerData.ast as RootNode

            // Assert dimension sizes
            assertEquals(
                dimSize.toInt(),
                rootNode.world.dimensions[0].size,
                "Dimension one size was not passed value of: $dimSize"
            )
            assertEquals(
                dimSize.toInt(),
                rootNode.world.dimensions[1].size,
                "Dimension two size was not passed value of: $dimSize"
            )
        }

        /**
         * Returns a list of integers from 10 to 240 with step-size of 20 for testing tickrate
         */
        fun worldDeclTickrateData(): List<String> {
            return IntRange(10, 240).step(20).map { it.toString() }
        }

        /**
         * Tests tickrate is equal to passed value
         */
        @ParameterizedTest
        @MethodSource("worldDeclTickrateData")
        fun worldDeclTickrateTest(tickrate: String) {
            // Get compiler data of boilerplate program with only world declaration and tickrate set
            val compilerData =
                compileTestProgramParserASTInsecure(getWorldDeclString(tickrate = tickrate))
            // Casting ast to RootNode
            val rootNode = compilerData.ast as RootNode

            // Assert dimension sizes
            assertEquals(
                tickrate.toInt(),
                rootNode.world.tickrate,
                "Tickrate was not passed value: $tickrate"
            )
        }

        /**
         * Returns a list of integers from 1 to 20 with step-size 2, for testing cellsize
         */
        fun worldDeclCellsizeData(): List<String> {
            return IntRange(1, 20).step(2).map { it.toString() }
        }

        /**
         * Tests cellsize is equal to passed value
         */
        @ParameterizedTest
        @MethodSource("worldDeclCellsizeData")
        fun worldDeclCellsizeTest(cellsize: String) {
            // Get compiler data of boilerplate program with only world declaration and cellsize set
            val compilerData =
                compileTestProgramParserASTInsecure(getWorldDeclString(tickrate = cellsize))
            // Casting ast to RootNode
            val rootNode = compilerData.ast as RootNode

            // Assert dimension sizes
            assertEquals(
                cellsize.toInt(),
                rootNode.world.tickrate,
                "Tickrate was not passed value: $cellsize"
            )
        }

        /**
         * Tests dimension one world type is equal to set value
         */
        @Test
        fun worldDeclOneDimensionWorldTypeTest() {
            // Get compiler data of boilerplate program with only world declaration and dimOneType as wrapping
            val compilerData = compileTestProgramParserASTInsecure(getWorldDeclString(dimOneType = "wrap"))
            // Casting ast to RootNode
            val rootNode = compilerData.ast as RootNode

            // Assert dimension one type
            assertEquals(
                WorldType.WRAPPING,
                rootNode.world.dimensions[0].type,
                "Dimension one world type was not 'wrap'"
            )
        }

        /**
         * Tests dimension two world type is equal to set value
         */
        @Test
        fun worldDeclTwoDimensionWorldTypeTest() {
            // Get compiler data of boilerplate program with only world declaration and dimTwoType as wrapping,
            // setting twoDimensional to true as this is required by getWorldDeclString
            val compilerData = compileTestProgramParserASTInsecure(
                getWorldDeclString(
                    dimTwoType = "wrap",
                    twoDimensional = true
                )
            )
            // Casting ast to RootNode
            val rootNode = compilerData.ast as RootNode

            // Assert dimension two type
            assertEquals(
                WorldType.WRAPPING,
                rootNode.world.dimensions[1].type,
                "Dimension one world type was not 'wrap'"
            )

        }
    }

    @TestInstance(PER_CLASS)
    @DisplayName("Constant declaration tests")
    class ConstDeclTests {

        /**
         * Returns a list of integers from -10000 to 10000 at step-size of a 1000,
         * including INT_MIN and INT_MAX for testing integer literals. List is sorted in ascending order
         */
        fun constDeclIntegerData(): List<String> {
            return IntRange(-10000, 10000).step(1000).union(listOf(Integer.MIN_VALUE, Integer.MAX_VALUE)).sorted()
                .map { it.toString() }
        }

        /**
         * Test constant declaration integer values. Assumes that default world declaration passes.
         */
        @ParameterizedTest
        @MethodSource("constDeclIntegerData")
        fun constDeclIntegerTest(value: String) {
            // Get AST for boilerplate program with only world declaration and constant declaration
            val compilerData = compileTestProgramInsecure(getWorldDeclString() + "\n\n" + getConstDeclString(value = value))


            // Cast first body of rootNode to ConstDecl
            val const = (compilerData.ast as RootNode).body[0] as ConstDecl
            // Assert that identifier of constant is default; "ident"
            assertEquals("ident", const.ident)

            // Cast expression of constant-node to IntLiteral
            val constExpr = const.expr as IntLiteral
            // Assert value and type of constant
            assertEquals(value.toInt(), constExpr.value)
            assertTrue(const.type is IntegerType)
        }


        /**
         * Test constant declaration boolean values. Assumes that default world declaration passes.
         */
        @ParameterizedTest
        @ValueSource(strings = ["true", "false"])
        fun constDeclBooleanTest(value: String) {
            // Get AST for boilerplate program with only world declaration and constant declaration
            val compilerData = compileTestProgramInsecure(getWorldDeclString() + "\n\n" + getConstDeclString(value = value))

            // Cast first body of rootNode to ConstDecl
            val const = (compilerData.ast as RootNode).body[0] as ConstDecl
            // Assert that identifier of constant is default; "ident"
            assertEquals("ident", const.ident)

            // Cast expression of constant-node to BoolLiteral
            val constExpr = const.expr as BoolLiteral
            // Assert value and type of constant
            assertEquals(value.toBoolean(), constExpr.value)
            assertTrue(const.type is BooleanType)
        }

        /**
         * Test constant declaration float values. Assumes that default world declaration passes.
         */
        @ParameterizedTest
        @ValueSource(strings = ["3.14159", "1000.99", "123456.789", "-42.0000123401234", "-42.00001234012340"])
        fun constDeclFloatTest(value: String) {
            // Get AST for boilerplate program with only world declaration and constant declaration
            val compilerData = compileTestProgramInsecure(getWorldDeclString() + "\n\n" + getConstDeclString(value = value))

            // Cast first body of rootNode to ConstDecl
            val const = (compilerData.ast as RootNode).body[0] as ConstDecl
            // Assert that identifier of constant is default; "ident"
            assertEquals("ident", const.ident)

            // Cast expression of constant-node to BoolLiteral
            val constExpr = const.expr as FloatLiteral

            // Assert value and type of constant
            assertEquals(value.toFloat(), constExpr.value, 0.000001f)
            assertTrue(const.type is FloatType)
        }
    }

    @TestInstance(PER_CLASS)
    @DisplayName("State declaration tests")
    class StateDeclTests {

        /**
         * Test default values of boilerplate program
         */
        @Test
        fun stateDeclTest() {
            // Get AST for boilerplate program
            val compilerData = compileTestProgramParserASTInsecure(getBoilerplateProgramString())

            // Second body of RootNode should be StateDecl
            val state = (compilerData.ast as RootNode).body[1] as StateDecl
            // Assert identifier and colour-declaration
            assertEquals("stage", state.ident)
            assertEquals(255, state.red)
            assertEquals(200, state.green)
            assertEquals(100, state.blue)

            // Cast first element of state-codeblock-body to BecomeStmt,
            // then to Identifier, and assert identifier of BecomeStmt
            assertEquals("stage", ((state.body.body[0] as BecomeStmt).state as Identifier).spelling)
        }

        /**
         * Returns a list of identifiers which conform to identifier restrictions for testing state identifiers
         */
        fun stateDeclIdentifierData(): List<String> {
            return listOf(
                "stateIdentifier",
                "st4t3Id3nt1f13r",
                "stage",
                "verylongstringwhichshoulddefinitelywork"
            ).union(
                IntRange(
                    0,
                    10
                ).map { "dead$it" }).toList()
        }

        /**
         * Test boilerplate program AST state identifier, the program consists of a world- and state-declaration
         */
        @ParameterizedTest
        @MethodSource("stateDeclIdentifierData")
        fun stateDeclIdentifierTest(identifier: String) {
            // Get AST for boilerplate program
            val compilerData = compileTestProgramParserASTInsecure(getWorldDeclString() + getStateDeclString(ident = identifier))

            // First body of RootNode should be StateDecl
            val state = (compilerData.ast as RootNode).body[0] as StateDecl
            // Assert identifier is equal to passed parameter
            assertEquals(identifier, state.ident)

            // Cast first element of state-codeblock-body to BecomeStmt,
            // then to NamedExpr, and assert identifier of BecomeStmt
            assertEquals(identifier, ((state.body.body[0] as BecomeStmt).state as Identifier).spelling)
        }
    }
}