#!/bin/bash
set -e

# ==============================================================================
# DroidCast Receiver - build/install/push (InkFloat-style)
# ==============================================================================

# Dispositivo principale (pannello / receiver) dove installi l'app receiver
DEVICE_ID="an4009059505841100406"

# Telefono Android che fa da "APK server" (quello che deve ricevere l'APK via push)
APK_SERVER_ID="ZY3275T8QC"

# Dove pushare l'APK sul telefono server (modifica se la tua app server usa un path diverso)
REMOTE_PATH="/storage/emulated/0/Android/data/com.forteur.launcherupdateserverforandroid/files/apk_files/"

# Nome finale dell'APK sul server (scegli tu)
REMOTE_APK_NAME="com.forteur.droidcast_receiver.apk"

# Parametro di ingresso
SKIP_INSTALLATION=false
TARGET_MODE=""
if [ "${1:-}" == "no-install" ]; then
  SKIP_INSTALLATION=true
  TARGET_MODE="${2:-}"
fi

# Verifica se JAVA_HOME è impostato
if [ -z "${JAVA_HOME:-}" ]; then
  echo "JAVA_HOME non è impostato. Eseguo l'export..."
  export JAVA_HOME="/home/daniele/android-studio/jbr"
  export PATH="$PATH:$JAVA_HOME/bin"
fi

# Esegui la build con l'opzione per mostrare le deprecazioni
./gradlew assembleDebug --warning-mode all

echo "Build completata con successo!"

# Trova l'APK più recente nella directory DEL PROGETTO CORRENTE (non InkFloat)
APK_DIR="./app/build/outputs/apk/debug"
APK_PATH=$(find "$APK_DIR" -type f -name "*.apk" | sort | tail -n 1)

if [ -z "$APK_PATH" ]; then
  echo "Errore: Nessun APK trovato in: $APK_DIR"
  exit 1
fi

echo "APK trovata: $APK_PATH"

# Verifica se bisogna saltare l'installazione
if [ "$SKIP_INSTALLATION" = true ]; then
  if [ "$TARGET_MODE" == "server" ]; then
    echo "Modalità server: push dell'APK al telefono server..."

    # (Opzionale) Copia in una dir locale per tenere traccia dell'ultima APK buildata
    DEST_DIR="$HOME/akhter/DroidCast/apk_files"
    mkdir -p "$DEST_DIR"
    DEST_APK="$DEST_DIR/$REMOTE_APK_NAME"
    cp "$APK_PATH" "$DEST_APK"
    echo "APK copiata in: $DEST_APK"

    # Assicura dir remota e pusha
    echo "Creazione directory remota (se manca): $REMOTE_PATH"
    adb -s "$APK_SERVER_ID" shell "mkdir -p \"$REMOTE_PATH\""

    echo "Pushing APK to device at: $REMOTE_PATH"
    adb -s "$APK_SERVER_ID" push "$DEST_APK" "$REMOTE_PATH"

    echo "✅ Push completato."
  else
    echo "Installazione saltata su richiesta."
  fi

else
  # Se DEBUG=true, avvia in debug, altrimenti install normale
  if [ "${DEBUG:-}" = "true" ]; then
    echo "Esecuzione in modalità debug..."
    adb -s "$DEVICE_ID" shell am start -D -n "com.forteur.droidcast_receiver/.MainActivity"
  else
    echo "Installazione su device receiver: $DEVICE_ID"
    adb -s "$DEVICE_ID" install -r "$APK_PATH"
  fi
fi

