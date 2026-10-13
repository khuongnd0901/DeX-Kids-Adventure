# T-047 native resident memory correction
Owner Main; dependency M3 1a43018. New SDK prerequisite NDK27.2.12479018.
The M3 full-mode heap had ~16MiB allocated/~71MiB free, yet ~75MiB Native PSS.
Use public Bionic mallopt: API34+ M_PURGE_ALL, API30–33 M_PURGE. Only memory
already free is purged; no forced GC, global allocator-policy changes or live
allocation freeing. One coalesced daemon worker, foreground requests at most
once/30s, 32MiB-free threshold; no HTTP, voice, permission or navigation changes.
Default core callback is a no-op on desktop. Three JNI ABIs compiled (3952/2672/
3920 bytes before compression); pinned NDK cross-compilation and APK packaging.

Verification: local contracts +172 core tests PASS, Android unit NO-SOURCE;
debug/test APK builds PASS. Actual GL100 cycles PASS on Fold3 (prototype before
paced foreground check); native allocation did not grow. Final APK installed
only after whole SHA match: 03eb13f68fdb56f45326f88278ae6fa07ff717af1c3407fb55a0348699e456d2.
Actual current 600s DeX1920x1080:59.88896FPS/P95upper19ms,60 display checks,
8creative beats,14resolutions,3Audio-only resolutions,F10/Continue,voiceUnavailable0.
Provider disabled throughout. Purge=true observed; calls2–5ms on the worker.
Full steady-state sample means KiB PSS: {'Java Heap': 15512.9, 'Native Heap': 12042.1, 'Graphics': 96939.5, 'TOTAL PSS': 155350.1}. Compare before Full:
Java16264.7,Native18749.6,Graphics96934.8,Total160697.9. Thus no full-mode PSS
increase in this measured window. Logical heap still reserves free address space;
its physical resident pages are reduced. Do not conflate reserved bytes and PSS.

30/60-minute final-source soak, actual road/nav-audio and visual/child comfort
remain NOT VERIFIED. No universal leak-freedom or release-ready claim.
Primary API contract: https://android.googlesource.com/platform/bionic/+/master/libc/include/malloc.h
