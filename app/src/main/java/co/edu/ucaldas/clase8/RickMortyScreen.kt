package co.edu.ucaldas.clase8

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage

@Composable
fun RickMortyScreen(viewModel: PersonajesViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "lista") {
        composable("lista") {
            ListaPersonajesScreen(
                viewModel = viewModel,
                onPersonajeClick = { id ->
                    navController.navigate("detalle/$id")
                }
            )
        }
        composable(
            route = "detalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val personaje = viewModel.buscarPorId(id)
            DetallePersonajeScreen(
                personaje = personaje,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaPersonajesScreen(
    viewModel: PersonajesViewModel,
    onPersonajeClick: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rick & Morty") },
                actions = {
                    TextButton(onClick = viewModel::cargar) {
                        Text("Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val current = state) {
                UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(current.items, key = { it.id }) { personaje ->
                        PersonajeRow(
                            personaje = personaje,
                            onClick = { onPersonajeClick(personaje.id) }
                        )
                        HorizontalDivider()
                    }
                }
                is UiState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(current.message)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = viewModel::cargar) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun PersonajeRow(
    personaje: Personaje,
    onClick: () -> Unit
) {
    var failed by remember(personaje.image) { mutableStateOf(false) }
    var loading by remember(personaje.image) { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            if (personaje.image.isBlank() || failed) {
                Text(
                    text = "Sin imagen",
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                AsyncImage(
                    model = personaje.image,
                    contentDescription = "Imagen de ${personaje.name}",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    onLoading = { loading = true },
                    onSuccess = { loading = false },
                    onError = {
                        loading = false
                        failed = true
                    }
                )
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = personaje.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Estado: ${personaje.status}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePersonajeScreen(
    personaje: Personaje?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Detalle del Personaje") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (personaje != null) {
                var detailFailed by remember(personaje.image) { mutableStateOf(false) }

                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (personaje.image.isBlank() || detailFailed) {
                        Text("Sin imagen")
                    } else {
                        AsyncImage(
                            model = personaje.image,
                            contentDescription = "Imagen de ${personaje.name}",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            onError = { detailFailed = true }
                        )
                    }
                }

                Text(
                    text = personaje.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Estado: ${personaje.status}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Especie: ${personaje.species}",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text("No se encontró la información del personaje.")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Volver")
            }
        }
    }
}