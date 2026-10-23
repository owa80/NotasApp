# NotasApp

App Android nativa (Kotlin + SQLite) cun widget tipo pósit amarelo para as tarefas
pendentes, organizado en tres lapelas: Traballo, Persoal e Outros. A interface está en galego.

## Funcións

- Widget da pantalla de inicio en 5 tamaños (columnas x filas): 2x2, 2x3, 3x2, 3x3 e 3x4.
- Na app, o botón **Poñer o pósit na pantalla de inicio** abre unha grella: arrastra o dedo
  para escoller o tamaño, como ao inserir unha táboa en Office, e preme **Colocar**.
- Tarefas con prioridade por cor: vermello (alta), amarelo (media) e verde (baixa).
- As tarefas colócanse por orde de entrada, de arriba abaixo.
- Na propia lista do pósit, cada tarefa ten:
  - unha caixa para marcala como feita (queda riscada),
  - unha papeleira para borrala (pide confirmación).
- Ao tocar o texto dunha tarefa: editar, cambiar a cor, subir, baixar, movela a outra
  lapela ou borrala. Na app tamén se pode arrastrar desde a asa ☰ para reordenar, e o menú
  **Borrar as feitas** limpa as tarefas rematadas da lapela.
- Nova tarefa co botón **+** (escribir) ou co **micrófono** (ditado por voz en galego).

## Obter o .apk sen Android Studio

1. GitHub compila a app en cada push a `main` e en cada PR.
2. Na lapela **Actions** do repositorio, abre a última execución e descarga o artefacto `NotasApp-apk`.
3. Nas versións publicadas en `main`, o .apk tamén aparece en **Releases**.
4. Copia o .apk ao móbil e ábreo. Android pedirá permiso para instalar apps descoñecidas a primeira vez.

O .apk asínase cunha clave de depuración fixa incluída no repositorio, así que cada versión nova instálase enriba da anterior sen perder as tarefas.

## Limitacións de Android

- Android non deixa a unha app escoller a posición exacta do widget: o lanzador pono no
  primeiro oco libre. Se non queda arriba á dereita, mantelo premido e arrástrao alí.
- O ditado usa o recoñecemento de voz do móbil (Google) en galego (gl-ES).

## Estrutura

- `app/src/main/java/com/notasapp/Db.kt`: base de datos SQLite (táboa `tasks`).
- `app/src/main/java/com/notasapp/Widget.kt`: widget, os seus 5 tamaños e a lista do pósit.
- `app/src/main/java/com/notasapp/TaskActions.kt`: menú de accións dunha tarefa.
- `app/src/main/java/com/notasapp/*Activity.kt`: pantallas (lista, alta/edición, selector de tamaño).
- `.github/workflows/build-apk.yml`: compilación automática.
