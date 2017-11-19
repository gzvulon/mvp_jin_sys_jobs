#!/usr/bin/env bash

if [[ "$1" == "clean" ]]; then
	LINK_CMD='cd {} &&  unlink lib && echo removed {}'
else
	LINK_CMD='cd {} &&  ln -f -s ../../../lib lib && echo created {}'
fi

JOBDSL_DIR="$(dirname $(readlink -f $0))/../"
pushd ${JOBDSL_DIR}
	find projects -maxdepth 2 -mindepth 2 -type d -exec bash -c "${LINK_CMD}" \;
popd
