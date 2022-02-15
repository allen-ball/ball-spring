#!/bin/bash
# mysqld-run.sh

PRG="$0"

while [ -h "$PRG" ]; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")"/$link"
    fi
done

cd $(dirname "$PRG")

MYCNF=$(pwd)/my.cnf
DATADIR=$(pwd)/data
SOCKET=$(pwd)/socket

if [ ! -f "${MYCNF}" ]; then
    cat > "${MYCNF}" <<EOF
[mysqld]
general_log = ON
log_output = TABLE
EOF
fi

DEFAULTS_OPT=--no-defaults
DATADIR_OPT=--datadir="${DATADIR}"

if [ -f "${MYCNF}" ]; then
    DEFAULTS_OPT=--defaults-file="${MYCNF}"
fi

if [ ! -d "${DATADIR}" ]; then
    mysqld "${DEFAULTS_OPT}" "${DATADIR_OPT}" --initialize-insecure
fi

exec mysqld "${DEFAULTS_OPT}" "${DATADIR_OPT}" --socket="${SOCKET}"
