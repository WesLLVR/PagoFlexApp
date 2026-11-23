# PagoFlex App

PagoFlex reúne en una sola app los pagos mensuales de una persona (arriendo, colegio, créditos, clubes, seguros)
y le avisa antes de que venzan. También tiene las vistas del agente de soporte, que atiende los reportes de
problema, y del ejecutivo de cada empresa, que registra los cobros.

Desde esta versión la app ya no guarda los datos en el teléfono con SQLite: todo se consulta con Retrofit a los
microservicios de https://github.com/WesLLVR/PagoFlexApp-ms, así que lo que hace una persona en un teléfono lo
ven todos los demás.

## Integrantes

- Lucas Valderrama
- Bastián González

## Cómo ejecutarla

1. Levantar los microservicios siguiendo el README de `PagoFlexApp-ms` (MySQL, Eureka, los 4 servicios y el gateway).
2. Abrir esta carpeta en Android Studio y ejecutar la app.

La dirección del servidor se cambia desde la misma app, en el botón **Servidor** de la pantalla de inicio de
sesión, sin volver a compilar:

- En el emulador del mismo computador que tiene el backend se usa `http://10.0.2.2:8080/` (viene por defecto).
- En un celular físico o en el emulador de **otro** computador se escribe la IP del computador que tiene el
  backend, por ejemplo `192.168.1.15:8080` (se ve con `ipconfig`). Todos deben estar en la misma red y el
  firewall de Windows debe permitir el puerto 8080.
- También sirve la URL pública del puerto 8080 reenviado desde Visual Studio Code (visibilidad *Public*).

La dirección queda guardada en el teléfono. Un interceptor de OkHttp la aplica a todas las llamadas de Retrofit
y a las fotos que carga Coil.

Las pantallas se recargan solas cada 10 segundos y también al volver a ellas, así se ven los cambios hechos
desde otro teléfono.

## Cuentas de prueba

Todas usan la contraseña `123456`.

| Perfil | Correo | Qué puede hacer |
|---|---|---|
| Usuario final | `camila@pagoflex.cl` | Tiene cuotas al día, una vencida y historial de 9 meses |
| Usuario final | `luis@pagoflex.cl` | Cuota pie de un departamento (36 cuotas), con una en revisión |
| Agente de soporte | `andres@pagoflex.cl`, `daniela@pagoflex.cl` | Toma y resuelve reportes |
| Ejecutivo | `paula@pagoflex.cl` (Altos del Maipo), `claudia@pagoflex.cl` (Crédito Andino) | Registra, anula y elimina compromisos |

Hay más personas: `rosa`, `matias`, `fernanda`, `hector`, `javiera` y `benjamin` (usuarios finales), `ricardo`,
`valentina` y `tomas` (agentes) y `marco`, `felipe`, `gloria`, `sebastian` y `monica` (ejecutivos), todos `@pagoflex.cl`.

Cuando un ejecutivo registra un compromiso para un RUT que no existe, se crea la persona sin contraseña.
Esa persona entra a la app con **¿Olvidaste tu contraseña?**: recibe un código de 6 números como
notificación y crea su clave.

## Lo que se mejoró en esta entrega

- **Inicio de sesión real** con correo y contraseña, y **recuperar contraseña** con un código que llega como
  notificación y vence en 15 minutos.
- **Mi perfil** para los tres perfiles: cambiar nombre, correo, teléfono, comuna y foto (cámara o galería), y
  cambiar la contraseña.
- **Pago de cuotas**: el detalle muestra todas las cuotas de la deuda separando pagadas y pendientes, no deja
  pagar una cuota si hay una anterior pendiente y, después de pagar, el comprobante ofrece pagar la siguiente.
- **Reportes de problema** con un flujo claro: *Abierto → En gestión → Resuelto → Cerrado*. El usuario confirma
  si quedó solucionado (lo cierra) o lo reabre explicando qué falta. Se ve una línea de tiempo con cada cambio.
- **DatePicker** en la fecha de vencimiento al registrar y en el filtro de fechas del historial.
- **Paginación, filtros y búsqueda** en las listas de los tres perfiles (5 elementos por página).
- **Notificaciones del sistema** para los vencimientos: se revisan una vez al día con WorkManager y al iniciar
  sesión. En Android 13 o superior la app pide el permiso de notificaciones.
- Las fotos se suben al microservicio y la app las muestra desde su URL con Coil.

## Estructura

```
app/src/main/java/cl/duocuc/pagoflex/
├── data/
│   ├── remote/          RetrofitClient, una Api y sus DTO (request/response) por microservicio
│   └── repository/      UsuarioRepository, CompromisoRepository, PagoRepository, ReporteRepository
├── logica/              Validador, ReglasNegocio
├── model/               Modelos que usan las pantallas
├── navigation/          Rutas y un archivo de navegación por perfil
├── notificaciones/      Canales, AvisosWorker (WorkManager) y permiso POST_NOTIFICATIONS
├── viewmodel/           cuenta, cliente, agente y ejecutivo
└── ui/
    ├── components/      Campos, DatePicker, búsqueda, filtros, paginador, línea de tiempo, avatar
    └── screens/         cuenta, cliente, agente y ejecutivo
```

## Pruebas unitarias

En `app/src/test` hay 32 pruebas en 4 archivos, uno por funcionalidad: `CuentaTest`, `CompromisosTest`,
`PagosTest` y `ReportesTest`. Prueban las validaciones, las reglas de negocio y los repositorios con una API
falsa, así que no necesitan el servidor. Se ejecutan con clic derecho en `app/src/test` → *Run 'Tests in ...'*.

## Distribución

La app se instala con el nombre **PagoFlex**, con su propio ícono y Splash Screen. El `.apk` y el `.aab` se firman
con el almacén de claves `pagoflex.jks` desde *Build → Generate Signed App Bundle or APK*.
