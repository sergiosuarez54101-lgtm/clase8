1. Predicción inicial
-Pregunta: Si el Repository llama a Retrofit sin try/catch desde viewModelScope.launch, y desactivo internet antes de volver a cargar, ¿qué ocurre y por qué?
-Mi predicción: Si el Repository no envuelve la petición en un try/catch, al cortar internet Retrofit lanzará una excepción de red. Como nadie la captura dentro de viewModelScope.launch, la corrutina falla de forma no controlada y Android cierra la aplicación (crash).
2. Exploración del contrato JSON
-URL explorada: https://rickandmortyapi.com/api/character
-Diferencia con PokéAPI: En PokéAPI, el arreglo results solo trae name y una url secundaria hacia el detalle; en Rick & Morty, cada objeto dentro de results ya trae directamente los datos completos del personaje (id, name, status, species, image).

3. Excepción observada, causa técnica y corrección
Excepción observada en Logcat: java.net.UnknownHostException
Causa técnica del crash: Al cortar el acceso a internet, Retrofit lanza una excepción de red que viaja desde PersonajesRepository hasta viewModelScope.launch. Como ninguna capa captura la excepción, la corrutina falla sin control y Android mata el proceso de la aplicación.
Corrección aplicada: Se envolvió la llamada de red del Repository dentro de un bloque try/catch retornando Result<T> (relanzando CancellationException para no romper la cancelación normal de corrutinas). En el ViewModel, .fold() transforma el fallo en UiState.Error para mostrar un mensaje y un botón de reintento sin cerrar la app.
4. Diferencia entre error HTTP y fallo de conexión
Fallo de conexión (IOException / UnknownHostException / ConnectException): Ocurre en la capa de transporte antes de obtener respuesta del servidor (por falta de internet, DNS no resuelto o timeout). No existe código de estado HTTP.
Error HTTP (HttpException): Significa que sí hubo comunicación exitosa con el servidor, pero este respondió con un código de error (por ejemplo, 404 Not Found si la ruta no existe o 500 Internal Server Error).
5. Decisión de diseño del detalle
Opción implementada: Opción B (recuperar el personaje ya cargado en memoria desde el PersonajesViewModel compartido enviando únicamente su id como argumento de ruta detalle/{id}).
¿Qué pasaría si se abre directamente la ruta de detalle sin lista previa? Si la pantalla detalle/{id} se abriera directamente (por ejemplo, mediante un deep link o tras recrear el proceso desde cero sin pasar por la lista), el estado del ViewModel estaría en Loading y la lista aún no existiría en memoria; por lo tanto, buscarPorId(id) devolvería null y sería necesario consultar el endpoint GET character/{id} (obtenerPersonaje(id)) como respaldo.
6. Bonus opcional: Carga de imágenes con Coil
Librería agregada: io.coil-kt:coil-compose:2.6.0, usando el Composable AsyncImage tanto en cada fila de la lista (PersonajeRow) como en DetallePersonajeScreen.
Manejo de estados y comportamiento si la URL falla:
-Se gestionan dos estados locales con remember(personaje.image): loading (inicia en true) y failed (inicia en false).
-Mientras Coil descarga la imagen (onLoading), se superpone un CircularProgressIndicator pequeño dentro del contenedor de 72.dp.
-Si la descarga termina bien (onSuccess), loading pasa a false y se muestra la fotografía.
-Si la URL viene vacía o falla la descarga (onError, por ejemplo por un enlace roto o pérdida puntual de red al cargar el recurso), failed cambia a true y la interfaz reemplaza la imagen por el texto "Sin imagen", manteniendo siempre visibles el nombre y el estado del personaje sin provocar ningún crash.