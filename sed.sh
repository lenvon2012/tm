#!/bin/sh
#sed -i -e 's/[\t]*$//g' `find . -name *.java`
#sed -i -e 's/[\ ]*$//g' `find . -name *.java`
dos2unix `find . -name *.java`
dos2unix `find . -name *.html`
dos2unix `find . -name *.css`
#  sed -i -e 's/formMain/tmMain/g' `find . -name *.html`
#  sed -i -e 's/formMain/tmMain/g' `find . -name *.js`
#   sed -i -e 's/formMain/tmMain/g' `find . -name *.css`
