#!/bin/bash

echo '#ignore_settings_by:=placing_#_at_start_of_field_name=;
db_addr:=127.0.0.1=;
db_name:=jbend=;
db_user:=root=;
db_pass:=admin=;
ws_addr:=ws://127.0.0.1:42069/ws=;
' > universe/env.cfg

echo '/login -> /pages/login/login.html
/login.js -> /pages/login/login.js' > universe/routes.cfg

mkdir universe/logs
mkdir universe/out

