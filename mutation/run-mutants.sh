#!/usr/bin/env bash
# Runs WithdrawalTest against every mutant in mutation/mutants and prints KILLED or SURVIVED.
# Run it from the project root:   bash mutation/run-mutants.sh
# To use another Maven:          MVN=/path/to/mvn bash mutation/run-mutants.sh
# The original Withdrawal.java is restored at the end, even if the script is interrupted.
set -u
MVN="${MVN:-mvn}"
TARGET="src/main/java/com/bankapp/operation/Withdrawal.java"
BACKUP="$(mktemp)"
cp "$TARGET" "$BACKUP"
trap 'cp "$BACKUP" "$TARGET"; rm -f "$BACKUP"' EXIT

killed=0
total=0
for dir in mutation/mutants/M*/; do
  id="$(basename "$dir")"
  cp "$dir/Withdrawal.java" "$TARGET"
  log="$("$MVN" -q -B test -Dtest=WithdrawalTest -Djacoco.skip=true 2>&1)"
  code=$?
  total=$((total + 1))
  if echo "$log" | grep -q "COMPILATION ERROR"; then
    status="COMPILE ERROR"
  elif [ "$code" -ne 0 ]; then
    status="KILLED"
    killed=$((killed + 1))
  else
    status="SURVIVED"
  fi
  printf '%s  %s\n' "$id" "$status"
  echo "$log" | grep -E '^\[ERROR\]   WithdrawalTest' | sed 's/^\[ERROR\]   /      /'
done
echo "Mutation score: $killed / $total killed"
