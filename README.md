# NotasApp

App Android nativa (Kotlin + SQLite) con un widget tipo post-it amarillo para las
tareas pendientes, organizado en tres pestañas: Trabajo, Personal y Otros.

## Funciones

- Widget de pantalla de inicio en 5 tamaños (columnas x filas): 2x2, 2x3, 3x2, 3x3 y 3x4.
- En la app, el botón **Poner el post-it en la pantalla de inicio** abre una rejilla:
  arrastra el dedo para elegir el tamaño, como al insertar una tabla en Office, y pulsa
  **Colocar**. La rejilla crece desde la esquina superior derecha.
- Tareas con prioridad por color: rojo (alta), amarillo (media) y verde (baja).
- Las tareas se colocan por orden de entrada, de arriba hacia abajo.
- Toca la casilla de una tarea para marcarla como realizada (queda tachada).
- Toca el texto de una tarea para: editar, cambiar el color, subir, bajar, moverla a otra
  pestaña o eliminarla. En la app también se puede arrastrar desde el asa ☰ para reordenar.
- Nueva tarea con el botón **+** (escribir) o con el **micrófono** (dictado por voz en español).

## Obtener el .apk sin Android Studio

1. GitHub compila la app en cada push a `main` y en cada PR.
2. Entra en la pestaña **Actions** del repositorio, abre la última ejecución y descarga el artefacto `NotasApp-apk`.
3. En las versiones publicadas en `main`, el .apk también aparece en **Releases**.
4. Copia el .apk al móvil y ábrelo. Android pedirá permiso para instalar apps desconocidas la primera vez.

El .apk se firma con una clave de depuración fija incluida en el repositorio, así que cada versión nueva se instala encima de la anterior.

## Limitaciones de Android

- Android no deja a una app elegir la posición exacta del widget: el lanzador lo pone en
  el primer hueco libre. Si no queda arriba a la derecha, mantenlo pulsado y arrástralo allí.
- El dictado usa el reconocimiento de voz del móvil (Google). Si no está instalado, la app lo avisa.

## Estructura

- `app/src/main/java/com/notasapp/Db.kt`: base de datos SQLite (tabla `tasks`).
- `app/src/main/java/com/notasapp/Widget.kt`: widget, sus 5 tamaños y la lista del post-it.
- `app/src/main/java/com/notasapp/TaskActions.kt`: menú de acciones de una tarea.
- `app/src/main/java/com/notasapp/*Activity.kt`: pantallas (lista, alta/edición, selector de tamaño).
- `.github/workflows/build-apk.yml`: compilación automática.
