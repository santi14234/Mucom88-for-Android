# Mucom88-for-Android

Editor y reproductor MML al estilo MUCOM88 para Android

## Estado

- Día 1: proyecto base e interfaz (editor MML, monitor, teclado, editor de tonos FM). Sin audio todavía.
- Banco ADPCM por defecto (`mucompcm.bin`) incluido en el APK.
- Funciona en vertical y horizontal.

## Interfaz

- Pantalla principal: editor MML con colores y panel inferior CONSOLA / MONITOR / TECLADO.
- Desliza a la izquierda: editor de tonos FM. Desliza a la derecha: vuelve al editor.

## Compilar

Cada `push` a `main` ejecuta GitHub Actions. El APK queda en Actions > último run > Artifacts.

## Fuente del editor

El workflow baja la fuente a `app/src/main/assets/fonts/editor.ttf` antes de compilar.
Para usar otra fuente, cambia la URL en `.github/workflows/build.yml` o sube tu propio `editor.ttf` a esa carpeta.

## Resaltado MML

Colores según la referencia de MUCOM88: directivas `#`, pistas A-K, tonos `@`, comandos (t, T, C, o, v, q, p, l, D, K, M, E, y...),
octavas `< >`, bucles y volumen relativo, macros `*n` y comentarios `;`.
