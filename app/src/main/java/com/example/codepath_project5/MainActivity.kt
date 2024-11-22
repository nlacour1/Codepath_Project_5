package com.example.codepath_project5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.codepath_project5.ui.theme.CodePath_Project5Theme
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodePath_Project5Theme {
                PokemonApp()
            }
        }
    }
}

@Composable
fun PokemonApp() {
    var pokemonName by remember { mutableStateOf("") }
    var pokemonInfo by remember { mutableStateOf("Enter a Pokémon name and press Search") }
    val apiUrl = "https://pokeapi.co/api/v2/pokemon/"

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = pokemonName,
                    onValueChange = { pokemonName = it },
                    label = { Text("Pokémon Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        fetchPokemonInfo(apiUrl + pokemonName.lowercase()) { response ->
                            pokemonInfo = response ?: "Failed to fetch Pokémon information."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Search")
                }

                Text(
                    text = pokemonInfo,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}

fun fetchPokemonInfo(apiUrl: String, onResult: (String?) -> Unit) {
    val client = AsyncHttpClient()

    client.get(apiUrl, object : JsonHttpResponseHandler() {
        override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
            response?.let {
                val name = it.optString("name", "Unknown")
                val height = it.optInt("height", 0)
                val weight = it.optInt("weight", 0)
                val typesArray = it.getJSONArray("types")
                val types = StringBuilder()

                for (i in 0 until typesArray.length()) {
                    val type = typesArray.getJSONObject(i).getJSONObject("type").getString("name")
                    types.append(type).append(if (i < typesArray.length() - 1) ", " else "")
                }

                val result = """
                    Name: $name
                    Height: ${height / 10.0} m
                    Weight: ${weight / 10.0} kg
                    Types: $types
                """.trimIndent()

                onResult(result)
            }
        }

        override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
            onResult("Error: Unable to fetch Pokémon information (status code: $statusCode)")
        }
    })
}
