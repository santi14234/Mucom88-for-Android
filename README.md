# Mucom88-for-Android

Editor y reproductor MML al estilo MUCOM88 para Android (pensado para Samsung J260M, Android 8.1).

## Estado

- Día 1: proyecto base e interfaz (editor MML, monitor, teclado, editor de tonos FM). Sin audio todavía.
- Día 2: conversor de muestras WAV a ADPCM-B (YM2608), vista previa, guardar/cargar bancos .bin.
- Banco ADPCM por defecto (`mucompcm.bin`) incluido en el APK.
- Funciona en vertical y horizontal.

## Interfaz

- Pantalla principal: editor MML con colores y panel inferior CONSOLA / MONITOR / TECLADO.
- Desliza a la izquierda: editor de tonos FM. Desliza a la derecha: vuelve al editor.

## Compilar

Cada `push` a `main` ejecuta GitHub Actions. El APK queda en Actions > último run > Artifacts.

## Fuente del editor

Viene incluida en `app/src/main/assets/fonts/editor.ttf` (DejaVu Sans Mono). Para cambiarla, reemplaza ese archivo por otro .ttf con el mismo nombre.

## Resaltado MML

Colores según la referencia de MUCOM88: directivas `#`, pistas A-K, tonos `@`, comandos (t, T, C, o, v, q, p, l, D, K, M, E, y...),
octavas `< >`, bucles y volumen relativo, macros `*n` y comentarios `;`.

## Conversor de muestras (pestaña ADPCM)

- `+ WAV`: elige un .wav (8 o 16 bits, mono o estéreo), pon nombre (máx. 14) y frecuencia; con OK se convierte y se añade.
- `▶`: escucha la muestra. `✕`: quita una muestra añadida.
- `GUARDAR`: escribe un banco .bin (hasta 32 muestras, 256 KB de memoria ADPCM, 64 KB por muestra).
- `CARGAR`: abre otro banco .bin.
