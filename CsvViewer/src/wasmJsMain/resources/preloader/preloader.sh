#!/usr/bin/env bash

WASM_APP_NAME="CsvViewer"
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

cd $SCRIPT_DIR/..

RESOURCES=($(find ./composeResources -type f -name "*" ))
WASMS=($(find . -type f -name "*.wasm" ))
JS_ALL=($(find . -type f -name "*.*js" ))
JS=()

for element in "${JS_ALL[@]}"
do
    [ $element != ./preloader/Preload.js ] && [ $element != ./preloader/preloader.js ] && JS+=("$element")
done

ALL=("${RESOURCES[@]}" "${WASMS[@]}" "${JS[@]}")

LIST_STR="const list = ["
for element in "${ALL[@]}"
do
    LIST_STR+="    \"${element/#.\/}\",\n"
done
LIST_STR+="]"

INJECT_START="\n<!-- PRELOADER_INJECT_START -->\n"
INJECT_END="\n<!-- PRELOADER_INJECT_END -->\n"

sed -i -z -E "s#const list = \[((.|\n)*)\]#$LIST_STR#g" preloader/preloader.js
sed -i -E "s|const moduleScript = \".*\"|const moduleScript = \"$WASM_APP_NAME\"|g" preloader/preloader.js

perl -0777 -pe "s#$INJECT_START((.|\n)*?)$INJECT_END##g" -i index.html

sed -i "s|</head>|$INJECT_START<link type="text/css" rel="stylesheet" href="preloader/preloader.css">$INJECT_END</head>|g" index.html
sed -z "s|</body>|$INJECT_START$(cat preloader/preloader.html)$INJECT_END</body>|g" -i index.html
sed -i "s|</html>|$INJECT_START<script src="preloader/Preload.js"></script>$INJECT_END</html>|g" index.html
sed -i "s|</html>|$INJECT_START<script type="application/javascript" src="preloader/preloader.js"></script>$INJECT_END</html>|g" index.html
