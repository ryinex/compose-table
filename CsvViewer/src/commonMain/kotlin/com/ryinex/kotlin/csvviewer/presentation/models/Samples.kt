package com.ryinex.kotlin.csvviewer.presentation.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import com.ryinex.kotlin.csv.CsvFile
import composetable.csvviewer.generated.resources.Res
import composetable.csvviewer.generated.resources.ic_pet_21
import composetable.csvviewer.generated.resources.ic_pet_30
import composetable.csvviewer.generated.resources.ic_pet_39
import composetable.csvviewer.generated.resources.ic_pet_40
import kotlin.random.Random
import org.jetbrains.compose.resources.DrawableResource

internal data class Person(
    val id: Long,
    val icon: ImageVector,
    val pet: DrawableResource,
    val name: String,
    val age: Int,
    val city: String,
    val country: String,
    val occupation: String,
    val salary: Double,
    val company: String,
    val industry: String,
    val insurance: Boolean,
    var description: String
)

internal object Samples {
    private var counter = 0L
    private val firstNames =
        listOf("John", "Jane", "Bob", "Alice", "Mike", "Sarah", "David", "Anna", "Peter", "Steve", "Lucy")
    private val lastNames =
        listOf(
            "Doe",
            "Smith",
            "Johnson",
            "Williams",
            "Brown",
            "Jones",
            "Miller",
            "Davis",
            "Wilson",
            "Anderson",
            "Taylor"
        )
    private val cities =
        listOf(
            "New York",
            "Los Angeles",
            "San Francisco",
            "Chicago",
            "Houston",
            "Philadelphia",
            "Phoenix",
            "San Diego",
            "Dallas",
            "San Jose",
            "Austin"
        )
    private val countries =
        listOf("USA", "Canada", "Mexico", "UK", "Germany", "France", "Spain", "Italy", "Japan", "China", "Brazil")
    private val occupations =
        listOf(
            "Software Engineer",
            "Data Scientist",
            "Marketing Manager",
            "Sales Representative",
            "Customer Support",
            "IT Manager",
            "Project Manager",
            "Human Resources Manager",
            "Accountant",
            "Sales Manager",
            "Marketing Assistant"
        )
    private val salaries =
        listOf(80000.0, 60000.0, 100000.0, 75000.0, 90000.0, 110000.0, 120000.0, 130000.0, 140000.0, 150000.0, 160000.0)
    private val companies =
        listOf(
            "Google",
            "Apple",
            "Microsoft",
            "Amazon",
            "Facebook",
            "Twitter",
            "Netflix",
            "Uber",
            "Tesla",
            "Walmart",
            "Verizon"
        )
    private val industries =
        listOf(
            "Software",
            "Hardware",
            "Electronics",
            "Automotive",
            "Media",
            "Finance",
            "Telecommunications",
            "Retail",
            "Manufacturing",
            "Food and Beverage",
            "Health Care"
        )
    private val insurances =
        listOf(true, false)
    private val descriptionText = "Lorem ipsum dolor sit amet,\n consectetur adipiscing elit, sed do eiusmod tempor\n" +
            " incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,\n quis nostrud exercitation" +
            " ullamco laboris nisi ut aliquip\n ex ea commodo consequat.\n Duis aute irure dolor in reprehenderit " +
            "in voluptate\n velit esse cillum dolore eu fugiat nulla pariatur. \nExcepteur sint occaecat cupidatat" +
            " non proident,\n sunt in culpa qui officia deserunt mollit anim id est laborum."
    private val pets =
        listOf(
            Res.drawable.ic_pet_21,
            Res.drawable.ic_pet_30,
            Res.drawable.ic_pet_39,
            Res.drawable.ic_pet_40
        )

    private fun person(): Person {
        return Person(
            id = ++counter,
            name = "${firstNames.random()} ${lastNames.random()}",
            icon = icon(),
            age = (8..80).random(),
            city = cities.random(),
            country = countries.random(),
            occupation = occupations.random(),
            salary = salaries.random(),
            description = descriptionText.split("\n").shuffled().take(Random.nextInt(2, 4))
                .mapIndexed { index, s -> "${index + 1}- ${s.trim()}::${s.trim()}" }
                .joinToString("\n").trim(),
            company = companies.random(),
            industry = industries.random(),
            insurance = insurances.random(),
            pet = pets.random()
        )
    }

    fun persons() = List(100) { person() }

    fun icon() = listOf(
        Icons.Filled.Lock,
        Icons.Default.Home,
        Icons.Default.Person,
        Icons.Default.Place,
        Icons.Default.Done,
        Icons.Default.Face,
        Icons.Default.Build,
        Icons.Default.Favorite
    ).random()
}

fun empty(name: String = "Untitled"): CsvFile {
    val list = (0..99).map {
        val list = ('A'..'Z').map { it.toString() to "" }
        mutableMapOf(*list.toTypedArray())
    }
    return CsvFile(name, list)
}