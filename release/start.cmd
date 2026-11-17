chcp 65001

REM java -DWORDS_PATH="C:\dict\words.csv" -DWORD_FROM=2 -DWORDS_COUNT=10 -jar ..\target\Test-jar-with-dependencies.jar

REM set WORD_FROM=1
REM set WORDS_COUNT=0
set WORDS_PATH=HSK3.csv
java -jar ..\target\Test-jar-with-dependencies.jar

pause