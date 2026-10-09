#!/usr/bin/env bash
set -euo pipefail
usage() { echo "Usage: $0 --venv PATH"; echo 'Creates/reuses an isolated Python art/QA environment; does not modify system Python.'; }
[[ ${1:-} == --help || ${1:-} == -h ]] && { usage; exit 0; }
[[ $# -eq 2 && $1 == --venv && -n $2 ]] || { usage >&2; exit 2; }
ROOT=$(cd "$(dirname "$0")/../.." && pwd)
VENV_PATH=$2
if [[ ! -x "$VENV_PATH/bin/python" ]]; then python3 -m venv "$VENV_PATH"; fi
"$VENV_PATH/bin/python" -m pip install -r "$ROOT/tools/requirements-art.txt"
"$VENV_PATH/bin/python" -c 'import cairosvg, PIL; print("Art tools:", cairosvg.__version__, PIL.__version__)'
echo "Set PATH=$VENV_PATH/bin:\$PATH for Gradle art generation."
