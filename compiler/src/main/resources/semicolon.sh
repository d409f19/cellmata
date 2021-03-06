#!/bin/bash

# Adds semicolon to EOL for each line with a statement, if missing for all '*.cell' files in current working directory

editor() {
    $(sed -i -r 's/([^;}{,()\s])$/\1;/gm' $file)
}

printf "Putting semicolons at EOL of statements in Cellamata source code if missing\nPrograms:\n"
for file in $(find -name '*.cell'); do
    printf "\t- $file\n"
    editor $file
done
