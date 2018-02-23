#!/bin/bash

set -e
set -x

OOTPAI_DATA=${OOTPAI_DATA:-ootp-ai-data}

if [[ -z "$SKIP_GIT_SYNC" ]]
then
  if [ -d "$OOTPAI_DATA/.git" ]; then
    echo "Pulling latest data..."
    pushd $OOTPAI_DATA
    git reset --hard HEAD
    git clean -fd || true
    git pull --rebase
    popd
  else
    echo "Cloning latest data..."
    rm -rf $OOTPAI_DATA/{*,.*}
    git clone --depth 1 "https://${GITHUB_TOKEN}@github.com/lstephen/ootp-ai-data.git" $OOTPAI_DATA
  fi
fi

echo "Running..."
MAVEN_OPTS="-Xmx1G" mvn -B exec:java -Dgpg.skip=true

if [[ -z "$SKIP_GIT_SYNC" ]]
then
  echo "Updating data..."
  cd $OOTPAI_DATA

  [[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
  [[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

  git add --all
  git commit -m "$(date)"
  git pull --rebase
  git push origin master
fi

echo "Done."

