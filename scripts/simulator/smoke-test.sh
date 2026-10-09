#!/usr/bin/env bash
# Same real-time sampler; default duration 120 seconds.
exec "$(dirname "$0")/stability-test.sh" "$@"
