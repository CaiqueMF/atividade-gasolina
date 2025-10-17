package com.example.exemplosimplesdecompose.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.data.Coordinates
import com.example.exemplosimplesdecompose.data.GasStation
import org.json.JSONObject
import com.example.exemplosimplesdecompose.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListofGasStations(navController: NavHostController) {
    val context= LocalContext.current

    val postosComp = getListOfGasStation(context)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_title)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(postosComp) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        item.alcoolPrice?.let { Text("${stringResource(R.string.alcohol_label)} R$ %.2f".format(it)) }
                        item.gasolinaPrice?.let { Text("${stringResource(R.string.gasoline_label)} R$ %.2f".format(it)) }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                val uri = Uri.parse("geo:${item.coord.lat},${item.coord.lgt}?q=${item.coord.lat},${item.coord.lgt}(${item.name})")
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }) {
                                Text(stringResource(R.string.see_on_map))
                            }

                            Button(onClick = {
                                navController.navigate("detalhesPosto/${item.name}")
                            }) {
                                Text(stringResource(R.string.edit_station))
                            }
                        }
                    }
                }
            }
        }
    }
}


fun gasStationToJson(gasStation: GasStation): JSONObject {
    return JSONObject().apply {
        put("name", gasStation.name)
        put("lat", gasStation.coord.lat)
        put("lgt", gasStation.coord.lgt)
        if (gasStation.alcoolPrice != null) put("alcoolPrice", gasStation.alcoolPrice) else put("alcoolPrice", JSONObject.NULL)
        if (gasStation.gasolinaPrice != null) put("gasolinaPrice", gasStation.gasolinaPrice) else put("gasolinaPrice", JSONObject.NULL)
        put("date", gasStation.date)
    }
}
fun jsonToGasStation(json: JSONObject?): GasStation {
    // valores padrão
    val name = json?.optString("name", "") ?: ""
    val lat = json?.optDouble("lat", 0.0) ?: 0.0
    val lgt = json?.optDouble("lgt", 0.0) ?: 0.0

    // alcoolPrice: trata null e NaN
    val alcool: Double? = json?.let {
        val v = it.optDouble("alcoolPrice", Double.NaN)
        if (v.isNaN()) null else v
    }

    // gasolinaPrice: trata null e NaN
    val gasolina: Double? = json?.let {
        val v = it.optDouble("gasolinaPrice", Double.NaN)
        if (v.isNaN()) null else v
    }

    // date: garante um Long não-nulo (fallback para agora)
    val date: Long = json?.optLong("date") ?: System.currentTimeMillis()

    return GasStation(
        name = name,
        alcoolPrice = alcool,
        gasolinaPrice = gasolina,
        coord = Coordinates(lat, lgt),
        date = date
    )
}



//Sugestão de métodos a serem usados para a versão final
fun getListOfGasStation(context: Context):List<GasStation>{
    val sharedFileName = "gas_station_list"
    val sp = context.getSharedPreferences(sharedFileName, Context.MODE_PRIVATE)
    val jsonString = sp.getString("list", "[]")
    val jsonArray = org.json.JSONArray(jsonString)

    val list = mutableListOf<GasStation>()
    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(jsonToGasStation(obj))
    }
    return list
}
fun addGasStation(context: Context, gasStation: GasStation){
    val sharedFileName = "gas_station_list"
    val sp = context.getSharedPreferences(sharedFileName, Context.MODE_PRIVATE)
    val editor = sp.edit()

    // Lê lista existente
    val existingJson = sp.getString("list", "[]")
    val jsonArray = org.json.JSONArray(existingJson)

    // Converte novo posto em JSON
    val newStation = gasStationToJson(gasStation)
    jsonArray.put(newStation)

    // Salva lista atualizada
    editor.putString("list", jsonArray.toString())
    editor.apply()
}
fun deleteGasStation(context: Context, name: String) {
    val sharedFileName = "gas_station_list"
    val sp = context.getSharedPreferences(sharedFileName, Context.MODE_PRIVATE)
    val jsonString = sp.getString("list", "[]")
    val jsonArray = org.json.JSONArray(jsonString)
    val newArray = org.json.JSONArray()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        if (obj.getString("name") != name) {
            newArray.put(obj)
        }
    }

    sp.edit().putString("list", newArray.toString()).apply()
}

fun updateGasStation(context: Context, updated: GasStation) {
    val sharedFileName = "gas_station_list"
    val sp = context.getSharedPreferences(sharedFileName, Context.MODE_PRIVATE)
    val jsonString = sp.getString("list", "[]")
    val jsonArray = org.json.JSONArray(jsonString)
    val newArray = org.json.JSONArray()

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val station = jsonToGasStation(obj)
        if (station.name == updated.name) {
            newArray.put(gasStationToJson(updated))
        } else {
            newArray.put(obj)
        }
    }

    sp.edit().putString("list", newArray.toString()).apply()
}