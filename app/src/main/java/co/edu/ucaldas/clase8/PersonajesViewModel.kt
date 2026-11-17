package co.edu.ucaldas.clase8

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonajesViewModel(
    private val repository: PersonajesRepository = PersonajesRepository(RetrofitInstance.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = repository.obtenerPersonajes().fold(
                onSuccess = { UiState.Success(it.results) },
                onFailure = { UiState.Error("No se pudo cargar. Reintenta.") }
            )
        }
    }

    fun buscarPorId(id: Int): Personaje? {
        return (_uiState.value as? UiState.Success)?.items?.find { it.id == id }
    }
}