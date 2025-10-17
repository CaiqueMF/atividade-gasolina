package com.example.exemplosimplesdecompose.view

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.R
import com.example.exemplosimplesdecompose.data.Coordinates
import com.example.exemplosimplesdecompose.data.GasStation

@Composable
fun AlcoolGasolinaPreco(navController: NavHostController,check:Boolean) {
    val context = LocalContext.current
    val currentLocation = rememberCurrentLocation()
    var alcool by remember { mutableStateOf("") }
    var gasolina by remember { mutableStateOf("") }
    var nomeDoPosto by remember { mutableStateOf("") }
    var checkedState by remember { mutableStateOf(check) }
    val percentageDescription = stringResource(R.string.percentage)
    val station = stringResource(R.string.station)
    val calcPrompt = stringResource(R.string.calc_prompt)
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo de texto para entrada do preço
            OutlinedTextField(
                value = alcool,
                onValueChange = { alcool = it }, // Atualiza o estado
                label = { Text(stringResource(R.string.price_alcool)) },
                modifier = Modifier.fillMaxWidth(), // Preenche a largura disponível
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Configuração do teclado
            )
            // Campo de texto para preço da Gasolina
            OutlinedTextField(
                value = gasolina,
                onValueChange = { gasolina = it },
                label = { Text(stringResource(R.string.price_gasoline)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Campo de texto para preço da Gasolina
            OutlinedTextField(
                value = nomeDoPosto,
                onValueChange = { nomeDoPosto = it },
                label = { Text(stringResource(R.string.station_name)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.Start) {
                Text(
                    text = stringResource(R.string.switch_label),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Switch(
                    modifier = Modifier.semantics { contentDescription = percentageDescription },
                    checked = checkedState,
                    onCheckedChange = { checkedState = it
                        saveConfig(context,checkedState)
                    },
                    thumbContent = {
                        if (checkedState) {
                            // Icon isn't focusable, no need for content description
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            }
            var result by remember { mutableStateOf(calcPrompt)}
            // Botão de cálculo
            Button(
                onClick = {
                    val alcoolValor = alcool.toDoubleOrNull()
                    val gasolinaValor = gasolina.toDoubleOrNull()

                    if (alcoolValor == null || gasolinaValor == null) {
                        result = "Preencha valores válidos"
                        return@Button
                    }

                    val percentual = if (checkedState) 0.75 else 0.7

                    result = if (alcoolValor <= gasolinaValor * percentual) {
                        "Álcool é mais vantajoso"
                    } else {
                        "Gasolina é mais vantajosa"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.calc_button))
            }
            Button(
                onClick = {
                    navController.navigate("ListaDePostos") // Navega para a tela de lista
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver lista de postos")
            }
            // Texto do resultado
            Text(
                text = result,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.End) {
                FloatingActionButton(
                    onClick = {
                        val alcoolValor = alcool.toDoubleOrNull()
                        val gasolinaValor = gasolina.toDoubleOrNull()
                        val canAddStation = alcoolValor != null && gasolinaValor != null && nomeDoPosto.isNotBlank()
                        val coord = currentLocation ?: Coordinates(0.0, 0.0)
                        val posto = GasStation(
                            name = if (nomeDoPosto.isNotBlank()) nomeDoPosto else "${station} ${System.currentTimeMillis()}",
                            alcoolPrice = alcoolValor,
                            gasolinaPrice = gasolinaValor,
                            coord = coord
                        )
                        if (canAddStation){
                            addGasStation(context, posto)
                            navController.navigate("ListaDePostos")
                        }else{
                            result = "Preencha valores válidos"
                        }
                    },
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.save_station))
                }
            }
        }
    }
}
fun saveConfig(context: Context, switch_state:Boolean){
    val sharedFileName="config_Alc_ou_Gas"
    var sp: SharedPreferences = context.getSharedPreferences(sharedFileName, Context.MODE_PRIVATE)
    var editor = sp.edit()
    editor.putBoolean("is_75_checked",switch_state)
    editor.apply()
}

@Composable
fun rememberCurrentLocation(): Coordinates? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<Coordinates?>(null) }

    val fusedClient = remember {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                // Se o usuário conceder, tenta pegar a localização
                try {
                    fusedClient.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            location = Coordinates(loc.latitude, loc.longitude)
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("PDM25", "Erro de permissão: ${e.message}")
                }
            } else {
                Log.w("PDM25", "Permissão de localização negada pelo usuário")
            }
        }
    )

    LaunchedEffect(Unit) {
        val finePermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarsePermission = android.Manifest.permission.ACCESS_COARSE_LOCATION

        val fineGranted = ContextCompat.checkSelfPermission(context, finePermission) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, coarsePermission) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        location = Coordinates(loc.latitude, loc.longitude)
                    }
                }
            } catch (e: SecurityException) {
                Log.e("PDM25", "Tentativa de acessar localização sem permissão: ${e.message}")
            }
        } else {
            // Pede a permissão fina
            permissionLauncher.launch(finePermission)
        }
    }

    return location
}
