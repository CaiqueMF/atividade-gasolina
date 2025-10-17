package com.example.exemplosimplesdecompose.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.R

@Composable
fun GasStationDetail(navController: NavHostController, stationName: String) {
    val context = LocalContext.current
    val stations = getListOfGasStation(context).toMutableList()
    val current = stations.find { it.name == stationName }

    if (current == null) {
        Text(stringResource(R.string.station_not_found), modifier = Modifier.padding(16.dp))
        return
    }

    var nome by remember { mutableStateOf(current.name) }
    var alcool by remember { mutableStateOf(current.alcoolPrice?.toString() ?: "") }
    var gasolina by remember { mutableStateOf(current.gasolinaPrice?.toString() ?: "") }
    val stringUpdated = stringResource(R.string.station_updated)
    val stringInvalid = stringResource(R.string.invalid_parameters)
    val stringRemoved = stringResource(R.string.station_removed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.edit_station), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = alcool,
            onValueChange = { alcool = it },
            label = { Text(stringResource(R.string.price_alcool)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = gasolina,
            onValueChange = { gasolina = it },
            label = { Text(stringResource(R.string.price_gasoline)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão salvar
        Button(
            onClick = {
                val alc = alcool.toDoubleOrNull()
                val gas = gasolina.toDoubleOrNull()

                if (alc != null && gas != null) {
                    val updated = current.copy(
                        name = nome,
                        alcoolPrice = alc,
                        gasolinaPrice = gas,
                        date = System.currentTimeMillis()
                    )
                    updateGasStation(context, updated)
                    Toast.makeText(context, stringUpdated, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, stringInvalid, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_station))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botão excluir
        Button(
            onClick = {
                deleteGasStation(context, current.name)
                Toast.makeText(context, stringRemoved, Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.delete_station))
        }
    }
}
