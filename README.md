# PagoFlex App

Punto de contacto móvil para los usuarios finales de **PagoFlex**, una fintech de recaudación y cobranza. La app reúne en un solo lugar los compromisos de pago que una persona tiene con distintas empresas, le permite pagarlos, guardar sus comprobantes, conocer su cumplimiento y reportar problemas. Incluye además las vistas mínimas para el agente de soporte de PagoFlex y para el ejecutivo de cobranza de una empresa cliente.

Proyecto de la asignatura **DSY1105 Desarrollo de Aplicaciones Móviles** — Evaluación Parcial 2, Duoc UC, sede Plaza Norte.

## Integrantes

- Lucas Valderrama
- Bastián González

## Funcionalidades implementadas

### Ingreso por perfil (RF-01)
- Pantalla de ingreso con tres perfiles ya identificados: Camila Rojas (usuario final), Andrés Lagos (agente PagoFlex) y Paula Saavedra (ejecutiva de Altos del Maipo).
- La sesión queda guardada en `SharedPreferences` y se puede cambiar desde el menú lateral.

### Usuario final (R-01)
- **Inicio:** total por pagar del mes, próximo vencimiento, cantidad de compromisos vencidos, avisos, cumplimiento y la lista de compromisos vigentes de todas las empresas, con la fecha de la última actualización (RF-02, RF-03, RNF-08).
- **Detalle** con folio, cuota, días de atraso, recargo y total a pagar (RF-04, RN-02, RN-03).
- **Pago** con elección del medio de pago, confirmación y posibilidad de cancelar (RF-05). Respeta el orden de pago dentro de una misma empresa (RN-04) y bloquea los compromisos en revisión o anulados (RN-08, RN-09).
- **Comprobante** con número único y RUT parcialmente oculto (RF-06, RN-05, RNF-07).
- **Historial de pagos** de todos los canales (app, portal y agente conversacional), con filtros por empresa y por año, y sección de compromisos anulados (RF-07, RN-07).
- **Indicador de cumplimiento** con su categoría (Ejemplar, Al día o En camino) y una barra de progreso animada (RF-08, RN-10, RN-11).
- **Reporte de problema** con motivo, comentario y foto opcional del comprobante tomada con la cámara o elegida de la galería. El compromiso pasa a *En revisión* (RF-09).
- **Avisos de vencimiento personalizados (RF-10 → RF-11):** el usuario elige 1, 3 o 5 días de anticipación o desactiva los avisos. Los avisos aparecen en el inicio con tono positivo (RN-11, RN-15).

### Agente PagoFlex (R-02)
- Lista de reportes con filtro por estado e indicador de reportes atrasados, es decir, con más de 2 días hábiles abiertos (RF-14, RN-23).
- Tomar un reporte y resolverlo. La respuesta para el usuario es obligatoria (RF-15).

### Ejecutivo de empresa cliente (R-03)
- Compromisos de su propia empresa con filtro por estado (RF-17, RN-22).
- Registro de un compromiso nuevo con validaciones: RUT con módulo 11, monto entero mayor que cero, fecha DD-MM-AAAA no anterior a hoy, cuota menor o igual al total y sin duplicados. Si la persona no existe en PagoFlex, se piden su nombre, correo y teléfono (RF-18, RN-20).
- Anulación con motivo obligatorio. Si el motivo es *Otro*, el comentario también es obligatorio (RF-19, RN-21).
- Resumen de cobranza del mes con tasa de cobranza, monto recaudado, barra de progreso y gráfico de barras (RF-20, RN-24).

## Requisitos técnicos cubiertos

| Requisito | Implementación |
|---|---|
| Interfaz y navegación | Jetpack Compose + Material 3, `NavHost` con rutas en `Ruta`, menú lateral por rol y barra superior |
| Formularios validados | Pago, reporte de problema, avisos, registro de compromiso, anulación y respuesta del agente. Cada campo tiene ícono, `isError` y mensaje de apoyo |
| Validación desacoplada | `logica/Validador.kt` y `logica/ReglasNegocio.kt`. Los ViewModels las usan y la UI solo muestra el estado |
| Gestión de estado | `MutableStateFlow` / `StateFlow` + `collectAsStateWithLifecycle` |
| Animaciones | `Animatable` + `tween` (mismo patrón de GraficosApp) en las barras de progreso de cumplimiento y de tasa de cobranza y en el gráfico de barras; `AnimatedVisibility` para mostrar errores, el pago en proceso, la foto adjunta, el panel de respuesta del agente y los mensajes de guardado; transición `fadeIn`/`fadeOut` entre pantallas |
| Persistencia local | SQLite con `SQLiteOpenHelper` (5 tablas con claves foráneas) y `SharedPreferences` para la sesión y los avisos |
| Recursos nativos | Cámara (`TakePicturePreview`) y galería (`ACTION_PICK`), con permisos `CAMERA` y `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` pedidos en tiempo de ejecución |
| Arquitectura | MVVM: `model` → `data` (DbHelper + Repository) → `viewmodel` → `ui` |

## Estructura del proyecto

```
app/src/main/java/cl/duocuc/pagoflex/
├── MainActivity.kt
├── data/                  PagoFlexDbHelper, DatosIniciales, SesionPreferencias, AvisosPreferencias, ConvertidorImagen
│   └── repository/        UsuarioRepository, CompromisoRepository, PagoRepository, ReporteRepository
├── logica/                Validador, ReglasNegocio
├── model/                 Usuario, Empresa, Compromiso, Pago, Reporte, Enums, Resumenes
├── navigation/            Rutas, AppNavegacion
├── permisos/              Permisos
├── util/                  Fechas, Formato
├── viewmodel/             Un ViewModel por funcionalidad, cada uno con su UiState
└── ui/
    ├── components/        Componentes reutilizables (campos, botones, tarjetas, gráfico, barra de progreso, menú)
    ├── screens/           IngresoScreen
    │   ├── usuario/       Inicio, Detalle, Pago, Comprobante, Historial, Reporte, Cumplimiento, Avisos
    │   ├── agente/        Reportes, Detalle del reporte
    │   └── ejecutivo/     Compromisos, Detalle, Registrar, Resumen
    ├── styles/            EstilosBoton, EstilosCampo, EstilosTarjeta
    └── theme/             Color, Dimens, Type, Theme
```

## Pasos para ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/WesLLVR/PagoFlexApp.git
   ```
2. Abrir la carpeta `PagoFlexApp` en **Android Studio** (versión con soporte para AGP 9.4 y JDK 25, la misma usada en clases).
3. Esperar a que termine la sincronización de Gradle.
4. Crear o elegir un emulador con Android 8.0 (API 26) o superior. Para probar la cámara, conviene un emulador con cámara virtual o un dispositivo físico.
5. Ejecutar la configuración `app` con el botón **Run**.
6. En la pantalla de ingreso, elegir uno de los tres perfiles.

Para volver a los datos iniciales, desinstala la app o borra sus datos desde *Ajustes → Apps → PagoFlex → Almacenamiento*.

## Datos de prueba

Los datos se precargan en SQLite la primera vez que se abre la app y corresponden al Anexo 9 del caso. Las fechas de vencimiento se calculan a partir del día de instalación, para que siempre existan compromisos pendientes y vencidos.

| Perfil | Qué probar |
|---|---|
| Camila Rojas (usuario final) | Pagar el arriendo; intentar pagar la cuota 11/12 del club antes de la 10/12 (RN-04); reportar un problema con foto; ver el cumplimiento de 91,7 % (33 de 36); configurar los avisos |
| Andrés Lagos (agente) | REP-0012 aparece como atrasado; tomarlo, intentar resolverlo sin respuesta y luego resolverlo |
| Paula Saavedra (ejecutiva) | Registrar la cuota 16/36 de Luis Muñoz (RUT 12.476.318-5); intentar registrarla de nuevo (duplicado); anular un compromiso; revisar el resumen del mes |

## Tecnologías

- Kotlin 2.2.10, Android Gradle Plugin 9.4.1, Gradle 9.6.0
- Jetpack Compose (BOM 2026.02.01), Material 3
- Navigation Compose 2.10.2, Lifecycle ViewModel Compose 2.6.1
- SQLite (`SQLiteOpenHelper`) y `SharedPreferences`
- minSdk 26, targetSdk 37
