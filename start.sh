#!/bin/sh
# Если fastcgi-lib.jar нужна в classpath, используйте -cp; иначе -jar достаточно
java -DFCGI_PORT=${FASTCGI_PORT} -cp /app/fcgi-bin/fastcgi-lib.jar:/app/fcgi-bin/server.jar Server
# Или, если server.jar исполняемый и включает всё: java -DFCGI_PORT=${FASTCGI_PORT} -jar /app/fcgi-bin/server.jar