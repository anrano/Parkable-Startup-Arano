# Parkable — App Android (TFG) - ANTONIO RAÑO

App Android nativa para el TFG del CFGS de Desarrollo de Aplicaciones Multiplataforma.
Implementa la propuesta de **Parkable S.L.**: un marketplace de plazas de garaje (alquiler
por horas/días/semanas/meses) **+** un foro tipo SocialDrive con sistema de puntos canjeables.

## Tecnologías

- **Kotlin** + **Jetpack Compose** (UI declarativa).
- **Material 3** con paleta corporativa (azul + verde sostenibilidad).
- **Firebase**: Authentication, Firestore y Storage.
- **Google Maps Compose** para anuncios geolocalizados y avisos en directo.
- **DataStore Preferences** para persistir idioma y tema.
- **Navigation Compose** con bottom bar de 5 secciones.
- Arquitectura **MVVM** con repositorios y factoría manual de ViewModels (sin Hilt).
- `minSdk = 26`, `targetSdk = 34`, AGP 8.2.2, Kotlin 1.9.22.

## Estructura del proyecto

```
app/src/main/java/com/parkable/app/
├── MainActivity.kt              ← única Activity, aplica locale en caliente
├── ParkableApp.kt               ← Application: lee idioma antes de cargar recursos
├── data/
│   ├── model/                   ← User, Listing, Booking, ParkingAlert, Reward, …
│   ├── firebase/                ← FirebaseModule, Collections
│   └── repository/              ← Auth, User, Listing, Alert (capa de datos)
├── locale/
│   ├── PreferencesRepository.kt ← DataStore (idioma/tema/onboarded)
│   └── LocaleHelper.kt          ← createConfigurationContext con el Locale guardado
├── ui/
│   ├── theme/                   ← Color, Type, Theme + gradientes
│   ├── components/              ← botones, cards, inputs reutilizables
│   ├── navigation/              ← Routes + ParkableNavHost (NavHost + bottom bar)
│   └── screens/
│       ├── settings/            ← LanguageSelectionScreen, SettingsScreen
│       ├── auth/LoginScreen.kt
│       ├── home/HomeScreen.kt
│       ├── marketplace/         ← lista, detalle, publicar, pago (sector 1)
│       ├── socialdrive/         ← feed, nuevo aviso, detalle (sector 2)
│       ├── points/PointsScreen.kt
│       └── profile/ProfileScreen.kt
├── util/RewardsCatalog.kt
└── viewmodel/                   ← AuthVM, ListingVM, AlertVM, PointsVM, SettingsVM
```

## Configuración antes de ejecutar

### 1) Firebase

1. Entra en <https://console.firebase.google.com> y crea un proyecto (p.ej. "Parkable").
2. Añade una app Android con el package **`com.parkable.app`**.
3. Descarga el `google-services.json` y reemplaza el placeholder en `app/google-services.json`.
4. En la consola de Firebase, activa:
   - **Authentication** → método **Email/Password**.
   - **Firestore Database** → modo **test** mientras desarrollas (luego configura reglas).
   - **Storage** → modo **test** al inicio.

### 2) Google Maps

1. Entra en <https://console.cloud.google.com> con la misma cuenta del proyecto Firebase.
2. **APIs y servicios → Biblioteca**: habilita **Maps SDK for Android**.
3. **APIs y servicios → Credenciales**: crea una API key.
4. Restringe la key (recomendado): a Android, indicando el package `com.parkable.app` y la huella SHA-1 de tu llavero de depuración (`./gradlew signingReport`).
5. Edita `gradle.properties` y sustituye:

   ```properties
   MAPS_API_KEY=TU_API_KEY_AQUI
   ```

### 3) Compilar

Abre el proyecto en Android Studio (Iguana o superior recomendado) y deja que sincronice
Gradle. Después: **Run** ▶ con un emulador (API ≥ 26) o dispositivo físico.

## Funcionalidades implementadas

### Selector de idioma al inicio
La primera vez que se abre la app aparece la pantalla **Selecciona tu idioma** con
ES / EN. La elección se guarda en DataStore y se aplica en caliente: cualquier cambio
posterior desde Ajustes recrea la Activity y todos los textos cambian al instante.

### Sector 1 · Marketplace estilo Wallapop
- Vista de lista o de mapa (toggle en la TopAppBar).
- Detalle con galería, mapa de la ubicación y selector de modalidad (h/d/sem/mes).
- Publicación con **mínimo 5 fotos** (validado), título, descripción, dirección, precios y
  ubicación en mapa interactivo (toca para fijar el marker).
- Las fotos se suben a **Firebase Storage**; los metadatos viven en Firestore.
- Reserva con pasarela de pago **simulada** (deliberadamente etiquetada en pantalla).

### Sector 2 · Foro de avisos
- Feed en directo de avisos (Firestore con snapshot listeners → tiempo real).
- Cualquier conductor puede publicar uno indicando ubicación (mapa) y minutos para irse.
- Otro conductor lo "reclama"; ambos confirman cuando se completa la entrega.
- **Sistema de puntos** automático: 100 al ofreciente, 25 al receptor (transacción atómica
  con `FieldValue.increment` + entrada en histórico).

### Puntos y recompensas
- Saldo en hero gradient + grid 2×2 de recompensas (lavado, descuento carburante,
  semana premium, café).
- Canje con descuento atómico, feedback por Snackbar.
- Historial completo de movimientos.

### Tema y ajustes
- Toggle Sistema/Claro/Oscuro persistente en DataStore.
- Cambio de idioma desde Ajustes con efecto inmediato.

## Notas para la defensa del TFG

- **Modelo de datos**: la separación `Listing` + `Booking` + `ParkingAlert` + `PointsTransaction`
  permite trazar cada operación (auditoría) y simplifica el cálculo de comisiones futuras.
- **Por qué Firebase**: para un TFG aporta backend instantáneo (Auth, BBDD en tiempo real,
  Storage, reglas de seguridad), evitando montar un servidor propio. La capa repositorio
  permite sustituirlo por una API REST sin tocar la UI.
- **Por qué sin Hilt**: se prioriza legibilidad. La factoría manual deja explícito el grafo
  de dependencias; bastaría introducir Hilt cambiando `AppViewModelFactory` por `@HiltViewModel`.
- **Por qué pago simulado**: integrar Stripe/Bizum reales requiere alta de comerciante y
  certificados PCI; queda como evolución futura. La simulación valida el flujo UX completo.
- **Localización**: `attachBaseContext` aplica el locale antes de inflar recursos; el
  observador en `MainActivity.onCreate` recrea la Activity ante cambios → cambio en caliente
  sin reiniciar la app.

## Roadmap (mejoras posibles)

- Pasarela real de pago (Stripe / Redsys).
- Mensajería interna entre conductores (Firestore + FCM).
- Reseñas y reputación de propietarios.
- Filtros por radio en el mapa con la ubicación del usuario.
- Tests unitarios de repositorios y ViewModels (JUnit + MockK).
- Reglas de seguridad de Firestore/Storage para producción.

---
© Parkable S.L. — Proyecto académico TFG. - Antonio Raño
