<div align="center">
<a id="top"></a>
 
<img src="https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/Android-API_26+-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/Jetpack_Compose-2024.02-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
<img src="https://img.shields.io/badge/Firebase-Spark_Plan-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
<img src="https://img.shields.io/badge/Google_Maps-SDK-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white"/>
<br/><br/>
 
```
                     ██████╗  █████╗ ██████╗ ██╗  ██╗ █████╗ ██████╗ ██╗     ███████╗
                     ██╔══██╗██╔══██╗██╔══██╗██║ ██╔╝██╔══██╗██╔══██╗██║     ██╔════╝
                   ██████╔╝███████║██████╔╝█████╔╝ ███████║██████╔╝██║     █████╗
                   ██╔═══╝ ██╔══██║██╔══██╗██╔═██╗ ██╔══██║██╔══██╗██║     ██╔══╝
                     ██║     ██║  ██║██║  ██║██║  ██╗██║  ██║██████╔╝███████╗███████╗
                     ╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝╚══════╝
```
 
### Smart Parking. Shared Spaces.
 
**App Android nativa para el alquiler colaborativo de plazas de garaje**  
**y avisos de aparcamiento en la calle en tiempo real.**
 
🌿 *Menos tiempo buscando aparcamiento · Menos CO₂ · Más ciudad para todos*
 

 
[🌐 Ver página web](#página-web) · [📱 Características](#características) · [🚀 Instalación](#instalación) · [🏗️ Arquitectura](#arquitectura) · [📂 Estructura](#estructura-del-proyecto)
 
</div>
---
 
## Página Web
 
Este proyecto incluye también una **landing page** orientada al usuario final, desarrollada con HTML, CSS y JavaScript puro (sin frameworks).
 
> 🔗 **[parkable-web/](./parkable-web/)** — Carpeta con los 3 archivos de la web (`index.html`, `styles.css`, `main.js`)
 
La web presenta Parkable desde el punto de vista del cliente: beneficios, cómo funciona, impacto ambiental, sistema de recompensas y descarga de la app. Incluye:
 
- Diseño **dark mode** con paleta corporativa azul + verde eco
- Efecto de **cursor glow** y trail de partículas animado
- Cambio de idioma **ES / EN** en caliente (sin recargar)
- Totalmente **responsiva** (móvil, tablet, escritorio)
- Animaciones de scroll reveal, marquee y orbs ambientales
Para verla en local: abre `parkable-web/index.html` con Live Server en VSCode.
 
---
 
## Características
 
### Sector 1 — Marketplace de plazas
 
Conecta a propietarios de garajes con conductores que buscan aparcamiento.
 
| Función | Detalle |
|---------|---------|
| **Búsqueda** | Vista de mapa (Google Maps) o lista, con toggle |
| **Modalidades** | Alquiler por horas, días, semanas o meses |
| **Publicación** | Mínimo 5 fotos, descripción, precio y ubicación en mapa interactivo |
| **Reserva** | Instantánea con confirmación por notificación |
| **Pago** | Pasarela integrada (simulada en versión demo) |
| **Fotos** | Almacenadas como Base64 en Firestore (sin coste adicional) |
 
### Sector 2 — Avisos (foro de avisos en tiempo real)
 
Foro colaborativo donde los conductores se avisan mutuamente de plazas libres en la calle.
 
| Función | Detalle |
|---------|---------|
| **Crear aviso** | El conductor indica ubicación y minutos hasta que sale |
| **Reclamar** | Otro conductor reclama la plaza al instante |
| **Confirmación bilateral** | Ambos confirman para completar la transacción |
| **Puntos automáticos** | +100 al que ofrece · +25 al que aparca |
| **Tiempo real** | Firestore snapshot listeners — sin polling |
 
### Sistema de Puntos
 
Los puntos acumulados se canjean en el catálogo de recompensas:
 
| Recompensa | Coste |
|------------|-------|
| ☕ Café gratis | 200 pts |
| 🚿 Lavado de coche | 500 pts |
| ⛽ Descuento en gasolina (5 €) | 750 pts |
| ⭐ Semana Parkable Premium | 1500 pts |
 
### Otras funcionalidades
 
- **Registro de huella de carbono** — CO₂ ahorrado por cada reserva
- **Bilingüe ES / EN** — Cambio en caliente con `attachBaseContext` y `recreate()`
- **Notificaciones locales** — Confirmación de reserva y avisos
- **Autocompletado de direcciones** — Vía Nominatim (OpenStreetMap, sin coste)
- **Modo claro / oscuro / sistema** — Persistido en DataStore
---
 
## Instalación
 
### Requisitos previos
 
- [Android Studio Iguana](https://developer.android.com/studio) o superior
- JDK 17 (incluido en Android Studio)
- Cuenta de Google (para Firebase y Google Maps)
- Dispositivo o emulador con **Android 8.0+ (API 26)**
### 1. Clonar el repositorio
 
```bash
git clone https://github.com/TU_USUARIO/parkable.git
cd parkable
```
 
### 2. Configurar Firebase
 
1. Entra en [console.firebase.google.com](https://console.firebase.google.com)
2. Crea un proyecto nuevo → añade app Android con package `com.parkable.app`
3. Descarga `google-services.json` y cópialo en `app/google-services.json`
4. Activa en Firebase Console:
   - **Authentication** → Email/Password
   - **Firestore Database** → modo test
### 3. Configurar Google Maps
 
1. Entra en [console.cloud.google.com](https://console.cloud.google.com)
2. Activa **Maps SDK for Android**
3. Crea una API Key y pégala en `gradle.properties`:
```properties
MAPS_API_KEY=AIzaSy_TU_API_KEY_AQUI
```
 
### 4. Compilar y ejecutar
 
Abre el proyecto en Android Studio, sincroniza Gradle y pulsa ▶ **Run**.
 
---
 
## Arquitectura
 
El proyecto sigue el patrón **MVVM** con repositorios, sin Hilt (factoría manual para mayor legibilidad).
 
```
┌──────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│  Screens · Components · Navigation · Theme   │
└──────────────────┬───────────────────────────┘
                   │ StateFlow / collectAsState
┌──────────────────▼───────────────────────────┐
│             ViewModel Layer                  │
│  AuthVM · ListingVM · AlertVM · PointsVM     │
└──────────────────┬───────────────────────────┘
                   │ suspend functions / Flow
┌──────────────────▼────────────────────────────┐
│            Repository Layer                   │
│  AuthRepo · ListingRepo · AlertRepo · UserRepo│
└──────────────────┬────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────┐
│           Backend (Firebase)                 │
│  Authentication · Firestore · DataStore      │
│  Google Maps · Nominatim (geocoding)         │
└──────────────────────────────────────────────┘
```
 
### Base de datos (Firestore)
 
| Colección | Descripción |
|-----------|-------------|
| `users` | Perfil + saldo de puntos |
| `listings` | Anuncios del marketplace |
| `bookings` | Reservas confirmadas |
| `parking_alerts` | Avisos del foro SocialDrive |
| `points_transactions` | Historial auditable de puntos |
 
---
 
## Estructura del proyecto
 
```
parkable/
│
├── app/src/main/java/com/parkable/app/
│   ├── MainActivity.kt              # Única Activity, aplica locale en caliente
│   ├── ParkableApp.kt               # Application: lee idioma antes de cargar recursos
│   │
│   ├── data/
│   │   ├── model/Models.kt          # Data classes: User, Listing, Booking, ParkingAlert…
│   │   ├── firebase/                # FirebaseModule + Collections
│   │   ├── repository/              # Auth, User, Listing, Alert
│   │   └── GeocodingService.kt      # Cliente Nominatim (OpenStreetMap)
│   │
│   ├── locale/
│   │   ├── PreferencesRepository.kt # DataStore: idioma, tema, onboarded
│   │   └── LocaleHelper.kt          # createConfigurationContext con Locale
│   │
│   ├── ui/
│   │   ├── theme/                   # Color, Type, Theme + gradientes
│   │   ├── components/              # Botones, cards e inputs reutilizables
│   │   ├── navigation/              # Routes + ParkableNavHost + BottomBar
│   │   └── screens/
│   │       ├── auth/                # LoginScreen (login + registro)
│   │       ├── home/                # HomeScreen
│   │       ├── marketplace/         # Lista, detalle, publicar, pago
│   │       ├── socialdrive/         # Feed de avisos, nuevo aviso, detalle
│   │       ├── points/              # Saldo, catálogo y historial
│   │       ├── profile/             # Perfil, mis anuncios, mis reservas
│   │       └── settings/            # Selector de idioma, tema
│   │
│   ├── util/
│   │   ├── RewardsCatalog.kt        # Catálogo estático de recompensas
│   │   └── NotificationHelper.kt    # Notificaciones locales
│   │
│   └── viewmodel/                   # AuthVM, ListingVM, AlertVM, PointsVM, SettingsVM
│
├── parkable-web/                    # Landing page (HTML + CSS + JS)
│   ├── index.html
│   ├── styles.css
│   ├── main.js
│   └── README.md
│
└── gradle.properties                # ← Introducir aquí el MAPS_API_KEY
```
 
---
 
## Stack tecnológico
 
| Categoría | Tecnología | Versión |
|-----------|------------|---------|
| Lenguaje | Kotlin | 1.9.22 |
| UI | Jetpack Compose + Material 3 | BOM 2024.02 |
| Navegación | Navigation Compose | 2.7.7 |
| Backend | Firebase (Auth + Firestore) | BOM 32.7.2 |
| Mapas | Google Maps Compose | 4.3.0 |
| Imágenes | Coil | 2.5.0 |
| Preferencias | DataStore Preferences | 1.0.0 |
| Geocoding | Nominatim (OpenStreetMap) | REST API |
| Async | Kotlin Coroutines + Flow | 1.7.3 |
| Build | Android Gradle Plugin | 8.2.2 |
| SDK mínimo | Android 8.0 Oreo | API 26 |
 
---
 
## 🌿 Impacto ambiental
 
Parkable está diseñado con un propósito: **reducir las emisiones de CO₂** generadas por la búsqueda de aparcamiento en las ciudades.
 
- El conductor urbano medio pierde **+100 horas/año** buscando donde aparcar
- Eso genera **~18 kg de CO₂** innecesarios por conductor al año
- Con Parkable, el tiempo de búsqueda cae a **menos de 2 minutos**
- El ahorro equivale a lo que absorbe **1 árbol adulto al mes**
---
 
## 👤 Autor
 
**Antonio Raño**  
Ciclo Formativo de Grado Superior — Desarrollo de Aplicaciones Multiplataforma  
IES El Majuelo · Curso 2025-2026
 
---
 
## Licencia
 
Este proyecto es de carácter académico. Desarrollado como **Proyecto Fin de Ciclo (TFG)** del CFGS DAM. Se prohibe totalmente la venta de este contenido en cualquier plataforma.
 
---
 
<div align="center">
Hecho con 🩷 y 🌿 en Andalucía
 
**[⬆ Volver arriba](#top)**
 
</div>
