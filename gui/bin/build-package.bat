set INSTALLER_TYPE=%1
set INPUT=%2
set OUTPUT=%3
set JAR=%4
set VERSION=%5
set APP_ICON=%6

call "jpackage" ^
    --type "%INSTALLER_TYPE%" ^
    --verbose ^
    --input "%INPUT%" ^
    --dest "%OUTPUT%" ^
    --name "XJ Music WorkStation" ^
    --main-jar "%JAR%" ^
    --app-version "%VERSION%" ^
    --icon "%APP_ICON%" ^
    --win-menu-group "XJ Music WorkStation" ^
    --vendor "XJ Music" ^
    --win-shortcut ^
    --description "XJ Music WorkStation"^
        --win-dir-chooser ^
        --win-menu ^
